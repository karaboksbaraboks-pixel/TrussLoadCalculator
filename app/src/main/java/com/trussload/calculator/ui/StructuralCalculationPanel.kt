package com.trussload.calculator.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trussload.calculator.assembly.TrussAssembly
import com.trussload.calculator.calculation.AssemblyStructuralConverter
import com.trussload.calculator.calculation.SupportType
import com.trussload.calculator.calculation.setSupport
import com.trussload.calculator.calculation.supportType
import com.trussload.calculator.models.StructuralModel
import com.trussload.calculator.models.StructuralNode

// ============================================================
// ПАНЕЛЬ РАСЧЁТНОЙ МОДЕЛИ
//
// ЭТАП:
//
// - преобразование визуальной сборки в StructuralModel;
// - выбор расчётного узла;
// - назначение опоры;
// - снятие опоры;
// - отображение состояния выбранного узла.
//
// На следующем этапе выбор узла будет выполняться
// непосредственно касанием по рабочему полю.
// ============================================================

@Composable
fun StructuralCalculationPanel(
    assembly: TrussAssembly,
    modifier: Modifier = Modifier
) {

    // ========================================================
    // РАСЧЁТНАЯ МОДЕЛЬ
    // ========================================================

    var structuralModel by remember {
        mutableStateOf(
            AssemblyStructuralConverter.convert(
                assembly = assembly
            )
        )
    }

    // ========================================================
    // ВЫБРАННЫЙ УЗЕЛ
    // ========================================================

    var selectedNodeIndex by remember {
        mutableIntStateOf(0)
    }

    // ========================================================
    // ЕСЛИ ВИЗУАЛЬНАЯ СБОРКА ИЗМЕНИЛАСЬ
    //
    // Перестраиваем StructuralModel.
    //
    // На данном этапе опоры относятся к текущей расчётной
    // геометрии. Позже вынесем расчётное состояние выше,
    // чтобы оно полностью синхронизировалось с рабочим полем.
    // ========================================================

    LaunchedEffect(
        assembly.elements,
        assembly.connections
    ) {

        structuralModel =
            AssemblyStructuralConverter.convert(
                assembly = assembly
            )

        if (
            structuralModel.nodes.isEmpty()
        ) {

            selectedNodeIndex = 0

        } else if (
            selectedNodeIndex >
            structuralModel.nodes.lastIndex
        ) {

            selectedNodeIndex =
                structuralModel.nodes.lastIndex
        }
    }

    val selectedNode =
        structuralModel
            .nodes
            .getOrNull(
                selectedNodeIndex
            )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 4.dp
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

            // =================================================
            // ЗАГОЛОВОК
            // =================================================

            Text(
                text = "Расчётная модель",
                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            // =================================================
            // СТАТИСТИКА
            // =================================================

            StructuralModelStatistics(
                model = structuralModel
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            // =================================================
            // СОСТОЯНИЕ ГЕОМЕТРИИ
            // =================================================

            StructuralModelState(
                model = structuralModel
            )

            // =================================================
            // ВЫБОР УЗЛА
            // =================================================

            if (
                structuralModel
                    .nodes
                    .isNotEmpty()
            ) {

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                NodeSelector(
                    model = structuralModel,
                    selectedNodeIndex =
                        selectedNodeIndex,
                    onPrevious = {

                        if (
                            structuralModel
                                .nodes
                                .isNotEmpty()
                        ) {

                            selectedNodeIndex =
                                if (
                                    selectedNodeIndex <= 0
                                ) {

                                    structuralModel
                                        .nodes
                                        .lastIndex

                                } else {

                                    selectedNodeIndex - 1
                                }
                        }
                    },
                    onNext = {

                        if (
                            structuralModel
                                .nodes
                                .isNotEmpty()
                        ) {

                            selectedNodeIndex =
                                if (
                                    selectedNodeIndex >=
                                    structuralModel
                                        .nodes
                                        .lastIndex
                                ) {

                                    0

                                } else {

                                    selectedNodeIndex + 1
                                }
                        }
                    }
                )

                // =============================================
                // ДАННЫЕ ВЫБРАННОГО УЗЛА
                // =============================================

                if (
                    selectedNode != null
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    SelectedNodeInformation(
                        node = selectedNode,
                        index = selectedNodeIndex
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    // =========================================
                    // ОПОРЫ
                    // =========================================

                    SupportControls(
                        selectedNode = selectedNode,
                        onSupportSelected = {
                                supportType ->

                            structuralModel =
                                structuralModel
                                    .setSupport(
                                        nodeId =
                                            selectedNode.id,
                                        supportType =
                                            supportType
                                    )
                        }
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            // =================================================
            // РАСЧЁТ
            // =================================================

            CalculationControls(
                model = structuralModel
            )
        }
    }
}

// ============================================================
// СТАТИСТИКА
// ============================================================

@Composable
private fun StructuralModelStatistics(
    model: StructuralModel
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .horizontalScroll(
                    rememberScrollState()
                ),
        horizontalArrangement =
            Arrangement.spacedBy(18.dp)
    ) {

        StructuralStatisticValue(
            title = "Узлы",
            value =
                model
                    .nodes
                    .size
                    .toString()
        )

        StructuralStatisticValue(
            title = "Стержни",
            value =
                model
                    .members
                    .size
                    .toString()
        )

        StructuralStatisticValue(
            title = "Нагрузки",
            value =
                model
                    .loads
                    .size
                    .toString()
        )

        StructuralStatisticValue(
            title = "Опоры",
            value =
                model
                    .nodes
                    .count { node ->

                        node.supportX ||
                            node.supportY
                    }
                    .toString()
        )
    }
}

// ============================================================
// ОДНО ЗНАЧЕНИЕ СТАТИСТИКИ
// ============================================================

@Composable
private fun StructuralStatisticValue(
    title: String,
    value: String
) {

    Column {

        Text(
            text = title,
            style =
                MaterialTheme
                    .typography
                    .labelSmall
        )

        Text(
            text = value,
            style =
                MaterialTheme
                    .typography
                    .titleMedium
        )
    }
}

// ============================================================
// СОСТОЯНИЕ МОДЕЛИ
// ============================================================

@Composable
private fun StructuralModelState(
    model: StructuralModel
) {

    val message =
        when {

            model.nodes.isEmpty() -> {

                "Добавьте элементы фермы."
            }

            model.members.isEmpty() -> {

                "Расчётные стержни пока не сформированы."
            }

            else -> {

                "Расчётная геометрия сформирована."
            }
        }

    Text(
        text = message,
        style =
            MaterialTheme
                .typography
                .bodyMedium
    )
}

// ============================================================
// ВЫБОР УЗЛА
// ============================================================

@Composable
private fun NodeSelector(
    model: StructuralModel,
    selectedNodeIndex: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {

    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text(
            text = "Выбор узла",
            style =
                MaterialTheme
                    .typography
                    .titleSmall
        )

        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(
                        rememberScrollState()
                    ),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            OutlinedButton(
                onClick = onPrevious
            ) {

                Text(
                    text = "← Предыдущий"
                )
            }

            Text(
                text =
                    "Узел ${selectedNodeIndex + 1} / ${model.nodes.size}",
                modifier =
                    Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 12.dp
                    ),
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )

            OutlinedButton(
                onClick = onNext
            ) {

                Text(
                    text = "Следующий →"
                )
            }
        }
    }
}

// ============================================================
// ИНФОРМАЦИЯ О ВЫБРАННОМ УЗЛЕ
// ============================================================

@Composable
private fun SelectedNodeInformation(
    node: StructuralNode,
    index: Int
) {

    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text(
            text =
                "Узел ${index + 1}",
            style =
                MaterialTheme
                    .typography
                    .titleSmall
        )

        Spacer(
            modifier =
                Modifier.height(4.dp)
        )

        Text(
            text =
                "X: ${formatCoordinate(node.x)} м    Y: ${formatCoordinate(node.y)} м",
            style =
                MaterialTheme
                    .typography
                    .bodySmall
        )

        Text(
            text =
                "Опора: ${supportTitle(node.supportType)}",
            style =
                MaterialTheme
                    .typography
                    .bodySmall
        )
    }
}

// ============================================================
// УПРАВЛЕНИЕ ОПОРАМИ
// ============================================================

@Composable
private fun SupportControls(
    selectedNode: StructuralNode,
    onSupportSelected:
        (SupportType) -> Unit
) {

    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text(
            text = "Опора узла",
            style =
                MaterialTheme
                    .typography
                    .titleSmall
        )

        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(
                        rememberScrollState()
                    ),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            SupportButton(
                title = "Без опоры",
                selected =
                    selectedNode.supportType ==
                        SupportType.FREE,
                onClick = {

                    onSupportSelected(
                        SupportType.FREE
                    )
                }
            )

            SupportButton(
                title = "X",
                selected =
                    selectedNode.supportType ==
                        SupportType.X_ONLY,
                onClick = {

                    onSupportSelected(
                        SupportType.X_ONLY
                    )
                }
            )

            SupportButton(
                title = "Y",
                selected =
                    selectedNode.supportType ==
                        SupportType.Y_ONLY,
                onClick = {

                    onSupportSelected(
                        SupportType.Y_ONLY
                    )
                }
            )

            SupportButton(
                title = "Шарнир",
                selected =
                    selectedNode.supportType ==
                        SupportType.PINNED,
                onClick = {

                    onSupportSelected(
                        SupportType.PINNED
                    )
                }
            )
        }
    }
}

// ============================================================
// КНОПКА ТИПА ОПОРЫ
// ============================================================

@Composable
private fun SupportButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    if (selected) {

        Button(
            onClick = onClick
        ) {

            Text(
                text = title
            )
        }

    } else {

        OutlinedButton(
            onClick = onClick
        ) {

            Text(
                text = title
            )
        }
    }
}

// ============================================================
// ПАНЕЛЬ РАСЧЁТА
// ============================================================

@Composable
private fun CalculationControls(
    model: StructuralModel
) {

    val geometryAvailable =
        model.nodes.isNotEmpty() &&
            model.members.isNotEmpty()

    val hasSupport =
        model.nodes.any { node ->

            node.supportX ||
                node.supportY
        }

    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text(
            text = "Расчёт",
            style =
                MaterialTheme
                    .typography
                    .titleSmall
        )

        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(
                        rememberScrollState()
                    ),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            OutlinedButton(
                onClick = {
                    /*
                     * Добавление нагрузки
                     * подключим следующим этапом.
                     */
                },
                enabled =
                    geometryAvailable
            ) {

                Text(
                    text = "Добавить нагрузку"
                )
            }

            Button(
                onClick = {
                    /*
                     * Здесь будет запуск
                     * Truss2DSolver.
                     */
                },
                enabled = false
            ) {

                Text(
                    text = "Рассчитать"
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        val statusText =
            when {

                !geometryAvailable -> {

                    "Сначала создайте расчётную геометрию."
                }

                !hasSupport -> {

                    "Назначьте опоры."
                }

                model.loads.isEmpty() -> {

                    "Опоры назначены. Следующий этап — нагрузки."
                }

                else -> {

                    "Модель подготовлена к проверке."
                }
            }

        Text(
            text = statusText,
            style =
                MaterialTheme
                    .typography
                    .bodySmall
        )
    }
}

// ============================================================
// НАЗВАНИЕ ОПОРЫ
// ============================================================

private fun supportTitle(
    supportType: SupportType
): String {

    return when (
        supportType
    ) {

        SupportType.FREE ->
            "нет"

        SupportType.X_ONLY ->
            "закрепление X"

        SupportType.Y_ONLY ->
            "закрепление Y"

        SupportType.PINNED ->
            "шарнирная"
    }
}

// ============================================================
// ФОРМАТ КООРДИНАТ
// ============================================================

private fun formatCoordinate(
    value: Double
): String {

    return String.format(
        "%.3f",
        value
    )
}
