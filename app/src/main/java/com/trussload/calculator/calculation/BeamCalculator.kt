package com.trussload.calculator.calculation

import com.trussload.calculator.models.CalculationResult
import com.trussload.calculator.models.TrussInput
import kotlin.math.abs
import kotlin.math.max

object BeamCalculator {

    fun calculate(input: TrussInput): CalculationResult {

        require(input.spanM > 0.0) {
            "Длина фермы должна быть больше 0"
        }

        require(input.leftSupportM >= 0.0)
        require(input.rightSupportM <= input.spanM)
        require(input.rightSupportM > input.leftSupportM)

        val supportDistance =
            input.rightSupportM - input.leftSupportM

        var totalLoad = 0.0
        var totalMomentAboutLeftSupport = 0.0

        // Собственный вес фермы считаем равномерно
        if (input.trussWeightKg > 0.0) {

            totalLoad += input.trussWeightKg

            val center = input.spanM / 2.0

            totalMomentAboutLeftSupport +=
                input.trussWeightKg *
                    (center - input.leftSupportM)
        }

        // Точечные нагрузки
        input.pointLoads.forEach { load ->

            require(load.positionM in 0.0..input.spanM)
            require(load.loadKg >= 0.0)

            totalLoad += load.loadKg

            totalMomentAboutLeftSupport +=
                load.loadKg *
                    (load.positionM - input.leftSupportM)
        }

        // Распределённые нагрузки
        input.distributedLoads.forEach { load ->

            require(load.startM >= 0.0)
            require(load.endM <= input.spanM)
            require(load.endM > load.startM)
            require(load.loadKgPerM >= 0.0)

            val length =
                load.endM - load.startM

            val equivalentLoad =
                load.loadKgPerM * length

            val center =
                (load.startM + load.endM) / 2.0

            totalLoad += equivalentLoad

            totalMomentAboutLeftSupport +=
                equivalentLoad *
                    (center - input.leftSupportM)
        }

        val rightReaction =
            totalMomentAboutLeftSupport /
                supportDistance

        val leftReaction =
            totalLoad - rightReaction

        var maxMoment = 0.0
        var maxMomentPosition = 0.0
        var maxShear = max(
            abs(leftReaction),
            abs(rightReaction)
        )

        // Численно строим эпюры с достаточно мелким шагом
        val steps = 1000

        for (i in 0..steps) {

            val x =
                input.spanM * i.toDouble() /
                    steps.toDouble()

            var shear = 0.0
            var moment = 0.0

            if (x >= input.leftSupportM) {

                shear += leftReaction

                moment +=
                    leftReaction *
                        (x - input.leftSupportM)
            }

            if (x >= input.rightSupportM) {

                shear += rightReaction

                moment +=
                    rightReaction *
                        (x - input.rightSupportM)
            }

            // Собственный вес
            if (input.trussWeightKg > 0.0) {

                val q =
                    input.trussWeightKg /
                        input.spanM

                val loadedLength =
                    x.coerceIn(
                        0.0,
                        input.spanM
                    )

                shear -=
                    q * loadedLength

                moment -=
                    q *
                        loadedLength *
                        loadedLength / 2.0
            }

            // Точечные нагрузки
            input.pointLoads.forEach { load ->

                if (x >= load.positionM) {

                    shear -= load.loadKg

                    moment -=
                        load.loadKg *
                            (x - load.positionM)
                }
            }

            // Распределённые нагрузки
            input.distributedLoads.forEach { load ->

                if (x > load.startM) {

                    val loadedEnd =
                        minOf(
                            x,
                            load.endM
                        )

                    val loadedLength =
                        (loadedEnd - load.startM)
                            .coerceAtLeast(0.0)

                    val force =
                        load.loadKgPerM *
                            loadedLength

                    val forcePosition =
                        load.startM +
                            loadedLength / 2.0

                    shear -= force

                    moment -=
                        force *
                            (x - forcePosition)
                }
            }

            if (abs(moment) > abs(maxMoment)) {

                maxMoment = moment
                maxMomentPosition = x
            }

            maxShear =
                max(
                    maxShear,
                    abs(shear)
                )
        }

        return CalculationResult(
            totalLoadKg = totalLoad,
            leftReactionKg = leftReaction,
            rightReactionKg = rightReaction,
            maxMomentKgM = abs(maxMoment),
            maxMomentPositionM = maxMomentPosition,
            maxShearKg = maxShear
        )
    }
}
