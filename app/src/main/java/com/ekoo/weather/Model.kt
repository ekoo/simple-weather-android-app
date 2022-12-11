package com.ekoo.weather

import com.google.gson.annotations.SerializedName

data class Location(
    @SerializedName("Key")
    val key: String,
    @SerializedName("LocalizedName")
    val localizedName: String
)

data class OneHourForecast(
    @SerializedName("DateTime")
    val dateTime: String,
    @SerializedName("Temperature")
    val temperature: Temperature,
    @SerializedName("IconPhrase")
    val iconPhase: String

) {
    data class Temperature(
        @SerializedName("Value")
        val value: Double
    )
}

data class OneDayForecast(
    @SerializedName("DailyForecasts")
    val dailyForecasts: List<DailyForecast>,
    @SerializedName("Headline")
    val headline: Headline
) {
    data class DailyForecast(
        @SerializedName("Temperature")
        val temperature: Temperature
    ) {
        data class Temperature(
            @SerializedName("Maximum")
            val maximum: Maximum,
            @SerializedName("Minimum")
            val minimum: Minimum
        ) {
            data class Maximum(
                @SerializedName("Value")
                val value: Int
            )

            data class Minimum(
                @SerializedName("Value")
                val value: Int
            )
        }
    }

    data class Headline(
        @SerializedName("Text")
        val text: String
    )
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}