package com.trussload.calculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

private data class V3PointLoad(
    val id: Int,
    val force: String,
    val position: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun V3Screen() {

    var span by remember { mutableStateOf("6") }
    var leftSupport by remember { mutableStateOf("0") }
    var rightSupport by remember { mutableStateOf("6") }

    var trussWeight by remember { mutableStateOf("0") }
    var distributedLoad by remember { mutableStateOf("0") }

    var pointLoads by remember {
        mutableStateOf(
            listOf(
                V3PointLoad(
                    id = 1,
                    force = "100",
                    position = "3"
                )
            )
        )
    }

    var nextLoadId by remember { mutableIntStateOf(2) }

    var calculated by remember { mutableStateOf(false) }

    var leftReaction by remember { mutableDoubleStateOf(0.0) }
    var rightReaction by remember { mutableDoubleStateOf(0.0) }
    var maxMoment by remember { mutableDoubleStateOf(0.0) }

    val spanValue = span.toDoubleOrNull() ?: 0.0
    val leftSupportValue = leftSupport.toDoubleOrNull() ?: 0.0
    val rightSupportValue = rightSupport.toDoubleOrNull() ?: spanValue
    val distributedValue = distributedLoad.toDoubleOrNull() ?: 0.0
    val ownWeightValue = trussWeight.toDoubleOrNull() ?: 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Truss Load Calculator",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "V3",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            SectionTitle("Ферма")

            OutlinedTextField(
                value = span,
                onValueChange = {
                    span = it
                    calculated = false
                },
                label = {
                    Text("Общая длина фермы, м")
                },
                keyboardOptions = numberKeyboard(),
                modifier = Modifier.fillMaxWidth()
            )

            SectionTitle("Опоры")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                OutlinedTextField(
                    value = leftSupport,
                    onValueChange = {
                        leftSupport = it
                        calculated = false
                    },
                    label = {
                        Text("Левая, м")
                    },
                    keyboardOptions = numberKeyboard(),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = rightSupport,
                    onValueChange = {
                        rightSupport = it
                        calculated = false
                    },
                    label = {
                        Text("Правая, м")
                    },
                    keyboardOptions = numberKeyboard(),
                    modifier = Modifier.weight(1f)
                )
            }

            SectionTitle("Схема")

            TrussDiagram(
                span = spanValue,
                leftSupport = leftSupportValue,
                rightSupport = rightSupportValue,
                distributedLoad = distributedValue + ownWeightValue,
                pointLoads = pointLoads.mapNotNull { load ->

                    val force = load.force.toDoubleOrNull()
                    val position = load.position.toDoubleOrNull()

                    if (force != null && position != null) {
                        DiagramPointLoad(
                            position = position,
                            force = force
                        )
                    } else {
                        null
                    }
                }
            )

            SectionTitle("Постоянные нагрузки")

            OutlinedTextField(
                value = trussWeight,
                onValueChange = {
                    trussWeight = it
                    calculated = false
                },
                label = {
                    Text("Собственный вес, кг/м")
                },
                keyboardOptions = numberKeyboard(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = distributedLoad,
                onValueChange = {
                    distributedLoad = it
                    calculated = false
                },
                label = {
                    Text("Распределённая нагрузка, кг/м")
                },
                keyboardOptions = numberKeyboard(),
                modifier = Modifier.fillMaxWidth()
            )

            SectionTitle("Точечные нагрузки")

            pointLoads.forEach { load ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        Text(
                            text = "Нагрузка ${load.id}",
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            OutlinedTextField(
                                value = load.force,
                                onValueChange = { newValue ->

                                    pointLoads = pointLoads.map {
                                        if (it.id == load.id) {
                                            it.copy(force = newValue)
                                        } else {
                                            it
                                        }
                                    }

                                    calculated = false
                                },
                                label = {
                                    Text("Вес, кг")
                                },
                                keyboardOptions = numberKeyboard(),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = load.position,
                                onValueChange = { newValue ->

                                    pointLoads = pointLoads.map {
                                        if (it.id == load.id) {
                                            it.copy(position = newValue)
                                        } else {
                                            it
                                        }
                                    }

                                    calculated = false
                                },
                                label = {
                                    Text("Позиция, м")
                                },
                                keyboardOptions = numberKeyboard(),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (pointLoads.size > 1) {
                            TextButton(
                                onClick = {
                                    pointLoads =
                                        pointLoads.filterNot {
                                            it.id == load.id
                                        }

                                    calculated = false
                                }
                            ) {
                                Text("Удалить нагрузку")
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = {

                    pointLoads =
                        pointLoads +
                            V3PointLoad(
                                id = nextLoadId,
                                force = "",
                                position = ""
                            )

                    nextLoadId++
                    calculated = false
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Добавить точечную нагрузку")
            }

            Button(
                onClick = {

                    val beamLength =
                        rightSupportValue - leftSupportValue

                    if (
                        spanValue > 0.0 &&
                        beamLength > 0.0 &&
                        leftSupportValue >= 0.0 &&
                        rightSupportValue <= spanValue
                    ) {

                        val q =
                            distributedValue +
                                ownWeightValue

                        val validLoads =
                            pointLoads.mapNotNull { load ->

                                val force =
                                    load.force.toDoubleOrNull()

                                val position =
                                    load.position.toDoubleOrNull()

                                if (
                                    force != null &&
                                    position != null &&
                                    position >= leftSupportValue &&
                                    position <= rightSupportValue
                                ) {
                                    force to position
                                } else {
                                    null
                                }
                            }

                        val distributedTotal =
                            q * beamLength

                        val totalPointLoad =
                            validLoads.sumOf { it.first }

                        val totalLoad =
                            distributedTotal +
                                totalPointLoad

                        var momentAroundLeft =
                            distributedTotal *
                                beamLength / 2.0

                        validLoads.forEach { load ->

                            momentAroundLeft +=
                                load.first *
                                    (load.second - leftSupportValue)
                        }

                        rightReaction =
                            momentAroundLeft / beamLength

                        leftReaction =
                            totalLoad - rightReaction

                        /*
                         * На данном этапе Mmax считаем
                         * численным проходом по балке.
                         * Позже это заменит
                         * AdvancedBeamCalculator.
                         */

                        var calculatedMaxMoment = 0.0

                        val steps = 300

                        for (i in 0..steps) {

                            val localX =
                                beamLength *
                                    i.toDouble() /
                                    steps.toDouble()

                            val globalX =
                                leftSupportValue + localX

                            var moment =
                                leftReaction * localX

                            moment -=
                                q *
                                    localX *
                                    localX /
                                    2.0

                            validLoads.forEach { load ->

                                if (load.second <= globalX) {

                                    moment -=
                                        load.first *
                                            (
                                                globalX -
                                                    load.second
                                                )
                                }
                            }

                            if (
                                kotlin.math.abs(moment) >
                                kotlin.math.abs(calculatedMaxMoment)
                            ) {
                                calculatedMaxMoment = moment
                            }
                        }

                        maxMoment = calculatedMaxMoment
                        calculated = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("РАССЧИТАТЬ")
            }

            if (calculated) {

                SectionTitle("Результаты")

                ResultCard(
                    title = "Реакция левой опоры",
                    value = "${formatNumber(leftReaction)} кг"
                )

                ResultCard(
                    title = "Реакция правой опоры",
                    value = "${formatNumber(rightReaction)} кг"
                )

                ResultCard(
                    title = "Максимальный момент",
                    value = "${formatNumber(maxMoment)} кг·м"
                )

                val totalPoint =
                    pointLoads.sumOf {
                        it.force.toDoubleOrNull() ?: 0.0
                    }

                val totalDistributed =
                    (distributedValue + ownWeightValue) *
                        (
                            rightSupportValue -
                                leftSupportValue
                            ).coerceAtLeast(0.0)

                ResultCard(
                    title = "Полная нагрузка",
                    value = "${
                        formatNumber(
                            totalPoint + totalDistributed
                        )
                    } кг"
                )
            }

            HorizontalDivider()

            Text(
                text =
                    "V3 • Расчётная схема находится в разработке. " +
                    "Перед использованием для реальных конструкций " +
                    "результаты необходимо проверять по документации производителя.",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(
                modifier = Modifier.height(30.dp)
            )
        }
    }
}

@Composable
private fun SectionTitle(
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ResultCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Text(
                text = title
            )

            Text(
                text = value,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun numberKeyboard(): KeyboardOptions {
    return KeyboardOptions(
        keyboardType = KeyboardType.Decimal
    )
}

private fun formatNumber(
    value: Double
): String {
    return String.format(
        java.util.Locale.US,
        "%.2f",
        value
    )
}
