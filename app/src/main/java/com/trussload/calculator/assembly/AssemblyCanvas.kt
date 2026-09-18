package com.trussload.calculator.assembly

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

data class AssemblyCanvasElement(
    val id: Long,
    val lengthMeters: Float,
    val x: Float,
    val y: Float
)

@Composable
fun AssemblyCanvas(
    modifier: Modifier = Modifier
) {
    var elements by remember {
        mutableStateOf(
            listOf(
                AssemblyCanvasElement(
                    id = 1L,
                    lengthMeters = 2f,
                    x = 180f,
                    y = 300f
                )
            )
        )
    }

    var selectedId by remember { mutableStateOf<Long?>(1L) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6F8))
    ) {

        Text(
            text = "Конструктор фермы",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Button(
                onClick = {
                    val nextId =
                        (elements.maxOfOrNull { it.id } ?: 0L) + 1L

                    elements = elements + AssemblyCanvasElement(
                        id = nextId,
                        lengthMeters = 2f,
                        x = 180f,
                        y = 300f + elements.size * 80f
                    )

                    selectedId = nextId
                }
            ) {
                Text("+ 2 м")
            }

            Button(
                onClick = {
                    val nextId =
                        (elements.maxOfOrNull { it.id } ?: 0L) + 1L

                    elements = elements + AssemblyCanvasElement(
                        id = nextId,
                        lengthMeters = 3f,
                        x = 180f,
                        y = 300f + elements.size * 80f
                    )

                    selectedId = nextId
                }
            ) {
                Text("+ 3 м")
            }

            OutlinedButton(
                onClick = {
                    val id = selectedId
                    if (id != null) {
                        elements = elements.filterNot {
                            it.id == id
                        }

                        selectedId = null
                    }
                }
            ) {
                Text("Удалить")
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Секций: ${elements.size}   " +
                    "Общая длина: ${
                        "%.1f".format(
                            elements.sumOf {
                                it.lengthMeters.toDouble()
                            }
                        )
                    } м",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White)
                .pointerInput(elements, selectedId) {

                    detectDragGestures(
                        onDragStart = { touch ->

                            selectedId = elements
                                .minByOrNull { element ->
                                    abs(touch.y - element.y)
                                }
                                ?.takeIf { element ->
                                    abs(touch.y - element.y) < 70f
                                }
                                ?.id
                        },

                        onDrag = { change, dragAmount ->
                            change.consume()

                            val id = selectedId ?: return@detectDragGestures

                            elements = elements.map { element ->

                                if (element.id == id) {
                                    element.copy(
                                        x = element.x + dragAmount.x,
                                        y = element.y + dragAmount.y
                                    )
                                } else {
                                    element
                                }
                            }
                        }
                    )
                }
        ) {

            // рабочая сетка
            val grid = 50f

            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = Color(0xFFE5E7EB),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
                x += grid
            }

            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = Color(0xFFE5E7EB),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += grid
            }

            elements.forEach { element ->

                val pixelsPerMeter = 110f

                val start = Offset(
                    element.x,
                    element.y
                )

                val end = Offset(
                    element.x +
                            element.lengthMeters *
                            pixelsPerMeter,
                    element.y
                )

                val selected =
                    element.id == selectedId

                // верхний пояс
                drawLine(
                    color = if (selected)
                        Color(0xFF1565C0)
                    else
                        Color.DarkGray,
                    start = start,
                    end = end,
                    strokeWidth = if (selected) 12f else 8f,
                    cap = StrokeCap.Round
                )

                // нижний пояс
                drawLine(
                    color = if (selected)
                        Color(0xFF1565C0)
                    else
                        Color.DarkGray,
                    start = Offset(start.x, start.y + 45f),
                    end = Offset(end.x, end.y + 45f),
                    strokeWidth = if (selected) 12f else 8f,
                    cap = StrokeCap.Round
                )

                // вертикали
                drawLine(
                    color = Color.DarkGray,
                    start = start,
                    end = Offset(start.x, start.y + 45f),
                    strokeWidth = 5f
                )

                drawLine(
                    color = Color.DarkGray,
                    start = end,
                    end = Offset(end.x, end.y + 45f),
                    strokeWidth = 5f
                )

                // диагонали
                val sections =
                    (element.lengthMeters * 2)
                        .toInt()
                        .coerceAtLeast(1)

                val sectionWidth =
                    (end.x - start.x) / sections

                repeat(sections) { index ->

                    val x1 =
                        start.x + index * sectionWidth

                    val x2 =
                        x1 + sectionWidth

                    if (index % 2 == 0) {
                        drawLine(
                            color = Color.Gray,
                            start = Offset(x1, start.y),
                            end = Offset(x2, start.y + 45f),
                            strokeWidth = 4f
                        )
                    } else {
                        drawLine(
                            color = Color.Gray,
                            start = Offset(x1, start.y + 45f),
                            end = Offset(x2, start.y),
                            strokeWidth = 4f
                        )
                    }
                }

                if (selected) {
                    drawRect(
                        color = Color(0xFF1565C0),
                        topLeft = Offset(
                            start.x - 12f,
                            start.y - 12f
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            end.x - start.x + 24f,
                            69f
                        ),
                        style = Stroke(width = 3f)
                    )
                }
            }
        }
    }
}
