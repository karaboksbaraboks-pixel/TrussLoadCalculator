package com.trussload.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.trussload.calculator.assembly.*

@Composable
fun AssemblyWorkspaceScreen() {

    var project by remember {
        mutableStateOf(
            AssemblyProject()
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
        // БИБЛИОТЕКА
        // -----------------------------

        TrussComponentLibrary(
            onAddElement = { item ->

                val offsetIndex =
                    project.elements.size % 6

                val startPosition =
                    Offset(
                        x = 180f +
                            offsetIndex * 25f,
                        y = 220f +
                            offsetIndex * 25f
                    )

                project =
                    addLibraryElement(
                        project = project,
                        item = item,
                        position = startPosition
                    )
            },
            modifier = Modifier.fillMaxWidth()
        )

        // -----------------------------
        // ПАНЕЛЬ ИНСТРУМЕНТОВ
        // -----------------------------

        AssemblyToolbar(
            project = project,
            onProjectChange = {
                project = it
            },
            modifier = Modifier.fillMaxWidth()
        )

        // -----------------------------
        // ИНФОРМАЦИЯ О СБОРКЕ
        // -----------------------------

        ProjectInformation(
            project = project
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

                AssemblyCanvas(
                    project = project,
                    onProjectChange = {
                        project = it
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (project.elements.isEmpty()) {

                    Column(
                        modifier = Modifier
                            .align(
                                androidx.compose.ui.Alignment.Center
                            )
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
                }
            }
        }

        // -----------------------------
        // ВЫБРАННЫЙ ЭЛЕМЕНТ
        // -----------------------------

        SelectedElementInformation(
            project = project
        )
    }
}

@Composable
private fun ProjectInformation(
    project: AssemblyProject
) {

    val straightSections =
        project.elements.count {
            it.type ==
                AssemblyElementType.STRAIGHT_TRUSS
        }

    val connectors =
        project.elements.count {
            it.type ==
                AssemblyElementType.CORNER_90 ||
            it.type ==
                AssemblyElementType.CORNER_135 ||
            it.type ==
                AssemblyElementType.T_JUNCTION ||
            it.type ==
                AssemblyElementType.CROSS
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
                            project
                                .totalStraightLengthMeters
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
                        project.elements
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
private fun SelectedElementInformation(
    project: AssemblyProject
) {

    val selected =
        project.elements.firstOrNull {
            it.selected
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 3.dp
    ) {

        Column(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 8.dp
            )
        ) {

            if (selected == null) {

                Text(
                    text =
                        "Нажмите на элемент, " +
                        "чтобы выбрать его",
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall
                )

            } else {

                Text(
                    text =
                        "Выбрано: ${selected.name}",
                    style =
                        MaterialTheme
                            .typography
                            .titleSmall
                )

                if (
                    selected.type ==
                    AssemblyElementType.STRAIGHT_TRUSS
                ) {

                    Text(
                        text =
                            "Длина: " +
                            "${selected.lengthMeters} м   " +
                            "Поворот: " +
                            "${selected.rotationDegrees.toInt()}°",
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )
                }
            }
        }
    }
}
