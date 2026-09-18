package com.trussload.calculator.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.trussload.calculator.assembly.*

@Composable
fun TrussAssemblyScreen() {

    var assembly by remember {
        mutableStateOf(TrussAssembly())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Конструктор фермы",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Секции",
            style = MaterialTheme.typography.titleLarge
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            StandardTrussElements.straightSections.forEach { template ->

                ElevatedCard(
                    modifier = Modifier
                        .width(120.dp)
                        .clickable {

                            val newElement =
                                template.copy(
                                    id = java.util.UUID
                                        .randomUUID()
                                        .toString()
                                )

                            assembly = assembly.copy(
                                elements =
                                    assembly.elements +
                                        newElement
                            )
                        }
                ) {

                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        Text(template.name)

                        Text(
                            text = "${template.length} м",
                            style =
                                MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Text(
            text = "Соединители",
            style = MaterialTheme.typography.titleLarge
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            StandardTrussElements.connectors.forEach { template ->

                OutlinedButton(
                    onClick = {

                        val newElement =
                            template.copy(
                                id = java.util.UUID
                                    .randomUUID()
                                    .toString()
                            )

                        assembly = assembly.copy(
                            elements =
                                assembly.elements +
                                    newElement
                        )
                    }
                ) {
                    Text(template.name)
                }
            }
        }

        Text(
            text = "Рабочая область",
            style = MaterialTheme.typography.titleLarge
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {

            AssemblyCanvas(
                assembly = assembly,
                modifier = Modifier.fillMaxSize()
            )
        }

        Text(
            text =
                "Прямая длина: " +
                    "%.2f м".format(
                        assembly.totalStraightLength
                    )
        )

        Text(
            text =
                "Элементов: ${assembly.elements.size}"
        )

        if (assembly.elements.isNotEmpty()) {

            Text(
                text = "Состав сборки",
                style = MaterialTheme.typography.titleLarge
            )

            assembly.elements.forEachIndexed {
                    index,
                    element ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text =
                                    "${index + 1}. ${element.name}"
                            )

                            if (
                                element.type ==
                                AssemblyElementType.STRAIGHT
                            ) {
                                Text(
                                    text =
                                        "${element.length} м",
                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodySmall
                                )
                            }
                        }

                        TextButton(
                            onClick = {

                                assembly =
                                    assembly.copy(
                                        elements =
                                            assembly.elements
                                                .filterNot {
                                                    it.id ==
                                                        element.id
                                                }
                                    )
                            }
                        ) {
                            Text("Удалить")
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    assembly = TrussAssembly()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Очистить сборку")
            }
        }
    }
}

@Composable
private fun AssemblyCanvas(
    assembly: TrussAssembly,
    modifier: Modifier = Modifier
) {

    Canvas(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant
            )
    ) {

        if (assembly.elements.isEmpty()) {
            return@Canvas
        }

        var cursorX = 40f
        var cursorY = size.height / 2f

        val usableWidth =
            (size.width - 80f).coerceAtLeast(1f)

        val totalLength =
            assembly.totalStraightLength
                .coerceAtLeast(1.0)

        val scale =
            (usableWidth / totalLength.toFloat())
                .coerceAtMost(130f)

        var directionX = 1f
        var directionY = 0f

        assembly.elements.forEach { element ->

            when (element.type) {

                AssemblyElementType.STRAIGHT -> {

                    val visualLength =
                        element.length.toFloat() *
                            scale

                    val endX =
                        cursorX +
                            visualLength *
                            directionX

                    val endY =
                        cursorY +
                            visualLength *
                            directionY

                    drawLine(
                        color = Color(0xFF333333),
                        start = Offset(
                            cursorX,
                            cursorY
                        ),
                        end = Offset(
                            endX,
                            endY
                        ),
                        strokeWidth = 14f,
                        cap = StrokeCap.Round
                    )

                    drawLine(
                        color = Color(0xFF777777),
                        start = Offset(
                            cursorX,
                            cursorY - 12f
                        ),
                        end = Offset(
                            endX,
                            endY - 12f
                        ),
                        strokeWidth = 4f
                    )

                    cursorX = endX
                    cursorY = endY
                }

                AssemblyElementType.CORNER -> {

                    drawCircle(
                        color = Color(0xFFEF6C00),
                        radius = 15f,
                        center = Offset(
                            cursorX,
                            cursorY
                        )
                    )

                    val oldX = directionX

                    directionX = -directionY
                    directionY = oldX
                }

                AssemblyElementType.T_JUNCTION -> {

                    drawCircle(
                        color = Color(0xFF1565C0),
                        radius = 17f,
                        center = Offset(
                            cursorX,
                            cursorY
                        )
                    )
                }

                AssemblyElementType.X_JUNCTION -> {

                    drawCircle(
                        color = Color(0xFF7B1FA2),
                        radius = 18f,
                        center = Offset(
                            cursorX,
                            cursorY
                        )
                    )
                }
            }
        }
    }
}
