package com.trussload.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.trussload.calculator.assembly.AssemblyElementType
import com.trussload.calculator.assembly.TrussAssembly
import com.trussload.calculator.assembly.TrussComponentLibrary
import com.trussload.calculator.assembly.addLibraryElement

@Composable
fun AssemblyWorkspaceScreen() {

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

        // -----------------------------
        // ЗАГОЛОВОК
        // -----------------------------

        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 14.dp,
                    vertical = 10.dp
                )
            ) {

                Text(
                    text = "Конструктор фермы",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "V3.2 • визуальная сборка",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // -----------------------------
        // БИБЛИОТЕКА ЭЛЕМЕНТОВ
        // -----------------------------

        TrussComponentLibrary(
            onAddElement = { item ->

                val offsetIndex =
                    assembly.elements.size % 6

                val startX =
                    180f + offsetIndex * 25f

                val startY =
                    220f + offsetIndex * 25f

                assembly =
                    addLibraryElement(
                        assembly = assembly,
                        item = item,
                        x = startX,
                        y = startY
                    )
            },
            modifier = Modifier.fillMaxWidth()
        )

        // -----------------------------
        // ИНФОРМАЦИЯ О СБОРКЕ
        // -----------------------------

        AssemblyInformation(
            assembly = assembly
        )

        // -----------------------------
        // РАБОЧАЯ ОБЛАСТЬ
        // -----------------------------

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 3.dp
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {

                if (assembly.elements.isEmpty()) {

                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(30.dp)
                    ) {

                        Text(
                            text = "Рабочая область",
                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text =
                                "Добавьте секцию фермы " +
                                "из библиотеки сверху",
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium
                        )

                    }

                } else {

                    AssemblyElementsPreview(
                        assembly = assembly,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AssemblyInformation(
    assembly: TrussAssembly
) {

    val straightSections =
        assembly.elements.count {
            it.type ==
                AssemblyElementType.STRAIGHT
        }

    val connectors =
        assembly.elements.count {
            it.type ==
                AssemblyElementType.CORNER ||
            it.type ==
                AssemblyElementType.T_JUNCTION ||
            it.type ==
                AssemblyElementType.X_JUNCTION
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 8.dp
                ),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Column {

                Text(
                    text = "Длина"
                )

                Text(
                    text =
                        "%.2f м".format(
                            assembly.totalStraightLength
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium
                )
            }

            Column {

                Text(
                    text = "Секции"
                )

                Text(
                    text =
                        straightSections.toString(),
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium
                )
            }

            Column {

                Text(
                    text = "Соединители"
                )

                Text(
                    text =
                        connectors.toString(),
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium
                )
            }

            Column {

                Text(
                    text = "Всего"
                )

                Text(
                    text =
                        assembly.elements
                            .size
                            .toString(),
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium
                )
            }
        }
    }
}

@Composable
private fun AssemblyElementsPreview(
    assembly: TrussAssembly,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier,
        verticalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = "Элементы сборки",
            style =
                MaterialTheme
                    .typography
                    .titleMedium
        )

        assembly.elements.forEachIndexed {
                index,
                element ->

            Surface(
                modifier =
                    Modifier.fillMaxWidth(),
                tonalElevation = 1.dp,
                shape =
                    MaterialTheme
                        .shapes
                        .small
            ) {

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Text(
                        text =
                            "${index + 1}. ${element.name}"
                    )

                    when (element.type) {

                        AssemblyElementType.STRAIGHT -> {

                            Text(
                                text =
                                    "%.2f м".format(
                                        element.length
                                    )
                            )
                        }

                        AssemblyElementType.CORNER -> {

                            Text(
                                text = "Угол"
                            )
                        }

                        AssemblyElementType.T_JUNCTION -> {

                            Text(
                                text = "T"
                            )
                        }

                        AssemblyElementType.X_JUNCTION -> {

                            Text(
                                text = "X"
                            )
                        }
                    }
                }
            }
        }
    }
}
