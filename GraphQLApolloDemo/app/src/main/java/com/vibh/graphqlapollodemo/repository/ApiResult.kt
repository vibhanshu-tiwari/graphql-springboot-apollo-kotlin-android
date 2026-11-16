package com.vibh.graphqlapollodemo.repository


sealed interface ApiResult<out T> {
    data class Success<T>(
        val data: T
    ) : ApiResult<T>

    data class GraphQLError(
        val error: com.apollographql.apollo.api.Error
    ) : ApiResult<Nothing>

    data class NetworkError(
        val exception: Exception
    ) : ApiResult<Nothing>
}