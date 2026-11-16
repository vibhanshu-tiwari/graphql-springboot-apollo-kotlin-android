package com.vibh.graphqlapollodemo.repository

import com.vibh.graphqlapollodemo.AddProductMutation
import com.vibh.graphqlapollodemo.DeleteProductMutation
import com.vibh.graphqlapollodemo.GetProductQuery
import com.vibh.graphqlapollodemo.GetProductsQuery
import com.vibh.graphqlapollodemo.ProductAddedSubscription
import com.vibh.graphqlapollodemo.UpdateProductMutation
import com.vibh.graphqlapollodemo.model.Product
import com.vibh.graphqlapollodemo.network.ApolloClientProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepository {
    suspend fun getProducts() : ApiResult<List<Product>> {
        val response = ApolloClientProvider.client
            .query(GetProductsQuery())
            .execute()

        response.exception?.let{
            return ApiResult.NetworkError(it)
        }

        response.errors?.firstOrNull()?.let{
            return ApiResult.GraphQLError(it)
        }

        val productList = response.data?.products

        return if(productList == null){
            ApiResult.GraphQLError(
                com.apollographql.apollo.api.Error.Builder("Products not found").build()
            )
        }else{
            ApiResult.Success(
                productList.mapNotNull { product ->
                    product?.let{
                        Product(
                            id = product.id,
                            name = product.name,
                            price = product.price
                        )
                    }
                })
        }
    }

    suspend fun getProduct(id: String) : ApiResult<Product> {
        val response = ApolloClientProvider.client
            .query(GetProductQuery(id))
            .execute()

        response.exception?.let{
            return ApiResult.NetworkError(it)
        }

        response.errors?.firstOrNull()?.let{
            return ApiResult.GraphQLError(it)
        }

        val product = response.data?.product

        return if (product == null) {
            ApiResult.GraphQLError(
                com.apollographql.apollo.api.Error.Builder("Product not found").build()
            )
        } else {
            ApiResult.Success(
                Product(
                    id = product.id,
                    name = product.name,
                    price = product.price
                )
            )
        }
    }

    suspend fun addProduct(
        name: String,
        price: Double
    ): ApiResult<Product>{
        val response = ApolloClientProvider.client
            .mutation(
                AddProductMutation(
                    name = name,
                    price = price)

            ).execute()

        response.exception?.let {
            return ApiResult.NetworkError(it)
        }

        response.errors?.firstOrNull()?.let{
            return ApiResult.GraphQLError(it)
        }

        val product = response.data?.addProduct

        return if(product == null){
            ApiResult.GraphQLError(
                com.apollographql.apollo.api.Error.Builder("Product was not created").build()
            )
        }else{
            ApiResult.Success(
                Product(
                    id = product.id,
                    name = product.name,
                    price = product.price
                )
            )
        }
    }

    suspend fun updateProduct(
        id: String,
        name: String,
        price: Double
    ): ApiResult<Product> {

        val response = ApolloClientProvider
            .client
            .mutation(
                UpdateProductMutation(
                    id = id,
                    name = name,
                    price = price
                )
            ).execute()

        response.exception?.let {
            return ApiResult.NetworkError(it)
        }

        response.errors?.firstOrNull()?.let {
            return ApiResult.GraphQLError(it)
        }

        val product = response.data?.updateProduct

        return if (product == null) {
            ApiResult.GraphQLError(
                com.apollographql.apollo.api.Error.Builder("Product was not updated").build()
            )
        } else {
            ApiResult.Success(
                Product(
                    id = product.id,
                    name = product.name,
                    price = product.price
                )
            )
        }
    }

    suspend fun deleteProduct(id: String): ApiResult<Boolean>{
        val response = ApolloClientProvider.client
            .mutation(DeleteProductMutation(id = id))
            .execute()

        response.exception?.let{
           return ApiResult.NetworkError(it)
        }

        response.errors?.firstOrNull()?.let{
           return ApiResult.GraphQLError(it)
        }

        val isDeleted = response.data?.deleteProduct

        return if(isDeleted == null){
            ApiResult.GraphQLError(
                com.apollographql.apollo.api.Error.Builder("Product was not deleted").build()
            )
        }else{
            ApiResult.Success(true)
        }
    }

    fun subscribeToProductAdded(): Flow<ApiResult<Product>>{
        return ApolloClientProvider.client
            .subscription(ProductAddedSubscription())
            .toFlow()
            .map { response ->

                response.exception?.let{
                    //return@map ProductSubscriptionResult.NetworkError(it)
                    throw it
                }

                response.errors?.firstOrNull()?.let {
                    return@map ApiResult.GraphQLError(it)
                }

                val product = response.data?.productAdded

                if(product == null) {
                    ApiResult.GraphQLError(
                        com.apollographql.apollo.api.Error.Builder(
                            "Product event data was empty"
                        ).build()
                    )
                }else{
                    ApiResult.Success(
                        Product(
                            id = product.id,
                            name = product.name,
                            price = product.price
                        )
                    )
                }
            }
    }
}