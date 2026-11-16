package com.vibh.graphqlapollodemo.network

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.Subscription
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import com.apollographql.apollo.interceptor.RetryOnErrorInterceptor
import com.apollographql.apollo.interceptor.RetryStrategy
import com.apollographql.apollo.interceptor.defaultRetryStrategy
import com.apollographql.apollo.network.http.DefaultHttpEngine
import com.apollographql.apollo.network.websocket.GraphQLWsProtocol
import com.apollographql.apollo.network.websocket.WebSocketEngine
import com.apollographql.apollo.network.websocket.WebSocketNetworkTransport
import com.vibh.graphqlapollodemo.viewmodel.SubscriptionStatus
import com.vibh.graphqlapollodemo.viewmodel.SubscriptionStatusHolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

object ApolloClientProvider {

    // 1. HTTP Logger for Queries and Mutations
    private val httpOkHttpClient = OkHttpClient.Builder()
        .addInterceptor(Interceptor { chain ->
            val request = chain.request()
            Log.d("APOLLO_HTTP", "--> ${request.method} ${request.url}")

            val startTime = System.currentTimeMillis()
            val response: Response
            try {
                response = chain.proceed(request)
            } catch (e: Exception) {
                Log.e("APOLLO_HTTP", "<-- HTTP FAILED: ${e.message}")
                throw e
            }
            val duration = System.currentTimeMillis() - startTime

            Log.d("APOLLO_HTTP", "<-- ${response.code} ${response.message} (${duration}ms)")
            response
        })
        .build()

    // 2. Base OkHttpClient for WebSockets
    private val rawWsOkHttpClient = OkHttpClient.Builder().build()

    // 3. WebSocket.Factory that wraps Apollo's listener to capture handshake and frames
    private val loggingWebSocketFactory = WebSocket.Factory { request, apolloListener ->
        Log.d("APOLLO_WS", "--> Initiating WS Handshake: ${request.url}")
        Log.d("APOLLO_WS", "    Headers: ${request.headers}")

        SubscriptionStatusHolder.updateStatus(
            SubscriptionStatus.CONNECTING
        )

        val wrappedListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                if (response.code == 101) {
                    SubscriptionStatusHolder.updateStatus(
                        SubscriptionStatus.CONNECTED
                    )
                }
                Log.i("APOLLO_WS", "<-- WS Handshake Success: ${response.code} ${response.message}")
                Log.i("APOLLO_WS", "    Response Headers: ${response.headers}")
                apolloListener.onOpen(webSocket, response)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("APOLLO_WS", "<-- Inbound WS Frame: $text")
                apolloListener.onMessage(webSocket, text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.w("APOLLO_WS", "WS Closing: code=$code, reason=$reason")
                apolloListener.onClosing(webSocket, code, reason)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                SubscriptionStatusHolder.updateStatus(
                    SubscriptionStatus.DISCONNECTED
                )
                Log.w("APOLLO_WS", "WS Closed: code=$code, reason=$reason")
                apolloListener.onClosed(webSocket, code, reason)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                SubscriptionStatusHolder.updateStatus(
                    SubscriptionStatus.RECONNECTING
                )
                Log.e("APOLLO_WS", "WS Handshake/Connection Failed: ${t.message}, HTTP code: ${response?.code}", t)
                apolloListener.onFailure(webSocket, t, response)
            }
        }

        rawWsOkHttpClient.newWebSocket(request, wrappedListener)
    }

    val client: ApolloClient = ApolloClient.Builder()
        .serverUrl("http://10.0.2.2:8080/graphql")
        .httpEngine(DefaultHttpEngine(httpOkHttpClient))

        // 4. Operation Dispatch Logger
        .addInterceptor(object : ApolloInterceptor {
            override fun <D : Operation.Data> intercept(
                request: ApolloRequest<D>,
                chain: ApolloInterceptorChain
            ): Flow<ApolloResponse<D>> {
                val operationType = when (request.operation) {
                    is Query -> "HTTP [Query]"
                    is Mutation -> "HTTP [Mutation]"
                    is Subscription -> "WS [Subscription]"
                    else -> "Operation"
                }
                Log.d("APOLLO_DISPATCH", "--> Dispatching $operationType: ${request.operation.name()}")

                return chain.proceed(request).onEach { response ->
                    if (response.exception != null) {
                        Log.e("APOLLO_DISPATCH", "Error on ${request.operation.name()}: ${response.exception?.message}")
                    } else {
                        Log.d("APOLLO_DISPATCH", "Data received for ${request.operation.name()}")
                    }
                }
            }
        })

        .retryOnError { request ->
            request.operation is Subscription
        }

        .retryOnErrorInterceptor(
            RetryOnErrorInterceptor(
                retryStrategy = RetryStrategy { context ->
                    if (
                        context.request.operation is Subscription &&
                        context.response.exception != null
                    ) {
                        Log.w("APOLLO_WS", "Subscription failed. Retrying (attempt ${context.attempt})...")
                        SubscriptionStatusHolder.updateStatus(SubscriptionStatus.RECONNECTING)
                    }
                    defaultRetryStrategy.shouldRetry(context)
                }
            )
        )

        .subscriptionNetworkTransport(
            WebSocketNetworkTransport.Builder()
                .serverUrl("ws://10.0.2.2:8080/graphql")
                .wsProtocol(GraphQLWsProtocol())
                .webSocketEngine(WebSocketEngine(loggingWebSocketFactory))
                .build()
        )
        .build()
}