package com.trussload.calculator.calculation

// ============================================================
// МЕХАНИЧЕСКИЕ ХАРАКТЕРИСТИКИ ДЛЯ РАСЧЁТНОГО ЯДРА
//
// Внутренние единицы:
//
// Площадь сечения:
//     м²
//
// Модуль упругости:
//     кН/м²
//
// Напряжение:
//     кН/м²
//
// Нагрузки:
//     кН
//
// Геометрия:
//     м
// ============================================================


// ============================================================
// МАТЕРИАЛ
// ============================================================

data class StructuralMaterial(

    val id: String,

    val name: String,

    // Модуль упругости, кН/м²
    val elasticModulusKnPerM2: Double,

    // Плотность, кг/м³
    val densityKgPerM3: Double? = null
) {

    init {

        require(
            id.isNotBlank()
        ) {
            "Material id must not be blank."
        }

        require(
            name.isNotBlank()
        ) {
            "Material name must not be blank."
        }

        require(
            elasticModulusKnPerM2.isFinite() &&
                elasticModulusKnPerM2 > 0.0
        ) {
            "Elastic modulus must be positive."
        }

        if (
            densityKgPerM3 != null
        ) {

            require(
                densityKgPerM3.isFinite() &&
                    densityKgPerM3 > 0.0
            ) {
                "Density must be positive."
            }
        }
    }
}


// ============================================================
// СЕЧЕНИЕ
// ============================================================

data class StructuralSection(

    val id: String,

    val name: String,

    // Площадь сечения, м²
    val areaM2: Double
) {

    init {

        require(
            id.isNotBlank()
        ) {
            "Section id must not be blank."
        }

        require(
            name.isNotBlank()
        ) {
            "Section name must not be blank."
        }

        require(
            areaM2.isFinite() &&
                areaM2 > 0.0
        ) {
            "Section area must be positive."
        }
    }
}


// ============================================================
// НАБОР ХАРАКТЕРИСТИК СТЕРЖНЯ
// ============================================================

data class StructuralMemberProperties(

    val material:
        StructuralMaterial,

    val section:
        StructuralSection
) {

    // --------------------------------------------------------
    // МОДУЛЬ УПРУГОСТИ
    // --------------------------------------------------------

    val elasticModulusKnPerM2: Double
        get() =
            material
                .elasticModulusKnPerM2


    // --------------------------------------------------------
    // ПЛОЩАДЬ СЕЧЕНИЯ
    // --------------------------------------------------------

    val areaM2: Double
        get() =
            section
                .areaM2


    // --------------------------------------------------------
    // ПРОДОЛЬНАЯ ЖЁСТКОСТЬ EA
    //
    // E = кН/м²
    // A = м²
    //
    // EA = кН
    // --------------------------------------------------------

    val axialRigidityKn: Double
        get() =
            elasticModulusKnPerM2 *
                areaM2


    // --------------------------------------------------------
    // МАССА НА ПОГОННЫЙ МЕТР
    //
    // кг/м
    // --------------------------------------------------------

    val massPerMeterKg: Double?
        get() {

            val density =
                material
                    .densityKgPerM3
                    ?: return null

            return density *
                areaM2
        }
}


// ============================================================
// КОНВЕРТАЦИЯ ПЛОЩАДИ
// ============================================================

fun squareMillimetersToSquareMeters(
    valueMm2: Double
): Double {

    require(
        valueMm2.isFinite() &&
            valueMm2 > 0.0
    ) {
        "Area in mm² must be positive."
    }

    return valueMm2 *
        1e-6
}


// ============================================================
// КОНВЕРТАЦИЯ МОДУЛЯ УПРУГОСТИ
//
// 1 MPa = 1000 kN/m²
// ============================================================

fun megaPascalsToKnPerSquareMeter(
    valueMpa: Double
): Double {

    require(
        valueMpa.isFinite() &&
            valueMpa > 0.0
    ) {
        "Elastic modulus in MPa must be positive."
    }

    return valueMpa *
        1000.0
}


// ============================================================
// КОНВЕРТАЦИЯ GPa -> kN/m²
//
// 1 GPa = 1 000 000 kN/m²
// ============================================================

fun gigaPascalsToKnPerSquareMeter(
    valueGpa: Double
): Double {

    require(
        valueGpa.isFinite() &&
            valueGpa > 0.0
    ) {
        "Elastic modulus in GPa must be positive."
    }

    return valueGpa *
        1_000_000.0
}


// ============================================================
// РАСЧЁТ ПЛОЩАДИ КРУГЛОЙ ТРУБЫ
//
// outerDiameterMm — наружный диаметр
// wallThicknessMm — толщина стенки
//
// Результат:
//     м²
// ============================================================

fun circularTubeAreaM2(
    outerDiameterMm: Double,
    wallThicknessMm: Double
): Double {

    require(
        outerDiameterMm.isFinite() &&
            outerDiameterMm > 0.0
    ) {
        "Outer diameter must be positive."
    }

    require(
        wallThicknessMm.isFinite() &&
            wallThicknessMm > 0.0
    ) {
        "Wall thickness must be positive."
    }

    require(
        wallThicknessMm * 2.0 <
            outerDiameterMm
    ) {
        "Wall thickness is too large."
    }

    val innerDiameterMm =
        outerDiameterMm -
            wallThicknessMm * 2.0

    val outerAreaMm2 =
        Math.PI *
            outerDiameterMm *
            outerDiameterMm /
            4.0

    val innerAreaMm2 =
        Math.PI *
            innerDiameterMm *
            innerDiameterMm /
            4.0

    val areaMm2 =
        outerAreaMm2 -
            innerAreaMm2

    return squareMillimetersToSquareMeters(
        areaMm2
    )
}


// ============================================================
// ПРОВЕРКА ХАРАКТЕРИСТИК
// ============================================================

fun StructuralMemberProperties.validate():
    List<String> {

    val errors =
        mutableListOf<String>()

    if (
        !areaM2.isFinite() ||
        areaM2 <= 0.0
    ) {

        errors +=
            "Некорректная площадь сечения."
    }

    if (
        !elasticModulusKnPerM2.isFinite() ||
        elasticModulusKnPerM2 <= 0.0
    ) {

        errors +=
            "Некорректный модуль упругости."
    }

    if (
        !axialRigidityKn.isFinite() ||
        axialRigidityKn <= 0.0
    ) {

        errors +=
            "Некорректная продольная жёсткость EA."
    }

    return errors
    }
