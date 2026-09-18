package com.trussload.calculator.assembly

import java.util.UUID

enum class AssemblyElementType {
    STRAIGHT,
    CORNER,
    T_JUNCTION,
    X_JUNCTION
}

data class TrussSection(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val length: Double,
    val weight: Double = 0.0,
    val width: Double = 0.29,
    val height: Double = 0.29
)

data class AssemblyElement(
    val id: String = UUID.randomUUID().toString(),
    val type: AssemblyElementType,
    val name: String,
    val length: Double = 0.0,
    val weight: Double = 0.0,
    val rotation: Float = 0f,
    val x: Float = 0f,
    val y: Float = 0f
)

data class TrussAssembly(
    val name: String = "Новая сборка",
    val elements: List<AssemblyElement> = emptyList()
) {
    val totalStraightLength: Double
        get() = elements
            .filter { it.type == AssemblyElementType.STRAIGHT }
            .sumOf { it.length }

    val totalWeight: Double
        get() = elements.sumOf { it.weight }
}

object StandardTrussElements {

    val straightSections = listOf(
        AssemblyElement(
            type = AssemblyElementType.STRAIGHT,
            name = "Ферма 0.5 м",
            length = 0.5
        ),
        AssemblyElement(
            type = AssemblyElementType.STRAIGHT,
            name = "Ферма 1 м",
            length = 1.0
        ),
        AssemblyElement(
            type = AssemblyElementType.STRAIGHT,
            name = "Ферма 2 м",
            length = 2.0
        ),
        AssemblyElement(
            type = AssemblyElementType.STRAIGHT,
            name = "Ферма 3 м",
            length = 3.0
        ),
        AssemblyElement(
            type = AssemblyElementType.STRAIGHT,
            name = "Ферма 4 м",
            length = 4.0
        )
    )

    val connectors = listOf(
        AssemblyElement(
            type = AssemblyElementType.CORNER,
            name = "Угол 90°"
        ),
        AssemblyElement(
            type = AssemblyElementType.T_JUNCTION,
            name = "T-соединитель"
        ),
        AssemblyElement(
            type = AssemblyElementType.X_JUNCTION,
            name = "X-соединитель"
        )
    )
}
