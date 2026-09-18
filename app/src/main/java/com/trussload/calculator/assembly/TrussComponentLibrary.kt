package com.trussload.calculator.assembly

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

data class LibraryItem(
    val title: String,
    val type: AssemblyElementType,
    val lengthMeters: Float = 0f
)

val defaultTrussLibrary = listOf(
    LibraryItem(
        title = "0.5 м",
        type = AssemblyElementType.STRAIGHT_TRUSS,
        lengthMeters = 0.5f
    ),
    LibraryItem(
        title = "1 м",
        type = AssemblyElementType.STRAIGHT_TRUSS,
        lengthMeters = 1f
    ),
    LibraryItem(
        title = "2 м",
        type = AssemblyElementType.STRAIGHT_TRUSS,
        lengthMeters = 2f
    ),
    LibraryItem(
        title = "3 м",
        type = AssemblyElementType.STRAIGHT_TRUSS,
        lengthMeters = 3f
    ),
    LibraryItem(
        title = "4 м",
        type = AssemblyElementType.STRAIGHT_TRUSS,
        lengthMeters = 4f
    ),
    LibraryItem(
        title = "90°",
        type = AssemblyElementType.CORNER_90
    ),
    LibraryItem(
        title = "135°",
        type = AssemblyElementType.CORNER_135
    ),
    LibraryItem(
        title = "T",
        type = AssemblyElementType.T_JUNCTION
    ),
    LibraryItem(
        title = "+",
        type = AssemblyElementType.CROSS
    ),
    LibraryItem(
        title = "Опора",
        type = AssemblyElementType.SUPPORT
    ),
    LibraryItem(
        title = "Нагрузка",
        type = AssemblyElementType.LOAD_POINT
    )
)

@Composable
fun TrussComponentLibrary(
    onAddElement: (LibraryItem) -> Unit,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier = modifier,
        tonalElevation = 3.dp
    ) {

        Column(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 10.dp
            )
        ) {

            Text(
                text = "Библиотека элементов",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Row(
                modifier = Modifier.horizontalScroll(
                    rememberScrollState()
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                defaultTrussLibrary.forEach { item ->

                    Button(
                        onClick = {
                            onAddElement(item)
                        },
                        colors = ButtonDefaults.buttonColors()
                    ) {
                        Text(item.title)
                    }

                    Spacer(
                        modifier = Modifier.width(2.dp)
                    )
                }
            }
        }
    }
}

fun addLibraryElement(
    project: AssemblyProject,
    item: LibraryItem,
    position: Offset
): AssemblyProject {

    val newId =
        (project.elements.maxOfOrNull {
            it.id
        } ?: 0L) + 1L

    val newElement =
        AssemblyElement(
            id = newId,
            type = item.type,
            name = item.title,
            lengthMeters = item.lengthMeters,
            position = position,
            rotationDegrees = 0f,
            selected = true
        )

    return project.copy(
        elements =
            project.elements
                .map {
                    it.copy(selected = false)
                } + newElement
    )
}
