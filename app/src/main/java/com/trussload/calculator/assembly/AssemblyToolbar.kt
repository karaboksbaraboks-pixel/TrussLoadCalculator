package com.trussload.calculator.assembly

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AssemblyToolbar(
    modifier: Modifier = Modifier,
    onAdd2m: () -> Unit = {},
    onAdd3m: () -> Unit = {},
    onRotate: () -> Unit = {},
    onDelete: () -> Unit = {},
    onClear: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Button(
            onClick = onAdd2m
        ) {
            Text("+2 м")
        }

        Button(
            onClick = onAdd3m
        ) {
            Text("+3 м")
        }

        OutlinedButton(
            onClick = onRotate
        ) {
            Text("↻")
        }

        OutlinedButton(
            onClick = onDelete
        ) {
            Text("Удалить")
        }

        OutlinedButton(
            onClick = onClear
        ) {
            Text("Очистить")
        }
    }
}
