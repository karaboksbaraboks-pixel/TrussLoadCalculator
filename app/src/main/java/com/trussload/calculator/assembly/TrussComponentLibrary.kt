package com.trussload.calculator.assembly

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.UUID

data class LibraryItem(
    val title: String,
    val type: AssemblyElementType,
    val length: Double = 0.0,
    val weight: Double = 0.0
)

val defaultTrussLibrary = listOf(
    LibraryItem(
        title = "0.5 м",
        type = AssemblyElementType.STRAIGHT,
        length = 0.5
    ),
    LibraryItem(
        title = "1 м",
        type = AssemblyElementType.STRAIGHT,
        length = 1.0
    ),
    LibraryItem(
        title = "2 м",
        type = AssemblyElementType.STRAIGHT,
        length = 2.0
    ),
    LibraryItem(
        title = "3 м",
        type = AssemblyElementType.STRAIGHT,
        length = 3.0
    ),
    LibraryItem(
        title = "4 м",
        type = AssemblyElementType.STRAIGHT,
        length = 4.0
    ),
    LibraryItem(
        title = "Угол 90°",
        type = AssemblyElementType.CORNER
    ),
    LibraryItem(
        title = "T",
        type = AssemblyElementType.T_JUNCTION
    ),
    LibraryItem(
        title = "X",
        type = AssemblyElementType.X_JUNCTION
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
                        }
                    ) {
                        Text(item.title)
                    }
                }
            }
        }
    }
}

fun createElementFromLibrary(
    item: LibraryItem,
    x: Float = 0f,
    y: Float = 0f
): AssemblyElement {

    return AssemblyElement(
        id = UUID.randomUUID().toString(),
        type = item.type,
        name = item.title,
        length = item.length,
        weight = item.weight,
        rotation = 0f,
        x = x,
        y = y
    )
}

fun addLibraryElement(
    assembly: TrussAssembly,
    item: LibraryItem,
    x: Float = 0f,
    y: Float = 0f
): TrussAssembly {

    val newElement = createElementFromLibrary(
        item = item,
        x = x,
        y = y
    )

    return assembly.copy(
        elements = assembly.elements + newElement
    )
}
