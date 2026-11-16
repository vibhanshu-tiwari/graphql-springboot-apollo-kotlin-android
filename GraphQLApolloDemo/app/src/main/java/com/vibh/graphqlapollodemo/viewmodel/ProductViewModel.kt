package com.vibh.graphqlapollodemo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibh.graphqlapollodemo.repository.ApiResult
import com.vibh.graphqlapollodemo.repository.ProductRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ProductViewModel: ViewModel() {
    private val productRepository = ProductRepository()

    private var subscriptionJob: Job? = null

    private val _productUIState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val productUIState: StateFlow<ProductUiState> = _productUIState.asStateFlow()

    private fun updateSubscriptionError(message: String?){
        val currentState =
            _productUIState.value as? ProductUiState.ProductListSuccess ?: return

        _productUIState.value = currentState.copy(
            subscriptionError = message
        )
    }

    fun getProducts(){
        viewModelScope.launch {
            _productUIState.value = ProductUiState.Loading
            when(val result = productRepository.getProducts()){
                is ApiResult.Success -> {
                    _productUIState.value =
                        ProductUiState.ProductListSuccess(result.data)
                }

                is ApiResult.GraphQLError -> {
                    _productUIState.value =
                        ProductUiState.Error(result.error.message)
                }

                is ApiResult.NetworkError -> {
                    _productUIState.value =
                        ProductUiState.Error(result.exception.message?: "Network Error")
                }
            }
        }
    }

    fun getProduct(id: String){
        viewModelScope.launch {
            //val result = productRepository.getProduct(id)
            //println("ViewModel Result: $result")
            // _productResult.value = result

            _productUIState.value = ProductUiState.Loading

            when(val result = productRepository.getProduct(id)){
                is ApiResult.Success ->{
                    _productUIState.value = ProductUiState.Success(result.data)
                }

                is ApiResult.GraphQLError ->{
                    _productUIState.value = ProductUiState.Error(result.error.message)
                }

                is ApiResult.NetworkError ->{
                    _productUIState.value = ProductUiState.Error(result.exception.message?: "Network Error")
                }
            }
        }
    }

    fun addProduct(
        name: String,
        price: Double,
        onSuccess: () -> Unit){
        viewModelScope.launch {
            when(val result = productRepository.addProduct(name,price)){
                is ApiResult.Success-> {

                    val currentProducts =
                        (productUIState.value as? ProductUiState.ProductListSuccess)
                            ?.productList
                            ?: emptyList()

                    if (currentProducts.none { it.id == result.data.id }) {

                        val updatedProducts = currentProducts + result.data

                        _productUIState.value =
                            ProductUiState.ProductListSuccess(updatedProducts)
                    }

                    onSuccess()
                }

                is ApiResult.GraphQLError->{
                    _productUIState.value =
                        ProductUiState.Error(result.error.message)
                }
                is ApiResult.NetworkError->{
                    _productUIState.value =
                        ProductUiState.Error(result.exception.message?: "Network Error")
                }
            }
        }
    }

    fun updateProduct(
        id: String,
        name: String,
        price: Double,
        onSuccess: () -> Unit){

        viewModelScope.launch {
            when(val result = productRepository.updateProduct(id, name, price)) {

                is ApiResult.Success ->{
                    val currentProductList =
                        (productUIState.value as? ProductUiState.ProductListSuccess)
                            ?.productList
                            ?: emptyList()

                    val updatedProductList = currentProductList.map{ product ->
                        if(product.id == result.data.id){
                            result.data
                        }else{
                            product
                        }
                    }

                    _productUIState.value = ProductUiState.ProductListSuccess(updatedProductList)

                    onSuccess()
                }

                is ApiResult.GraphQLError ->{
                    _productUIState.value = ProductUiState.Error(result.error.message)
                }
                is ApiResult.NetworkError ->{
                    _productUIState.value = ProductUiState.Error(result.exception.message?: "Network Error")
                }
            }
        }
    }

    fun deleteProduct(
        id: String,
        onSuccess: () -> Unit
    ){
        viewModelScope.launch {
            when(val result = productRepository.deleteProduct(id)){
                is ApiResult.Success -> {
                    val currentProductList =
                        (productUIState.value as? ProductUiState.ProductListSuccess)
                            ?.productList
                            ?: emptyList()

                    val updatedProductList = currentProductList.filter {
                        it.id != id
                    }

                    _productUIState.value = ProductUiState.ProductListSuccess(updatedProductList)

                    onSuccess()
                }

                is ApiResult.GraphQLError-> {
                    _productUIState.value = ProductUiState.Error(
                        result.error.message
                    )
                }

                is ApiResult.NetworkError-> {
                    _productUIState.value = ProductUiState.Error(
                        result.exception.message ?: "Network Error"
                    )
                }
            }
        }
    }

    fun observeProductAdded(){

        if(subscriptionJob?.isActive == true) return

        subscriptionJob = viewModelScope.launch {
            productRepository.subscribeToProductAdded()
                .catch { exception ->
                    SubscriptionStatusHolder.updateStatus(
                        SubscriptionStatus.DISCONNECTED
                    )
                    updateSubscriptionError(
                        exception.message ?: "Subscription failed"
                    )
                }
                .collect{ result ->
                    when (result){
                        is ApiResult.Success -> {

                            SubscriptionStatusHolder.updateStatus(
                                SubscriptionStatus.CONNECTED
                            )

                            updateSubscriptionError(null)

                            val newProduct = result.data
                            val currentProductList =
                                (productUIState.value as? ProductUiState.ProductListSuccess)
                                    ?.productList ?: emptyList()

                            if(currentProductList.none {it.id == newProduct.id}){
                                _productUIState.value =
                                    ProductUiState.
                                    ProductListSuccess(currentProductList + newProduct)
                            }
                        }

                        is ApiResult.GraphQLError -> {
                            _productUIState.value =
                                ProductUiState.Error(result.error.message)
                        }

                        is ApiResult.NetworkError -> {
                            /*_productUIState.value =
                                ProductUiState.Error(result.exception.message ?: "Network Error")*/
                            SubscriptionStatusHolder.updateStatus(
                                SubscriptionStatus.DISCONNECTED
                            )
                            updateSubscriptionError(
                                result.exception.message ?: "Network Error"
                            )
                        }
                    }
                }
        }
    }
}