package com.supersci.calculator.math

enum class UnitCategory {
    LENGTH, MASS, TEMPERATURE, AREA, VOLUME, SPEED, TIME, DATA
}

data class UnitItem(val name: String, val symbol: String, val factorToBase: Double)

class UnitConverter {

    companion object {
        val LENGTH_UNITS = listOf(
            UnitItem("Millimeter", "mm", 0.001),
            UnitItem("Centimeter", "cm", 0.01),
            UnitItem("Meter", "m", 1.0),
            UnitItem("Kilometer", "km", 1000.0),
            UnitItem("Inch", "in", 0.0254),
            UnitItem("Foot", "ft", 0.3048),
            UnitItem("Yard", "yd", 0.9144),
            UnitItem("Mile", "mi", 1609.344)
        )

        val MASS_UNITS = listOf(
            UnitItem("Milligram", "mg", 0.000001),
            UnitItem("Gram", "g", 0.001),
            UnitItem("Kilogram", "kg", 1.0),
            UnitItem("Ounce", "oz", 0.028349523125),
            UnitItem("Pound", "lb", 0.45359237)
        )

        val TEMPERATURE_UNITS = listOf(
            UnitItem("Celsius", "°C", 1.0),
            UnitItem("Fahrenheit", "°F", 1.0),
            UnitItem("Kelvin", "K", 1.0)
        )

        val SPEED_UNITS = listOf(
            UnitItem("Meter/Second", "m/s", 1.0),
            UnitItem("Km/Hour", "km/h", 0.277777778),
            UnitItem("Mile/Hour", "mph", 0.44704),
            UnitItem("Knot", "kn", 0.514444)
        )

        val TIME_UNITS = listOf(
            UnitItem("Millisecond", "ms", 0.001),
            UnitItem("Second", "s", 1.0),
            UnitItem("Minute", "min", 60.0),
            UnitItem("Hour", "h", 3600.0),
            UnitItem("Day", "d", 86400.0)
        )

        val DATA_UNITS = listOf(
            UnitItem("Byte", "B", 1.0),
            UnitItem("Kilobyte", "KB", 1024.0),
            UnitItem("Megabyte", "MB", 1048576.0),
            UnitItem("Gigabyte", "GB", 1073741824.0),
            UnitItem("Terabyte", "TB", 1099511627776.0)
        )
    }

    fun convert(value: Double, from: UnitItem, to: UnitItem, category: UnitCategory): Double {
        if (from == to) return value
        if (category == UnitCategory.TEMPERATURE) {
            return convertTemperature(value, from.symbol, to.symbol)
        }
        val baseVal = value * from.factorToBase
        return baseVal / to.factorToBase
    }

    private fun convertTemperature(value: Double, fromSymbol: String, toSymbol: String): Double {
        val celsius = when (fromSymbol) {
            "°C" -> value
            "°F" -> (value - 32.0) * (5.0 / 9.0)
            "K" -> value - 273.15
            else -> value
        }
        return when (toSymbol) {
            "°C" -> celsius
            "°F" -> (celsius * (9.0 / 5.0)) + 32.0
            "K" -> celsius + 273.15
            else -> celsius
        }
    }
}
