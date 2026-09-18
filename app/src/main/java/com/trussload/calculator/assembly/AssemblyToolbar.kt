package com.trussload.calculator.assembly

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AssemblyToolbar(
    project: AssemblyProject,
    onProjectChange: (AssemblyProject) -> Unit,
    modifier: Modifier = Modifier
) {

    val selected =
        project.elements.firstOrNull {
            it.selected
        }

    Row(
        modifier = modifier
            .horizontalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal = 10.dp,
                vertical = 8.dp
            ),
        horizontalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {

        Button(
            enabled = selected != null,
            onClick = {

                selected?.let {

                    onProjectChange(
                        AssemblyEngine.rotate90(
                            project,
                            it.id
                        )
                    )
                }
            }
        ) {
            Text("↻ 90°")
        }

        Button(
            enabled = selected != null,
            onClick = {

                onProjectChange(
                    AssemblyEngine.duplicateSelected(
                        project
                    )
                )
            }
        ) {
            Text("Копировать")
        }

        Button(
            enabled = selected != null,
            onClick = {

                onProjectChange(
                    AssemblyEngine.deleteSelected(
                        project
                    )
                )
            }
        ) {
            Text("Удалить")
        }

        OutlinedButton(
            enabled = project.elements.isNotEmpty(),
            onClick = {

                onProjectChange(
                    AssemblyProject()
                )
            }
        ) {
            Text("Очистить")
        }
    }
}
