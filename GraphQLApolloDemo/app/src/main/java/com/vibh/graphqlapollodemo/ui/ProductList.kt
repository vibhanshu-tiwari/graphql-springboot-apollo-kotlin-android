package com.vibh.graphqlapollodemo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vibh.graphqlapollodemo.model.Product

@Composable
fun ProductList(
    products: List<Product>,
    onProductClick: (Product) -> Unit){
    if(products.isEmpty()){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "No products found")
        }
        return
    }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)){
        items(products){product ->
           ProductItem(
               product,
               onClick = {
                   onProductClick(product)
               }
           )
        }
    }
}