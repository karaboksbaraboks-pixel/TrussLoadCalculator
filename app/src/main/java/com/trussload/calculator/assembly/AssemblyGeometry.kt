package com.trussload.calculator.assembly

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

// ============================================================
// НАСТРОЙКИ ГЕОМЕТРИИ
// ============================================================

object AssemblyGeometryConfig {

    // Масштаб прямых секций
    const val PIXELS_PER_METER = 120f

    // Радиус точки соединения
    const val NODE_RADIUS = 8f

    // Допуск выбора прямой секции пальцем
    const val SELECTION_DISTANCE = 38f

    // Расстояние автоматического прилипания
    const val SNAP_DISTANCE = 40f

    // Длина плеча соединителя
    const val CONNECTOR_SIZE = 54f
}

// ============================================================
// УЗЕЛ ЭЛЕМЕНТА
// ============================================================

data class AssemblyNode(
    val elementId: String,
    val nodeIndex: Int,
    val x: Float,
    val y: Float
)

// ============================================================
// РЕЗУЛЬТАТ SNAP
// ============================================================

data class SnapResult(
    val movingElementId: String,
    val movingNodeIndex: Int,

    val targetElementId: String,
    val targetNodeIndex: Int,

    val targetX: Float,
    val targetY: Float,

    val distance: Float
)

// ============================================================
// ПОВОРОТ ЛОКАЛЬНОЙ ТОЧКИ
// ============================================================

private fun rotateLocalPoint(
    localX: Float,
    localY: Float,
    rotationDegrees: Float
): AssemblyPoint {

    val radians =
        Math.toRadians(
            rotationDegrees.toDouble()
        )

    val cosValue =
        cos(radians).toFloat()

    val sinValue =
        sin(radians).toFloat()

    return AssemblyPoint(
        x =
            localX * cosValue -
                localY * sinValue,

        y =
            localX * sinValue +
                localY * cosValue
    )
}

// ============================================================
// ПРЕОБРАЗОВАНИЕ ЛОКАЛЬНЫХ УЗЛОВ В ГЛОБАЛЬНЫЕ
// ============================================================

private fun buildNodes(
    element: AssemblyElement,
    localPoints: List<AssemblyPoint>
): List<AssemblyNode> {

    return localPoints.mapIndexed { index, point ->

        val rotated =
            rotateLocalPoint(
                localX = point.x,
                localY = point.y,
                rotationDegrees = element.rotation
            )

        AssemblyNode(
            elementId = element.id,
            nodeIndex = index,
            x = element.x + rotated.x,
            y = element.y + rotated.y
        )
    }
}

// ============================================================
// ПРЯМАЯ СЕКЦИЯ
// ============================================================

private fun straightNodes(
    element: AssemblyElement,
    pixelsPerMeter: Float
): List<AssemblyNode> {

    val endpoints =
        element.getStraightEndpoints(
            pixelsPerMeter = pixelsPerMeter
        )

    return listOf(

        AssemblyNode(
            elementId = element.id,
            nodeIndex = 0,
            x = endpoints.first.x,
            y = endpoints.first.y
        ),

        AssemblyNode(
            elementId = element.id,
            nodeIndex = 1,
            x = endpoints.second.x,
            y = endpoints.second.y
        )
    )
}

// ============================================================
// УГОЛ 90°
//
// Узел 0 — левое плечо
// Узел 1 — верхнее плечо
// ============================================================

private fun corner90Nodes(
    element: AssemblyElement
): List<AssemblyNode> {

    val arm =
        AssemblyGeometryConfig.CONNECTOR_SIZE

    return buildNodes(
        element = element,
        localPoints =
            listOf(
                AssemblyPoint(
                    x = -arm,
                    y = 0f
                ),
                AssemblyPoint(
                    x = 0f,
                    y = -arm
                )
            )
    )
}

// ============================================================
// УГОЛ 135°
//
// Между двумя плечами 135°.
// ============================================================

private fun corner135Nodes(
    element: AssemblyElement
): List<AssemblyNode> {

    val arm =
        AssemblyGeometryConfig.CONNECTOR_SIZE

    val angle45 =
        Math.toRadians(45.0)

    val diagonalX =
        (cos(angle45) * arm)
            .toFloat()

    val diagonalY =
        (sin(angle45) * arm)
            .toFloat()

    return buildNodes(
        element = element,
        localPoints =
            listOf(
                AssemblyPoint(
                    x = -arm,
                    y = 0f
                ),
                AssemblyPoint(
                    x = diagonalX,
                    y = -diagonalY
                )
            )
    )
}

// ============================================================
// T-СОЕДИНИТЕЛЬ
//
//          2
//          |
//     0 ---+--- 1
//
// Три реальных точки соединения.
// ============================================================

private fun tJunctionNodes(
    element: AssemblyElement
): List<AssemblyNode> {

    val arm =
        AssemblyGeometryConfig.CONNECTOR_SIZE

    return buildNodes(
        element = element,
        localPoints =
            listOf(
                // Левая
                AssemblyPoint(
                    x = -arm,
                    y = 0f
                ),

                // Правая
                AssemblyPoint(
                    x = arm,
                    y = 0f
                ),

                // Верхняя
                AssemblyPoint(
                    x = 0f,
                    y = -arm
                )
            )
    )
}

// ============================================================
// X-СОЕДИНИТЕЛЬ
//
//          2
//          |
//     0 ---+--- 1
//          |
//          3
//
// Четыре реальных точки соединения.
// ============================================================

private fun xJunctionNodes(
    element: AssemblyElement
): List<AssemblyNode> {

    val arm =
        AssemblyGeometryConfig.CONNECTOR_SIZE

    return buildNodes(
        element = element,
        localPoints =
            listOf(
                // Левая
                AssemblyPoint(
                    x = -arm,
                    y = 0f
                ),

                // Правая
                AssemblyPoint(
                    x = arm,
                    y = 0f
                ),

                // Верхняя
                AssemblyPoint(
                    x = 0f,
                    y = -arm
                ),

                // Нижняя
                AssemblyPoint(
                    x = 0f,
                    y = arm
                )
            )
    )
}

// ============================================================
// ПОЛУЧЕНИЕ УЗЛОВ ЛЮБОГО ЭЛЕМЕНТА
// ============================================================

fun AssemblyElement.getNodes(
    pixelsPerMeter: Float =
        AssemblyGeometryConfig.PIXELS_PER_METER
): List<AssemblyNode> {

    return when (type) {

        AssemblyElementType.STRAIGHT ->
            straightNodes(
                element = this,
                pixelsPerMeter = pixelsPerMeter
            )

        AssemblyElementType.CORNER_90 ->
            corner90Nodes(
                element = this
            )

        AssemblyElementType.CORNER_135 ->
            corner135Nodes(
                element = this
            )

        AssemblyElementType.T_JUNCTION ->
            tJunctionNodes(
                element = this
            )

        AssemblyElementType.X_JUNCTION ->
            xJunctionNodes(
                element = this
            )
    }
}

// ============================================================
// РАССТОЯНИЕ МЕЖДУ ТОЧКАМИ
// ============================================================

fun distanceBetween(
    firstX: Float,
    firstY: Float,
    secondX: Float,
    secondY: Float
): Float {

    return hypot(
        firstX - secondX,
        firstY - secondY
    )
}

// ============================================================
// РАССТОЯНИЕ ОТ ТОЧКИ ДО ОТРЕЗКА
// ============================================================

private fun distancePointToSegment(
    pointX: Float,
    pointY: Float,

    startX: Float,
    startY: Float,

    endX: Float,
    endY: Float
): Float {

    val segmentX =
        endX - startX

    val segmentY =
        endY - startY

    val segmentLengthSquared =
        segmentX * segmentX +
            segmentY * segmentY

    if (segmentLengthSquared <= 0.0001f) {

        return distanceBetween(
            firstX = pointX,
            firstY = pointY,
            secondX = startX,
            secondY = startY
        )
    }

    val projection =
        (
            (pointX - startX) * segmentX +
                (pointY - startY) * segmentY
        ) / segmentLengthSquared

    val clamped =
        projection.coerceIn(
            0f,
            1f
        )

    val nearestX =
        startX +
            clamped * segmentX

    val nearestY =
        startY +
            clamped * segmentY

    return distanceBetween(
        firstX = pointX,
        firstY = pointY,
        secondX = nearestX,
        secondY = nearestY
    )
}

// ============================================================
// РАССТОЯНИЕ ДО СОЕДИНИТЕЛЯ
//
// Для T/X/углов проверяем не только центр,
// но и каждое плечо соединителя.
// ============================================================

private fun distanceToConnector(
    element: AssemblyElement,
    pointX: Float,
    pointY: Float
): Float {

    val nodes =
        element.getNodes()

    if (nodes.isEmpty()) {
        return Float.MAX_VALUE
    }

    var bestDistance =
        distanceBetween(
            firstX = pointX,
            firstY = pointY,
            secondX = element.x,
            secondY = element.y
        )

    nodes.forEach { node ->

        val distance =
            distancePointToSegment(
                pointX = pointX,
                pointY = pointY,

                startX = element.x,
                startY = element.y,

                endX = node.x,
                endY = node.y
            )

        if (distance < bestDistance) {
            bestDistance = distance
        }
    }

    return bestDistance
}

// ============================================================
// ПОИСК ЭЛЕМЕНТА ПОД ПАЛЬЦЕМ
// ============================================================

fun TrussAssembly.findElementAt(
    x: Float,
    y: Float,
    pixelsPerMeter: Float =
        AssemblyGeometryConfig.PIXELS_PER_METER
): AssemblyElement? {

    return elements
        .asReversed()
        .firstOrNull { element ->

            when (element.type) {

                AssemblyElementType.STRAIGHT -> {

                    val endpoints =
                        element.getStraightEndpoints(
                            pixelsPerMeter =
                                pixelsPerMeter
                        )

                    distancePointToSegment(
                        pointX = x,
                        pointY = y,

                        startX = endpoints.first.x,
                        startY = endpoints.first.y,

                        endX = endpoints.second.x,
                        endY = endpoints.second.y
                    ) <=
                        AssemblyGeometryConfig
                            .SELECTION_DISTANCE
                }

                AssemblyElementType.CORNER_90,
                AssemblyElementType.CORNER_135,
                AssemblyElementType.T_JUNCTION,
                AssemblyElementType.X_JUNCTION -> {

                    distanceToConnector(
                        element = element,
                        pointX = x,
                        pointY = y
                    ) <=
                        AssemblyGeometryConfig
                            .SELECTION_DISTANCE
                }
            }
        }
}

// ============================================================
// ПОИСК БЛИЖАЙШЕЙ ТОЧКИ SNAP
// ============================================================

fun TrussAssembly.findSnapForElement(
    movingElementId: String,
    snapDistance: Float =
        AssemblyGeometryConfig.SNAP_DISTANCE,
    pixelsPerMeter: Float =
        AssemblyGeometryConfig.PIXELS_PER_METER
): SnapResult? {

    val movingElement =
        elements.firstOrNull {
            it.id == movingElementId
        } ?: return null

    val movingNodes =
        movingElement.getNodes(
            pixelsPerMeter =
                pixelsPerMeter
        )

    val targetElements =
        elements.filter {
            it.id != movingElementId
        }

    var bestResult: SnapResult? =
        null

    movingNodes.forEach { movingNode ->

        targetElements.forEach { targetElement ->

            val targetNodes =
                targetElement.getNodes(
                    pixelsPerMeter =
                        pixelsPerMeter
                )

            targetNodes.forEach { targetNode ->

                val distance =
                    distanceBetween(
                        firstX =
                            movingNode.x,

                        firstY =
                            movingNode.y,

                        secondX =
                            targetNode.x,

                        secondY =
                            targetNode.y
                    )

                val currentBest =
                    bestResult

                if (
                    distance <= snapDistance &&
                    (
                        currentBest == null ||
                            distance <
                                currentBest.distance
                    )
                ) {

                    bestResult =
                        SnapResult(
                            movingElementId =
                                movingElement.id,

                            movingNodeIndex =
                                movingNode.nodeIndex,

                            targetElementId =
                                targetElement.id,

                            targetNodeIndex =
                                targetNode.nodeIndex,

                            targetX =
                                targetNode.x,

                            targetY =
                                targetNode.y,

                            distance =
                                distance
                        )
                }
            }
        }
    }

    return bestResult
}

// ============================================================
// ПРИМЕНЕНИЕ SNAP
// ============================================================

fun TrussAssembly.applySnap(
    snapResult: SnapResult,
    pixelsPerMeter: Float =
        AssemblyGeometryConfig.PIXELS_PER_METER
): TrussAssembly {

    val movingElement =
        elements.firstOrNull {
            it.id ==
                snapResult.movingElementId
        } ?: return this

    val movingNode =
        movingElement
            .getNodes(
                pixelsPerMeter =
                    pixelsPerMeter
            )
            .firstOrNull {
                it.nodeIndex ==
                    snapResult.movingNodeIndex
            }
            ?: return this

    val deltaX =
        snapResult.targetX -
            movingNode.x

    val deltaY =
        snapResult.targetY -
            movingNode.y

    val movedAssembly =
        moveElement(
            elementId =
                movingElement.id,

            deltaX =
                deltaX,

            deltaY =
                deltaY
        )

    // Не допускаем повторной записи того же соединения.

    val alreadyExists =
        movedAssembly.connections.any { connection ->

            (
                connection.firstElementId ==
                    movingElement.id &&
                    connection.firstNodeIndex ==
                        snapResult.movingNodeIndex &&
                    connection.secondElementId ==
                        snapResult.targetElementId &&
                    connection.secondNodeIndex ==
                        snapResult.targetNodeIndex
            ) ||
                (
                    connection.secondElementId ==
                        movingElement.id &&
                    connection.secondNodeIndex ==
                        snapResult.movingNodeIndex &&
                    connection.firstElementId ==
                        snapResult.targetElementId &&
                    connection.firstNodeIndex ==
                        snapResult.targetNodeIndex
                )
        }

    if (alreadyExists) {
        return movedAssembly
    }

    val newConnection =
        AssemblyConnection(
            firstElementId =
                movingElement.id,

            firstNodeIndex =
                snapResult.movingNodeIndex,

            secondElementId =
                snapResult.targetElementId,

            secondNodeIndex =
                snapResult.targetNodeIndex
        )

    return movedAssembly.copy(
        connections =
            movedAssembly.connections +
                newConnection
    )
}

// ============================================================
// SNAP ОДНИМ ВЫЗОВОМ
// ============================================================

fun TrussAssembly.snapElementIfNeeded(
    elementId: String,
    snapDistance: Float =
        AssemblyGeometryConfig.SNAP_DISTANCE,
    pixelsPerMeter: Float =
        AssemblyGeometryConfig.PIXELS_PER_METER
): TrussAssembly {

    val snap =
        findSnapForElement(
            movingElementId =
                elementId,

            snapDistance =
                snapDistance,

            pixelsPerMeter =
                pixelsPerMeter
        ) ?: return this

    return applySnap(
        snapResult = snap,
        pixelsPerMeter =
            pixelsPerMeter
    )
}

// ============================================================
// УГОЛ МЕЖДУ ТОЧКАМИ
// ============================================================

fun angleBetweenPoints(
    firstX: Float,
    firstY: Float,
    secondX: Float,
    secondY: Float
): Float {

    val radians =
        atan2(
            secondY - firstY,
            secondX - firstX
        )

    return Math.toDegrees(
        radians.toDouble()
    ).toFloat()
}
