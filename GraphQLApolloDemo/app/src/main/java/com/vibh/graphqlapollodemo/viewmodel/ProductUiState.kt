package com.vibh.graphqlapollodemo.viewmodel

import com.vibh.graphqlapollodemo.model.Product

sealed class ProductUiState {
    data object Loading : ProductUiState()

    data class Success(
        val product: Product
    ): ProductUiState()

    data class ProductListSuccess(
        val productList: List<Product>,
        val subscriptionError: String? = null
    ): ProductUiState()

    data class Error(
        val message: String
    ): ProductUiState()
}