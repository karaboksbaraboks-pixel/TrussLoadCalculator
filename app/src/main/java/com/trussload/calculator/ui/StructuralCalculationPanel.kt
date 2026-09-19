package com.trussload.calculator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trussload.calculator.assembly.TrussAssembly
import com.trussload.calculator.calculation.AssemblyStructuralConverter
import com.trussload.calculator.models.StructuralModel

// ============================================================
// ПАНЕЛЬ РАСЧЁТНОЙ МОДЕЛИ
//
// Назначение:
//
// TrussAssembly
//      ↓
// AssemblyStructuralConverter
//      ↓
// StructuralModel
//
// На этом этапе панель:
//
// - создаёт расчётную модель;
// - показывает количество узлов;
// - показывает количество стержней;
// - показывает количество нагрузок;
// - показывает количество закреплений;
// - выполняет базовую проверку модели.
//
// Назначение опор и нагрузок будем подключать следующим шагом.
// ============================================================

@Composable
fun StructuralCalculationPanel(
    assembly: TrussAssembly,
    modifier: Modifier = Modifier
) {

    val structuralModel =
        AssemblyStructuralConverter.convert(
            assembly = assembly
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
            // СОСТОЯНИЕ МОДЕЛИ
            // =================================================

            StructuralModelState(
                model = structuralModel
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            // =================================================
            // БУДУЩИЕ ИНСТРУМЕНТЫ
            // =================================================

            StructuralCalculationTools(
                model = structuralModel
            )
        }
    }
}

// ============================================================
// СТАТИСТИКА РАСЧЁТНОЙ МОДЕЛИ
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
//
// Здесь специально не вызываем пока общий
// validateForCalculation(), чтобы этот UI-файл
// не зависел от extension-функций опор/нагрузок.
//
// Полную проверку подключим вместе с запуском solver.
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
// ИНСТРУМЕНТЫ РАСЧЁТА
//
// Кнопки пока являются визуальной подготовкой.
//
// На следующем этапе:
//
// ОПОРЫ
//     выбор узла
//     PINNED / X_ONLY / Y_ONLY
//
// НАГРУЗКА
//     значение
//     кг / кН
//     направление
//
// РАСЧЁТ
//     Truss2DSolver
// ============================================================

@Composable
private fun StructuralCalculationTools(
    model: StructuralModel
) {

    val geometryAvailable =
        model.nodes.isNotEmpty() &&
            model.members.isNotEmpty()

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
                     * Подключим выбор
                     * и назначение опор
                     * следующим шагом.
                     */
                },
                enabled =
                    geometryAvailable
            ) {

                Text(
                    text = "Опора"
                )
            }

            OutlinedButton(
                onClick = {
                    /*
                     * Подключим создание
                     * сосредоточенной нагрузки.
                     */
                },
                enabled =
                    geometryAvailable
            ) {

                Text(
                    text = "Нагрузка"
                )
            }

            Button(
                onClick = {
                    /*
                     * Здесь будет:
                     *
                     * Truss2DSolver.solve(...)
                     */
                },
                enabled = false
            ) {

                Text(
                    text = "Рассчитать"
                )
            }
        }

        if (geometryAvailable) {

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Text(
                text =
                    "Следующий этап: назначение опор и нагрузок на расчётные узлы.",
                style =
                    MaterialTheme
                        .typography
                        .bodySmall
            )
        }
    }
}
