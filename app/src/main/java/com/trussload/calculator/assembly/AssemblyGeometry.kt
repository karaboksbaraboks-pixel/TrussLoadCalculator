package com.trussload.calculator.assembly

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

// ============================================================
// НАСТРОЙКИ ГЕОМЕТРИИ КОНСТРУКТОРА
// ============================================================

object AssemblyGeometryConfig {

    // Базовый масштаб отображения прямой секции.
    const val PIXELS_PER_METER = 120f

    // Радиус визуального узла.
    const val NODE_RADIUS = 9f

    // Радиус выбора элемента пальцем.
    const val SELECTION_DISTANCE = 32f

    // Расстояние, на котором узлы начинают "прилипать".
    const val SNAP_DISTANCE = 35f

    // Размер соединителей на рабочем поле.
    const val CONNECTOR_SIZE = 54f
}

// ============================================================
// УЗЕЛ ЭЛЕМЕНТА В ГЛОБАЛЬНЫХ КООРДИНАТАХ
// ============================================================

data class AssemblyNode(
    val elementId: String,
    val nodeIndex: Int,
    val x: Float,
    val y: Float
)

// ============================================================
// РЕЗУЛЬТАТ ПОИСКА ПРИВЯЗКИ
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
        Math.toRadians(rotationDegrees.toDouble())

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
// УЗЛЫ ПРЯМОЙ СЕКЦИИ
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
// УЗЛЫ УГЛА 90°
// ============================================================

private fun corner90Nodes(
    element: AssemblyElement
): List<AssemblyNode> {

    val arm =
        AssemblyGeometryConfig.CONNECTOR_SIZE

    val localPoints =
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

    return localPoints.mapIndexed {
            index,
            localPoint ->

        val rotated =
            rotateLocalPoint(
                localX = localPoint.x,
                localY = localPoint.y,
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
// УЗЛЫ УГЛА 135°
// ============================================================

private fun corner135Nodes(
    element: AssemblyElement
): List<AssemblyNode> {

    val arm =
        AssemblyGeometryConfig.CONNECTOR_SIZE

    val angle =
        Math.toRadians(45.0)

    val diagonalX =
        (cos(angle) * arm).toFloat()

    val diagonalY =
        (sin(angle) * arm).toFloat()

    val localPoints =
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

    return localPoints.mapIndexed {
            index,
            localPoint ->

        val rotated =
            rotateLocalPoint(
                localX = localPoint.x,
                localY = localPoint.y,
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
// УЗЛЫ T-СОЕДИНИТЕЛЯ
// ============================================================

private fun tJunctionNodes(
    element: AssemblyElement
): List<AssemblyNode> {

    val arm =
        AssemblyGeometryConfig.CONNECTOR_SIZE

    val localPoints =
        listOf(
            AssemblyPoint(
                x = -arm,
                y = 0f
            ),
            AssemblyPoint(
                x = arm,
                y = 0f
            ),
            AssemblyPoint(
                x = 0f,
                y = -arm
            )
        )

    return localPoints.mapIndexed {
            index,
            localPoint ->

        val rotated =
            rotateLocalPoint(
                localX = localPoint.x,
                localY = localPoint.y,
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
// УЗЛЫ X-СОЕДИНИТЕЛЯ
// ============================================================

private fun xJunctionNodes(
    element: AssemblyElement
): List<AssemblyNode> {

    val arm =
        AssemblyGeometryConfig.CONNECTOR_SIZE

    val localPoints =
        listOf(
            AssemblyPoint(
                x = -arm,
                y = 0f
            ),
            AssemblyPoint(
                x = arm,
                y = 0f
            ),
            AssemblyPoint(
                x = 0f,
                y = -arm
            ),
            AssemblyPoint(
                x = 0f,
                y = arm
            )
        )

    return localPoints.mapIndexed {
            index,
            localPoint ->

        val rotated =
            rotateLocalPoint(
                localX = localPoint.x,
                localY = localPoint.y,
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
// ВСЕ УЗЛЫ ОДНОГО ЭЛЕМЕНТА
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
            minimumValue = 0f,
            maximumValue = 1f
        )

    val nearestX =
        startX + clamped * segmentX

    val nearestY =
        startY + clamped * segmentY

    return distanceBetween(
        firstX = pointX,
        firstY = pointY,
        secondX = nearestX,
        secondY = nearestY
    )
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

    // Идём с конца списка:
    // последний нарисованный элемент выбирается первым.

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

                    val distance =
                        distancePointToSegment(
                            pointX = x,
                            pointY = y,

                            startX =
                                endpoints.first.x,

                            startY =
                                endpoints.first.y,

                            endX =
                                endpoints.second.x,

                            endY =
                                endpoints.second.y
                        )

                    distance <=
                        AssemblyGeometryConfig
                            .SELECTION_DISTANCE
                }

                else -> {

                    distanceBetween(
                        firstX = x,
                        firstY = y,
                        secondX = element.x,
                        secondY = element.y
                    ) <=
                        AssemblyGeometryConfig
                            .CONNECTOR_SIZE
                }
            }
        }
}

// ============================================================
// ПОИСК БЛИЖАЙШЕГО УЗЛА ДЛЯ SNAP
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

    var bestResult: SnapResult? = null

    movingNodes.forEach { movingNode ->

        targetElements.forEach { targetElement ->

            targetElement
                .getNodes(
                    pixelsPerMeter =
                        pixelsPerMeter
                )
                .forEach { targetNode ->

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

                    if (
                        distance <= snapDistance &&
                        (
                            bestResult == null ||
                                distance <
                                    bestResult!!.distance
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

    val connectionAlreadyExists =
        movedAssembly.connections.any {

            (
                it.firstElementId ==
                    movingElement.id &&
                    it.firstNodeIndex ==
                    snapResult.movingNodeIndex &&
                    it.secondElementId ==
                    snapResult.targetElementId &&
                    it.secondNodeIndex ==
                    snapResult.targetNodeIndex
            ) ||
                (
                    it.secondElementId ==
                        movingElement.id &&
                        it.secondNodeIndex ==
                        snapResult.movingNodeIndex &&
                        it.firstElementId ==
                        snapResult.targetElementId &&
                        it.firstNodeIndex ==
                        snapResult.targetNodeIndex
                )
        }

    if (connectionAlreadyExists) {
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
// SNAP В ОДИН ВЫЗОВ
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
// УГОЛ МЕЖДУ ДВУМЯ УЗЛАМИ
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
