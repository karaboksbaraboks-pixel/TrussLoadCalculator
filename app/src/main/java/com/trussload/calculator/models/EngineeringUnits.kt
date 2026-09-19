package com.trussload.calculator.models

import java.util.Locale
import kotlin.math.abs

// ============================================================
// ЕДИНИЦЫ СИЛЫ / НАГРУЗКИ
//
// ВАЖНО:
// Внутри расчётного ядра все силы храним в кН.
//
// KG — пользовательское отображение нагрузки в килограммах
// (физически это эквивалент кгс).
//
// TON — пользовательское отображение нагрузки в тоннах
// (эквивалент тс).
// ============================================================

enum class ForceUnit(
    val symbol: String
) {

    KILONEWTON(
        symbol = "кН"
    ),

    KILOGRAM_FORCE(
        symbol = "кг"
    ),

    TON_FORCE(
        symbol = "т"
    )
}

// ============================================================
// ЕДИНИЦЫ РАСПРЕДЕЛЁННОЙ НАГРУЗКИ
// ============================================================

enum class DistributedLoadUnit(
    val symbol: String
) {

    KILONEWTON_PER_METER(
        symbol = "кН/м"
    ),

    KILOGRAM_FORCE_PER_METER(
        symbol = "кг/м"
    ),

    TON_FORCE_PER_METER(
        symbol = "т/м"
    )
}

// ============================================================
// КОНСТАНТЫ
// ============================================================

object EngineeringUnitConstants {

    // Стандартное ускорение свободного падения
    const val STANDARD_GRAVITY =
        9.80665

    // 1 кгс в кН
    const val KN_PER_KGF =
        STANDARD_GRAVITY / 1000.0

    // 1 тс в кН
    const val KN_PER_TON_FORCE =
        STANDARD_GRAVITY

    // 1 кН в кгс
    const val KGF_PER_KN =
        1000.0 / STANDARD_GRAVITY

    // 1 кН в тс
    const val TON_FORCE_PER_KN =
        1.0 / STANDARD_GRAVITY
}

// ============================================================
// СИЛА -> кН
// ============================================================

fun forceToKilonewtons(
    value: Double,
    unit: ForceUnit
): Double {

    return when (unit) {

        ForceUnit.KILONEWTON ->
            value

        ForceUnit.KILOGRAM_FORCE ->
            value *
                EngineeringUnitConstants
                    .KN_PER_KGF

        ForceUnit.TON_FORCE ->
            value *
                EngineeringUnitConstants
                    .KN_PER_TON_FORCE
    }
}

// ============================================================
// кН -> НУЖНАЯ ЕДИНИЦА
// ============================================================

fun kilonewtonsToForce(
    valueKn: Double,
    unit: ForceUnit
): Double {

    return when (unit) {

        ForceUnit.KILONEWTON ->
            valueKn

        ForceUnit.KILOGRAM_FORCE ->
            valueKn *
                EngineeringUnitConstants
                    .KGF_PER_KN

        ForceUnit.TON_FORCE ->
            valueKn *
                EngineeringUnitConstants
                    .TON_FORCE_PER_KN
    }
}

// ============================================================
// ПЕРЕСЧЁТ СИЛЫ ИЗ ОДНОЙ ЕДИНИЦЫ В ДРУГУЮ
// ============================================================

fun convertForce(
    value: Double,
    from: ForceUnit,
    to: ForceUnit
): Double {

    if (from == to) {
        return value
    }

    val valueKn =
        forceToKilonewtons(
            value = value,
            unit = from
        )

    return kilonewtonsToForce(
        valueKn = valueKn,
        unit = to
    )
}

// ============================================================
// РАСПРЕДЕЛЁННАЯ НАГРУЗКА -> кН/м
// ============================================================

fun distributedLoadToKnPerMeter(
    value: Double,
    unit: DistributedLoadUnit
): Double {

    return when (unit) {

        DistributedLoadUnit
            .KILONEWTON_PER_METER ->
            value

        DistributedLoadUnit
            .KILOGRAM_FORCE_PER_METER ->
            value *
                EngineeringUnitConstants
                    .KN_PER_KGF

        DistributedLoadUnit
            .TON_FORCE_PER_METER ->
            value *
                EngineeringUnitConstants
                    .KN_PER_TON_FORCE
    }
}

// ============================================================
// кН/м -> НУЖНАЯ ЕДИНИЦА
// ============================================================

fun knPerMeterToDistributedLoad(
    valueKnPerMeter: Double,
    unit: DistributedLoadUnit
): Double {

    return when (unit) {

        DistributedLoadUnit
            .KILONEWTON_PER_METER ->
            valueKnPerMeter

        DistributedLoadUnit
            .KILOGRAM_FORCE_PER_METER ->
            valueKnPerMeter *
                EngineeringUnitConstants
                    .KGF_PER_KN

        DistributedLoadUnit
            .TON_FORCE_PER_METER ->
            valueKnPerMeter *
                EngineeringUnitConstants
                    .TON_FORCE_PER_KN
    }
}

// ============================================================
// ПЕРЕСЧЁТ РАСПРЕДЕЛЁННОЙ НАГРУЗКИ
// ============================================================

fun convertDistributedLoad(
    value: Double,
    from: DistributedLoadUnit,
    to: DistributedLoadUnit
): Double {

    if (from == to) {
        return value
    }

    val valueKnPerMeter =
        distributedLoadToKnPerMeter(
            value = value,
            unit = from
        )

    return knPerMeterToDistributedLoad(
        valueKnPerMeter = valueKnPerMeter,
        unit = to
    )
}

// ============================================================
// СООТВЕТСТВУЮЩАЯ ЕДИНИЦА РАСПРЕДЕЛЁННОЙ НАГРУЗКИ
// ============================================================

fun ForceUnit.toDistributedLoadUnit():
    DistributedLoadUnit {

    return when (this) {

        ForceUnit.KILONEWTON ->
            DistributedLoadUnit
                .KILONEWTON_PER_METER

        ForceUnit.KILOGRAM_FORCE ->
            DistributedLoadUnit
                .KILOGRAM_FORCE_PER_METER

        ForceUnit.TON_FORCE ->
            DistributedLoadUnit
                .TON_FORCE_PER_METER
    }
}

// ============================================================
// НАСТРОЙКИ ЕДИНИЦ ПОЛЬЗОВАТЕЛЯ
// ============================================================

data class EngineeringUnitSettings(

    val forceUnit: ForceUnit =
        ForceUnit.KILOGRAM_FORCE
) {

    val distributedLoadUnit:
        DistributedLoadUnit
        get() =
            forceUnit
                .toDistributedLoadUnit()
}

// ============================================================
// ФОРМАТИРОВАНИЕ ЧИСЛА
// ============================================================

private fun formatEngineeringNumber(
    value: Double,
    decimals: Int
): String {

    if (!value.isFinite()) {
        return "—"
    }

    val safeDecimals =
        decimals.coerceIn(
            minimumValue = 0,
            maximumValue = 6
        )

    val threshold =
        0.5 *
            Math.pow(
                10.0,
                -safeDecimals.toDouble()
            )

    val normalizedValue =
        if (abs(value) < threshold) {
            0.0
        } else {
            value
        }

    return String.format(
        Locale.US,
        "%.${safeDecimals}f",
        normalizedValue
    )
        .trimEnd('0')
        .trimEnd('.')
}

// ============================================================
// ОТОБРАЖЕНИЕ СИЛЫ
// ============================================================

fun formatForce(
    valueKn: Double,
    unit: ForceUnit,
    decimals: Int = 2
): String {

    val converted =
        kilonewtonsToForce(
            valueKn = valueKn,
            unit = unit
        )

    return "${
        formatEngineeringNumber(
            value = converted,
            decimals = decimals
        )
    } ${unit.symbol}"
}

// ============================================================
// ОТОБРАЖЕНИЕ РАСПРЕДЕЛЁННОЙ НАГРУЗКИ
// ============================================================

fun formatDistributedLoad(
    valueKnPerMeter: Double,
    unit: DistributedLoadUnit,
    decimals: Int = 2
): String {

    val converted =
        knPerMeterToDistributedLoad(
            valueKnPerMeter =
                valueKnPerMeter,
            unit = unit
        )

    return "${
        formatEngineeringNumber(
            value = converted,
            decimals = decimals
        )
    } ${unit.symbol}"
}

// ============================================================
// БЫСТРЫЕ ПРОВЕРКИ ПЕРЕСЧЁТА
//
// Эти функции пока не вызываются приложением.
// Они нужны как простая контрольная точка при разработке.
// ============================================================

fun engineeringUnitConversionCheck():
    Boolean {

    val oneKnInKg =
        kilonewtonsToForce(
            valueKn = 1.0,
            unit =
                ForceUnit.KILOGRAM_FORCE
        )

    val thousandKgInKn =
        forceToKilonewtons(
            value = 1000.0,
            unit =
                ForceUnit.KILOGRAM_FORCE
        )

    val oneTonInKn =
        forceToKilonewtons(
            value = 1.0,
            unit =
                ForceUnit.TON_FORCE
        )

    return abs(
        oneKnInKg -
            101.9716212978
    ) < 0.0001 &&
        abs(
            thousandKgInKn -
                9.80665
        ) < 0.000001 &&
        abs(
            oneTonInKn -
                9.80665
        ) < 0.000001
    }
