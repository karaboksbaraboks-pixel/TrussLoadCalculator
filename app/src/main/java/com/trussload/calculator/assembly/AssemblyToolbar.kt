package com.trussload.calculator.assembly

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AssemblyToolbar(
    assembly: TrussAssembly,
    onAssemblyChange: (TrussAssembly) -> Unit,
    modifier: Modifier = Modifier
) {

    val selected =
        assembly.selectedElement

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal = 10.dp,
                vertical = 6.dp
            ),
        horizontalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {

        // ====================================================
        // ПОВОРОТ ВЛЕВО 15°
        // ====================================================

        OutlinedButton(
            enabled = selected != null,
            onClick = {

                val element =
                    assembly.selectedElement
                        ?: return@OutlinedButton

                onAssemblyChange(
                    assembly.rotateElement(
                        elementId = element.id,
                        degrees = -15f
                    )
                )
            }
        ) {
            Text("↶ 15°")
        }

        // ====================================================
        // ПОВОРОТ ВПРАВО 15°
        // ====================================================

        OutlinedButton(
            enabled = selected != null,
            onClick = {

                val element =
                    assembly.selectedElement
                        ?: return@OutlinedButton

                onAssemblyChange(
                    assembly.rotateElement(
                        elementId = element.id,
                        degrees = 15f
                    )
                )
            }
        ) {
            Text("↷ 15°")
        }

        // ====================================================
        // ПОВОРОТ ВЛЕВО 90°
        // ====================================================

        OutlinedButton(
            enabled = selected != null,
            onClick = {

                val element =
                    assembly.selectedElement
                        ?: return@OutlinedButton

                onAssemblyChange(
                    assembly.rotateElement(
                        elementId = element.id,
                        degrees = -90f
                    )
                )
            }
        ) {
            Text("↶ 90°")
        }

        // ====================================================
        // ПОВОРОТ ВПРАВО 90°
        // ====================================================

        OutlinedButton(
            enabled = selected != null,
            onClick = {

                val element =
                    assembly.selectedElement
                        ?: return@OutlinedButton

                onAssemblyChange(
                    assembly.rotateElement(
                        elementId = element.id,
                        degrees = 90f
                    )
                )
            }
        ) {
            Text("↷ 90°")
        }

        // ====================================================
        // УДАЛЕНИЕ ВЫБРАННОГО ЭЛЕМЕНТА
        // ====================================================

        Button(
            enabled = selected != null,
            onClick = {

                onAssemblyChange(
                    assembly.deleteSelectedElement()
                )
            },
            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        MaterialTheme.colorScheme.error,

                    contentColor =
                        MaterialTheme.colorScheme.onError
                )
        ) {
            Text("Удалить")
        }

        // ====================================================
        // ОЧИСТКА ВСЕЙ СБОРКИ
        // ====================================================

        OutlinedButton(
            enabled =
                assembly.elements.isNotEmpty(),
            onClick = {

                onAssemblyChange(
                    assembly.clearAssembly()
                )
            }
        ) {
            Text("Очистить")
        }
    }
}
