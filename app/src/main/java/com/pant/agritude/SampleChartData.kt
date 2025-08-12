package com.pant.agritude.data


import com.patrykandpatryk.vico.core.entry.FloatEntry

object SampleChartData {


    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")


    fun getSampleData(basePrice: Float): List<FloatEntry> {
        val prices = listOf(
            basePrice * 0.9f,
            basePrice * 1.1f,
            basePrice * 1.05f,
            basePrice * 0.95f,
            basePrice * 1.2f,
            basePrice * 1.15f
        )

        return prices.mapIndexed { index, price ->
            FloatEntry(x = index.toFloat(), y = price)
        }
    }
}
