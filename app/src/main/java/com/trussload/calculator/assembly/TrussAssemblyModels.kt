package com.trussload.calculator.assembly

import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin

// ============================================================
// ТИПЫ ЭЛЕМЕНТОВ
// ============================================================

enum class AssemblyElementType {
    STRAIGHT,
    CORNER_90,
    CORNER_135,
    T_JUNCTION,
    X_JUNCTION,
    CUBE
}

// ============================================================
// ЭЛЕМЕНТ ФЕРМЫ
// ============================================================

data class AssemblyElement(

    val id: String = UUID.randomUUID().toString(),

    val type: AssemblyElementType,

    val name: String,

    // --------------------------------------------------------
    // ГЕОМЕТРИЯ
    // --------------------------------------------------------

    // Длина элемента, м
    val length: Double = 0.0,

    // Габариты фермы, м
    val width: Double = 0.29,
    val height: Double = 0.29,

    // --------------------------------------------------------
    // МАССА
    // --------------------------------------------------------

    // Масса элемента, кг
    val weight: Double = 0.0,

    // --------------------------------------------------------
    // ПОЛОЖЕНИЕ НА РАБОЧЕМ ПОЛЕ
    // --------------------------------------------------------

    // Центр элемента, px
    val x: Float = 0f,
    val y: Float = 0f,

    // Поворот, градусы
    val rotation: Float = 0f,

    // Выбран ли элемент
    val selected: Boolean = false,

    // --------------------------------------------------------
    // ДАННЫЕ ПРОИЗВОДИТЕЛЯ
    // --------------------------------------------------------

    val manufacturer: String = "",
    val series: String = "",
    val article: String = "",

    // --------------------------------------------------------
    // ДОПУСТИМЫЕ НАГРУЗКИ
    // --------------------------------------------------------

    // Максимальная распределённая нагрузка, кН/м
    val maxDistributedLoad: Double? = null,

    // Максимальная точечная нагрузка, кН
    val maxPointLoad: Double? = null,

    // --------------------------------------------------------
    // РАСЧЁТНЫЕ ХАРАКТЕРИСТИКИ
    //
    // Эти значения передаются в StructuralMember.
    //
    // areaM2:
    // площадь расчётного сечения, м²
    //
    // elasticModulusKnPerM2:
    // модуль упругости, кН/м²
    // --------------------------------------------------------

    val areaM2: Double? = null,

    val elasticModulusKnPerM2: Double? = null
) {

    // --------------------------------------------------------
    // ПРЯМОЙ ЭЛЕМЕНТ
    // --------------------------------------------------------

    val isStraight: Boolean
        get() =
            type == AssemblyElementType.STRAIGHT

    // --------------------------------------------------------
    // СОЕДИНИТЕЛЬ
    // --------------------------------------------------------

    val isConnector: Boolean
        get() =
            type != AssemblyElementType.STRAIGHT

    // --------------------------------------------------------
    // НОРМАЛИЗОВАННЫЙ УГОЛ
    // --------------------------------------------------------

    val rotationNormalized: Float
        get() {

            var value =
                rotation % 360f

            if (value < 0f) {
                value += 360f
            }

            return value
        }

    // --------------------------------------------------------
    // ЕСТЬ ЛИ ДАННЫЕ ДЛЯ РАСЧЁТА ЖЁСТКОСТИ
    // --------------------------------------------------------

    val hasStructuralProperties: Boolean
        get() {

            val area =
                areaM2

            val elasticModulus =
                elasticModulusKnPerM2

            return area != null &&
                area.isFinite() &&
                area > 0.0 &&
                elasticModulus != null &&
                elasticModulus.isFinite() &&
                elasticModulus > 0.0
        }
}

// ============================================================
// СОЕДИНЕНИЕ ДВУХ УЗЛОВ
// ============================================================

data class AssemblyConnection(

    val id: String = UUID.randomUUID().toString(),

    val firstElementId: String,
    val firstNodeIndex: Int,

    val secondElementId: String,
    val secondNodeIndex: Int
)

// ============================================================
// ПРОЕКТ СБОРКИ
// ============================================================

data class TrussAssembly(

    val id: String = UUID.randomUUID().toString(),

    val name: String = "Новая сборка",

    val elements: List<AssemblyElement> = emptyList(),

    val connections: List<AssemblyConnection> = emptyList(),

    // Масштаб рабочего поля
    val zoom: Float = 1f,

    val panX: Float = 0f,
    val panY: Float = 0f
) {

    // --------------------------------------------------------
    // ОБЩАЯ ДЛИНА ПРЯМЫХ СЕКЦИЙ
    // --------------------------------------------------------

    val totalStraightLength: Double
        get() =
            elements
                .filter {
                    it.type ==
                        AssemblyElementType.STRAIGHT
                }
                .sumOf {
                    it.length
                }

    // --------------------------------------------------------
    // ОБЩАЯ МАССА
    // --------------------------------------------------------

    val totalWeight: Double
        get() =
            elements.sumOf {
                it.weight
            }

    // --------------------------------------------------------
    // КОЛИЧЕСТВО ПРЯМЫХ СЕКЦИЙ
    // --------------------------------------------------------

    val straightSectionCount: Int
        get() =
            elements.count {
                it.type ==
                    AssemblyElementType.STRAIGHT
            }

    // --------------------------------------------------------
    // КОЛИЧЕСТВО СОЕДИНИТЕЛЕЙ
    // --------------------------------------------------------

    val connectorCount: Int
        get() =
            elements.count {
                it.type !=
                    AssemblyElementType.STRAIGHT
            }

    // --------------------------------------------------------
    // ВЫБРАННЫЙ ЭЛЕМЕНТ
    // --------------------------------------------------------

    val selectedElement: AssemblyElement?
        get() =
            elements.firstOrNull {
                it.selected
            }
}

// ============================================================
// ТОЧКА В РАБОЧЕМ ПОЛЕ
// ============================================================

data class AssemblyPoint(
    val x: Float,
    val y: Float
)

// ============================================================
// КОНЦЫ ПРЯМОЙ СЕКЦИИ
// ============================================================

fun AssemblyElement.getStraightEndpoints(
    pixelsPerMeter: Float
): Pair<AssemblyPoint, AssemblyPoint> {

    if (
        type !=
        AssemblyElementType.STRAIGHT
    ) {

        val center =
            AssemblyPoint(
                x = x,
                y = y
            )

        return center to center
    }

    val lengthPixels =
        length.toFloat() *
            pixelsPerMeter

    val radians =
        Math.toRadians(
            rotation.toDouble()
        )

    val halfDx =
        (
            cos(radians) *
                lengthPixels /
                2.0
        ).toFloat()

    val halfDy =
        (
            sin(radians) *
                lengthPixels /
                2.0
        ).toFloat()

    val first =
        AssemblyPoint(
            x = x - halfDx,
            y = y - halfDy
        )

    val second =
        AssemblyPoint(
            x = x + halfDx,
            y = y + halfDy
        )

    return first to second
}

// ============================================================
// ВЫБОР ЭЛЕМЕНТА
// ============================================================

fun TrussAssembly.selectElement(
    elementId: String?
): TrussAssembly {

    return copy(

        elements =
            elements.map { element ->

                element.copy(
                    selected =
                        element.id ==
                            elementId
                )
            }
    )
}

// ============================================================
// СНЯТИЕ ВЫБОРА
// ============================================================

fun TrussAssembly.clearSelection():
    TrussAssembly {

    return copy(

        elements =
            elements.map { element ->

                element.copy(
                    selected = false
                )
            }
    )
}

// ============================================================
// ПЕРЕМЕЩЕНИЕ ЭЛЕМЕНТА
// ============================================================

fun TrussAssembly.moveElement(
    elementId: String,
    deltaX: Float,
    deltaY: Float
): TrussAssembly {

    return copy(

        elements =
            elements.map { element ->

                if (
                    element.id ==
                    elementId
                ) {

                    element.copy(
                        x =
                            element.x +
                                deltaX,

                        y =
                            element.y +
                                deltaY
                    )

                } else {

                    element
                }
            }
    )
}

// ============================================================
// ТОЧНАЯ УСТАНОВКА ПОЗИЦИИ
// ============================================================

fun TrussAssembly.setElementPosition(
    elementId: String,
    x: Float,
    y: Float
): TrussAssembly {

    return copy(

        elements =
            elements.map { element ->

                if (
                    element.id ==
                    elementId
                ) {

                    element.copy(
                        x = x,
                        y = y
                    )

                } else {

                    element
                }
            }
    )
}

// ============================================================
// ПОВОРОТ ЭЛЕМЕНТА
// ============================================================

fun TrussAssembly.rotateElement(
    elementId: String,
    degrees: Float
): TrussAssembly {

    return copy(

        elements =
            elements.map { element ->

                if (
                    element.id ==
                    elementId
                ) {

                    var newRotation =
                        element.rotation +
                            degrees

                    newRotation %=
                        360f

                    if (
                        newRotation <
                        0f
                    ) {

                        newRotation +=
                            360f
                    }

                    element.copy(
                        rotation =
                            newRotation
                    )

                } else {

                    element
                }
            }
    )
}

// ============================================================
// УДАЛЕНИЕ ЭЛЕМЕНТА
// ============================================================

fun TrussAssembly.deleteElement(
    elementId: String
): TrussAssembly {

    return copy(

        elements =
            elements.filterNot {
                it.id == elementId
            },

        connections =
            connections.filterNot {

                it.firstElementId ==
                    elementId ||
                    it.secondElementId ==
                    elementId
            }
    )
}

// ============================================================
// УДАЛЕНИЕ ВЫБРАННОГО ЭЛЕМЕНТА
// ============================================================

fun TrussAssembly.deleteSelectedElement():
    TrussAssembly {

    val selectedId =
        selectedElement?.id
            ?: return this

    return deleteElement(
        elementId =
            selectedId
    )
}

// ============================================================
// ОЧИСТКА СБОРКИ
// ============================================================

fun TrussAssembly.clearAssembly():
    TrussAssembly {

    return copy(
        elements =
            emptyList(),

        connections =
            emptyList()
    )
}

// ============================================================
// ОТСОЕДИНЕНИЕ ЭЛЕМЕНТА ОТ ВСЕХ СТЫКОВ
// ============================================================

fun TrussAssembly.disconnectElement(
    elementId: String
): TrussAssembly {

    return copy(

        connections =
            connections.filterNot {
                    connection ->

                connection.firstElementId ==
                    elementId ||
                    connection.secondElementId ==
                    elementId
            }
    )
}

// ============================================================
// СТАНДАРТНЫЕ ЭЛЕМЕНТЫ
// ============================================================

object StandardTrussElements {

    val straightSections =
        listOf(

            AssemblyElement(
                type =
                    AssemblyElementType.STRAIGHT,

                name =
                    "Ферма 0.5 м",

                length =
                    0.5
            ),

            AssemblyElement(
                type =
                    AssemblyElementType.STRAIGHT,

                name =
                    "Ферма 1 м",

                length =
                    1.0
            ),

            AssemblyElement(
                type =
                    AssemblyElementType.STRAIGHT,

                name =
                    "Ферма 2 м",

                length =
                    2.0
            ),

            AssemblyElement(
                type =
                    AssemblyElementType.STRAIGHT,

                name =
                    "Ферма 3 м",

                length =
                    3.0
            ),

            AssemblyElement(
                type =
                    AssemblyElementType.STRAIGHT,

                name =
                    "Ферма 4 м",

                length =
                    4.0
            )
        )

    val connectors =
        listOf(

            AssemblyElement(
                type =
                    AssemblyElementType.CORNER_90,

                name =
                    "Угол 90°"
            ),

            AssemblyElement(
                type =
                    AssemblyElementType.CORNER_135,

                name =
                    "Угол 135°"
            ),

            AssemblyElement(
                type =
                    AssemblyElementType.T_JUNCTION,

                name =
                    "T-соединитель"
            ),

            AssemblyElement(
                type =
                    AssemblyElementType.X_JUNCTION,

                name =
                    "X-соединитель"
            ),

            AssemblyElement(
                type =
                    AssemblyElementType.CUBE,

                name =
                    "Куб"
            )
        )
}
