package com.trussload.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                TrussCalculator()
            }
        }
    }
}

@Composable
fun TrussCalculator() {

    var span by remember { mutableStateOf("6") }
    var load by remember { mutableStateOf("600") }
    var result by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Truss Load Calculator",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = span,
                onValueChange = { span = it },
                label = {
                    Text("Длина пролёта, м")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = load,
                onValueChange = { load = it },
                label = {
                    Text("Общая нагрузка, кг")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {

                    val spanValue =
                        span.replace(",", ".").toDoubleOrNull()

                    val loadValue =
                        load.replace(",", ".").toDoubleOrNull()

                    result =
                        if (
                            spanValue != null &&
                            loadValue != null &&
                            spanValue > 0
                        ) {

                            val loadPerMeter =
                                loadValue / spanValue

                            "Нагрузка на метр: %.2f кг/м"
                                .format(loadPerMeter)

                        } else {

                            "Проверьте введённые данные"
                        }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {

                Text("РАССЧИТАТЬ")
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (result.isNotEmpty()) {

                Text(
                    text = result,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}
