package com.trussload.calculator.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

data class AssemblyElement(
    val id: Int,
    val lengthMeters: Float,
    val x: Float,
    val y: Float,
    val rotation: Float = 0f
)

@Composable
fun TrussAssemblyScreen() {

    var elements by remember {
        mutableStateOf(listOf<AssemblyElement>())
    }

    var selectedId by remember {
        mutableStateOf<Int?>(null)
    }

    var nextId by remember {
        mutableIntStateOf(1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F4))
    ) {

        Text(
            text = "Конструктор фермы",
            fontSize = 22.sp,
            modifier = Modifier.padding(12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            listOf(
                0.5f,
                1f,
                2f,
                3f,
                4f
            ).forEach { length ->

                Button(
                    onClick = {

                        elements = elements + AssemblyElement(
                            id = nextId,
                            lengthMeters = length,
                            x = 200f,
                            y = 300f + nextId * 20f
                        )

                        selectedId = nextId
                        nextId++
                    }
                ) {
                    Text("${length} м")
                }
            }
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Button(
                enabled = selectedId != null,
                onClick = {

                    val id = selectedId ?: return@Button

                    elements = elements.map {

                        if (it.id == id) {
                            it.copy(
                                rotation = it.rotation + 15f
                            )
                        } else {
                            it
                        }
                    }
                }
            ) {
                Text("↻ 15°")
            }

            Button(
                enabled = selectedId != null,
                onClick = {

                    val id = selectedId ?: return@Button

                    elements = elements.map {

                        if (it.id == id) {
                            it.copy(
                                rotation = it.rotation + 90f
                            )
                        } else {
                            it
                        }
                    }
                }
            ) {
                Text("↻ 90°")
            }

            Button(
                enabled = selectedId != null,
                onClick = {

                    val id = selectedId ?: return@Button

                    elements =
                        elements.filterNot {
                            it.id == id
                        }

                    selectedId = null
                }
            ) {
                Text("Удалить")
            }

            OutlinedButton(
                enabled = elements.isNotEmpty(),
                onClick = {
                    elements = emptyList()
                    selectedId = null
                }
            ) {
                Text("Очистить")
            }
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text =
                "Секций: ${elements.size}   " +
                "Общая длина: " +
                "%.1f".format(
                    elements.sumOf {
                        it.lengthMeters.toDouble()
                    }
                ) +
                " м",
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 4.dp
            )
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White)
                .pointerInput(elements, selectedId) {

                    detectDragGestures(

                        onDragStart = { position ->

                            selectedId =
                                findElementAt(
                                    position = position,
                                    elements = elements
                                )
                        },

                        onDrag = { change, dragAmount ->

                            change.consume()

                            val id =
                                selectedId
                                    ?: return@detectDragGestures

                            elements = elements.map {

                                if (it.id == id) {

                                    it.copy(
                                        x = it.x + dragAmount.x,
                                        y = it.y + dragAmount.y
                                    )

                                } else {
                                    it
                                }
                            }
                        }
                    )
                }
        ) {

            // Сетка

            val grid = 50f

            var gx = 0f

            while (gx < size.width) {

                drawLine(
                    color = Color(0xFFE8E8E8),
                    start = Offset(gx, 0f),
                    end = Offset(gx, size.height),
                    strokeWidth = 1f
                )

                gx += grid
            }

            var gy = 0f

            while (gy < size.height) {

                drawLine(
                    color = Color(0xFFE8E8E8),
                    start = Offset(0f, gy),
                    end = Offset(size.width, gy),
                    strokeWidth = 1f
                )

                gy += grid
            }

            // Элементы фермы

            elements.forEach { element ->

                val selected =
                    element.id == selectedId

                val radians =
                    Math.toRadians(
                        element.rotation.toDouble()
                    )

                val pixelLength =
                    element.lengthMeters * 120f

                val dx =
                    (cos(radians) * pixelLength / 2f)
                        .toFloat()

                val dy =
                    (sin(radians) * pixelLength / 2f)
                        .toFloat()

                val start =
                    Offset(
                        element.x - dx,
                        element.y - dy
                    )

                val end =
                    Offset(
                        element.x + dx,
                        element.y + dy
                    )

                val color =
                    if (selected) {
                        Color(0xFFFF9800)
                    } else {
                        Color(0xFF1565C0)
                    }

                // Основная секция

                drawLine(
                    color = color,
                    start = start,
                    end = end,
                    strokeWidth = 18f,
                    cap = StrokeCap.Round
                )

                // Центральная линия

                drawLine(
                    color = Color.White,
                    start = start,
                    end = end,
                    strokeWidth = 2f
                )

                // Узлы соединения

                drawCircle(
                    color = Color.Red,
                    radius = 8f,
                    center = start
                )

                drawCircle(
                    color = Color.Red,
                    radius = 8f,
                    center = end
                )

                if (selected) {

                    drawCircle(
                        color = Color(0x55000000),
                        radius = 25f,
                        center = Offset(
                            element.x,
                            element.y
                        )
                    )
                }
            }
        }
    }
}

private fun findElementAt(
    position: Offset,
    elements: List<AssemblyElement>
): Int? {

    var closestId: Int? = null
    var closestDistance = Float.MAX_VALUE

    elements.forEach { element ->

        val dx =
            position.x - element.x

        val dy =
            position.y - element.y

        val distance =
            dx * dx + dy * dy

        val selectionRadius =
            element.lengthMeters * 70f + 50f

        if (
            distance <
            selectionRadius * selectionRadius &&
            distance < closestDistance
        ) {

            closestDistance = distance
            closestId = element.id
        }
    }

    return closestId
}
