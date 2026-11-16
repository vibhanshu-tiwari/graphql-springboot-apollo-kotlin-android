package com.vibh.graphqlapollodemo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ProductForm(
    productName: String,
    productPrice: String,
    onProductNameChange: (String)-> Unit,
    onProductPriceChange: (String)-> Unit,
    onSubmit: (OperationType)-> Unit,
    buttonAddUpdateText: String,
    isButtonDeleteShow: Boolean,
    isUpdateEnabled: Boolean = false
){
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 64.dp,
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)) {

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = productName,
            onValueChange = onProductNameChange,
            label = { Text("Name") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
            )
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = productPrice,
            onValueChange = onProductPriceChange,
            label = {
                Text( text = "Price")
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            )
        )

        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center){
            Button(
                modifier = Modifier.height(40.dp),
                onClick = { onSubmit(OperationType.UPDATE)},
                enabled = isUpdateEnabled) {
                Text(
                    text = buttonAddUpdateText
                )
            }
            if(isButtonDeleteShow){
                Spacer(modifier = Modifier.padding(16.dp))
                Button(
                    modifier = Modifier.height(40.dp),
                    onClick = {
                        onSubmit(OperationType.DELETE)
                    }) {
                    Text(
                        text = "Delete"
                    )
                }
            }
        }
    }
}