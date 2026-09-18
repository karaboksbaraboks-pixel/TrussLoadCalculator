package com.trussload.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.trussload.calculator.calculation.BeamCalculator
import com.trussload.calculator.models.DistributedLoad
import com.trussload.calculator.models.PointLoad
import com.trussload.calculator.models.TrussInput
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                TrussCalculatorApp()
            }
        }
    }
}

@Composable
fun TrussCalculatorApp() {

    var span by remember { mutableStateOf("12") }
    var leftSupport by remember { mutableStateOf("0") }
    var rightSupport by remember { mutableStateOf("12") }
    var trussWeight by remember { mutableStateOf("0") }

    val pointLoads = remember {
        mutableStateListOf<PointLoad>()
    }

    val distributedLoads = remember {
        mutableStateListOf<DistributedLoad>()
    }

    var pointWeight by remember { mutableStateOf("100") }
    var pointPosition by remember { mutableStateOf("6") }

    var distributedValue by remember { mutableStateOf("50") }
    var distributedStart by remember { mutableStateOf("0") }
    var distributedEnd by remember { mutableStateOf("12") }

    var resultText by remember {
        mutableStateOf("Добавьте нагрузки и нажмите «РАССЧИТАТЬ»")
    }

    var nextPointId by remember { mutableIntStateOf(1) }
    var nextDistributedId by remember { mutableIntStateOf(1) }

    fun number(text: String): Double? {
        return text
            .replace(",", ".")
            .toDoubleOrNull()
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp)
        ) {

            Text(
                text = "TRUSS LOAD CALCULATOR",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Расчёт нагрузок на ферму",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(22.dp))

            SectionTitle("1. ГЕОМЕТРИЯ")

            NumberField(
                value = span,
                label = "Общая длина фермы, м",
                onChange = { span = it }
            )

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                NumberField(
                    value = leftSupport,
                    label = "Опора A, м",
                    onChange = { leftSupport = it },
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(10.dp))

                NumberField(
                    value = rightSupport,
                    label = "Опора B, м",
                    onChange = { rightSupport = it },
                    modifier = Modifier.weight(1f)
                )
            }

            NumberField(
                value = trussWeight,
                label = "Собственный вес фермы, кг",
                onChange = { trussWeight = it }
            )

            Spacer(Modifier.height(18.dp))

            SectionTitle("2. ТОЧЕЧНЫЕ НАГРУЗКИ")

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                NumberField(
                    value = pointWeight,
                    label = "Вес, кг",
                    onChange = { pointWeight = it },
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(10.dp))

                NumberField(
                    value = pointPosition,
                    label = "Позиция, м",
                    onChange = { pointPosition = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {

                    val weight = number(pointWeight)
                    val position = number(pointPosition)
                    val spanValue = number(span)

                    if (
                        weight != null &&
                        position != null &&
                        spanValue != null &&
                        weight >= 0 &&
                        position in 0.0..spanValue
                    ) {

                        pointLoads.add(
                            PointLoad(
                                id = nextPointId,
                                positionM = position,
                                loadKg = weight,
                                name = "P$nextPointId"
                            )
                        )

                        nextPointId++
                    }
                }
            ) {
                Text("+ ДОБАВИТЬ ТОЧЕЧНУЮ НАГРУЗКУ")
            }

            pointLoads.forEach { load ->

                LoadCard(
                    title = "${load.name}: ${fmt(load.loadKg)} кг",
                    subtitle = "x = ${fmt(load.positionM)} м",
                    onDelete = {
                        pointLoads.remove(load)
                    }
                )
            }

            Spacer(Modifier.height(18.dp))

            SectionTitle("3. РАСПРЕДЕЛЁННЫЕ НАГРУЗКИ")

            NumberField(
                value = distributedValue,
                label = "Нагрузка, кг/м",
                onChange = { distributedValue = it }
            )

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                NumberField(
                    value = distributedStart,
                    label = "Начало, м",
                    onChange = { distributedStart = it },
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(10.dp))

                NumberField(
                    value = distributedEnd,
                    label = "Конец, м",
                    onChange = { distributedEnd = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {

                    val q = number(distributedValue)
                    val start = number(distributedStart)
                    val end = number(distributedEnd)
                    val spanValue = number(span)

                    if (
                        q != null &&
                        start != null &&
                        end != null &&
                        spanValue != null &&
                        q >= 0 &&
                        start >= 0 &&
                        end > start &&
                        end <= spanValue
                    ) {

                        distributedLoads.add(
                            DistributedLoad(
                                id = nextDistributedId,
                                startM = start,
                                endM = end,
                                loadKgPerM = q,
                                name = "Q$nextDistributedId"
                            )
                        )

                        nextDistributedId++
                    }
                }
            ) {
                Text("+ ДОБАВИТЬ РАСПРЕДЕЛЁННУЮ НАГРУЗКУ")
            }

            distributedLoads.forEach { load ->

                LoadCard(
                    title = "${load.name}: ${fmt(load.loadKgPerM)} кг/м",
                    subtitle =
                        "${fmt(load.startM)} — ${fmt(load.endM)} м",
                    onDelete = {
                        distributedLoads.remove(load)
                    }
                )
            }

            Spacer(Modifier.height(22.dp))

            SectionTitle("4. СХЕМА")

            TrussDiagram(
                span = number(span) ?: 0.0,
                leftSupport = number(leftSupport) ?: 0.0,
                rightSupport = number(rightSupport) ?: 0.0,
                pointLoads = pointLoads
            )

            Spacer(Modifier.height(22.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                onClick = {

                    try {

                        val spanValue =
                            number(span)
                                ?: error("Введите длину")

                        val left =
                            number(leftSupport)
                                ?: error("Введите опору A")

                        val right =
                            number(rightSupport)
                                ?: error("Введите опору B")

                        val weight =
                            number(trussWeight)
                                ?: 0.0

                        val result =
                            BeamCalculator.calculate(
                                TrussInput(
                                    spanM = spanValue,
                                    leftSupportM = left,
                                    rightSupportM = right,
                                    trussWeightKg = weight,
                                    pointLoads = pointLoads.toList(),
                                    distributedLoads =
                                        distributedLoads.toList()
                                )
                            )

                        resultText = """
Суммарная нагрузка:
${fmt(result.totalLoadKg)} кг

Реакция опоры A:
${fmt(result.leftReactionKg)} кг

Реакция опоры B:
${fmt(result.rightReactionKg)} кг

Максимальный изгибающий момент:
${fmt(result.maxMomentKgM)} кг·м

Положение Mmax:
${fmt(result.maxMomentPositionM)} м

Максимальная поперечная сила:
${fmt(result.maxShearKg)} кг
                        """.trimIndent()

                    } catch (e: Exception) {

                        resultText =
                            "Ошибка исходных данных:\n" +
                                (e.message ?: "проверьте значения")
                    }
                }
            ) {

                Text(
                    "РАССЧИТАТЬ",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(20.dp))

            SectionTitle("5. РЕЗУЛЬТАТ")

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = resultText,
                    modifier = Modifier.padding(18.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(Modifier.height(30.dp))

            Text(
                text =
                    "Расчётная модель предназначена для анализа нагрузок " +
                    "и реакций. Допустимая грузоподъёмность конкретной " +
                    "фермы должна проверяться по данным производителя.",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun SectionTitle(text: String) {

    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleMedium
    )

    Spacer(Modifier.height(10.dp))
}

@Composable
fun NumberField(
    value: String,
    label: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth()
) {

    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = {
            Text(label)
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        ),
        singleLine = true,
        modifier = modifier.padding(bottom = 10.dp)
    )
}

@Composable
fun LoadCard(
    title: String,
    subtitle: String,
    onDelete: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold
                )

                Text(subtitle)
            }

            TextButton(
                onClick = onDelete
            ) {
                Text("УДАЛИТЬ")
            }
        }
    }
}

@Composable
fun TrussDiagram(
    span: Double,
    leftSupport: Double,
    rightSupport: Double,
    pointLoads: List<PointLoad>
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            if (span <= 0.0) return@Canvas

            val left = 20f
            val right = size.width - 20f
            val beamY = size.height * 0.58f

            fun xPosition(meters: Double): Float {

                val fraction =
                    (meters / span)
                        .coerceIn(0.0, 1.0)

                return left +
                    ((right - left) * fraction).toFloat()
            }

            // Ферма
            drawLine(
                color = Color.DarkGray,
                start = Offset(left, beamY),
                end = Offset(right, beamY),
                strokeWidth = 10f,
                cap = StrokeCap.Round
            )

            // Опоры
            listOf(leftSupport, rightSupport).forEach { support ->

                val x = xPosition(support)

                drawLine(
                    color = Color.Blue,
                    start = Offset(x, beamY),
                    end = Offset(x - 18f, beamY + 38f),
                    strokeWidth = 6f
                )

                drawLine(
                    color = Color.Blue,
                    start = Offset(x, beamY),
                    end = Offset(x + 18f, beamY + 38f),
                    strokeWidth = 6f
                )
            }

            // Точечные нагрузки
            pointLoads.forEach { load ->

                val x = xPosition(load.positionM)

                drawLine(
                    color = Color.Red,
                    start = Offset(x, beamY - 75f),
                    end = Offset(x, beamY - 8f),
                    strokeWidth = 6f
                )

                drawLine(
                    color = Color.Red,
                    start = Offset(x, beamY - 8f),
                    end = Offset(x - 10f, beamY - 23f),
                    strokeWidth = 5f
                )

                drawLine(
                    color = Color.Red,
                    start = Offset(x, beamY - 8f),
                    end = Offset(x + 10f, beamY - 23f),
                    strokeWidth = 5f
                )
            }
        }
    }
}

fun fmt(value: Double): String {

    return String.format(
        Locale.US,
        "%.2f",
        value
    )
}
