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

    // --------------------------------------------------------
    // ГЕОМЕТРИЯ
    // --------------------------------------------------------

    val length: Double = 0.0,

    val width: Double = 0.29,

    val height: Double = 0.29,

    // --------------------------------------------------------
    // МАССА
    // --------------------------------------------------------

    val weight: Double = 0.0,

    // --------------------------------------------------------
    // ПРОИЗВОДИТЕЛЬ
    // --------------------------------------------------------

    val manufacturer: String = "",

    val series: String = "",

    val article: String = "",

    // --------------------------------------------------------
    // ДОПУСТИМЫЕ НАГРУЗКИ
    // --------------------------------------------------------

    val maxDistributedLoad: Double? = null,

    val maxPointLoad: Double? = null,

    // --------------------------------------------------------
    // РАСЧЁТНЫЕ ХАРАКТЕРИСТИКИ
    //
    // areaM2:
    // эквивалентная площадь сечения в м²
    //
    // elasticModulusKnPerM2:
    // модуль упругости в кН/м²
    // --------------------------------------------------------

    val areaM2: Double? = null,

    val elasticModulusKnPerM2: Double? = null
)

// ============================================================
// СТАНДАРТНЫЕ РАСЧЁТНЫЕ ЗНАЧЕНИЯ
//
// Пока используем одинаковые значения для стандартных секций.
//
// В дальнейшем они будут заменяться характеристиками
// конкретной модели фермы производителя.
// ============================================================

private const val DEFAULT_TRUSS_AREA_M2 =
    0.001

private const val DEFAULT_ALUMINIUM_ELASTIC_MODULUS_KN_PER_M2 =
    70_000_000.0

// ============================================================
// СТАНДАРТНАЯ БИБЛИОТЕКА
// ============================================================

val defaultTrussLibrary =
    listOf(

        // ----------------------------------------------------
        // ПРЯМЫЕ СЕКЦИИ
        // ----------------------------------------------------

        LibraryItem(
            title = "0.5 м",
            type = AssemblyElementType.STRAIGHT,
            length = 0.5,
            areaM2 =
                DEFAULT_TRUSS_AREA_M2,
            elasticModulusKnPerM2 =
                DEFAULT_ALUMINIUM_ELASTIC_MODULUS_KN_PER_M2
        ),

        LibraryItem(
            title = "1 м",
            type = AssemblyElementType.STRAIGHT,
            length = 1.0,
            areaM2 =
                DEFAULT_TRUSS_AREA_M2,
            elasticModulusKnPerM2 =
                DEFAULT_ALUMINIUM_ELASTIC_MODULUS_KN_PER_M2
        ),

        LibraryItem(
            title = "2 м",
            type = AssemblyElementType.STRAIGHT,
            length = 2.0,
            areaM2 =
                DEFAULT_TRUSS_AREA_M2,
            elasticModulusKnPerM2 =
                DEFAULT_ALUMINIUM_ELASTIC_MODULUS_KN_PER_M2
        ),

        LibraryItem(
            title = "3 м",
            type = AssemblyElementType.STRAIGHT,
            length = 3.0,
            areaM2 =
                DEFAULT_TRUSS_AREA_M2,
            elasticModulusKnPerM2 =
                DEFAULT_ALUMINIUM_ELASTIC_MODULUS_KN_PER_M2
        ),

        LibraryItem(
            title = "4 м",
            type = AssemblyElementType.STRAIGHT,
            length = 4.0,
            areaM2 =
                DEFAULT_TRUSS_AREA_M2,
            elasticModulusKnPerM2 =
                DEFAULT_ALUMINIUM_ELASTIC_MODULUS_KN_PER_M2
        ),

        // ----------------------------------------------------
        // УГЛОВЫЕ БЛОКИ
        // ----------------------------------------------------

        LibraryItem(
            title = "90°",
            type = AssemblyElementType.CORNER_90,
            areaM2 =
                DEFAULT_TRUSS_AREA_M2,
            elasticModulusKnPerM2 =
                DEFAULT_ALUMINIUM_ELASTIC_MODULUS_KN_PER_M2
        ),

        LibraryItem(
            title = "135°",
            type = AssemblyElementType.CORNER_135,
            areaM2 =
                DEFAULT_TRUSS_AREA_M2,
            elasticModulusKnPerM2 =
                DEFAULT_ALUMINIUM_ELASTIC_MODULUS_KN_PER_M2
        ),

        // ----------------------------------------------------
        // МНОГОНАПРАВЛЕННЫЕ БЛОКИ
        // ----------------------------------------------------

        LibraryItem(
            title = "T",
            type = AssemblyElementType.T_JUNCTION,
            areaM2 =
                DEFAULT_TRUSS_AREA_M2,
            elasticModulusKnPerM2 =
                DEFAULT_ALUMINIUM_ELASTIC_MODULUS_KN_PER_M2
        ),

        LibraryItem(
            title = "X",
            type = AssemblyElementType.X_JUNCTION,
            areaM2 =
                DEFAULT_TRUSS_AREA_M2,
            elasticModulusKnPerM2 =
                DEFAULT_ALUMINIUM_ELASTIC_MODULUS_KN_PER_M2
        ),

        // ----------------------------------------------------
        // КУБ
        // ----------------------------------------------------

        LibraryItem(
            title = "Куб",
            type = AssemblyElementType.CUBE,
            width = 0.29,
            height = 0.29,
            areaM2 =
                DEFAULT_TRUSS_AREA_M2,
            elasticModulusKnPerM2 =
                DEFAULT_ALUMINIUM_ELASTIC_MODULUS_KN_PER_M2
        )
    )

// ============================================================
// ПАНЕЛЬ БИБЛИОТЕКИ
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
            modifier =
                Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                )
        ) {

            Text(
                text = "Элементы",
                style =
                    MaterialTheme
                        .typography
                        .titleSmall
            )

            Spacer(
                modifier =
                    Modifier.height(
                        6.dp
                    )
            )

            Row(
                modifier =
                    Modifier.horizontalScroll(
                        rememberScrollState()
                    ),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                defaultTrussLibrary.forEach { item ->

                    Button(
                        onClick = {
                            onAddElement(
                                item
                            )
                        }
                    ) {

                        Text(
                            text =
                                item.title
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// СОЗДАНИЕ ЭЛЕМЕНТА
// ============================================================

fun createElementFromLibrary(
    item: LibraryItem,
    x: Float = 0f,
    y: Float = 0f
): AssemblyElement {

    val elementName =
        when (item.type) {

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

            AssemblyElementType.CUBE ->
                "Куб"
        }

    return AssemblyElement(

        id =
            UUID
                .randomUUID()
                .toString(),

        type =
            item.type,

        name =
            elementName,

        length =
            item.length,

        width =
            item.width,

        height =
            item.height,

        weight =
            item.weight,

        x =
            x,

        y =
            y,

        rotation =
            0f,

        selected =
            true,

        manufacturer =
            item.manufacturer,

        series =
            item.series,

        article =
            item.article,

        maxDistributedLoad =
            item.maxDistributedLoad,

        maxPointLoad =
            item.maxPointLoad,

        areaM2 =
            item.areaM2,

        elasticModulusKnPerM2 =
            item.elasticModulusKnPerM2
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

    val unselectedElements =
        assembly.elements.map { element ->

            element.copy(
                selected = false
            )
        }

    val newElement =
        createElementFromLibrary(
            item = item,
            x = x,
            y = y
        )

    return assembly.copy(
        elements =
            unselectedElements +
                newElement
    )
}
