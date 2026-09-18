package com.trussload.calculator.models

data class DiagramPoint(
    val positionM: Double,
    val shearKg: Double,
    val momentKgM: Double
)

data class AdvancedCalculationResult(
    val totalAppliedLoadKg: Double,

    val leftReactionKg: Double,
    val rightReactionKg: Double,

    val maxPositiveMomentKgM: Double,
    val maxPositiveMomentPositionM: Double,

    val maxNegativeMomentKgM: Double,
    val maxNegativeMomentPositionM: Double,

    val maxAbsoluteMomentKgM: Double,
    val maxAbsoluteMomentPositionM: Double,

    val maxAbsoluteShearKg: Double,
    val maxAbsoluteShearPositionM: Double,

    val verticalBalanceErrorKg: Double,

    val diagram: List<DiagramPoint>
)
