package xyz.malkki.tallinkgtfsscraper.tallinkapi.model

import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@JsonIgnoreProperties(ignoreUnknown = true)
class Trips {
    @JsonIgnore
    private val _tripsByDate = mutableMapOf<LocalDate, TripsForDate>()

    @get:JsonIgnore
    val tripsByDate: Map<LocalDate, TripsForDate>
        get() = _tripsByDate

    @JsonAnySetter
    fun addTripsForDate(key: String, value: TripsForDate) {
        val date = LocalDate.parse(key, DateTimeFormatter.ISO_LOCAL_DATE)

        _tripsByDate[date] = value
    }
}
