package com.trussload.calculator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
        mutableStateOf(
            AppScreen.CALCULATOR
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        // ====================================================
        // ВЕРХНЯЯ НАВИГАЦИЯ
        // ====================================================

        Surface(
            modifier = Modifier.fillMaxWidth(),
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

                // ============================================
                // РАСЧЁТ
                // ============================================

                FilterChip(
                    selected =
                        currentScreen ==
                            AppScreen.CALCULATOR,

                    onClick = {
                        currentScreen =
                            AppScreen.CALCULATOR
                    },

                    label = {
                        Text(
                            text = "Расчёт"
                        )
                    }
                )

                // ============================================
                // КОНСТРУКТОР
                // ============================================

                FilterChip(
                    selected =
                        currentScreen ==
                            AppScreen.ASSEMBLY,

                    onClick = {
                        currentScreen =
                            AppScreen.ASSEMBLY
                    },

                    label = {
                        Text(
                            text = "Конструктор"
                        )
                    }
                )
            }
        }

        // ====================================================
        // СОДЕРЖИМОЕ ЭКРАНА
        // ====================================================

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
