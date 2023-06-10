package xyz.malkki.tallinkgtfsscraper.tallinkapi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TripsForDate(
    val outwards: List<Trip>,
    val returns: List<Trip>
)
