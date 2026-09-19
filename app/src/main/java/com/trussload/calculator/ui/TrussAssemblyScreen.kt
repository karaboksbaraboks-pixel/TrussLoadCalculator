package com.trussload.calculator.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.trussload.calculator.assembly.AssemblyElement
import com.trussload.calculator.assembly.AssemblyElementType
import com.trussload.calculator.assembly.AssemblyGeometryConfig
import com.trussload.calculator.assembly.AssemblyToolbar
import com.trussload.calculator.assembly.TrussAssembly
import com.trussload.calculator.assembly.TrussComponentLibrary
import com.trussload.calculator.assembly.addLibraryElement
import com.trussload.calculator.assembly.findElementAt
import com.trussload.calculator.assembly.getNodes
import com.trussload.calculator.assembly.getStraightEndpoints
import com.trussload.calculator.assembly.moveElement
import com.trussload.calculator.assembly.selectElement
import com.trussload.calculator.assembly.snapElementIfNeeded
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TrussAssemblyScreen() {

    var assembly by remember {
        mutableStateOf(
            TrussAssembly(
                name = "Новая сборка"
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
    ) {

        // ====================================================
        // ЗАГОЛОВОК
        // ====================================================

        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 2.dp
        ) {

            Column(
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                )
            ) {

                Text(
                    text = "Конструктор фермы",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "V3 • визуальная сборка",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // ====================================================
        // БИБЛИОТЕКА ЭЛЕМЕНТОВ
        // ====================================================

        TrussComponentLibrary(
            modifier = Modifier.fillMaxWidth(),
            onAddElement = { item ->

                val index =
                    assembly.elements.size % 5

                val startX =
                    180f + index * 25f

                val startY =
                    220f + index * 25f

                assembly =
                    addLibraryElement(
                        assembly = assembly,
                        item = item,
                        x = startX,
                        y = startY
                    )
            }
        )

        // ====================================================
        // ПАНЕЛЬ УПРАВЛЕНИЯ
        // ====================================================

        AssemblyToolbar(
            assembly = assembly,
            onAssemblyChange = {
                assembly = it
            },
            modifier = Modifier.fillMaxWidth()
        )

        // ====================================================
        // ИНФОРМАЦИЯ
        // ====================================================

        AssemblyStatusBar(
            assembly = assembly
        )

        // ====================================================
        // РАБОЧЕЕ ПОЛЕ
        // ====================================================

        AssemblyCanvas(
            assembly = assembly,
            onAssemblyChange = {
                assembly = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp)
                .padding(8.dp)
        )
    }
}


// ============================================================
// ИНФОРМАЦИЯ О СБОРКЕ
// ============================================================

@Composable
private fun AssemblyStatusBar(
    assembly: TrussAssembly
) {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 6.dp
                ),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Column {

                Text(
                    text = "Длина",
                    style = MaterialTheme.typography.labelSmall
                )

                Text(
                    text =
                        "%.1f м".format(
                            assembly.totalStraightLength
                        ),
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Column {

                Text(
                    text = "Секции",
                    style = MaterialTheme.typography.labelSmall
                )

                Text(
                    text =
                        assembly
                            .straightSectionCount
                            .toString(),
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Column {

                Text(
                    text = "Соединители",
                    style = MaterialTheme.typography.labelSmall
                )

                Text(
                    text =
                        assembly
                            .connectorCount
                            .toString(),
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Column {

                Text(
                    text = "Стыки",
                    style = MaterialTheme.typography.labelSmall
                )

                Text(
                    text =
                        assembly
                            .connections
                            .size
                            .toString(),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}


// ============================================================
// РАБОЧЕЕ ПОЛЕ
// ============================================================

@Composable
private fun AssemblyCanvas(
    assembly: TrussAssembly,
    onAssemblyChange: (TrussAssembly) -> Unit,
    modifier: Modifier = Modifier
) {

    /*
     * ВАЖНО:
     *
     * pointerInput больше НЕ зависит от assembly.
     *
     * Раньше использовалось:
     *
     *     .pointerInput(assembly)
     *
     * Из-за этого при каждом изменении координат элемента
     * Compose мог перезапускать обработчик жеста.
     *
     * Поэтому длинное перетаскивание обрывалось.
     *
     * rememberUpdatedState позволяет обработчику жеста
     * всегда получать самое свежее состояние assembly,
     * не перезапуская сам gesture detector.
     */

    val currentAssembly by rememberUpdatedState(assembly)

    Canvas(
        modifier = modifier
            .background(Color.White)
            .pointerInput(Unit) {

                detectDragGestures(

                    // ----------------------------------------
                    // НАЧАЛО ПЕРЕТАСКИВАНИЯ
                    // ----------------------------------------

                    onDragStart = { position ->

                        val latestAssembly =
                            currentAssembly

                        val found =
                            latestAssembly.findElementAt(
                                x = position.x,
                                y = position.y
                            )

                        onAssemblyChange(
                            latestAssembly.selectElement(
                                elementId = found?.id
                            )
                        )
                    },

                    // ----------------------------------------
                    // ПЕРЕТАСКИВАНИЕ
                    // ----------------------------------------

                    onDrag = { change, dragAmount ->

                        change.consume()

                        /*
                         * Берём актуальную сборку НА КАЖДОМ
                         * событии движения пальца.
                         */

                        val latestAssembly =
                            currentAssembly

                        val selected =
                            latestAssembly.selectedElement
                                ?: return@detectDragGestures

                        onAssemblyChange(
                            latestAssembly.moveElement(
                                elementId = selected.id,
                                deltaX = dragAmount.x,
                                deltaY = dragAmount.y
                            )
                        )
                    },

                    // ----------------------------------------
                    // КОНЕЦ ПЕРЕТАСКИВАНИЯ
                    // ----------------------------------------

                    onDragEnd = {

                        val latestAssembly =
                            currentAssembly

                        val selected =
                            latestAssembly.selectedElement
                                ?: return@detectDragGestures

                        /*
                         * После отпускания пальца пытаемся
                         * защёлкнуть элемент к ближайшему узлу.
                         */

                        onAssemblyChange(
                            latestAssembly.snapElementIfNeeded(
                                elementId = selected.id
                            )
                        )
                    },

                    // ----------------------------------------
                    // ОТМЕНА ЖЕСТА
                    // ----------------------------------------

                    onDragCancel = {
                        // Ничего не делаем.
                    }
                )
            }
    ) {

        // ====================================================
        // СЕТКА
        // ====================================================

        drawAssemblyGrid()

        // ====================================================
        // СОХРАНЁННЫЕ СОЕДИНЕНИЯ
        // ====================================================

        drawConnections(
            assembly = assembly
        )

        // ====================================================
        // ЭЛЕМЕНТЫ
        // ====================================================

        assembly.elements.forEach { element ->

            when (element.type) {

                AssemblyElementType.STRAIGHT -> {

                    drawStraightTruss(
                        element = element
                    )
                }

                AssemblyElementType.CORNER_90 -> {

                    drawCorner(
                        element = element
                    )
                }

                AssemblyElementType.CORNER_135 -> {

                    drawCorner(
                        element = element
                    )
                }

                AssemblyElementType.T_JUNCTION -> {

                    drawMultiConnector(
                        element = element
                    )
                }

                AssemblyElementType.X_JUNCTION -> {

                    drawMultiConnector(
                        element = element
                    )
                }
            }

            // ----------------------------------------
            // ТОЧКИ СОЕДИНЕНИЯ
            // ----------------------------------------

            drawElementNodes(
                element = element
            )

            // ----------------------------------------
            // ВЫДЕЛЕННЫЙ ЭЛЕМЕНТ
            // ----------------------------------------

            if (element.selected) {

                drawCircle(
                    color = Color(0x22FF9800),
                    radius = 38f,
                    center = Offset(
                        element.x,
                        element.y
                    )
                )

                drawCircle(
                    color = Color(0xFFFF9800),
                    radius = 5f,
                    center = Offset(
                        element.x,
                        element.y
                    )
                )
            }
        }
    }
}


// ============================================================
// СЕТКА
// ============================================================

private fun DrawScope.drawAssemblyGrid() {

    val grid = 50f

    var x = 0f

    while (x <= size.width) {

        drawLine(
            color = Color(0xFFE9E9E9),
            start = Offset(
                x,
                0f
            ),
            end = Offset(
                x,
                size.height
            ),
            strokeWidth = 1f
        )

        x += grid
    }

    var y = 0f

    while (y <= size.height) {

        drawLine(
            color = Color(0xFFE9E9E9),
            start = Offset(
                0f,
                y
            ),
            end = Offset(
                size.width,
                y
            ),
            strokeWidth = 1f
        )

        y += grid
    }
}


// ============================================================
// ПРЯМАЯ СЕКЦИЯ ФЕРМЫ
// ============================================================

private fun DrawScope.drawStraightTruss(
    element: AssemblyElement
) {

    val endpoints =
        element.getStraightEndpoints(
            pixelsPerMeter =
                AssemblyGeometryConfig.PIXELS_PER_METER
        )

    val start =
        Offset(
            endpoints.first.x,
            endpoints.first.y
        )

    val end =
        Offset(
            endpoints.second.x,
            endpoints.second.y
        )

    val radians =
        Math.toRadians(
            element.rotation.toDouble()
        )

    val normalX =
        (-sin(radians) * 13.0)
            .toFloat()

    val normalY =
        (cos(radians) * 13.0)
            .toFloat()

    val topStart =
        Offset(
            start.x + normalX,
            start.y + normalY
        )

    val topEnd =
        Offset(
            end.x + normalX,
            end.y + normalY
        )

    val bottomStart =
        Offset(
            start.x - normalX,
            start.y - normalY
        )

    val bottomEnd =
        Offset(
            end.x - normalX,
            end.y - normalY
        )

    val color =
        elementColor(
            element = element
        )

    // ====================================================
    // ВЕРХНИЙ ПОЯС
    // ====================================================

    drawLine(
        color = color,
        start = topStart,
        end = topEnd,
        strokeWidth = 5f,
        cap = StrokeCap.Round
    )

    // ====================================================
    // НИЖНИЙ ПОЯС
    // ====================================================

    drawLine(
        color = color,
        start = bottomStart,
        end = bottomEnd,
        strokeWidth = 5f,
        cap = StrokeCap.Round
    )

    // ====================================================
    // ТОРЦЫ
    // ====================================================

    drawLine(
        color = color,
        start = topStart,
        end = bottomStart,
        strokeWidth = 3f
    )

    drawLine(
        color = color,
        start = topEnd,
        end = bottomEnd,
        strokeWidth = 3f
    )

    // ====================================================
    // ПАНЕЛИ
    // ====================================================

    val panelCount =
        (element.length * 2.0)
            .toInt()
            .coerceAtLeast(1)

    for (index in 0 until panelCount) {

        val fractionA =
            index.toFloat() /
                panelCount.toFloat()

        val fractionB =
            (index + 1).toFloat() /
                panelCount.toFloat()

        val topA =
            lerpOffset(
                start = topStart,
                end = topEnd,
                fraction = fractionA
            )

        val topB =
            lerpOffset(
                start = topStart,
                end = topEnd,
                fraction = fractionB
            )

        val bottomA =
            lerpOffset(
                start = bottomStart,
                end = bottomEnd,
                fraction = fractionA
            )

        val bottomB =
            lerpOffset(
                start = bottomStart,
                end = bottomEnd,
                fraction = fractionB
            )

        // ----------------------------------------
        // ВЕРТИКАЛЬНАЯ СТОЙКА
        // ----------------------------------------

        if (index > 0) {

            drawLine(
                color = color,
                start = topA,
                end = bottomA,
                strokeWidth = 2f
            )
        }

        // ----------------------------------------
        // РАСКОС
        // ----------------------------------------

        if (index % 2 == 0) {

            drawLine(
                color = color,
                start = bottomA,
                end = topB,
                strokeWidth = 2.5f
            )

        } else {

            drawLine(
                color = color,
                start = topA,
                end = bottomB,
                strokeWidth = 2.5f
            )
        }
    }
}


// ============================================================
// УГЛОВЫЕ СОЕДИНИТЕЛИ 90° / 135°
// ============================================================

private fun DrawScope.drawCorner(
    element: AssemblyElement
) {

    val nodes =
        element.getNodes()

    if (nodes.size < 2) {
        return
    }

    val color =
        elementColor(
            element = element
        )

    val center =
        Offset(
            element.x,
            element.y
        )

    nodes.forEach { node ->

        drawLine(
            color = color,
            start = center,
            end = Offset(
                node.x,
                node.y
            ),
            strokeWidth = 12f,
            cap = StrokeCap.Round
        )
    }

    drawCircle(
        color = color,
        radius = 10f,
        center = center
    )
}


// ============================================================
// T / X СОЕДИНИТЕЛИ
// ============================================================

private fun DrawScope.drawMultiConnector(
    element: AssemblyElement
) {

    val nodes =
        element.getNodes()

    val color =
        elementColor(
            element = element
        )

    val center =
        Offset(
            element.x,
            element.y
        )

    nodes.forEach { node ->

        drawLine(
            color = color,
            start = center,
            end = Offset(
                node.x,
                node.y
            ),
            strokeWidth = 12f,
            cap = StrokeCap.Round
        )
    }

    drawCircle(
        color = color,
        radius = 10f,
        center = center
    )
}


// ============================================================
// ТОЧКИ СОЕДИНЕНИЯ
// ============================================================

private fun DrawScope.drawElementNodes(
    element: AssemblyElement
) {

    element
        .getNodes()
        .forEach { node ->

            val nodeColor =
                if (element.selected) {

                    Color(0xFFFF5722)

                } else {

                    Color(0xFFD32F2F)
                }

            drawCircle(
                color = nodeColor,
                radius =
                    AssemblyGeometryConfig.NODE_RADIUS,
                center = Offset(
                    node.x,
                    node.y
                )
            )

            drawCircle(
                color = Color.White,
                radius = 3f,
                center = Offset(
                    node.x,
                    node.y
                )
            )
        }
}


// ============================================================
// СОХРАНЁННЫЕ СОЕДИНЕНИЯ
// ============================================================

private fun DrawScope.drawConnections(
    assembly: TrussAssembly
) {

    assembly.connections.forEach { connection ->

        val firstElement =
            assembly.elements.firstOrNull {

                it.id ==
                    connection.firstElementId

            } ?: return@forEach

        val secondElement =
            assembly.elements.firstOrNull {

                it.id ==
                    connection.secondElementId

            } ?: return@forEach

        val firstNode =
            firstElement
                .getNodes()
                .firstOrNull {

                    it.nodeIndex ==
                        connection.firstNodeIndex

                }
                ?: return@forEach

        val secondNode =
            secondElement
                .getNodes()
                .firstOrNull {

                    it.nodeIndex ==
                        connection.secondNodeIndex

                }
                ?: return@forEach

        val center =
            Offset(
                x =
                    (
                        firstNode.x +
                            secondNode.x
                    ) / 2f,

                y =
                    (
                        firstNode.y +
                            secondNode.y
                    ) / 2f
            )

        // ----------------------------------------
        // ЗЕЛЁНАЯ ТОЧКА = СОХРАНЁННЫЙ СТЫК
        // ----------------------------------------

        drawCircle(
            color = Color(0xFF43A047),
            radius = 6f,
            center = center
        )
    }
}


// ============================================================
// ЦВЕТ ЭЛЕМЕНТА
// ============================================================

private fun elementColor(
    element: AssemblyElement
): Color {

    return if (element.selected) {

        Color(0xFFFF9800)

    } else {

        Color(0xFF1565C0)
    }
}


// ============================================================
// ЛИНЕЙНАЯ ИНТЕРПОЛЯЦИЯ
// ============================================================

private fun lerpOffset(
    start: Offset,
    end: Offset,
    fraction: Float
): Offset {

    return Offset(
        x =
            start.x +
                (end.x - start.x) *
                fraction,

        y =
            start.y +
                (end.y - start.y) *
                fraction
    )
}
