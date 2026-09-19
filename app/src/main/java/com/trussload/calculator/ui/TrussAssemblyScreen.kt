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
            .background(MaterialTheme.colorScheme.background)
    ) {

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
                    text = "V3.4 • выбор и свободное перемещение",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        TrussComponentLibrary(
            modifier = Modifier.fillMaxWidth(),
            onAddElement = { item ->

                val index = assembly.elements.size % 5

                assembly = addLibraryElement(
                    assembly = assembly,
                    item = item,
                    x = 180f + index * 25f,
                    y = 220f + index * 25f
                )
            }
        )

        AssemblyToolbar(
            assembly = assembly,
            onAssemblyChange = {
                assembly = it
            },
            modifier = Modifier.fillMaxWidth()
        )

        AssemblyStatusBar(
            assembly = assembly
        )

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
// СТРОКА СОСТОЯНИЯ
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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            StatusItem(
                title = "Длина",
                value = "%.1f м".format(
                    assembly.totalStraightLength
                )
            )

            StatusItem(
                title = "Секции",
                value = assembly
                    .straightSectionCount
                    .toString()
            )

            StatusItem(
                title = "Соединители",
                value = assembly
                    .connectorCount
                    .toString()
            )

            StatusItem(
                title = "Стыки",
                value = assembly
                    .connections
                    .size
                    .toString()
            )
        }
    }
}

@Composable
private fun StatusItem(
    title: String,
    value: String
) {

    Column {

        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall
        )
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
     * pointerInput(Unit), а НЕ pointerInput(assembly).
     *
     * Если использовать assembly как ключ,
     * pointerInput пересоздаётся при каждом движении элемента.
     * Из-за этого drag может обрываться.
     */

    var latestAssembly by remember {
        mutableStateOf(assembly)
    }

    latestAssembly = assembly

    Canvas(
        modifier = modifier
            .background(Color.White)
            .pointerInput(Unit) {

                detectDragGestures(

                    // ----------------------------------------
                    // НАЧАЛО КАСАНИЯ
                    // ----------------------------------------

                    onDragStart = { position ->

                        val current =
                            latestAssembly

                        val found =
                            current.findElementAt(
                                x = position.x,
                                y = position.y
                            )

                        val updated =
                            current.selectElement(
                                elementId = found?.id
                            )

                        latestAssembly = updated
                        onAssemblyChange(updated)
                    },

                    // ----------------------------------------
                    // ПЕРЕМЕЩЕНИЕ
                    // ----------------------------------------

                    onDrag = { change, dragAmount ->

                        change.consume()

                        val current =
                            latestAssembly

                        val selected =
                            current.selectedElement
                                ?: return@detectDragGestures

                        val moved =
                            current.moveElement(
                                elementId = selected.id,
                                deltaX = dragAmount.x,
                                deltaY = dragAmount.y
                            )

                        latestAssembly = moved
                        onAssemblyChange(moved)
                    },

                    // ----------------------------------------
                    // ОТПУСКАНИЕ
                    // ----------------------------------------

                    onDragEnd = {

                        val current =
                            latestAssembly

                        val selected =
                            current.selectedElement
                                ?: return@detectDragGestures

                        val snapped =
                            current.snapElementIfNeeded(
                                elementId = selected.id
                            )

                        latestAssembly = snapped
                        onAssemblyChange(snapped)
                    },

                    onDragCancel = {
                        // Ничего не делаем.
                        // Положение элемента сохраняется.
                    }
                )
            }
    ) {

        drawAssemblyGrid()

        drawConnections(
            assembly = assembly
        )

        assembly.elements.forEach { element ->

            when (element.type) {

                AssemblyElementType.STRAIGHT -> {
                    drawStraightTruss(
                        element = element
                    )
                }

                AssemblyElementType.CORNER_90,
                AssemblyElementType.CORNER_135 -> {
                    drawConnector(
                        element = element
                    )
                }

                AssemblyElementType.T_JUNCTION,
                AssemblyElementType.X_JUNCTION -> {
                    drawConnector(
                        element = element
                    )
                }
            }

            drawElementNodes(
                element = element
            )

            if (element.selected) {

                drawCircle(
                    color = Color(0x22FF9800),
                    radius = 42f,
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
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f
        )

        x += grid
    }

    var y = 0f

    while (y <= size.height) {

        drawLine(
            color = Color(0xFFE9E9E9),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )

        y += grid
    }
}

// ============================================================
// ПРЯМАЯ ФЕРМА
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
        (-sin(radians) * 13.0).toFloat()

    val normalY =
        (cos(radians) * 13.0).toFloat()

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
        elementColor(element)

    drawLine(
        color = color,
        start = topStart,
        end = topEnd,
        strokeWidth = 5f,
        cap = StrokeCap.Round
    )

    drawLine(
        color = color,
        start = bottomStart,
        end = bottomEnd,
        strokeWidth = 5f,
        cap = StrokeCap.Round
    )

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
                topStart,
                topEnd,
                fractionA
            )

        val topB =
            lerpOffset(
                topStart,
                topEnd,
                fractionB
            )

        val bottomA =
            lerpOffset(
                bottomStart,
                bottomEnd,
                fractionA
            )

        val bottomB =
            lerpOffset(
                bottomStart,
                bottomEnd,
                fractionB
            )

        if (index > 0) {

            drawLine(
                color = color,
                start = topA,
                end = bottomA,
                strokeWidth = 2f
            )
        }

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
// СОЕДИНИТЕЛИ
// ============================================================

private fun DrawScope.drawConnector(
    element: AssemblyElement
) {

    val nodes =
        element.getNodes()

    val color =
        elementColor(element)

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
            strokeWidth = 10f,
            cap = StrokeCap.Round
        )
    }

    drawCircle(
        color = color,
        radius = 9f,
        center = center
    )
}

// ============================================================
// УЗЛЫ
// ============================================================

private fun DrawScope.drawElementNodes(
    element: AssemblyElement
) {

    element.getNodes().forEach { node ->

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
// СОХРАНЁННЫЕ СТЫКИ
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
                    (firstNode.x + secondNode.x) / 2f,

                y =
                    (firstNode.y + secondNode.y) / 2f
            )

        drawCircle(
            color = Color(0xFF43A047),
            radius = 6f,
            center = center
        )
    }
}

// ============================================================
// ЦВЕТ
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
// ИНТЕРПОЛЯЦИЯ
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
