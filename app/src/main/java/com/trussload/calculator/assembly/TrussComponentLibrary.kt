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

// ============================================================
// ЭЛЕМЕНТ БИБЛИОТЕКИ
// ============================================================

data class LibraryItem(
    val title: String,
    val type: AssemblyElementType,

    // Размеры
    val length: Double = 0.0,
    val width: Double = 0.29,
    val height: Double = 0.29,

    // Масса
    val weight: Double = 0.0,

    // Дополнительные данные
    val manufacturer: String = "",
    val series: String = "",
    val article: String = "",

    // Нагрузочные характеристики
    val maxDistributedLoad: Double? = null,
    val maxPointLoad: Double? = null
)

// ============================================================
// СТАНДАРТНАЯ БИБЛИОТЕКА
// ============================================================

val defaultTrussLibrary = listOf(

    // --------------------------------------------------------
    // ПРЯМЫЕ СЕКЦИИ
    // --------------------------------------------------------

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

    // --------------------------------------------------------
    // СОЕДИНИТЕЛИ
    // --------------------------------------------------------

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
        title = "X",
        type = AssemblyElementType.X_JUNCTION
    )
)

// ============================================================
// ИНТЕРФЕЙС БИБЛИОТЕКИ
// ============================================================

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
                vertical = 8.dp
            )
        ) {

            Text(
                text = "Элементы",
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Row(
                modifier = Modifier.horizontalScroll(
                    rememberScrollState()
                ),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                defaultTrussLibrary.forEach { item ->

                    Button(
                        onClick = {
                            onAddElement(item)
                        }
                    ) {

                        Text(
                            text = item.title
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// СОЗДАНИЕ ЭЛЕМЕНТА ИЗ БИБЛИОТЕКИ
// ============================================================

fun createElementFromLibrary(
    item: LibraryItem,
    x: Float = 0f,
    y: Float = 0f
): AssemblyElement {

    return AssemblyElement(

        id = UUID.randomUUID().toString(),

        type = item.type,

        name = when (item.type) {

            AssemblyElementType.STRAIGHT ->
                "Ферма ${item.title}"

            AssemblyElementType.CORNER_90 ->
                "Угол 90°"

            AssemblyElementType.CORNER_135 ->
                "Угол 135°"

            AssemblyElementType.T_JUNCTION ->
                "T-соединитель"

            AssemblyElementType.X_JUNCTION ->
                "X-соединитель"
        },

        length = item.length,

        width = item.width,

        height = item.height,

        weight = item.weight,

        x = x,

        y = y,

        rotation = 0f,

        selected = true,

        manufacturer = item.manufacturer,

        series = item.series,

        article = item.article,

        maxDistributedLoad =
            item.maxDistributedLoad,

        maxPointLoad =
            item.maxPointLoad
    )
}

// ============================================================
// ДОБАВЛЕНИЕ ЭЛЕМЕНТА В СБОРКУ
// ============================================================

fun addLibraryElement(
    assembly: TrussAssembly,
    item: LibraryItem,
    x: Float = 0f,
    y: Float = 0f
): TrussAssembly {

    // Сначала снимаем выделение
    // со всех существующих элементов.

    val oldElements =
        assembly.elements.map {
            it.copy(
                selected = false
            )
        }

    // Создаём новый элемент.
    // Он сразу становится выбранным.

    val newElement =
        createElementFromLibrary(
            item = item,
            x = x,
            y = y
        )

    return assembly.copy(
        elements =
            oldElements + newElement
    )
}
