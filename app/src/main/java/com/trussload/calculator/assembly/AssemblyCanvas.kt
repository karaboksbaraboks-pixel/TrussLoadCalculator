package com.trussload.calculator.assembly

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val PIXELS_PER_METER = 100f
private const val HIT_DISTANCE = 55f

@Composable
fun AssemblyCanvas(
    project: AssemblyProject,
    onProjectChange: (AssemblyProject) -> Unit,
    modifier: Modifier = Modifier
) {

    Canvas(
        modifier = modifier
            .fillMaxSize()

            // Нажатие — выбор элемента
            .pointerInput(project) {
                detectTapGestures { tap ->

                    val element =
                        findElementAt(
                            project = project,
                            point = tap
                        )

                    onProjectChange(
                        AssemblyEngine.select(
                            project,
                            element?.id
                        )
                    )
                }
            }

            // Перетаскивание
            .pointerInput(project) {

                var draggingId: Long? = null

                detectDragGestures(

                    onDragStart = { point ->

                        draggingId =
                            findElementAt(
                                project,
                                point
                            )?.id

                        draggingId?.let { id ->

                            onProjectChange(
                                AssemblyEngine.select(
                                    project,
                                    id
                                )
                            )
                        }
                    },

                    onDrag = { change, dragAmount ->

                        change.consume()

                        draggingId?.let { id ->

                            onProjectChange(
                                AssemblyEngine.move(
                                    project,
                                    id,
                                    dragAmount
                                )
                            )
                        }
                    },

                    onDragEnd = {

                        draggingId?.let { id ->

                            onProjectChange(
                                AssemblyEngine.snap(
                                    project,
                                    id,
                                    PIXELS_PER_METER
                                )
                            )
                        }

                        draggingId = null
                    },

                    onDragCancel = {
                        draggingId = null
                    }
                )
            }
    ) {

        drawGrid()

        project.elements.forEach { element ->

            when (element.type) {

                AssemblyElementType.STRAIGHT_TRUSS ->
                    drawStraightTruss(element)

                AssemblyElementType.CORNER_90 ->
                    drawCorner90(element)

                AssemblyElementType.CORNER_135 ->
                    drawCorner135(element)

                AssemblyElementType.T_JUNCTION ->
                    drawTJunction(element)

                AssemblyElementType.CROSS ->
                    drawCross(element)

                AssemblyElementType.SUPPORT ->
                    drawSupport(element)

                AssemblyElementType.LOAD_POINT ->
                    drawLoadPoint(element)
            }
        }
    }
}

private fun DrawScope.drawGrid() {

    val step = 50f

    var x = 0f

    while (x <= size.width) {

        drawLine(
            color = Color(0xFFE7E7E7),
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f
        )

        x += step
    }

    var y = 0f

    while (y <= size.height) {

        drawLine(
            color = Color(0xFFE7E7E7),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )

        y += step
    }
}

private fun DrawScope.drawStraightTruss(
    element: AssemblyElement
) {

    val length =
        element.lengthMeters *
            PIXELS_PER_METER

    val angle =
        Math.toRadians(
            element.rotationDegrees.toDouble()
        )

    val dx =
        cos(angle).toFloat() *
            length / 2f

    val dy =
        sin(angle).toFloat() *
            length / 2f

    val start =
        element.position -
            Offset(dx, dy)

    val end =
        element.position +
            Offset(dx, dy)

    val color =
        if (element.selected)
            Color(0xFF1565C0)
        else
            Color(0xFF303030)

    // Основная ферма
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth =
            if (element.selected) 16f
            else 12f,
        cap = StrokeCap.Round
    )

    // Узлы соединения
    drawCircle(
        color = Color(0xFFFF9800),
        radius = 10f,
        center = start
    )

    drawCircle(
        color = Color(0xFFFF9800),
        radius = 10f,
        center = end
    )

    if (element.selected) {

        drawCircle(
            color = Color(0xFF1565C0),
            radius = 26f,
            center = element.position,
            style = Stroke(width = 4f)
        )
    }
}

private fun DrawScope.drawCorner90(
    element: AssemblyElement
) {

    val p = element.position

    val color =
        if (element.selected)
            Color(0xFF1565C0)
        else
            Color(0xFF424242)

    drawLine(
        color,
        p,
        p + Offset(70f, 0f),
        12f,
        StrokeCap.Round
    )

    drawLine(
        color,
        p,
        p + Offset(0f, 70f),
        12f,
        StrokeCap.Round
    )

    drawCircle(
        Color(0xFFFF9800),
        10f,
        p
    )
}

private fun DrawScope.drawCorner135(
    element: AssemblyElement
) {

    val p = element.position

    val color =
        if (element.selected)
            Color(0xFF1565C0)
        else
            Color(0xFF424242)

    drawLine(
        color,
        p,
        p + Offset(70f, 0f),
        12f,
        StrokeCap.Round
    )

    drawLine(
        color,
        p,
        p + Offset(-50f, 50f),
        12f,
        StrokeCap.Round
    )
}

private fun DrawScope.drawTJunction(
    element: AssemblyElement
) {

    val p = element.position

    val color =
        if (element.selected)
            Color(0xFF1565C0)
        else
            Color(0xFF424242)

    drawLine(
        color,
        p + Offset(-60f, 0f),
        p + Offset(60f, 0f),
        12f
    )

    drawLine(
        color,
        p,
        p + Offset(0f, 70f),
        12f
    )
}

private fun DrawScope.drawCross(
    element: AssemblyElement
) {

    val p = element.position

    val color =
        if (element.selected)
            Color(0xFF1565C0)
        else
            Color(0xFF424242)

    drawLine(
        color,
        p + Offset(-60f, 0f),
        p + Offset(60f, 0f),
        12f
    )

    drawLine(
        color,
        p + Offset(0f, -60f),
        p + Offset(0f, 60f),
        12f
    )
}

private fun DrawScope.drawSupport(
    element: AssemblyElement
) {

    val p = element.position

    val color =
        if (element.selected)
            Color(0xFF1565C0)
        else
            Color(0xFF2E7D32)

    drawLine(
        color,
        p,
        p + Offset(-30f, 45f),
        8f
    )

    drawLine(
        color,
        p,
        p + Offset(30f, 45f),
        8f
    )

    drawLine(
        color,
        p + Offset(-40f, 45f),
        p + Offset(40f, 45f),
        8f
    )
}

private fun DrawScope.drawLoadPoint(
    element: AssemblyElement
) {

    val p = element.position

    val color =
        if (element.selected)
            Color(0xFF1565C0)
        else
            Color(0xFFD32F2F)

    drawLine(
        color,
        p + Offset(0f, -50f),
        p,
        8f
    )

    drawLine(
        color,
        p,
        p + Offset(-15f, -20f),
        8f
    )

    drawLine(
        color,
        p,
        p + Offset(15f, -20f),
        8f
    )
}

private fun findElementAt(
    project: AssemblyProject,
    point: Offset
): AssemblyElement? {

    return project.elements
        .asReversed()
        .minByOrNull { element ->

            val dx =
                point.x -
                    element.position.x

            val dy =
                point.y -
                    element.position.y

            sqrt(dx * dx + dy * dy)
        }
        ?.takeIf { element ->

            val dx =
                point.x -
                    element.position.x

            val dy =
                point.y -
                    element.position.y

            sqrt(dx * dx + dy * dy) <=
                maxOf(
                    HIT_DISTANCE,
                    element.lengthMeters *
                        PIXELS_PER_METER /
                        2f
                )
        }
}
