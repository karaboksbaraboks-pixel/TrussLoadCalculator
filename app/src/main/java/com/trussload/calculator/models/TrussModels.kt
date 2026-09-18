package com.trussload.calculator.models

data class PointLoad(
    val id: Int,
    val positionM: Double,
    val loadKg: Double,
    val name: String = "Точечная нагрузка"
)

data class DistributedLoad(
    val id: Int,
    val startM: Double,
    val endM: Double,
    val loadKgPerM: Double,
    val name: String = "Распределённая нагрузка"
)

data class Support(
    val positionM: Double,
    val name: String
)

data class TrussInput(
    val spanM: Double,
    val leftSupportM: Double = 0.0,
    val rightSupportM: Double,
    val trussWeightKg: Double = 0.0,
    val pointLoads: List<PointLoad> = emptyList(),
    val distributedLoads: List<DistributedLoad> = emptyList()
)

data class CalculationResult(
    val totalLoadKg: Double,
    val leftReactionKg: Double,
    val rightReactionKg: Double,
    val maxMomentKgM: Double,
    val maxMomentPositionM: Double,
    val maxShearKg: Double
)
