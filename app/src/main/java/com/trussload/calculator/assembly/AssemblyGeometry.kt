package com.trussload.calculator.assembly

import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

// ============================================================
// НАСТРОЙКИ ГЕОМЕТРИИ
// ============================================================

object AssemblyGeometryConfig {

    const val PIXELS_PER_METER = 120f

    const val NODE_RADIUS = 8f

    // Увеличенная зона выбора пальцем
    const val SELECTION_DISTANCE = 42f

    // Расстояние автоматического соединения
    const val SNAP_DISTANCE = 32f

    // Длина плеч соединителей
    const val CONNECTOR_SIZE = 42f

    // Размер графического куба
    const val CUBE_SIZE = 64f
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
// ПРЕОБРАЗОВАНИЕ ЛОКАЛЬНЫХ ТОЧЕК В УЗЛЫ
// ============================================================

private fun createNodes(
    element: AssemblyElement,
    localPoints: List<AssemblyPoint>
): List<AssemblyNode> {

    return localPoints.mapIndexed { index, localPoint ->

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
// УЗЛЫ ПРЯМОЙ ФЕРМЫ
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
// Два плеча образуют настоящий угол 90°.
// ============================================================

private fun corner90Nodes(
    element: AssemblyElement
): List<AssemblyNode> {

    val arm =
        AssemblyGeometryConfig.CONNECTOR_SIZE

    return createNodes(
        element = element,
        localPoints = listOf(
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
// Между первым и вторым плечом 135°.
// ============================================================

private fun corner135Nodes(
    element: AssemblyElement
): List<AssemblyNode> {

    val arm =
        AssemblyGeometryConfig.CONNECTOR_SIZE

    val diagonal =
        (
            arm /
                kotlin.math.sqrt(2f)
        )

    return createNodes(
        element = element,
        localPoints = listOf(
            AssemblyPoint(
                x = -arm,
                y = 0f
            ),
            AssemblyPoint(
                x = diagonal,
                y = -diagonal
            )
        )
    )
}

// ============================================================
// T-СОЕДИНИТЕЛЬ
//
// Три точки подключения.
// ============================================================

private fun tJunctionNodes(
    element: AssemblyElement
): List<AssemblyNode> {

    val arm =
        AssemblyGeometryConfig.CONNECTOR_SIZE

    return createNodes(
        element = element,
        localPoints = listOf(
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
    )
}

// ============================================================
// X-СОЕДИНИТЕЛЬ
//
// Четыре точки подключения.
// ============================================================

private fun xJunctionNodes(
    element: AssemblyElement
): List<AssemblyNode> {

    val arm =
        AssemblyGeometryConfig.CONNECTOR_SIZE

    return createNodes(
        element = element,
        localPoints = listOf(
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
    )
}

// ============================================================
// КУБ
//
// В 2D-конструкторе куб представлен как центральный
// соединительный блок с четырьмя рабочими направлениями.
//
// 0 = слева
// 1 = справа
// 2 = сверху
// 3 = снизу
//
// Позже эту модель можно расширить для 3D.
// ============================================================

private fun cubeNodes(
    element: AssemblyElement
): List<AssemblyNode> {

    val halfSize =
        AssemblyGeometryConfig.CUBE_SIZE / 2f

    return createNodes(
        element = element,
        localPoints = listOf(
            AssemblyPoint(
                x = -halfSize,
                y = 0f
            ),
            AssemblyPoint(
                x = halfSize,
                y = 0f
            ),
            AssemblyPoint(
                x = 0f,
                y = -halfSize
            ),
            AssemblyPoint(
                x = 0f,
                y = halfSize
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
            corner90Nodes(this)

        AssemblyElementType.CORNER_135 ->
            corner135Nodes(this)

        AssemblyElementType.T_JUNCTION ->
            tJunctionNodes(this)

        AssemblyElementType.X_JUNCTION ->
            xJunctionNodes(this)

        AssemblyElementType.CUBE ->
            cubeNodes(this)
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

    val lengthSquared =
        segmentX * segmentX +
            segmentY * segmentY

    if (lengthSquared <= 0.0001f) {

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
        ) / lengthSquared

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
// ПОИСК ЭЛЕМЕНТА ПОД ПАЛЬЦЕМ
//
// Это важная часть исправления выбора старых элементов.
// Проверяем не selectedElement, а ВСЕ элементы.
//
// asReversed() означает:
// если элементы визуально перекрываются,
// выбирается нарисованный последним.
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

                        startX =
                            endpoints.first.x,

                        startY =
                            endpoints.first.y,

                        endX =
                            endpoints.second.x,

                        endY =
                            endpoints.second.y
                    ) <=
                        AssemblyGeometryConfig
                            .SELECTION_DISTANCE
                }

                AssemblyElementType.CUBE -> {

                    val radius =
                        AssemblyGeometryConfig.CUBE_SIZE *
                            0.75f

                    distanceBetween(
                        firstX = x,
                        firstY = y,
                        secondX = element.x,
                        secondY = element.y
                    ) <= radius
                }

                else -> {

                    val radius =
                        AssemblyGeometryConfig.CONNECTOR_SIZE +
                            AssemblyGeometryConfig.SELECTION_DISTANCE

                    distanceBetween(
                        firstX = x,
                        firstY = y,
                        secondX = element.x,
                        secondY = element.y
                    ) <= radius
                }
            }
        }
}

// ============================================================
// ПОИСК SNAP
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
            pixelsPerMeter = pixelsPerMeter
        )

    val otherElements =
        elements.filter {
            it.id != movingElementId
        }

    var bestResult: SnapResult? = null

    movingNodes.forEach { movingNode ->

        otherElements.forEach { targetElement ->

            targetElement
                .getNodes(
                    pixelsPerMeter =
                        pixelsPerMeter
                )
                .forEach { targetNode ->

                    val distance =
                        distanceBetween(
                            firstX = movingNode.x,
                            firstY = movingNode.y,
                            secondX = targetNode.x,
                            secondY = targetNode.y
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

    val connectionExists =
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

    if (connectionExists) {
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

    val snapResult =
        findSnapForElement(
            movingElementId = elementId,
            snapDistance = snapDistance,
            pixelsPerMeter = pixelsPerMeter
        ) ?: return this

    return applySnap(
        snapResult = snapResult,
        pixelsPerMeter = pixelsPerMeter
    )
}
