package com.trussload.calculator.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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
                    text = "V4 • визуальная сборка",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // ====================================================
        // БИБЛИОТЕКА
        // ====================================================

        TrussComponentLibrary(
            modifier = Modifier.fillMaxWidth(),
            onAddElement = { item ->

                val index =
                    assembly.elements.size % 6

                val startX =
                    180f + index * 24f

                val startY =
                    180f + index * 30f

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
        // УПРАВЛЕНИЕ
        // ====================================================

        AssemblyToolbar(
            assembly = assembly,
            onAssemblyChange = {
                assembly = it
            },
            modifier = Modifier.fillMaxWidth()
        )

        // ====================================================
        // СТАТУС
        // ====================================================

        AssemblyStatusBar(
            assembly = assembly
        )

        // ====================================================
        // РАБОЧЕЕ ПОЛЕ
        // ====================================================

        AssemblyWorkspaceCanvas(
            assembly = assembly,
            onAssemblyChange = {
                assembly = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        )
    }
}

// ============================================================
// СТАТУСНАЯ ПАНЕЛЬ
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

            StatusValue(
                title = "Длина",
                value =
                    "%.1f м".format(
                        assembly.totalStraightLength
                    )
            )

            StatusValue(
                title = "Секции",
                value =
                    assembly
                        .straightSectionCount
                        .toString()
            )

            StatusValue(
                title = "Блоки",
                value =
                    assembly
                        .connectorCount
                        .toString()
            )

            StatusValue(
                title = "Стыки",
                value =
                    assembly
                        .connections
                        .size
                        .toString()
            )
        }
    }
}

@Composable
private fun StatusValue(
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
private fun AssemblyWorkspaceCanvas(
    assembly: TrussAssembly,
    onAssemblyChange: (TrussAssembly) -> Unit,
    modifier: Modifier = Modifier
) {

    /*
     * Важный момент:
     *
     * pointerInput(Unit) не пересоздаёт gesture detector
     * после каждого пикселя перемещения.
     *
     * А rememberUpdatedState даёт обработчику жеста
     * актуальную версию assembly.
     *
     * Это исправляет проблему, когда drag мог
     * неожиданно прекращаться после небольшого движения.
     */

    val currentAssembly by
        androidx.compose.runtime.rememberUpdatedState(
            assembly
        )

    val currentOnAssemblyChange by
        androidx.compose.runtime.rememberUpdatedState(
            onAssemblyChange
        )

    Canvas(
        modifier = modifier
            .background(Color.White)
            .pointerInput(Unit) {

                var draggingElementId: String? = null

                detectDragGestures(

                    // ----------------------------------------
                    // НАЧАЛО ПЕРЕМЕЩЕНИЯ
                    // ----------------------------------------

                    onDragStart = { position ->

                        val found =
                            currentAssembly
                                .findElementAt(
                                    x = position.x,
                                    y = position.y
                                )

                        draggingElementId =
                            found?.id

                        currentOnAssemblyChange(
                            currentAssembly
                                .selectElement(
                                    elementId = found?.id
                                )
                        )
                    },

                    // ----------------------------------------
                    // ПЕРЕМЕЩЕНИЕ
                    // ----------------------------------------

                    onDrag = { change, dragAmount ->

                        change.consume()

                        val elementId =
                            draggingElementId
                                ?: return@detectDragGestures

                        val latest =
                            currentAssembly

                        currentOnAssemblyChange(
                            latest.moveElement(
                                elementId = elementId,
                                deltaX = dragAmount.x,
                                deltaY = dragAmount.y
                            )
                        )
                    },

                    // ----------------------------------------
                    // ОТПУСКАНИЕ
                    // ----------------------------------------

                    onDragEnd = {

                        val elementId =
                            draggingElementId

                        if (elementId != null) {

                            currentOnAssemblyChange(
                                currentAssembly
                                    .snapElementIfNeeded(
                                        elementId = elementId
                                    )
                            )
                        }

                        draggingElementId = null
                    },

                    onDragCancel = {

                        draggingElementId = null
                    }
                )
            }
    ) {

        drawWorkspaceGrid()

        drawSavedConnections(
            assembly = assembly
        )

        assembly.elements.forEach { element ->

            when (element.type) {

                AssemblyElementType.STRAIGHT -> {
                    drawStraightTruss(
                        element = element
                    )
                }

                AssemblyElementType.CORNER_90 -> {
                    drawConnectorTruss(
                        element = element
                    )
                }

                AssemblyElementType.CORNER_135 -> {
                    drawConnectorTruss(
                        element = element
                    )
                }

                AssemblyElementType.T_JUNCTION -> {
                    drawConnectorTruss(
                        element = element
                    )
                }

                AssemblyElementType.X_JUNCTION -> {
                    drawConnectorTruss(
                        element = element
                    )
                }

                AssemblyElementType.CUBE -> {
                    drawCube(
                        element = element
                    )
                }
            }

            drawConnectionNodes(
                element = element
            )

            if (element.selected) {

                drawSelection(
                    element = element
                )
            }
        }
    }
}

// ============================================================
// СЕТКА
// ============================================================

private fun DrawScope.drawWorkspaceGrid() {

    val grid =
        50f

    var x =
        0f

    while (x <= size.width) {

        drawLine(
            color = Color(0xFFE7E9EC),
            start = Offset(
                x = x,
                y = 0f
            ),
            end = Offset(
                x = x,
                y = size.height
            ),
            strokeWidth = 1f
        )

        x += grid
    }

    var y =
        0f

    while (y <= size.height) {

        drawLine(
            color = Color(0xFFE7E9EC),
            start = Offset(
                x = 0f,
                y = y
            ),
            end = Offset(
                x = size.width,
                y = y
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
                AssemblyGeometryConfig
                    .PIXELS_PER_METER
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

    val halfHeight =
        13f

    val normalX =
        (
            -sin(radians) *
                halfHeight
        ).toFloat()

    val normalY =
        (
            cos(radians) *
                halfHeight
        ).toFloat()

    val firstA =
        Offset(
            start.x + normalX,
            start.y + normalY
        )

    val firstB =
        Offset(
            start.x - normalX,
            start.y - normalY
        )

    val secondA =
        Offset(
            end.x + normalX,
            end.y + normalY
        )

    val secondB =
        Offset(
            end.x - normalX,
            end.y - normalY
        )

    val color =
        elementColor(element)

    // Два пояса

    drawLine(
        color = color,
        start = firstA,
        end = secondA,
        strokeWidth = 5f,
        cap = StrokeCap.Round
    )

    drawLine(
        color = color,
        start = firstB,
        end = secondB,
        strokeWidth = 5f,
        cap = StrokeCap.Round
    )

    // Торцы

    drawLine(
        color = color,
        start = firstA,
        end = firstB,
        strokeWidth = 3f
    )

    drawLine(
        color = color,
        start = secondA,
        end = secondB,
        strokeWidth = 3f
    )

    // Панели

    val panelCount =
        (element.length * 2.0)
            .toInt()
            .coerceAtLeast(1)

    for (index in 0 until panelCount) {

        val a =
            index.toFloat() /
                panelCount.toFloat()

        val b =
            (index + 1).toFloat() /
                panelCount.toFloat()

        val topA =
            interpolate(
                firstA,
                secondA,
                a
            )

        val topB =
            interpolate(
                firstA,
                secondA,
                b
            )

        val bottomA =
            interpolate(
                firstB,
                secondB,
                a
            )

        val bottomB =
            interpolate(
                firstB,
                secondB,
                b
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
// УГЛЫ / T / X
//
// Теперь блок рисуется по реальным узлам из Geometry,
// поэтому T и X не имеют отдельной конфликтующей геометрии.
// ============================================================

private fun DrawScope.drawConnectorTruss(
    element: AssemblyElement
) {

    val nodes =
        element.getNodes()

    val center =
        Offset(
            element.x,
            element.y
        )

    val color =
        elementColor(element)

    nodes.forEach { node ->

        val end =
            Offset(
                node.x,
                node.y
            )

        drawConnectorArm(
            start = center,
            end = end,
            color = color
        )
    }

    drawCircle(
        color = color,
        radius = 12f,
        center = center
    )
}

// ============================================================
// ПЛЕЧО СОЕДИНИТЕЛЯ В ВИДЕ МИНИ-ФЕРМЫ
// ============================================================

private fun DrawScope.drawConnectorArm(
    start: Offset,
    end: Offset,
    color: Color
) {

    val dx =
        end.x - start.x

    val dy =
        end.y - start.y

    val length =
        kotlin.math.sqrt(
            dx * dx +
                dy * dy
        )

    if (length < 1f) {
        return
    }

    val normalX =
        -dy / length * 7f

    val normalY =
        dx / length * 7f

    val startA =
        Offset(
            start.x + normalX,
            start.y + normalY
        )

    val startB =
        Offset(
            start.x - normalX,
            start.y - normalY
        )

    val endA =
        Offset(
            end.x + normalX,
            end.y + normalY
        )

    val endB =
        Offset(
            end.x - normalX,
            end.y - normalY
        )

    drawLine(
        color = color,
        start = startA,
        end = endA,
        strokeWidth = 4f
    )

    drawLine(
        color = color,
        start = startB,
        end = endB,
        strokeWidth = 4f
    )

    drawLine(
        color = color,
        start = startA,
        end = endB,
        strokeWidth = 2f
    )

    drawLine(
        color = color,
        start = endA,
        end = endB,
        strokeWidth = 2f
    )
}

// ============================================================
// КУБ
//
// Рисуем псевдо-3D блок.
// Рабочие узлы подключения берутся из AssemblyGeometry.
// ============================================================

private fun DrawScope.drawCube(
    element: AssemblyElement
) {

    val color =
        elementColor(element)

    val center =
        Offset(
            element.x,
            element.y
        )

    val cubeSize =
        AssemblyGeometryConfig.CUBE_SIZE

    val half =
        cubeSize / 2f

    // Поворот квадрата в соответствии с rotation

    val corners =
        listOf(
            rotateScreenPoint(
                -half,
                -half,
                element.rotation
            ),
            rotateScreenPoint(
                half,
                -half,
                element.rotation
            ),
            rotateScreenPoint(
                half,
                half,
                element.rotation
            ),
            rotateScreenPoint(
                -half,
                half,
                element.rotation
            )
        ).map {
            Offset(
                center.x + it.x,
                center.y + it.y
            )
        }

    val frontPath =
        Path().apply {

            moveTo(
                corners[0].x,
                corners[0].y
            )

            lineTo(
                corners[1].x,
                corners[1].y
            )

            lineTo(
                corners[2].x,
                corners[2].y
            )

            lineTo(
                corners[3].x,
                corners[3].y
            )

            close()
        }

    drawPath(
        path = frontPath,
        color = color,
        style = Stroke(
            width = 5f
        )
    )

    // Смещение задней грани для эффекта объёма

    val depth =
        rotateScreenPoint(
            12f,
            -12f,
            element.rotation
        )

    val backCorners =
        corners.map {
            Offset(
                it.x + depth.x,
                it.y + depth.y
            )
        }

    val backPath =
        Path().apply {

            moveTo(
                backCorners[0].x,
                backCorners[0].y
            )

            lineTo(
                backCorners[1].x,
                backCorners[1].y
            )

            lineTo(
                backCorners[2].x,
                backCorners[2].y
            )

            lineTo(
                backCorners[3].x,
                backCorners[3].y
            )

            close()
        }

    drawPath(
        path = backPath,
        color = color,
        style = Stroke(
            width = 3f
        )
    )

    for (index in 0..3) {

        drawLine(
            color = color,
            start = corners[index],
            end = backCorners[index],
            strokeWidth = 3f
        )
    }

    // Центр

    drawCircle(
        color = color,
        radius = 7f,
        center = center
    )
}

// ============================================================
// УЗЛЫ ПОДКЛЮЧЕНИЯ
// ============================================================

private fun DrawScope.drawConnectionNodes(
    element: AssemblyElement
) {

    element.getNodes().forEach { node ->

        drawCircle(
            color =
                if (element.selected) {
                    Color(0xFFFF5722)
                } else {
                    Color(0xFFD32F2F)
                },
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
// ВЫДЕЛЕНИЕ
// ============================================================

private fun DrawScope.drawSelection(
    element: AssemblyElement
) {

    if (
        element.type ==
        AssemblyElementType.STRAIGHT
    ) {

        // Для длинной фермы достаточно центрального маркера.

        drawCircle(
            color = Color(0x44FF9800),
            radius = 25f,
            center = Offset(
                element.x,
                element.y
            )
        )

    } else {

        val selectionSize =
            if (
                element.type ==
                AssemblyElementType.CUBE
            ) {
                AssemblyGeometryConfig.CUBE_SIZE +
                    30f
            } else {
                AssemblyGeometryConfig.CONNECTOR_SIZE *
                    2f +
                    30f
            }

        drawRect(
            color = Color(0xFFFF9800),
            topLeft = Offset(
                element.x -
                    selectionSize / 2f,
                element.y -
                    selectionSize / 2f
            ),
            size = Size(
                selectionSize,
                selectionSize
            ),
            style = Stroke(
                width = 3f
            )
        )
    }

    drawCircle(
        color = Color(0xFFFF9800),
        radius = 5f,
        center = Offset(
            element.x,
            element.y
        )
    )
}

// ============================================================
// СОХРАНЁННЫЕ СТЫКИ
// ============================================================

private fun DrawScope.drawSavedConnections(
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

private fun interpolate(
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

// ============================================================
// ПОВОРОТ ТОЧКИ ДЛЯ ОТРИСОВКИ
// ============================================================

private fun rotateScreenPoint(
    x: Float,
    y: Float,
    rotationDegrees: Float
): Offset {

    val radians =
        Math.toRadians(
            rotationDegrees.toDouble()
        )

    val cosine =
        cos(radians).toFloat()

    val sine =
        sin(radians).toFloat()

    return Offset(
        x =
            x * cosine -
                y * sine,

        y =
            x * sine +
                y * cosine
    )
}
