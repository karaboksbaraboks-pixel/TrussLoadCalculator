package com.trussload.calculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class AppScreen {
    CALCULATOR,
    ASSEMBLY
}

@Composable
fun AppNavigation(
    calculatorContent: @Composable () -> Unit
) {
    var currentScreen by remember {
        mutableStateOf(AppScreen.CALCULATOR)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Surface(
            tonalElevation = 3.dp
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    ),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                FilterChip(
                    selected =
                        currentScreen ==
                            AppScreen.CALCULATOR,
                    onClick = {
                        currentScreen =
                            AppScreen.CALCULATOR
                    },
                    label = {
                        Text("Расчёт")
                    }
                )

                FilterChip(
                    selected =
                        currentScreen ==
                            AppScreen.ASSEMBLY,
                    onClick = {
                        currentScreen =
                            AppScreen.ASSEMBLY
                    },
                    label = {
                        Text("Конструктор")
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {

            when (currentScreen) {

                AppScreen.CALCULATOR -> {
                    calculatorContent()
                }

                AppScreen.ASSEMBLY -> {
                    TrussAssemblyScreen()
                }
            }
        }
    }
}
