package com.trussload.calculator.models

// ============================================================
// РЕЗУЛЬТАТ РАСЧЁТА УЗЛА
//
// displacementX / displacementY:
// перемещения узла в метрах.
//
// reactionXKn / reactionYKn:
// реакции опор в кН.
// ============================================================

data class StructuralNodeResult(

    val nodeId: String,

    val displacementX: Double = 0.0,

    val displacementY: Double = 0.0,

    val reactionXKn: Double = 0.0,

    val reactionYKn: Double = 0.0
)

// ============================================================
// СОСТОЯНИЕ СТЕРЖНЯ
// ============================================================

enum class StructuralMemberForceState {

    TENSION,

    COMPRESSION,

    ZERO
}

// ============================================================
// РЕЗУЛЬТАТ РАСЧЁТА СТЕРЖНЯ
//
// axialForceKn:
// + растяжение
// - сжатие
// 0 отсутствие продольного усилия
// ============================================================

data class StructuralMemberResult(

    val memberId: String,

    val axialForceKn: Double = 0.0,

    val state: StructuralMemberForceState =
        StructuralMemberForceState.ZERO
)

// ============================================================
// ПОЛНЫЙ РЕЗУЛЬТАТ РАСЧЁТА
// ============================================================

data class StructuralCalculationResult(

    val success: Boolean,

    val nodeResults:
        List<StructuralNodeResult> =
        emptyList(),

    val memberResults:
        List<StructuralMemberResult> =
        emptyList(),

    val errors:
        List<String> =
        emptyList()
) {

    // --------------------------------------------------------
    // РЕЗУЛЬТАТ УЗЛА
    // --------------------------------------------------------

    fun nodeResult(
        nodeId: String
    ): StructuralNodeResult? {

        return nodeResults.firstOrNull {
            it.nodeId == nodeId
        }
    }

    // --------------------------------------------------------
    // РЕЗУЛЬТАТ СТЕРЖНЯ
    // --------------------------------------------------------

    fun memberResult(
        memberId: String
    ): StructuralMemberResult? {

        return memberResults.firstOrNull {
            it.memberId == memberId
        }
    }

    // --------------------------------------------------------
    // МАКСИМАЛЬНОЕ УСИЛИЕ ПО МОДУЛЮ
    // --------------------------------------------------------

    val maximumAbsoluteAxialForceKn: Double
        get() =
            memberResults
                .maxOfOrNull {
                    kotlin.math.abs(
                        it.axialForceKn
                    )
                }
                ?: 0.0

    // --------------------------------------------------------
    // МАКСИМАЛЬНОЕ ПЕРЕМЕЩЕНИЕ ПО МОДУЛЮ
    // --------------------------------------------------------

    val maximumAbsoluteDisplacement: Double
        get() {

            var maximum =
                0.0

            nodeResults.forEach { result ->

                val displacement =
                    kotlin.math.hypot(
                        result.displacementX,
                        result.displacementY
                    )

                if (displacement > maximum) {
                    maximum = displacement
                }
            }

            return maximum
        }
}

// ============================================================
// ФАБРИКА НЕУДАЧНОГО РАСЧЁТА
// ============================================================

fun structuralCalculationFailure(
    errors: List<String>
): StructuralCalculationResult {

    return StructuralCalculationResult(
        success = false,
        errors = errors
    )
}

// ============================================================
// ФАБРИКА ОДИНОЧНОЙ ОШИБКИ
// ============================================================

fun structuralCalculationFailure(
    error: String
): StructuralCalculationResult {

    return StructuralCalculationResult(
        success = false,
        errors = listOf(error)
    )
}
