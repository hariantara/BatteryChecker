package xyz.batterychecker.database

data class BatteryAggregateData(
    val timePoint: String,  // hour or day depending on aggregation
    val avgLevel: Float,
    val avgTemp: Float,
    val maxLevel: Int,
    val minLevel: Int
) 