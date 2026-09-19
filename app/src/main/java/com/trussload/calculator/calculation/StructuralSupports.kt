package com.trussload.calculator.calculation

import com.trussload.calculator.models.StructuralModel
import com.trussload.calculator.models.StructuralNode

// ============================================================
// ТИП ОПОРЫ
//
// FREE     - свободный узел
// X_ONLY   - запрещено перемещение по X
// Y_ONLY   - запрещено перемещение по Y
// PINNED   - запрещено перемещение по X и Y
//
// Для текущей 2D стержневой модели этого достаточно.
// Вращательная степень свободы здесь не используется.
// ============================================================

enum class SupportType(
    val title: String
) {

    FREE(
        title = "Без опоры"
    ),

    X_ONLY(
        title = "Закрепление X"
    ),

    Y_ONLY(
        title = "Закрепление Y"
    ),

    PINNED(
        title = "Шарнирная опора"
    )
}

// ============================================================
// ПОЛУЧЕНИЕ ТИПА ОПОРЫ ИЗ STRUCTURAL NODE
// ============================================================

val StructuralNode.supportType: SupportType
    get() =
        when {

            supportX && supportY ->
                SupportType.PINNED

            supportX ->
                SupportType.X_ONLY

            supportY ->
                SupportType.Y_ONLY

            else ->
                SupportType.FREE
        }

// ============================================================
// УСТАНОВКА ОПОРЫ НА УЗЕЛ
//
// StructuralNode immutable,
// поэтому возвращаем новый экземпляр.
// ============================================================

fun StructuralNode.withSupport(
    supportType: SupportType
): StructuralNode {

    return when (supportType) {

        SupportType.FREE ->
            copy(
                supportX = false,
                supportY = false
            )

        SupportType.X_ONLY ->
            copy(
                supportX = true,
                supportY = false
            )

        SupportType.Y_ONLY ->
            copy(
                supportX = false,
                supportY = true
            )

        SupportType.PINNED ->
            copy(
                supportX = true,
                supportY = true
            )
    }
}

// ============================================================
// УСТАНОВКА ОПОРЫ В STRUCTURAL MODEL
// ============================================================

fun StructuralModel.setSupport(
    nodeId: String,
    supportType: SupportType
): StructuralModel {

    require(
        findNode(nodeId) != null
    ) {
        "Невозможно установить опору: узел $nodeId не найден."
    }

    val updatedNodes =
        nodes.map { node ->

            if (node.id == nodeId) {

                node.withSupport(
                    supportType = supportType
                )

            } else {

                node
            }
        }

    return copy(
        nodes = updatedNodes
    )
}

// ============================================================
// СНЯТИЕ ОПОРЫ С УЗЛА
// ============================================================

fun StructuralModel.removeSupport(
    nodeId: String
): StructuralModel {

    return setSupport(
        nodeId = nodeId,
        supportType = SupportType.FREE
    )
}

// ============================================================
// УДАЛЕНИЕ ВСЕХ ОПОР
// ============================================================

fun StructuralModel.clearSupports():
    StructuralModel {

    return copy(
        nodes =
            nodes.map { node ->

                node.copy(
                    supportX = false,
                    supportY = false
                )
            }
    )
}

// ============================================================
// УЗЛЫ С ОПОРАМИ
// ============================================================

val StructuralModel.supportedNodes:
    List<StructuralNode>
    get() =
        nodes.filter { node ->

            node.supportX ||
                node.supportY
        }

// ============================================================
// КОЛИЧЕСТВО ЗАКРЕПЛЁННЫХ СТЕПЕНЕЙ СВОБОДЫ
// ============================================================

val StructuralModel.restrainedDegreeCount: Int
    get() =
        nodes.sumOf { node ->

            var count = 0

            if (node.supportX) {
                count++
            }

            if (node.supportY) {
                count++
            }

            count
        }

// ============================================================
// ПРОВЕРКА ОПОР
//
// Это пока базовая проверка.
//
// Для плоской конструкции необходимо ограничить
// перемещение всей конструкции по X и Y.
//
// Более строгая проверка устойчивости будет выполняться
// непосредственно расчётным solver.
// ============================================================

fun StructuralModel.validateSupports():
    List<String> {

    val errors =
        mutableListOf<String>()

    if (nodes.isEmpty()) {

        errors +=
            "Невозможно проверить опоры: расчётная схема не содержит узлов."

        return errors
    }

    val hasSupportX =
        nodes.any {
            it.supportX
        }

    val hasSupportY =
        nodes.any {
            it.supportY
        }

    if (!hasSupportX) {

        errors +=
            "Конструкция не имеет закрепления по оси X."
    }

    if (!hasSupportY) {

        errors +=
            "Конструкция не имеет закрепления по оси Y."
    }

    return errors
}

// ============================================================
// ОБЩАЯ ПРЕДВАРИТЕЛЬНАЯ ПРОВЕРКА МОДЕЛИ
//
// Объединяем уже созданные проверки:
// - геометрия
// - нагрузки
// - опоры
//
// Solver позже будет выполнять дополнительные проверки.
// ============================================================

fun StructuralModel.validateForCalculation():
    List<String> {

    val errors =
        mutableListOf<String>()

    errors +=
        validateGeometry()

    errors +=
        validateLoads()

    errors +=
        validateSupports()

    return errors.distinct()
    }
