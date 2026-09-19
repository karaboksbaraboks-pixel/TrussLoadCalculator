package com.trussload.calculator.models

// ============================================================
// УЗЕЛ РАСЧЁТНОЙ СХЕМЫ
// ============================================================

data class StructuralNode(
    val id: String,

    // Координаты узла в метрах
    val x: Double,
    val y: Double,

    // Связь с элементом визуальной сборки
    val sourceElementId: String? = null,
    val sourceNodeIndex: Int? = null,

    // Закрепления
    val supportX: Boolean = false,
    val supportY: Boolean = false
)

// ============================================================
// СТЕРЖЕНЬ РАСЧЁТНОЙ СХЕМЫ
// ============================================================

data class StructuralMember(
    val id: String,

    val startNodeId: String,
    val endNodeId: String,

    // Элемент визуальной сборки, из которого получен стержень
    val sourceElementId: String? = null,

    // Геометрические / физические параметры
    val area: Double? = null,
    val elasticModulus: Double? = null,

    // Масса элемента, кг
    val massKg: Double = 0.0
)

// ============================================================
// НАГРУЗКА НА УЗЕЛ
// ============================================================

data class StructuralLoad(
    val id: String,

    val nodeId: String,

    // Силы хранятся внутри расчётного ядра в кН
    val forceXKn: Double = 0.0,
    val forceYKn: Double = 0.0
)

// ============================================================
// ПОЛНАЯ РАСЧЁТНАЯ МОДЕЛЬ
// ============================================================

data class StructuralModel(
    val nodes: List<StructuralNode> = emptyList(),

    val members: List<StructuralMember> = emptyList(),

    val loads: List<StructuralLoad> = emptyList()
) {

    // --------------------------------------------------------
    // ПОИСК УЗЛА
    // --------------------------------------------------------

    fun findNode(
        nodeId: String
    ): StructuralNode? {

        return nodes.firstOrNull {
            it.id == nodeId
        }
    }

    // --------------------------------------------------------
    // ПОИСК СТЕРЖНЯ
    // --------------------------------------------------------

    fun findMember(
        memberId: String
    ): StructuralMember? {

        return members.firstOrNull {
            it.id == memberId
        }
    }

    // --------------------------------------------------------
    // НАГРУЗКИ КОНКРЕТНОГО УЗЛА
    // --------------------------------------------------------

    fun loadsForNode(
        nodeId: String
    ): List<StructuralLoad> {

        return loads.filter {
            it.nodeId == nodeId
        }
    }

    // --------------------------------------------------------
    // ПРОВЕРКА ПУСТОЙ МОДЕЛИ
    // --------------------------------------------------------

    val isEmpty: Boolean
        get() =
            nodes.isEmpty() &&
                members.isEmpty()

    // --------------------------------------------------------
    // КОЛИЧЕСТВО УЗЛОВ
    // --------------------------------------------------------

    val nodeCount: Int
        get() = nodes.size

    // --------------------------------------------------------
    // КОЛИЧЕСТВО СТЕРЖНЕЙ
    // --------------------------------------------------------

    val memberCount: Int
        get() = members.size

    // --------------------------------------------------------
    // КОЛИЧЕСТВО НАГРУЗОК
    // --------------------------------------------------------

    val loadCount: Int
        get() = loads.size
}
