package com.trussload.calculator.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicText

data class DiagramPointLoad(
    val position: Double,
    val force: Double
)

@Composable
fun TrussDiagram(
    span: Double,
    leftSupport: Double = 0.0,
    rightSupport: Double = span,
    pointLoads: List<DiagramPointLoad> = emptyList(),
    distributedLoad: Double = 0.0,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(
                text = "Схема фермы",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (span <= 0.0) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("Введите длину фермы")
                }
                return@Column
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                val startX = 40f
                val endX = size.width - 40f

                val beamY = size.height * 0.55f

                val beamWidth = endX - startX

                fun positionToX(position: Double): Float {
                    val safePosition = position.coerceIn(0.0, span)

                    return startX +
                        (safePosition / span).toFloat() * beamWidth
                }

                // Основная линия фермы
                drawLine(
                    color = Color(0xFF333333),
                    start = Offset(startX, beamY),
                    end = Offset(endX, beamY),
                    strokeWidth = 8f,
                    cap = StrokeCap.Round
                )

                // Верхний пояс
                val topY = beamY - 45f

                drawLine(
                    color = Color(0xFF555555),
                    start = Offset(startX, topY),
                    end = Offset(endX, topY),
                    strokeWidth = 5f
                )

                // Вертикали и диагонали
                val sections = 10

                for (i in 0 until sections) {

                    val x1 =
                        startX + beamWidth * i / sections

                    val x2 =
                        startX + beamWidth * (i + 1) / sections

                    drawLine(
                        color = Color.Gray,
                        start = Offset(x1, beamY),
                        end = Offset(x1, topY),
                        strokeWidth = 2.5f
                    )

                    if (i % 2 == 0) {
                        drawLine(
                            color = Color.Gray,
                            start = Offset(x1, beamY),
                            end = Offset(x2, topY),
                            strokeWidth = 2.5f
                        )
                    } else {
                        drawLine(
                            color = Color.Gray,
                            start = Offset(x1, topY),
                            end = Offset(x2, beamY),
                            strokeWidth = 2.5f
                        )
                    }
                }

                drawLine(
                    color = Color.Gray,
                    start = Offset(endX, beamY),
                    end = Offset(endX, topY),
                    strokeWidth = 2.5f
                )

                // Левая опора
                drawSupport(
                    x = positionToX(leftSupport),
                    y = beamY
                )

                // Правая опора
                drawSupport(
                    x = positionToX(rightSupport),
                    y = beamY
                )

                // Сосредоточенные нагрузки
                pointLoads.forEach { load ->

                    if (load.position in 0.0..span) {

                        val x = positionToX(load.position)

                        drawArrow(
                            x = x,
                            top = 10f,
                            bottom = topY - 5f,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }

                // Распределённая нагрузка
                if (distributedLoad > 0.0) {

                    val arrows = 9

                    for (i in 0..arrows) {

                        val x =
                            startX +
                                beamWidth * i / arrows

                        drawArrow(
                            x = x,
                            top = 10f,
                            bottom = topY - 5f,
                            color = Color(0xFF1976D2)
                        )
                    }

                    drawLine(
                        color = Color(0xFF1976D2),
                        start = Offset(startX, 10f),
                        end = Offset(endX, 10f),
                        strokeWidth = 2f
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                BasicText(
                    text = "0 м",
                    style = TextStyle(
                        fontSize = 12.sp
                    )
                )

                BasicText(
                    text = "%.2f м".format(span),
                    style = TextStyle(
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSupport(
    x: Float,
    y: Float
) {
    val supportPath = Path().apply {

        moveTo(x, y + 5f)

        lineTo(
            x - 18f,
            y + 35f
        )

        lineTo(
            x + 18f,
            y + 35f
        )

        close()
    }

    drawPath(
        path = supportPath,
        color = Color(0xFF2E7D32),
        style = Stroke(
            width = 4f
        )
    )

    drawLine(
        color = Color(0xFF2E7D32),
        start = Offset(
            x - 25f,
            y + 40f
        ),
        end = Offset(
            x + 25f,
            y + 40f
        ),
        strokeWidth = 4f
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArrow(
    x: Float,
    top: Float,
    bottom: Float,
    color: Color
) {

    drawLine(
        color = color,
        start = Offset(x, top),
        end = Offset(x, bottom),
        strokeWidth = 3f
    )

    drawLine(
        color = color,
        start = Offset(x, bottom),
        end = Offset(
            x - 7f,
            bottom - 10f
        ),
        strokeWidth = 3f
    )

    drawLine(
        color = color,
        start = Offset(x, bottom),
        end = Offset(
            x + 7f,
            bottom - 10f
        ),
        strokeWidth = 3f
    )
}
