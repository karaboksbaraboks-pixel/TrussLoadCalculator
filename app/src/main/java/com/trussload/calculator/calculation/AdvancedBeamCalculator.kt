package com.trussload.calculator.calculation

import com.trussload.calculator.models.AdvancedCalculationResult
import com.trussload.calculator.models.DiagramPoint
import com.trussload.calculator.models.TrussInput
import kotlin.math.abs

object AdvancedBeamCalculator {

    fun calculate(
        input: TrussInput,
        steps: Int = 1000
    ): AdvancedCalculationResult {

        require(input.spanM > 0.0) {
            "Длина фермы должна быть больше 0"
        }

        require(input.leftSupportM >= 0.0) {
            "Опора A находится за пределами фермы"
        }

        require(input.rightSupportM <= input.spanM) {
            "Опора B находится за пределами фермы"
        }

        require(input.rightSupportM > input.leftSupportM) {
            "Опора B должна находиться правее опоры A"
        }

        require(input.trussWeightKg >= 0.0) {
            "Собственный вес не может быть отрицательным"
        }

        require(steps >= 100)

        input.pointLoads.forEach {
            require(it.positionM in 0.0..input.spanM) {
                "Точечная нагрузка находится за пределами фермы"
            }

            require(it.loadKg >= 0.0) {
                "Точечная нагрузка не может быть отрицательной"
            }
        }

        input.distributedLoads.forEach {
            require(it.startM >= 0.0)
            require(it.endM <= input.spanM)
            require(it.endM > it.startM)
            require(it.loadKgPerM >= 0.0)
        }

        var totalLoad = input.trussWeightKg
        var momentAboutLeftSupport = 0.0

        // Собственный вес фермы
        if (input.trussWeightKg > 0.0) {

            momentAboutLeftSupport +=
                input.trussWeightKg *
                    (
                        input.spanM / 2.0 -
                            input.leftSupportM
                        )
        }

        // Точечные нагрузки
        input.pointLoads.forEach { load ->

            totalLoad += load.loadKg

            momentAboutLeftSupport +=
                load.loadKg *
                    (
                        load.positionM -
                            input.leftSupportM
                        )
        }

        // Распределённые нагрузки
        input.distributedLoads.forEach { load ->

            val length =
                load.endM - load.startM

            val resultant =
                load.loadKgPerM * length

            val center =
                (load.startM + load.endM) / 2.0

            totalLoad += resultant

            momentAboutLeftSupport +=
                resultant *
                    (
                        center -
                            input.leftSupportM
                        )
        }

        val supportDistance =
            input.rightSupportM -
                input.leftSupportM

        val rightReaction =
            momentAboutLeftSupport /
                supportDistance

        val leftReaction =
            totalLoad - rightReaction

        val points =
            mutableListOf<DiagramPoint>()

        var maxPositiveMoment =
            Double.NEGATIVE_INFINITY

        var maxPositiveMomentX = 0.0

        var maxNegativeMoment =
            Double.POSITIVE_INFINITY

        var maxNegativeMomentX = 0.0

        var maxAbsoluteMoment = 0.0
        var maxAbsoluteMomentX = 0.0

        var maxAbsoluteShear = 0.0
        var maxAbsoluteShearX = 0.0

        val selfWeightPerMeter =
            if (input.spanM > 0.0) {
                input.trussWeightKg /
                    input.spanM
            } else {
                0.0
            }

        for (i in 0..steps) {

            val x =
                input.spanM *
                    i.toDouble() /
                    steps.toDouble()

            var shear = 0.0
            var moment = 0.0

            // Реакция A
            if (x >= input.leftSupportM) {

                shear += leftReaction

                moment +=
                    leftReaction *
                        (x - input.leftSupportM)
            }

            // Реакция B
            if (x >= input.rightSupportM) {

                shear += rightReaction

                moment +=
                    rightReaction *
                        (x - input.rightSupportM)
            }

            // Собственный вес
            if (selfWeightPerMeter > 0.0) {

                val loadedLength =
                    x.coerceIn(
                        0.0,
                        input.spanM
                    )

                val force =
                    selfWeightPerMeter *
                        loadedLength

                shear -= force

                moment -=
                    force *
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
                        (
                            loadedEnd -
                                load.startM
                            )
                            .coerceAtLeast(0.0)

                    val force =
                        load.loadKgPerM *
                            loadedLength

                    if (force > 0.0) {

                        val forcePosition =
                            load.startM +
                                loadedLength / 2.0

                        shear -= force

                        moment -=
                            force *
                                (
                                    x -
                                        forcePosition
                                    )
                    }
                }
            }

            points +=
                DiagramPoint(
                    positionM = x,
                    shearKg = shear,
                    momentKgM = moment
                )

            if (moment > maxPositiveMoment) {
                maxPositiveMoment = moment
                maxPositiveMomentX = x
            }

            if (moment < maxNegativeMoment) {
                maxNegativeMoment = moment
                maxNegativeMomentX = x
            }

            if (abs(moment) > maxAbsoluteMoment) {
                maxAbsoluteMoment = abs(moment)
                maxAbsoluteMomentX = x
            }

            if (abs(shear) > maxAbsoluteShear) {
                maxAbsoluteShear = abs(shear)
                maxAbsoluteShearX = x
            }
        }

        if (
            maxPositiveMoment ==
            Double.NEGATIVE_INFINITY
        ) {
            maxPositiveMoment = 0.0
        }

        if (
            maxNegativeMoment ==
            Double.POSITIVE_INFINITY
        ) {
            maxNegativeMoment = 0.0
        }

        val balanceError =
            leftReaction +
                rightReaction -
                totalLoad

        return AdvancedCalculationResult(
            totalAppliedLoadKg =
                totalLoad,

            leftReactionKg =
                leftReaction,

            rightReactionKg =
                rightReaction,

            maxPositiveMomentKgM =
                maxPositiveMoment,

            maxPositiveMomentPositionM =
                maxPositiveMomentX,

            maxNegativeMomentKgM =
                maxNegativeMoment,

            maxNegativeMomentPositionM =
                maxNegativeMomentX,

            maxAbsoluteMomentKgM =
                maxAbsoluteMoment,

            maxAbsoluteMomentPositionM =
                maxAbsoluteMomentX,

            maxAbsoluteShearKg =
                maxAbsoluteShear,

            maxAbsoluteShearPositionM =
                maxAbsoluteShearX,

            verticalBalanceErrorKg =
                balanceError,

            diagram =
                points
        )
    }
}
