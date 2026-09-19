package com.trussload.calculator.calculation

import com.trussload.calculator.assembly.AssemblyElement
import com.trussload.calculator.assembly.AssemblyElementType
import com.trussload.calculator.assembly.AssemblyGeometryConfig
import com.trussload.calculator.assembly.AssemblyNode
import com.trussload.calculator.assembly.TrussAssembly
import com.trussload.calculator.assembly.getNodes
import com.trussload.calculator.models.StructuralMember
import com.trussload.calculator.models.StructuralModel
import com.trussload.calculator.models.StructuralNode
import kotlin.math.abs
import kotlin.math.hypot

// ============================================================
// КОНВЕРТЕР ВИЗУАЛЬНОЙ СБОРКИ В РАСЧЁТНУЮ МОДЕЛЬ
//
// Визуальный конструктор:
//     координаты в пикселях
//
// Расчётная модель:
//     координаты в метрах
//
// Все сохранённые AssemblyConnection используются для
// объединения физических узлов.
//
// ВАЖНО:
// этот класс НЕ изменяет TrussAssembly.
// ============================================================

object AssemblyStructuralConverter {

    // --------------------------------------------------------
    // ОСНОВНОЙ МЕТОД
    // --------------------------------------------------------

    fun convert(
        assembly: TrussAssembly
    ): StructuralModel {

        if (assembly.elements.isEmpty()) {

            return StructuralModel(
                name = assembly.name
            )
        }

        val nodeReferences =
            createNodeReferences(
                assembly = assembly
            )

        val groups =
            buildConnectedNodeGroups(
                assembly = assembly,
                nodeReferences = nodeReferences
            )

        val structuralNodes =
            createStructuralNodes(
                groups = groups
            )

        val structuralNodeByReference =
            createStructuralNodeReferenceMap(
                groups = groups,
                structuralNodes = structuralNodes
            )

        val structuralMembers =
            createStructuralMembers(
                assembly = assembly,
                structuralNodeByReference =
                    structuralNodeByReference
            )

        return StructuralModel(
            name = assembly.name,
            nodes = structuralNodes,
            members = structuralMembers
        )
    }

    // --------------------------------------------------------
    // СОЗДАЁМ ВСЕ УЗЛЫ ВИЗУАЛЬНОЙ СБОРКИ
    // --------------------------------------------------------

    private fun createNodeReferences(
        assembly: TrussAssembly
    ): List<AssemblyNodeReference> {

        val result =
            mutableListOf<AssemblyNodeReference>()

        assembly.elements.forEach { element ->

            element
                .getNodes(
                    pixelsPerMeter =
                        AssemblyGeometryConfig
                            .PIXELS_PER_METER
                )
                .forEach { node ->

                    result +=
                        AssemblyNodeReference(
                            key =
                                NodeKey(
                                    elementId =
                                        node.elementId,
                                    nodeIndex =
                                        node.nodeIndex
                                ),

                            node = node
                        )
                }
        }

        return result
    }

    // --------------------------------------------------------
    // ОБЪЕДИНЕНИЕ СОЕДИНЁННЫХ УЗЛОВ
    //
    // Используется Union-Find.
    // --------------------------------------------------------

    private fun buildConnectedNodeGroups(
        assembly: TrussAssembly,
        nodeReferences:
            List<AssemblyNodeReference>
    ): List<List<AssemblyNodeReference>> {

        val keys =
            nodeReferences.map {
                it.key
            }

        val unionFind =
            NodeUnionFind(
                keys = keys
            )

        assembly.connections.forEach { connection ->

            val first =
                NodeKey(
                    elementId =
                        connection.firstElementId,

                    nodeIndex =
                        connection.firstNodeIndex
                )

            val second =
                NodeKey(
                    elementId =
                        connection.secondElementId,

                    nodeIndex =
                        connection.secondNodeIndex
                )

            if (
                unionFind.contains(first) &&
                unionFind.contains(second)
            ) {

                unionFind.union(
                    first = first,
                    second = second
                )
            }
        }

        return nodeReferences
            .groupBy { reference ->

                unionFind.find(
                    reference.key
                )
            }
            .values
            .toList()
    }

    // --------------------------------------------------------
    // СОЗДАНИЕ ФИЗИЧЕСКИХ РАСЧЁТНЫХ УЗЛОВ
    // --------------------------------------------------------

    private fun createStructuralNodes(
        groups:
            List<List<AssemblyNodeReference>>
    ): List<StructuralNode> {

        return groups.map { group ->

            val averageX =
                group
                    .map {
                        it.node.x.toDouble()
                    }
                    .average()

            val averageY =
                group
                    .map {
                        it.node.y.toDouble()
                    }
                    .average()

            StructuralNode(
                x =
                    pixelsToMeters(
                        pixels = averageX
                    ),

                y =
                    pixelsToMeters(
                        pixels = averageY
                    ),

                sourceElementId =
                    group
                        .firstOrNull()
                        ?.key
                        ?.elementId,

                sourceNodeIndex =
                    group
                        .firstOrNull()
                        ?.key
                        ?.nodeIndex
            )
        }
    }

    // --------------------------------------------------------
    // КАРТА:
    //
    // elementId + nodeIndex
    //          ->
    // StructuralNode
    // --------------------------------------------------------

    private fun createStructuralNodeReferenceMap(
        groups:
            List<List<AssemblyNodeReference>>,

        structuralNodes:
            List<StructuralNode>
    ): Map<NodeKey, StructuralNode> {

        val result =
            mutableMapOf<NodeKey, StructuralNode>()

        groups.forEachIndexed {
                index,
                group ->

            val structuralNode =
                structuralNodes
                    .getOrNull(index)
                    ?: return@forEachIndexed

            group.forEach { reference ->

                result[
                    reference.key
                ] = structuralNode
            }
        }

        return result
    }

    // --------------------------------------------------------
    // СОЗДАНИЕ РАСЧЁТНЫХ ЭЛЕМЕНТОВ
    // --------------------------------------------------------

    private fun createStructuralMembers(
        assembly: TrussAssembly,

        structuralNodeByReference:
            Map<NodeKey, StructuralNode>
    ): List<StructuralMember> {

        val result =
            mutableListOf<StructuralMember>()

        assembly.elements.forEach { element ->

            when (element.type) {

                AssemblyElementType.STRAIGHT -> {

                    addStraightMember(
                        element = element,

                        structuralNodeByReference =
                            structuralNodeByReference,

                        result = result
                    )
                }

                AssemblyElementType.CORNER_90,
                AssemblyElementType.CORNER_135,
                AssemblyElementType.T_JUNCTION,
                AssemblyElementType.X_JUNCTION,
                AssemblyElementType.CUBE -> {

                    addConnectorMembers(
                        element = element,

                        structuralNodeByReference =
                            structuralNodeByReference,

                        result = result
                    )
                }
            }
        }

        return result
    }

    // --------------------------------------------------------
    // ПРЯМАЯ СЕКЦИЯ
    // --------------------------------------------------------

    private fun addStraightMember(
        element: AssemblyElement,

        structuralNodeByReference:
            Map<NodeKey, StructuralNode>,

        result:
            MutableList<StructuralMember>
    ) {

        val start =
            structuralNodeByReference[
                NodeKey(
                    elementId = element.id,
                    nodeIndex = 0
                )
            ] ?: return

        val end =
            structuralNodeByReference[
                NodeKey(
                    elementId = element.id,
                    nodeIndex = 1
                )
            ] ?: return

        if (start.id == end.id) {
            return
        }

        result +=
            StructuralMember(
                startNodeId =
                    start.id,

                endNodeId =
                    end.id,

                sourceElementId =
                    element.id,

                massKg =
                    element.weight,

                maxDistributedLoadKnPerM =
                    element.maxDistributedLoad
                        ?: 0.0,

                maxPointLoadKn =
                    element.maxPointLoad
                        ?: 0.0
            )
    }

    // --------------------------------------------------------
    // СОЕДИНИТЕЛЬ / КУБ
    // --------------------------------------------------------

    private fun addConnectorMembers(
        element: AssemblyElement,

        structuralNodeByReference:
            Map<NodeKey, StructuralNode>,

        result:
            MutableList<StructuralMember>
    ) {

        val visualNodes =
            element.getNodes(
                pixelsPerMeter =
                    AssemblyGeometryConfig
                        .PIXELS_PER_METER
            )

        if (visualNodes.isEmpty()) {
            return
        }

        /*
         * Для углов, T, X и куба сейчас используем
         * существующие внешние расчётные узлы.
         *
         * Отдельный центральный StructuralNode
         * на данном этапе не создаётся.
         */

        val nodes =
            visualNodes
                .mapNotNull { visualNode ->

                    structuralNodeByReference[
                        NodeKey(
                            elementId =
                                element.id,

                            nodeIndex =
                                visualNode.nodeIndex
                        )
                    ]
                }
                .distinctBy {
                    it.id
                }

        if (nodes.size < 2) {
            return
        }

        val first =
            nodes.first()

        nodes
            .drop(1)
            .forEach { node ->

                if (first.id != node.id) {

                    result +=
                        StructuralMember(
                            startNodeId =
                                first.id,

                            endNodeId =
                                node.id,

                            sourceElementId =
                                element.id,

                            massKg =
                                0.0,

                            maxDistributedLoadKnPerM =
                                element
                                    .maxDistributedLoad
                                    ?: 0.0,

                            maxPointLoadKn =
                                element
                                    .maxPointLoad
                                    ?: 0.0
                        )
                }
            }
    }

    // --------------------------------------------------------
    // PIXELS -> METERS
    // --------------------------------------------------------

    private fun pixelsToMeters(
        pixels: Double
    ): Double {

        return pixels /
            AssemblyGeometryConfig
                .PIXELS_PER_METER
                .toDouble()
    }
}

// ============================================================
// ССЫЛКА НА УЗЕЛ ВИЗУАЛЬНОГО ЭЛЕМЕНТА
// ============================================================

private data class AssemblyNodeReference(

    val key: NodeKey,

    val node: AssemblyNode
)

// ============================================================
// УНИКАЛЬНЫЙ КЛЮЧ УЗЛА
// ============================================================

private data class NodeKey(

    val elementId: String,

    val nodeIndex: Int
)

// ============================================================
// UNION-FIND
// ============================================================

private class NodeUnionFind(
    keys: List<NodeKey>
) {

    private val parent:
        MutableMap<NodeKey, NodeKey> =
        mutableMapOf()

    init {

        keys.forEach { key ->

            parent[key] =
                key
        }
    }

    fun contains(
        key: NodeKey
    ): Boolean {

        return parent.containsKey(
            key
        )
    }

    fun find(
        key: NodeKey
    ): NodeKey {

        val current =
            parent[key]
                ?: return key

        if (current == key) {
            return key
        }

        val root =
            find(
                current
            )

        parent[key] =
            root

        return root
    }

    fun union(
        first: NodeKey,
        second: NodeKey
    ) {

        val firstRoot =
            find(first)

        val secondRoot =
            find(second)

        if (
            firstRoot !=
            secondRoot
        ) {

            parent[secondRoot] =
                firstRoot
        }
    }
}

// ============================================================
// ПРОВЕРКА ГЕОМЕТРИИ РАСЧЁТНОЙ МОДЕЛИ
// ============================================================

fun StructuralModel.validateGeometry():
    List<String> {

    val errors =
        mutableListOf<String>()

    if (nodes.isEmpty()) {

        errors +=
            "В расчётной схеме отсутствуют узлы."
    }

    if (members.isEmpty()) {

        errors +=
            "В расчётной схеме отсутствуют элементы."
    }

    members.forEach { member ->

        val start =
            findNode(
                member.startNodeId
            )

        val end =
            findNode(
                member.endNodeId
            )

        if (start == null) {

            errors +=
                "Не найден начальный узел элемента ${member.id}."

            return@forEach
        }

        if (end == null) {

            errors +=
                "Не найден конечный узел элемента ${member.id}."

            return@forEach
        }

        val length =
            hypot(
                end.x - start.x,
                end.y - start.y
            )

        if (
            !length.isFinite() ||
            length <= 0.000001
        ) {

            errors +=
                "Элемент ${member.id} имеет нулевую или некорректную длину."
        }
    }

    return errors
}

// ============================================================
// ПРОВЕРКА СОВПАДАЮЩИХ НЕСОЕДИНЁННЫХ УЗЛОВ
// ============================================================

fun StructuralModel.findNearlyCoincidentNodes(
    toleranceMeters: Double = 0.005
): List<Pair<StructuralNode, StructuralNode>> {

    val result =
        mutableListOf<
            Pair<
                StructuralNode,
                StructuralNode
            >
        >()

    for (
        firstIndex in
        nodes.indices
    ) {

        for (
            secondIndex in
            firstIndex + 1 until
                nodes.size
        ) {

            val first =
                nodes[firstIndex]

            val second =
                nodes[secondIndex]

            val dx =
                first.x -
                    second.x

            val dy =
                first.y -
                    second.y

            if (
                abs(dx) <= toleranceMeters &&
                abs(dy) <= toleranceMeters
            ) {

                val distance =
                    hypot(
                        dx,
                        dy
                    )

                if (
                    distance <=
                    toleranceMeters
                ) {

                    result +=
                        first to second
                }
            }
        }
    }

    return result
}
