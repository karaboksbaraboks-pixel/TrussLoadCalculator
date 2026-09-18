package com.trussload.calculator.models

enum class LoadType(
    val title: String
) {
    UDL("Равномерно распределённая"),
    POINT_CENTER("Точечная по центру"),
    TWO_POINT("Две симметричные точечные")
}

data class TrussModel(
    val manufacturer: String,
    val series: String,
    val model: String,

    // Геометрия
    val heightMm: Int,
    val widthMm: Int,

    // Масса одного погонного метра
    val weightKgPerMeter: Double,

    // Допустимые значения.
    // Позже заполняем по официальным таблицам производителя.
    val maxSpanM: Double? = null,
    val maxUdlKgPerM: Double? = null,
    val maxCenterPointKg: Double? = null,

    // Дополнительная информация
    val material: String = "EN AW-6082 T6",
    val description: String = "",
    val source: String = ""
)

data class CalculationInput(
    val spanM: Double,
    val loadKg: Double,
    val loadType: LoadType,
    val selectedTruss: TrussModel? = null
)

data class CalculationResult(
    val spanM: Double,

    // Введённая пользователем нагрузка
    val totalLoadKg: Double,

    // Эквивалентная нагрузка
    val loadKgPerM: Double,

    // Реакция на каждой опоре
    val reactionKg: Double,

    // Максимальный изгибающий момент
    val maxMomentKgM: Double,

    // Результат проверки выбранной фермы
    val utilizationPercent: Double? = null,
    val isWithinLimit: Boolean? = null,

    val message: String = ""
)
