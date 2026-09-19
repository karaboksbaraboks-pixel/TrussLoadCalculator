package com.trussload.calculator.calculation

import com.trussload.calculator.models.StructuralCalculationResult
import com.trussload.calculator.models.StructuralMember
import com.trussload.calculator.models.StructuralMemberForceState
import com.trussload.calculator.models.StructuralMemberResult
import com.trussload.calculator.models.StructuralModel
import com.trussload.calculator.models.StructuralNode
import com.trussload.calculator.models.StructuralNodeResult
import com.trussload.calculator.models.structuralCalculationFailure
import kotlin.math.abs
import kotlin.math.hypot

// ============================================================
// 2D SOLVER ДЛЯ ШАРНИРНО-СТЕРЖНЕВОЙ СИСТЕМЫ
//
// Расчёт:
// - 2 степени свободы на узел: X, Y
// - стержни работают только на растяжение / сжатие
// - нагрузки хранятся в кН
// - координаты хранятся в метрах
//
// ВАЖНО:
//
// Для расчёта перемещений каждому стержню необходимы:
// area            — площадь сечения, м²
// elasticModulus  — модуль упругости, кН/м²
//
// Если эти параметры отсутствуют, solver вернёт ошибку.
// ============================================================

object Truss2DSolver {

    private const val GEOMETRY_EPSILON =
        1e-9

    private const val MATRIX_EPSILON =
        1e-12

    private const val FORCE_EPSILON =
        1e-8

    // --------------------------------------------------------
    // ОСНОВНОЙ РАСЧЁТ
    // --------------------------------------------------------

    fun solve(
        model: StructuralModel
    ): StructuralCalculationResult {

        val validationErrors =
            validateModel(
                model = model
            )

        if (validationErrors.isNotEmpty()) {

            return structuralCalculationFailure(
                errors = validationErrors
            )
        }

        val nodes =
            model.nodes

        val members =
            model.members

        val nodeIndexById =
            nodes
                .mapIndexed { index, node ->
                    node.id to index
                }
                .toMap()

        val degreeCount =
            nodes.size * 2

        val stiffnessMatrix =
            Array(degreeCount) {
                DoubleArray(degreeCount)
            }

        val loadVector =
            DoubleArray(degreeCount)

        // ====================================================
        // ГЛОБАЛЬНАЯ МАТРИЦА ЖЁСТКОСТИ
        // ====================================================

        members.forEach { member ->

            val startNode =
                model.findNode(
                    member.startNodeId
                )
                    ?: return structuralCalculationFailure(
                        "Не найден начальный узел стержня ${member.id}."
                    )

            val endNode =
                model.findNode(
                    member.endNodeId
                )
                    ?: return structuralCalculationFailure(
                        "Не найден конечный узел стержня ${member.id}."
                    )

            val startIndex =
                nodeIndexById[
                    startNode.id
                ]
                    ?: return structuralCalculationFailure(
                        "Не найден индекс узла ${startNode.id}."
                    )

            val endIndex =
                nodeIndexById[
                    endNode.id
                ]
                    ?: return structuralCalculationFailure(
                        "Не найден индекс узла ${endNode.id}."
                    )

            val localMatrix =
                createMemberGlobalStiffness(
                    member = member,
                    startNode = startNode,
                    endNode = endNode
                )
                    ?: return structuralCalculationFailure(
                        "Невозможно сформировать матрицу жёсткости стержня ${member.id}."
                    )

            val dof =
                intArrayOf(
                    startIndex * 2,
                    startIndex * 2 + 1,
                    endIndex * 2,
                    endIndex * 2 + 1
                )

            for (row in 0 until 4) {

                for (column in 0 until 4) {

                    stiffnessMatrix[
                        dof[row]
                    ][
                        dof[column]
                    ] +=
                        localMatrix[row][column]
                }
            }
        }

        // ====================================================
        // ВЕКТОР ВНЕШНИХ НАГРУЗОК
        // ====================================================

        model.loads.forEach { load ->

            val nodeIndex =
                nodeIndexById[
                    load.nodeId
                ]
                    ?: return structuralCalculationFailure(
                        "Нагрузка ${load.id} привязана к неизвестному узлу ${load.nodeId}."
                    )

            loadVector[
                nodeIndex * 2
            ] +=
                load.forceXKn

            loadVector[
                nodeIndex * 2 + 1
            ] +=
                load.forceYKn
        }

        // Сохраняем исходные K и F.
        // Они понадобятся для вычисления реакций.

        val originalStiffness =
            copyMatrix(
                stiffnessMatrix
            )

        val originalLoadVector =
            loadVector.copyOf()

        // ====================================================
        // ЗАКРЕПЛЕНИЯ
        // ====================================================

        nodes.forEachIndexed {
                nodeIndex,
                node ->

            if (node.supportX) {

                applyConstraint(
                    matrix = stiffnessMatrix,
                    vector = loadVector,
                    degreeIndex =
                        nodeIndex * 2
                )
            }

            if (node.supportY) {

                applyConstraint(
                    matrix = stiffnessMatrix,
                    vector = loadVector,
                    degreeIndex =
                        nodeIndex * 2 + 1
                )
            }
        }

        // ====================================================
        // РЕШЕНИЕ K * U = F
        // ====================================================

        val displacements =
            solveLinearSystem(
                matrix = stiffnessMatrix,
                vector = loadVector
            )
                ?: return structuralCalculationFailure(
                    "Расчётная схема геометрически неустойчива или матрица жёсткости вырождена."
                )

        // ====================================================
        // РЕАКЦИИ
        //
        // R = K_original * U - F_original
        // ====================================================

        val reactions =
            multiplyMatrixVector(
                matrix =
                    originalStiffness,

                vector =
                    displacements
            )

        for (index in reactions.indices) {

            reactions[index] -=
                originalLoadVector[index]
        }

        // ====================================================
        // РЕЗУЛЬТАТЫ УЗЛОВ
        // ====================================================

        val nodeResults =
            nodes.mapIndexed {
                    index,
                    node ->

                val reactionX =
                    if (node.supportX) {
                        reactions[
                            index * 2
                        ]
                    } else {
                        0.0
                    }

                val reactionY =
                    if (node.supportY) {
                        reactions[
                            index * 2 + 1
                        ]
                    } else {
                        0.0
                    }

                StructuralNodeResult(
                    nodeId =
                        node.id,

                    displacementX =
                        displacements[
                            index * 2
                        ],

                    displacementY =
                        displacements[
                            index * 2 + 1
                        ],

                    reactionXKn =
                        cleanSmallValue(
                            reactionX
                        ),

                    reactionYKn =
                        cleanSmallValue(
                            reactionY
                        )
                )
            }

        // ====================================================
        // ПРОДОЛЬНЫЕ УСИЛИЯ В СТЕРЖНЯХ
        // ====================================================

        val memberResults =
            members.map { member ->

                val startNode =
                    model.findNode(
                        member.startNodeId
                    )!!

                val endNode =
                    model.findNode(
                        member.endNodeId
                    )!!

                val startIndex =
                    nodeIndexById[
                        startNode.id
                    ]!!

                val endIndex =
                    nodeIndexById[
                        endNode.id
                    ]!!

                val axialForce =
                    calculateMemberAxialForce(
                        member = member,

                        startNode =
                            startNode,

                        endNode =
                            endNode,

                        startDisplacementX =
                            displacements[
                                startIndex * 2
                            ],

                        startDisplacementY =
                            displacements[
                                startIndex * 2 + 1
                            ],

                        endDisplacementX =
                            displacements[
                                endIndex * 2
                            ],

                        endDisplacementY =
                            displacements[
                                endIndex * 2 + 1
                            ]
                    )

                val cleanedForce =
                    cleanSmallValue(
                        axialForce
                    )

                val state =
                    when {

                        cleanedForce >
                            FORCE_EPSILON -> {

                            StructuralMemberForceState
                                .TENSION
                        }

                        cleanedForce <
                            -FORCE_EPSILON -> {

                            StructuralMemberForceState
                                .COMPRESSION
                        }

                        else -> {

                            StructuralMemberForceState
                                .ZERO
                        }
                    }

                StructuralMemberResult(
                    memberId =
                        member.id,

                    axialForceKn =
                        cleanedForce,

                    state =
                        state
                )
            }

        return StructuralCalculationResult(
            success = true,
            nodeResults = nodeResults,
            memberResults = memberResults,
            errors = emptyList()
        )
    }

    // ========================================================
    // ПРОВЕРКА МОДЕЛИ
    // ========================================================

    private fun validateModel(
        model: StructuralModel
    ): List<String> {

        val errors =
            mutableListOf<String>()

        errors +=
            model.validateForCalculation()

        model.members.forEach { member ->

            val start =
                model.findNode(
                    member.startNodeId
                )

            val end =
                model.findNode(
                    member.endNodeId
                )

            if (
                start == null ||
                end == null
            ) {

                return@forEach
            }

            val length =
                hypot(
                    end.x - start.x,
                    end.y - start.y
                )

            if (
                !length.isFinite() ||
                length <=
                    GEOMETRY_EPSILON
            ) {

                errors +=
                    "Стержень ${member.id} имеет некорректную длину."
            }

            val area =
                member.area

            if (
                area == null ||
                !area.isFinite() ||
                area <= 0.0
            ) {

                errors +=
                    "Для стержня ${member.id} не задана корректная площадь сечения."
            }

            val elasticModulus =
                member.elasticModulus

            if (
                elasticModulus == null ||
                !elasticModulus.isFinite() ||
                elasticModulus <= 0.0
            ) {

                errors +=
                    "Для стержня ${member.id} не задан корректный модуль упругости."
            }
        }

        return errors.distinct()
    }

    // ========================================================
    // МАТРИЦА ЖЁСТКОСТИ ОДНОГО 2D СТЕРЖНЯ
    // ========================================================

    private fun createMemberGlobalStiffness(
        member: StructuralMember,
        startNode: StructuralNode,
        endNode: StructuralNode
    ): Array<DoubleArray>? {

        val area =
            member.area
                ?: return null

        val elasticModulus =
            member.elasticModulus
                ?: return null

        val dx =
            endNode.x -
                startNode.x

        val dy =
            endNode.y -
                startNode.y

        val length =
            hypot(
                dx,
                dy
            )

        if (
            !length.isFinite() ||
            length <=
                GEOMETRY_EPSILON
        ) {

            return null
        }

        val cosine =
            dx / length

        val sine =
            dy / length

        val coefficient =
            area *
                elasticModulus /
                length

        val c2 =
            cosine * cosine

        val s2 =
            sine * sine

        val cs =
            cosine * sine

        return arrayOf(

            doubleArrayOf(
                coefficient * c2,
                coefficient * cs,
                -coefficient * c2,
                -coefficient * cs
            ),

            doubleArrayOf(
                coefficient * cs,
                coefficient * s2,
                -coefficient * cs,
                -coefficient * s2
            ),

            doubleArrayOf(
                -coefficient * c2,
                -coefficient * cs,
                coefficient * c2,
                coefficient * cs
            ),

            doubleArrayOf(
                -coefficient * cs,
                -coefficient * s2,
                coefficient * cs,
                coefficient * s2
            )
        )
    }

    // ========================================================
    // ЗАКРЕПЛЕНИЕ ОДНОЙ СТЕПЕНИ СВОБОДЫ
    // ========================================================

    private fun applyConstraint(
        matrix: Array<DoubleArray>,
        vector: DoubleArray,
        degreeIndex: Int
    ) {

        for (index in matrix.indices) {

            matrix[
                degreeIndex
            ][
                index
            ] = 0.0

            matrix[
                index
            ][
                degreeIndex
            ] = 0.0
        }

        matrix[
            degreeIndex
        ][
            degreeIndex
        ] = 1.0

        vector[
            degreeIndex
        ] = 0.0
    }

    // ========================================================
    // РЕШЕНИЕ СИСТЕМЫ ЛИНЕЙНЫХ УРАВНЕНИЙ
    //
    // Метод Гаусса с выбором главного элемента.
    // ========================================================

    private fun solveLinearSystem(
        matrix: Array<DoubleArray>,
        vector: DoubleArray
    ): DoubleArray? {

        val size =
            vector.size

        if (
            matrix.size != size
        ) {

            return null
        }

        val a =
            copyMatrix(
                matrix
            )

        val b =
            vector.copyOf()

        for (
            pivotIndex in
            0 until size
        ) {

            var bestRow =
                pivotIndex

            var bestValue =
                abs(
                    a[
                        pivotIndex
                    ][
                        pivotIndex
                    ]
                )

            for (
                row in
                pivotIndex + 1 until
                    size
            ) {

                val candidate =
                    abs(
                        a[row][pivotIndex]
                    )

                if (
                    candidate >
                    bestValue
                ) {

                    bestValue =
                        candidate

                    bestRow =
                        row
                }
            }

            if (
                !bestValue.isFinite() ||
                bestValue <
                    MATRIX_EPSILON
            ) {

                return null
            }

            if (
                bestRow !=
                pivotIndex
            ) {

                val temporaryRow =
                    a[pivotIndex]

                a[pivotIndex] =
                    a[bestRow]

                a[bestRow] =
                    temporaryRow

                val temporaryValue =
                    b[pivotIndex]

                b[pivotIndex] =
                    b[bestRow]

                b[bestRow] =
                    temporaryValue
            }

            val pivot =
                a[
                    pivotIndex
                ][
                    pivotIndex
                ]

            for (
                row in
                pivotIndex + 1 until
                    size
            ) {

                val factor =
                    a[row][pivotIndex] /
                        pivot

                if (
                    abs(factor) <
                    MATRIX_EPSILON
                ) {

                    continue
                }

                a[row][pivotIndex] =
                    0.0

                for (
                    column in
                    pivotIndex + 1 until
                        size
                ) {

                    a[row][column] -=
                        factor *
                            a[
                                pivotIndex
                            ][
                                column
                            ]
                }

                b[row] -=
                    factor *
                        b[pivotIndex]
            }
        }

        val result =
            DoubleArray(size)

        for (
            row in
            size - 1 downTo 0
        ) {

            var value =
                b[row]

            for (
                column in
                row + 1 until
                    size
            ) {

                value -=
                    a[row][column] *
                        result[column]
            }

            val diagonal =
                a[row][row]

            if (
                abs(diagonal) <
                MATRIX_EPSILON
            ) {

                return null
            }

            result[row] =
                value /
                    diagonal

            if (
                !result[row]
                    .isFinite()
            ) {

                return null
            }
        }

        return result
    }

    // ========================================================
    // ПРОДОЛЬНОЕ УСИЛИЕ СТЕРЖНЯ
    //
    // N = EA/L * deltaL
    //
    // Положительное значение = растяжение.
    // Отрицательное = сжатие.
    // ========================================================

    private fun calculateMemberAxialForce(
        member: StructuralMember,
        startNode: StructuralNode,
        endNode: StructuralNode,
        startDisplacementX: Double,
        startDisplacementY: Double,
        endDisplacementX: Double,
        endDisplacementY: Double
    ): Double {

        val area =
            member.area
                ?: return 0.0

        val elasticModulus =
            member.elasticModulus
                ?: return 0.0

        val dx =
            endNode.x -
                startNode.x

        val dy =
            endNode.y -
                startNode.y

        val length =
            hypot(
                dx,
                dy
            )

        if (
            length <=
            GEOMETRY_EPSILON
        ) {

            return 0.0
        }

        val cosine =
            dx / length

        val sine =
            dy / length

        val axialDisplacement =
            cosine *
                (
                    endDisplacementX -
                        startDisplacementX
                ) +
                sine *
                (
                    endDisplacementY -
                        startDisplacementY
                )

        return area *
            elasticModulus /
            length *
            axialDisplacement
    }

    // ========================================================
    // УМНОЖЕНИЕ МАТРИЦЫ НА ВЕКТОР
    // ========================================================

    private fun multiplyMatrixVector(
        matrix: Array<DoubleArray>,
        vector: DoubleArray
    ): DoubleArray {

        val result =
            DoubleArray(
                matrix.size
            )

        for (
            row in
            matrix.indices
        ) {

            var value =
                0.0

            for (
                column in
                matrix[row].indices
            ) {

                value +=
                    matrix[row][column] *
                        vector[column]
            }

            result[row] =
                value
        }

        return result
    }

    // ========================================================
    // КОПИЯ МАТРИЦЫ
    // ========================================================

    private fun copyMatrix(
        source: Array<DoubleArray>
    ): Array<DoubleArray> {

        return Array(
            source.size
        ) { index ->

            source[index]
                .copyOf()
        }
    }

    // ========================================================
    // УБИРАЕМ ЧИСЛЕННЫЙ ШУМ
    // ========================================================

    private fun cleanSmallValue(
        value: Double
    ): Double {

        return if (
            abs(value) <
            FORCE_EPSILON
        ) {

            0.0

        } else {

            value
        }
    }
}
