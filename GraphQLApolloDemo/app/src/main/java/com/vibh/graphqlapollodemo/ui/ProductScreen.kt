package com.vibh.graphqlapollodemo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vibh.graphqlapollodemo.model.Product
import com.vibh.graphqlapollodemo.ui.theme.Primary
import com.vibh.graphqlapollodemo.viewmodel.ProductUiState
import com.vibh.graphqlapollodemo.viewmodel.ProductViewModel
import com.vibh.graphqlapollodemo.viewmodel.SubscriptionStatus
import com.vibh.graphqlapollodemo.viewmodel.SubscriptionStatusHolder
import kotlinx.coroutines.launch

@Composable
fun ProductScreen(
    innerPadding: PaddingValues
) {
    val productVM: ProductViewModel = viewModel()

    val productUIState by productVM.productUIState.collectAsState()

    val subscriptionStatus by SubscriptionStatusHolder.status.collectAsState()

    val searchText = remember { mutableStateOf("") }

    val selectedProduct = remember { mutableStateOf<Product?>(null) }

    val showProductDialog = remember { mutableStateOf(false) }

    val showDeleteConfirmationDialog = remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()

    val productName = remember { mutableStateOf("") }

    val productPrice = remember { mutableStateOf("") }

    var dialogMode by remember { mutableStateOf(ProductDialogMode.ADD) }

    val hasFormValuesChanges = selectedProduct.value?.let{ product ->
        productName.value.trim() != product.name.trim() ||
                productPrice.value.toDoubleOrNull() != product.price
    } ?: false

    LaunchedEffect(Unit) {
        productVM.getProducts()
        productVM.observeProductAdded()
    }

    LaunchedEffect(selectedProduct.value) {
        selectedProduct.value.let {
            productName.value = it?.name ?: ""
            productPrice.value = it?.price?.toString() ?: ""
        }
    }

    Scaffold(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        containerColor = Color.White,
        floatingActionButton = {
            FloatingActionButton(
                containerColor = Primary,
                contentColor = Color.White,
                onClick = {
                    showProductDialog.value = true
                    dialogMode = ProductDialogMode.ADD
                    selectedProduct.value = null
                }
            ) {
                Text("+")
            }
        }) { innerPadding ->

        Box {
            Column {
                TextField(
                    modifier = Modifier.padding(
                        start = innerPadding
                            .calculateStartPadding(LocalLayoutDirection.current),
                        end = innerPadding
                            .calculateEndPadding(LocalLayoutDirection.current),
                        top = innerPadding.calculateTopPadding(),
                        bottom = 12.dp
                    )
                        .fillMaxWidth(),
                    value = searchText.value,
                    onValueChange = { searchText.value = it },
                    label = { Text("Search Products") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                    )
                )

                SubscriptionStatusView(status = subscriptionStatus)

                when (val state = productUIState) {

                    is ProductUiState.Loading -> Row(
                        modifier = Modifier.padding(innerPadding)
                            .fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "Loading...")
                    }

                    is ProductUiState.Success -> {
                        Text(
                            text = "${state.product.name} - $ ${state.product.price}"
                        )
                    }

                    is ProductUiState.Error -> {
                        Row(
                            modifier = Modifier.padding(innerPadding)
                                .fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = "Error: ${state.message}")
                        }
                    }

                    is ProductUiState.ProductListSuccess -> {
                        val filteredProductList = state.productList.filter {
                            it.name.contains(searchText.value, ignoreCase = true)
                        }

                        state.subscriptionError?.let {
                            println("Subscription Error: $it")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Live updates unavailable",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        ProductList(
                            products = filteredProductList,
                            onProductClick = {
                                selectedProduct.value = it
                                dialogMode = ProductDialogMode.EDIT
                                showProductDialog.value = true
                            }
                        )
                    }
                }
            }

            if (showProductDialog.value) {
                Dialog(
                    onDismissRequest = {
                        showProductDialog.value = false
                    }
                ) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = Color.White
                    ) {
                        Box {
                            Column(
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        text = if(dialogMode == ProductDialogMode.ADD){
                                            "Add Product"
                                        }else{
                                            "Edit Product"
                                        },
                                        style = MaterialTheme.typography.headlineSmall,
                                    )
                                }

                                Spacer(modifier = Modifier.padding(24.dp))

                                ProductForm(
                                    productName = productName.value,
                                    productPrice = productPrice.value,
                                    onProductNameChange = {
                                        productName.value = it
                                    },
                                    onProductPriceChange = {
                                        productPrice.value = it
                                    },
                                    onSubmit = { operationType ->
                                        if (operationType == OperationType.UPDATE){
                                            val name = productName.value.trim()
                                            val price = productPrice.value.toDoubleOrNull()

                                            when {
                                                name.isEmpty() -> {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            "Product Name is Required!"
                                                        )
                                                    }
                                                }

                                                price == null -> {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            "Valid Price Required!"
                                                        )
                                                    }
                                                }

                                                price <= 0 -> {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            "Price must be greater than 0!"
                                                        )
                                                    }
                                                }

                                                else -> {
                                                    if(dialogMode == ProductDialogMode.ADD){
                                                        productVM
                                                            .addProduct(
                                                                name = name,
                                                                price = price,
                                                                onSuccess = {
                                                                    showProductDialog.value = false
                                                                    productName.value = ""
                                                                    productPrice.value = ""
                                                                }
                                                            )
                                                    }else{
                                                        selectedProduct.value?.let { product ->
                                                            productVM.updateProduct(
                                                                id = product.id,
                                                                name = name,
                                                                price = price,
                                                                onSuccess = {
                                                                    showProductDialog.value = false
                                                                    productName.value = ""
                                                                    productPrice.value = ""
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }else{
                                            showDeleteConfirmationDialog.value = true
                                        }
                                    },
                                    buttonAddUpdateText = if(dialogMode == ProductDialogMode.ADD){
                                        "Add"
                                    }else{
                                        "Update"
                                    },
                                    isButtonDeleteShow = dialogMode != ProductDialogMode.ADD,
                                    isUpdateEnabled = dialogMode != ProductDialogMode.EDIT || hasFormValuesChanges
                                )
                                // Spacer(modifier = Modifier.padding(16.dp))

                                /*  Row(
                                      modifier = Modifier.fillMaxWidth(),
                                      horizontalArrangement = Arrangement.Center
                                  ) {
                                      Button(
                                          onClick = {
                                              showProductDialog.value = false
                                          }
                                      ) {
                                          Text(text = "Cancel")
                                      }
                                  }*/
                            }
                            IconButton(
                                onClick = {
                                    showProductDialog.value = false
                                },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ){
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close dialog"
                                )
                            }
                            SnackbarHost(
                                hostState = snackbarHostState,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 8.dp)
                            )
                        }
                    }
                }
            }

            if(showDeleteConfirmationDialog.value){
                Dialog(
                    onDismissRequest = {
                        showDeleteConfirmationDialog.value = false
                    }
                ){
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Delete Product?",
                                style = MaterialTheme.typography.headlineSmall
                            )

                            Spacer(modifier = Modifier.padding(8.dp))

                            Text(
                                text = "Are you sure you want to delete ${selectedProduct.value?.name}?",
                            )

                            Spacer(modifier = Modifier.padding(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ){
                                Button(
                                    onClick = {
                                        showDeleteConfirmationDialog.value = false
                                    }
                                ) {
                                    Text(
                                        text = "Cancel"
                                    )
                                }

                                Button(
                                    onClick = {
                                        selectedProduct.value?.let{ product ->
                                            productVM.deleteProduct(product.id) {
                                                showDeleteConfirmationDialog.value = false
                                                showProductDialog.value = false
                                                selectedProduct.value = null
                                                productName.value = ""
                                                productPrice.value = ""
                                            }
                                        }
                                    }
                                ) {
                                    Text(
                                        text = "Delete"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionStatusView(
    status: SubscriptionStatus
) {
    val message = when (status) {
        SubscriptionStatus.CONNECTING ->
            "Waiting for the first live update..."

        SubscriptionStatus.CONNECTED ->
            "Live updates connected"

        SubscriptionStatus.RECONNECTING ->
            "Reconnecting to live updates..."

        SubscriptionStatus.DISCONNECTED ->
            "Live updates unavailable"
    }

    Text(
        text = message,
        color = when (status) {
            SubscriptionStatus.CONNECTED ->
                Color(0xFF2E7D32)

            SubscriptionStatus.RECONNECTING,
            SubscriptionStatus.DISCONNECTED ->
                MaterialTheme.colorScheme.error

            SubscriptionStatus.CONNECTING ->
                MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = Modifier.padding(8.dp)
    )
}
