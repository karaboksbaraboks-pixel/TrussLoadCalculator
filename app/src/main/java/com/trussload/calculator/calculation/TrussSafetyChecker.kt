package com.trussload.calculator.calculation

import com.trussload.calculator.models.LoadType
import com.trussload.calculator.models.TrussCheckInput
import com.trussload.calculator.models.TrussCheckResult

object TrussSafetyChecker {

    fun check(input: TrussCheckInput): TrussCheckResult {

        val truss = input.selectedTruss
            ?: return TrussCheckResult(
                utilizationPercent = null,
                isWithinLimit = null,
                allowableLoadKg = null,
                message = "Ферма не выбрана"
            )

        if (input.spanM <= 0.0) {
            return TrussCheckResult(
                utilizationPercent = null,
                isWithinLimit = false,
                allowableLoadKg = null,
                message = "Длина пролёта должна быть больше 0"
            )
        }

        if (input.loadKg < 0.0) {
            return TrussCheckResult(
                utilizationPercent = null,
                isWithinLimit = false,
                allowableLoadKg = null,
                message = "Нагрузка не может быть отрицательной"
            )
        }

        if (truss.maxSpanM != null && input.spanM > truss.maxSpanM) {
            return TrussCheckResult(
                utilizationPercent = null,
                isWithinLimit = false,
                allowableLoadKg = null,
                message = "Пролёт превышает допустимый для выбранной фермы"
            )
        }

        val allowableLoadKg = when (input.loadType) {

            LoadType.UDL -> {
                truss.maxUdlKgPerM?.times(input.spanM)
            }

            LoadType.POINT_CENTER -> {
                truss.maxCenterPointKg
            }

            LoadType.TWO_POINT -> {
                // Для этой схемы потребуется отдельное
                // значение из таблицы производителя.
                null
            }
        }

        if (allowableLoadKg == null) {
            return TrussCheckResult(
                utilizationPercent = null,
                isWithinLimit = null,
                allowableLoadKg = null,
                message = "Для этой модели и схемы нагрузки пока нет проверенных данных производителя"
            )
        }

        if (allowableLoadKg <= 0.0) {
            return TrussCheckResult(
                utilizationPercent = null,
                isWithinLimit = null,
                allowableLoadKg = allowableLoadKg,
                message = "Некорректное допустимое значение нагрузки"
            )
        }

        val utilization =
            (input.loadKg / allowableLoadKg) * 100.0

        val withinLimit =
            input.loadKg <= allowableLoadKg

        val message = when {
            utilization <= 80.0 ->
                "Нагрузка составляет ${format(utilization)}% от указанного предела"

            utilization <= 100.0 ->
                "Нагрузка составляет ${format(utilization)}% от указанного предела"

            else ->
                "Указанный предел превышен: ${format(utilization)}%"
        }

        return TrussCheckResult(
            utilizationPercent = utilization,
            isWithinLimit = withinLimit,
            allowableLoadKg = allowableLoadKg,
            message = message
        )
    }

    private fun format(value: Double): String {
        return String.format("%.1f", value)
    }
}
