package xyz.malkki.tallinkgtfsscraper.tallinkapi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Timetables(
    val trips: Trips
)
