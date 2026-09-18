package com.trussload.calculator.models

enum class LoadType(
    val title: String
) {
    UDL("Равномерно распределённая"),
    POINT_CENTER("Точечная по центру"),
    TWO_POINT("Две симметричные точечные")
}

data class TrussCatalogItem(
    val manufacturer: String,
    val series: String,
    val model: String,

    val heightMm: Int,
    val widthMm: Int,

    val weightKgPerMeter: Double,

    val maxSpanM: Double? = null,
    val maxUdlKgPerM: Double? = null,
    val maxCenterPointKg: Double? = null,

    val material: String = "EN AW-6082 T6",
    val description: String = "",
    val source: String = ""
)

data class TrussCheckInput(
    val spanM: Double,
    val loadKg: Double,
    val loadType: LoadType,
    val selectedTruss: TrussCatalogItem? = null
)

data class TrussCheckResult(
    val utilizationPercent: Double?,
    val isWithinLimit: Boolean?,
    val allowableLoadKg: Double?,
    val message: String
)
