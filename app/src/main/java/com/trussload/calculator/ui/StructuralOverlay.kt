package com.trussload.calculator.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.trussload.calculator.assembly.AssemblyGeometryConfig
import com.trussload.calculator.calculation.SupportType
import com.trussload.calculator.calculation.supportType
import com.trussload.calculator.models.StructuralModel
import kotlin.math.sqrt

// ============================================================
// ОТОБРАЖЕНИЕ РАСЧЁТНОЙ СХЕМЫ ПОВЕРХ СБОРКИ
//
// StructuralModel:
//     координаты узлов хранятся в метрах
//
// Canvas:
//     координаты используются в пикселях
//
// Поэтому координаты переводятся через
// AssemblyGeometryConfig.PIXELS_PER_METER.
// ============================================================

fun DrawScope.drawStructuralOverlay(
    model: StructuralModel
) {

    // --------------------------------------------------------
    // СНАЧАЛА РИСУЕМ РАСЧЁТНЫЕ СТЕРЖНИ
    // --------------------------------------------------------

    model.members.forEach { member ->

        val startNode =
            model.findNode(
                member.startNodeId
            )
                ?: return@forEach

        val endNode =
            model.findNode(
                member.endNodeId
            )
                ?: return@forEach

        val start =
            structuralPointToCanvas(
                xMeters = startNode.x,
                yMeters = startNode.y
            )

        val end =
            structuralPointToCanvas(
                xMeters = endNode.x,
                yMeters = endNode.y
            )

        drawLine(
            color = Color(
                0x6639A844
            ),
            start = start,
            end = end,
            strokeWidth = 3f
        )
    }

    // --------------------------------------------------------
    // ПОТОМ РИСУЕМ УЗЛЫ И ОПОРЫ
    // --------------------------------------------------------

    model.nodes.forEach { node ->

        val center =
            structuralPointToCanvas(
                xMeters = node.x,
                yMeters = node.y
            )

        drawStructuralNode(
            center = center
        )

        when (node.supportType) {

            SupportType.FREE -> {
                // Ничего не рисуем.
            }

            SupportType.X_ONLY -> {

                drawSupportX(
                    center = center
                )
            }

            SupportType.Y_ONLY -> {

                drawSupportY(
                    center = center
                )
            }

            SupportType.PINNED -> {

                drawPinnedSupport(
                    center = center
                )
            }
        }
    }
}

// ============================================================
// ПЕРЕВОД КООРДИНАТ РАСЧЁТНОЙ МОДЕЛИ В CANVAS
// ============================================================

private fun structuralPointToCanvas(
    xMeters: Double,
    yMeters: Double
): Offset {

    val scale =
        AssemblyGeometryConfig
            .PIXELS_PER_METER
            .toDouble()

    return Offset(
        x =
            (
                xMeters *
                    scale
                ).toFloat(),

        y =
            (
                yMeters *
                    scale
                ).toFloat()
    )
}

// ============================================================
// РАСЧЁТНЫЙ УЗЕЛ
// ============================================================

private fun DrawScope.drawStructuralNode(
    center: Offset
) {

    // Белая подложка.

    drawCircle(
        color = Color.White,
        radius = 9f,
        center = center
    )

    // Контур узла.

    drawCircle(
        color = Color(
            0xFF6A1B9A
        ),
        radius = 7f,
        center = center,
        style = Stroke(
            width = 3f
        )
    )

    // Центральная точка.

    drawCircle(
        color = Color(
            0xFF6A1B9A
        ),
        radius = 2.5f,
        center = center
    )
}

// ============================================================
// ОПОРА X
//
// Ограничение перемещения по X.
// Показываем вертикальную линию рядом с узлом.
// ============================================================

private fun DrawScope.drawSupportX(
    center: Offset
) {

    val supportX =
        center.x - 15f

    drawLine(
        color = Color(
            0xFF00897B
        ),
        start =
            Offset(
                x = supportX,
                y = center.y - 15f
            ),
        end =
            Offset(
                x = supportX,
                y = center.y + 15f
            ),
        strokeWidth = 5f
    )

    // Штриховка.

    for (index in -2..2) {

        val y =
            center.y +
                index * 6f

        drawLine(
            color = Color(
                0xFF00897B
            ),
            start =
                Offset(
                    x = supportX - 8f,
                    y = y + 5f
                ),
            end =
                Offset(
                    x = supportX,
                    y = y
                ),
            strokeWidth = 2f
        )
    }
}

// ============================================================
// ОПОРА Y
//
// Ограничение перемещения по Y.
// Показываем треугольную опору под узлом.
// ============================================================

private fun DrawScope.drawSupportY(
    center: Offset
) {

    val top =
        Offset(
            x = center.x,
            y = center.y + 8f
        )

    val left =
        Offset(
            x = center.x - 13f,
            y = center.y + 25f
        )

    val right =
        Offset(
            x = center.x + 13f,
            y = center.y + 25f
        )

    drawLine(
        color = Color(
            0xFF00897B
        ),
        start = top,
        end = left,
        strokeWidth = 3f
    )

    drawLine(
        color = Color(
            0xFF00897B
        ),
        start = top,
        end = right,
        strokeWidth = 3f
    )

    drawLine(
        color = Color(
            0xFF00897B
        ),
        start = left,
        end = right,
        strokeWidth = 3f
    )

    drawSupportGround(
        center =
            Offset(
                x = center.x,
                y = center.y + 28f
            )
    )
}

// ============================================================
// ШАРНИРНАЯ ОПОРА X + Y
// ============================================================

private fun DrawScope.drawPinnedSupport(
    center: Offset
) {

    val top =
        Offset(
            x = center.x,
            y = center.y + 8f
        )

    val left =
        Offset(
            x = center.x - 15f,
            y = center.y + 27f
        )

    val right =
        Offset(
            x = center.x + 15f,
            y = center.y + 27f
        )

    // Треугольник.

    drawLine(
        color = Color(
            0xFF1565C0
        ),
        start = top,
        end = left,
        strokeWidth = 4f
    )

    drawLine(
        color = Color(
            0xFF1565C0
        ),
        start = top,
        end = right,
        strokeWidth = 4f
    )

    drawLine(
        color = Color(
            0xFF1565C0
        ),
        start = left,
        end = right,
        strokeWidth = 4f
    )

    // Шарнир.

    drawCircle(
        color = Color.White,
        radius = 5f,
        center = top
    )

    drawCircle(
        color = Color(
            0xFF1565C0
        ),
        radius = 5f,
        center = top,
        style = Stroke(
            width = 2f
        )
    )

    // Основание.

    drawSupportGround(
        center =
            Offset(
                x = center.x,
                y = center.y + 31f
            )
    )
}

// ============================================================
// ОСНОВАНИЕ ОПОРЫ
// ============================================================

private fun DrawScope.drawSupportGround(
    center: Offset
) {

    val halfWidth =
        20f

    drawLine(
        color = Color(
            0xFF455A64
        ),
        start =
            Offset(
                x = center.x - halfWidth,
                y = center.y
            ),
        end =
            Offset(
                x = center.x + halfWidth,
                y = center.y
            ),
        strokeWidth = 3f
    )

    // Штриховка основания.

    for (index in -3..3) {

        val x =
            center.x +
                index * 6f

        drawLine(
            color = Color(
                0xFF455A64
            ),
            start =
                Offset(
                    x = x,
                    y = center.y
                ),
            end =
                Offset(
                    x = x - 6f,
                    y = center.y + 7f
                ),
            strokeWidth = 2f
        )
    }
}

// ============================================================
// ПОИСК БЛИЖАЙШЕГО РАСЧЁТНОГО УЗЛА
//
// Нужен следующим этапом:
// пользователь нажимает на узел и назначает ему опору.
// ============================================================

fun StructuralModel.findStructuralNodeAtCanvasPosition(
    x: Float,
    y: Float,
    hitRadiusPx: Float = 24f
): String? {

    val scale =
        AssemblyGeometryConfig
            .PIXELS_PER_METER
            .toDouble()

    var nearestNodeId:
        String? = null

    var nearestDistance =
        Double.MAX_VALUE

    nodes.forEach { node ->

        val nodeX =
            node.x *
                scale

        val nodeY =
            node.y *
                scale

        val dx =
            nodeX -
                x.toDouble()

        val dy =
            nodeY -
                y.toDouble()

        val distance =
            sqrt(
                dx * dx +
                    dy * dy
            )

        if (
            distance <=
                hitRadiusPx.toDouble() &&
            distance <
                nearestDistance
        ) {

            nearestDistance =
                distance

            nearestNodeId =
                node.id
        }
    }

    return nearestNodeId
}
