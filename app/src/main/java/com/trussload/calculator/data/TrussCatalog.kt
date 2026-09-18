package com.trussload.calculator.data

import com.trussload.calculator.models.TrussCatalogItem

object TrussCatalog {

    val trusses: List<TrussCatalogItem> = listOf(

        TrussCatalogItem(
            manufacturer = "Generic",
            series = "Triangle",
            model = "TRI-290",
            heightMm = 290,
            widthMm = 290,
            weightKgPerMeter = 0.0,
            material = "EN AW-6082 T6",
            description = "Треугольная алюминиевая ферма 290 мм",
            source = ""
        ),

        TrussCatalogItem(
            manufacturer = "Generic",
            series = "Square",
            model = "BOX-290",
            heightMm = 290,
            widthMm = 290,
            weightKgPerMeter = 0.0,
            material = "EN AW-6082 T6",
            description = "Квадратная алюминиевая ферма 290 мм",
            source = ""
        ),

        TrussCatalogItem(
            manufacturer = "Generic",
            series = "Square",
            model = "BOX-400",
            heightMm = 400,
            widthMm = 400,
            weightKgPerMeter = 0.0,
            material = "EN AW-6082 T6",
            description = "Квадратная алюминиевая ферма 400 мм",
            source = ""
        ),

        TrussCatalogItem(
            manufacturer = "Generic",
            series = "Heavy Duty",
            model = "BOX-520",
            heightMm = 520,
            widthMm = 520,
            weightKgPerMeter = 0.0,
            material = "EN AW-6082 T6",
            description = "Квадратная ферма тяжёлой серии 520 мм",
            source = ""
        )
    )

    fun manufacturers(): List<String> {
        return trusses
            .map { it.manufacturer }
            .distinct()
            .sorted()
    }

    fun seriesForManufacturer(
        manufacturer: String
    ): List<String> {
        return trusses
            .filter { it.manufacturer == manufacturer }
            .map { it.series }
            .distinct()
            .sorted()
    }

    fun modelsForSeries(
        manufacturer: String,
        series: String
    ): List<TrussCatalogItem> {
        return trusses.filter {
            it.manufacturer == manufacturer &&
            it.series == series
        }
    }

    fun findByModel(
        model: String
    ): TrussCatalogItem? {
        return trusses.find {
            it.model == model
        }
    }
}
