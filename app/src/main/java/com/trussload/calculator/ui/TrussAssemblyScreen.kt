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
import com.trussload.calculator.assembly.disconnectElement
import com.trussload.calculator.assembly.findElementAt
import com.trussload.calculator.assembly.getNodes
import com.trussload.calculator.assembly.getStraightEndpoints
import com.trussload.calculator.assembly.moveElement
import com.trussload.calculator.assembly.selectElement
import com.trussload.calculator.assembly.snapElementIfNeeded
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

// ============================================================
// ГЛАВНЫЙ ЭКРАН КОНСТРУКТОРА
// ============================================================

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
                    style =
                        MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "V3.3 • визуальная сборка",
                    style =
                        MaterialTheme.typography.bodySmall
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
        // СТАТУС
        // ====================================================

        AssemblyStatusBar(
            assembly = assembly
        )

        // ====================================================
        // CANVAS
        // ====================================================

        AssemblyEditorCanvas(
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
// СТАТУСНАЯ СТРОКА
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
                title = "Соединители",
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
            style =
                MaterialTheme.typography.labelSmall
        )

        Text(
            text = value,
            style =
                MaterialTheme.typography.titleSmall
        )
    }
}

// ============================================================
// РАБОЧЕЕ ПОЛЕ
// ============================================================

@Composable
private fun AssemblyEditorCanvas(
    assembly: TrussAssembly,
    onAssemblyChange: (TrussAssembly) -> Unit,
    modifier: Modifier = Modifier
) {

    /*
     * Обработчик pointerInput не пересоздаётся
     * при каждом изменении assembly.
     *
     * currentAssembly при этом всегда содержит
     * актуальное состояние проекта.
     */

    val currentAssembly by
        rememberUpdatedState(assembly)

    Canvas(
        modifier = modifier
            .background(Color.White)
            .pointerInput(Unit) {

                detectDragGestures(

                    // ========================================
                    // ПАЛЕЦ КОСНУЛСЯ ЭЛЕМЕНТА
                    // ========================================

                    onDragStart = { position ->

                        val latest =
                            currentAssembly

                        val found =
                            latest.findElementAt(
                                x = position.x,
                                y = position.y
                            )

                        if (found == null) {

                            onAssemblyChange(
                                latest.selectElement(
                                    elementId = null
                                )
                            )

                        } else {

                            /*
                             * При начале движения старые
                             * соединения этого элемента
                             * разрываются.
                             */

                            val disconnected =
                                latest.disconnectElement(
                                    elementId = found.id
                                )

                            onAssemblyChange(
                                disconnected.selectElement(
                                    elementId = found.id
                                )
                            )
                        }
                    },

                    // ========================================
                    // НЕПРЕРЫВНОЕ ПЕРЕМЕЩЕНИЕ
                    // ========================================

                    onDrag = { change, dragAmount ->

                        change.consume()

                        val latest =
                            currentAssembly

                        val selected =
                            latest.selectedElement
                                ?: return@detectDragGestures

                        onAssemblyChange(
                            latest.moveElement(
                                elementId = selected.id,
                                deltaX = dragAmount.x,
                                deltaY = dragAmount.y
                            )
                        )
                    },

                    // ========================================
                    // ОТПУСТИЛИ ПАЛЕЦ
                    // ========================================

                    onDragEnd = {

                        val latest =
                            currentAssembly

                        val selected =
                            latest.selectedElement
                                ?: return@detectDragGestures

                        /*
                         * Если рядом имеется свободный узел,
                         * элемент притягивается к нему.
                         */

                        onAssemblyChange(
                            latest.snapElementIfNeeded(
                                elementId = selected.id
                            )
                        )
                    },

                    onDragCancel = {
                        // Ничего не требуется.
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

        drawAssemblyConnections(
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

                AssemblyElementType.CORNER_90,
                AssemblyElementType.CORNER_135,
                AssemblyElementType.T_JUNCTION,
                AssemblyElementType.X_JUNCTION -> {

                    drawTrussConnector(
                        element = element
                    )
                }
            }

            // Узлы

            drawElementNodes(
                element = element
            )

            // Маркер выбранного элемента

            if (element.selected) {

                drawCircle(
                    color = Color(0x22FF9800),
                    radius = 36f,
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

    val grid =
        50f

    var x =
        0f

    while (x <= size.width) {

        drawLine(
            color = Color(0xFFE8EAED),
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

    var y =
        0f

    while (y <= size.height) {

        drawLine(
            color = Color(0xFFE8EAED),
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
// ПРЯМАЯ СЕКЦИЯ
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

    drawTrussSegment(
        start =
            Offset(
                endpoints.first.x,
                endpoints.first.y
            ),

        end =
            Offset(
                endpoints.second.x,
                endpoints.second.y
            ),

        color =
            elementColor(
                element = element
            ),

        panelCount =
            (element.length * 2.0)
                .toInt()
                .coerceAtLeast(1)
    )
}

// ============================================================
// УГЛЫ / T / X
//
// Каждый луч теперь рисуется не толстой полосой,
// а маленькой секцией фермы.
// ============================================================

private fun DrawScope.drawTrussConnector(
    element: AssemblyElement
) {

    val center =
        Offset(
            element.x,
            element.y
        )

    val color =
        elementColor(
            element = element
        )

    val nodes =
        element.getNodes()

    nodes.forEach { node ->

        drawTrussSegment(
            start = center,
            end =
                Offset(
                    node.x,
                    node.y
                ),
            color = color,
            panelCount = 1
        )
    }

    /*
     * Центральный узел закрывает пересечение
     * внутренних поясов и делает T/X визуально
     * единым элементом.
     */

    drawCircle(
        color = color,
        radius = 8f,
        center = center
    )
}

// ============================================================
// ОТРИСОВКА ОДНОЙ ФЕРМЕННОЙ ВЕТВИ
//
// Используется и прямыми секциями,
// и плечами соединителей.
// ============================================================

private fun DrawScope.drawTrussSegment(
    start: Offset,
    end: Offset,
    color: Color,
    panelCount: Int
) {

    val dx =
        end.x - start.x

    val dy =
        end.y - start.y

    val length =
        hypot(
            dx,
            dy
        )

    if (length < 1f) {
        return
    }

    /*
     * Нормаль к оси фермы.
     */

    val normalX =
        -dy / length

    val normalY =
        dx / length

    /*
     * Половина визуальной высоты фермы.
     */

    val halfHeight =
        13f

    val offsetX =
        normalX * halfHeight

    val offsetY =
        normalY * halfHeight

    val topStart =
        Offset(
            start.x + offsetX,
            start.y + offsetY
        )

    val topEnd =
        Offset(
            end.x + offsetX,
            end.y + offsetY
        )

    val bottomStart =
        Offset(
            start.x - offsetX,
            start.y - offsetY
        )

    val bottomEnd =
        Offset(
            end.x - offsetX,
            end.y - offsetY
        )

    // ========================================================
    // ДВА ПОЯСА
    // ========================================================

    drawLine(
        color = color,
        start = topStart,
        end = topEnd,
        strokeWidth = 4.5f,
        cap = StrokeCap.Round
    )

    drawLine(
        color = color,
        start = bottomStart,
        end = bottomEnd,
        strokeWidth = 4.5f,
        cap = StrokeCap.Round
    )

    // ========================================================
    // ТОРЦЫ
    // ========================================================

    drawLine(
        color = color,
        start = topStart,
        end = bottomStart,
        strokeWidth = 2.5f
    )

    drawLine(
        color = color,
        start = topEnd,
        end = bottomEnd,
        strokeWidth = 2.5f
    )

    // ========================================================
    // ПАНЕЛИ И РАСКОСЫ
    // ========================================================

    val safePanelCount =
        panelCount.coerceAtLeast(1)

    for (index in 0 until safePanelCount) {

        val fractionA =
            index.toFloat() /
                safePanelCount.toFloat()

        val fractionB =
            (index + 1).toFloat() /
                safePanelCount.toFloat()

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
                strokeWidth = 2f
            )

        } else {

            drawLine(
                color = color,
                start = topA,
                end = bottomB,
                strokeWidth = 2f
            )
        }
    }
}

// ============================================================
// УЗЛЫ
// ============================================================

private fun DrawScope.drawElementNodes(
    element: AssemblyElement
) {

    val nodeColor =
        if (element.selected) {
            Color(0xFFFF5722)
        } else {
            Color(0xFFD32F2F)
        }

    element
        .getNodes()
        .forEach { node ->

            drawCircle(
                color = nodeColor,
                radius =
                    AssemblyGeometryConfig
                        .NODE_RADIUS,
                center =
                    Offset(
                        node.x,
                        node.y
                    )
            )

            drawCircle(
                color = Color.White,
                radius = 3f,
                center =
                    Offset(
                        node.x,
                        node.y
                    )
            )
        }
}

// ============================================================
// СОХРАНЁННЫЕ СТЫКИ
// ============================================================

private fun DrawScope.drawAssemblyConnections(
    assembly: TrussAssembly
) {

    assembly.connections.forEach { connection ->

        val firstElement =
            assembly.elements
                .firstOrNull {
                    it.id ==
                        connection.firstElementId
                }
                ?: return@forEach

        val secondElement =
            assembly.elements
                .firstOrNull {
                    it.id ==
                        connection.secondElementId
                }
                ?: return@forEach

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

        /*
         * Зелёная точка означает:
         * соединение записано в модели сборки.
         */

        drawCircle(
            color = Color(0xFF2E7D32),
            radius = 7f,
            center = center
        )

        drawCircle(
            color = Color.White,
            radius = 2.5f,
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
                (
                    end.x -
                        start.x
                ) * fraction,

        y =
            start.y +
                (
                    end.y -
                        start.y
                ) * fraction
    )
}
