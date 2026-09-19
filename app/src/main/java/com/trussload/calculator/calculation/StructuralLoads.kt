package com.trussload.calculator.calculation

import com.trussload.calculator.models.StructuralLoad
import com.trussload.calculator.models.StructuralModel
import java.util.UUID
import kotlin.math.abs

// ============================================================
// ЕДИНИЦЫ СИЛЫ
//
// ВАЖНО:
//
// Внутри расчётного ядра ВСЕ силы хранятся в кН.
//
// Пользователь может вводить:
// - кН
// - кг (кгс)
//
// KG здесь означает килограмм-силу, а не массу.
// ============================================================

enum class ForceUnit(
    val title: String,
    val symbol: String
) {

    KN(
        title = "Килоньютон",
        symbol = "кН"
    ),

    KG(
        title = "Килограмм-сила",
        symbol = "кг"
    )
}

// ============================================================
// НАПРАВЛЕНИЕ НАГРУЗКИ
// ============================================================

enum class LoadDirection(
    val title: String
) {

    X_POSITIVE(
        title = "→ X"
    ),

    X_NEGATIVE(
        title = "← X"
    ),

    Y_POSITIVE(
        title = "↓ Y"
    ),

    Y_NEGATIVE(
        title = "↑ Y"
    )
}

// ============================================================
// КОНВЕРТЕР ЕДИНИЦ СИЛЫ
// ============================================================

object ForceUnitConverter {

    /*
     * Стандартное ускорение свободного падения:
     *
     * 1 кгс = 9.80665 Н
     * 1 кгс = 0.00980665 кН
     *
     * 1 кН ≈ 101.971621 кгс
     */

    private const val KG_FORCE_TO_KN =
        0.00980665

    // --------------------------------------------------------
    // ЛЮБАЯ ПОЛЬЗОВАТЕЛЬСКАЯ ЕДИНИЦА -> кН
    // --------------------------------------------------------

    fun toKn(
        value: Double,
        unit: ForceUnit
    ): Double {

        return when (unit) {

            ForceUnit.KN ->
                value

            ForceUnit.KG ->
                value *
                    KG_FORCE_TO_KN
        }
    }

    // --------------------------------------------------------
    // кН -> ВЫБРАННАЯ ПОЛЬЗОВАТЕЛЕМ ЕДИНИЦА
    // --------------------------------------------------------

    fun fromKn(
        valueKn: Double,
        unit: ForceUnit
    ): Double {

        return when (unit) {

            ForceUnit.KN ->
                valueKn

            ForceUnit.KG ->
                valueKn /
                    KG_FORCE_TO_KN
        }
    }

    // --------------------------------------------------------
    // ПЕРЕВОД МЕЖДУ ЛЮБЫМИ ПОДДЕРЖИВАЕМЫМИ ЕДИНИЦАМИ
    // --------------------------------------------------------

    fun convert(
        value: Double,
        from: ForceUnit,
        to: ForceUnit
    ): Double {

        if (from == to) {
            return value
        }

        val valueKn =
            toKn(
                value = value,
                unit = from
            )

        return fromKn(
            valueKn = valueKn,
            unit = to
        )
    }
}

// ============================================================
// СОЗДАНИЕ СОСРЕДОТОЧЕННОЙ НАГРУЗКИ
//
// Пользователь вводит положительное значение.
//
// Знак определяется направлением.
// ============================================================

fun createPointLoad(
    nodeId: String,
    value: Double,
    unit: ForceUnit,
    direction: LoadDirection
): StructuralLoad {

    require(
        nodeId.isNotBlank()
    ) {
        "ID узла не может быть пустым."
    }

    require(
        value.isFinite()
    ) {
        "Нагрузка должна быть конечным числом."
    }

    require(
        value >= 0.0
    ) {
        "Величина нагрузки не может быть отрицательной. Используйте направление нагрузки."
    }

    val forceKn =
        ForceUnitConverter.toKn(
            value = value,
            unit = unit
        )

    val forceXKn: Double
    val forceYKn: Double

    when (direction) {

        LoadDirection.X_POSITIVE -> {

            forceXKn =
                forceKn

            forceYKn =
                0.0
        }

        LoadDirection.X_NEGATIVE -> {

            forceXKn =
                -forceKn

            forceYKn =
                0.0
        }

        LoadDirection.Y_POSITIVE -> {

            forceXKn =
                0.0

            forceYKn =
                forceKn
        }

        LoadDirection.Y_NEGATIVE -> {

            forceXKn =
                0.0

            forceYKn =
                -forceKn
        }
    }

    return StructuralLoad(
        id =
            UUID
                .randomUUID()
                .toString(),

        nodeId =
            nodeId,

        forceXKn =
            forceXKn,

        forceYKn =
            forceYKn
    )
}

// ============================================================
// ДОБАВЛЕНИЕ НАГРУЗКИ В STRUCTURAL MODEL
//
// StructuralModel остаётся immutable.
// Возвращается новая модель.
// ============================================================

fun StructuralModel.addPointLoad(
    nodeId: String,
    value: Double,
    unit: ForceUnit,
    direction: LoadDirection
): StructuralModel {

    require(
        findNode(nodeId) != null
    ) {
        "Невозможно добавить нагрузку: узел $nodeId не найден."
    }

    val load =
        createPointLoad(
            nodeId = nodeId,
            value = value,
            unit = unit,
            direction = direction
        )

    return copy(
        loads =
            loads + load
    )
}

// ============================================================
// УДАЛЕНИЕ НАГРУЗКИ
// ============================================================

fun StructuralModel.removeLoad(
    loadId: String
): StructuralModel {

    return copy(
        loads =
            loads.filterNot {
                it.id == loadId
            }
    )
}

// ============================================================
// УДАЛЕНИЕ ВСЕХ НАГРУЗОК С УЗЛА
// ============================================================

fun StructuralModel.removeLoadsFromNode(
    nodeId: String
): StructuralModel {

    return copy(
        loads =
            loads.filterNot {
                it.nodeId == nodeId
            }
    )
}

// ============================================================
// УДАЛЕНИЕ ВСЕХ НАГРУЗОК
// ============================================================

fun StructuralModel.clearLoads():
    StructuralModel {

    return copy(
        loads = emptyList()
    )
}

// ============================================================
// СУММАРНАЯ НАГРУЗКА НА УЗЕЛ ПО X
//
// Результат всегда в кН.
// ============================================================

fun StructuralModel.totalForceXKn(
    nodeId: String
): Double {

    return loadsForNode(
        nodeId = nodeId
    ).sumOf {
        it.forceXKn
    }
}

// ============================================================
// СУММАРНАЯ НАГРУЗКА НА УЗЕЛ ПО Y
//
// Результат всегда в кН.
// ============================================================

fun StructuralModel.totalForceYKn(
    nodeId: String
): Double {

    return loadsForNode(
        nodeId = nodeId
    ).sumOf {
        it.forceYKn
    }
}

// ============================================================
// СУММАРНАЯ НАГРУЗКА НА ВСЮ КОНСТРУКЦИЮ ПО X
// ============================================================

val StructuralModel.totalForceXKn: Double
    get() =
        loads.sumOf {
            it.forceXKn
        }

// ============================================================
// СУММАРНАЯ НАГРУЗКА НА ВСЮ КОНСТРУКЦИЮ ПО Y
// ============================================================

val StructuralModel.totalForceYKn: Double
    get() =
        loads.sumOf {
            it.forceYKn
        }

// ============================================================
// ВЕЛИЧИНА НАГРУЗКИ ДЛЯ ОТОБРАЖЕНИЯ
//
// Например:
//
// forceYKn = -9.80665
//
// при KN:
// 9.80665
//
// при KG:
// 1000
//
// Направление при этом определяется знаком исходной силы.
// ============================================================

fun StructuralLoad.displayForceX(
    unit: ForceUnit
): Double {

    return ForceUnitConverter.fromKn(
        valueKn =
            abs(forceXKn),

        unit = unit
    )
}

fun StructuralLoad.displayForceY(
    unit: ForceUnit
): Double {

    return ForceUnitConverter.fromKn(
        valueKn =
            abs(forceYKn),

        unit = unit
    )
}

// ============================================================
// ПРОВЕРКА НАГРУЗОК
// ============================================================

fun StructuralModel.validateLoads():
    List<String> {

    val errors =
        mutableListOf<String>()

    loads.forEach { load ->

        if (
            load.nodeId.isBlank()
        ) {

            errors +=
                "Нагрузка ${load.id} не содержит ID узла."

            return@forEach
        }

        if (
            findNode(
                load.nodeId
            ) == null
        ) {

            errors +=
                "Нагрузка ${load.id} привязана к несуществующему узлу ${load.nodeId}."
        }

        if (
            !load.forceXKn.isFinite()
        ) {

            errors +=
                "Нагрузка ${load.id} содержит некорректную силу по X."
        }

        if (
            !load.forceYKn.isFinite()
        ) {

            errors +=
                "Нагрузка ${load.id} содержит некорректную силу по Y."
        }

        if (
            load.forceXKn == 0.0 &&
            load.forceYKn == 0.0
        ) {

            errors +=
                "Нагрузка ${load.id} имеет нулевое значение."
        }
    }

    return errors
}
