package com.vibh.graphqlapollodemo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vibh.graphqlapollodemo.model.Product

@Composable
fun ProductItem(
    product: Product,
    onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick = onClick
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = product.name)
            Text(text = product.price.toString())
        }
    }
}