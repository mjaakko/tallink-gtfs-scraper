package xyz.malkki.tallinkgtfsscraper.tallinkapi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import xyz.malkki.tallinkgtfsscraper.constants.pierTimezones
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@JsonIgnoreProperties(ignoreUnknown = true)
data class Trip(
    val arrivalIsoDate: LocalDateTime, //Arrival time and departure time are always LOCAL time
    val departureIsoDate: LocalDateTime,
    val shipCode: String,
    val cityFrom: String,
    val cityTo: String,
    val pierFrom: String,
    val pierTo: String
) {
    companion object {
        fun List<Trip>.isContinuousVoyage(): Boolean {
            var prevTo = first().pierTo
            var prevArrival = first().arrivalTime

            forEachIndexed { index, trip ->
                if (index > 0) {
                    if (trip.pierFrom == prevTo && trip.departureTime >= prevArrival) {
                        prevTo = trip.pierTo
                        prevArrival = trip.arrivalTime
                    } else {
                        return false
                    }
                }
            }

            return map { it.shipCode }.distinct().size == 1
        }

        fun List<Trip>.createVoyage(): List<Trip> {
            if (size > 1) {
                subList(1, size - 1).forEach {
                    if (!first().isSameVoyage(it)) {
                        throw IllegalArgumentException("Trips must be from same voyage to build a trip set")
                    }
                }
            }

            val setOfTrips = toMutableSet()

            val sorted = mutableListOf<Trip>()
            val first = setOfTrips.minWithOrNull { first, second ->
                val byDepartureTime = first.departureTime.compareTo(second.departureTime)

                if (byDepartureTime == 0) {
                    first.departureTime
                        .until(first.arrivalTime, ChronoUnit.MINUTES)
                        .compareTo(
                            second.departureTime
                                .until(second.arrivalTime, ChronoUnit.MINUTES)
                        )
                } else {
                    byDepartureTime
                }
            }!!
            sorted.add(first)
            setOfTrips.remove(first)

            while (sorted.size < size - 1) {
                val last = sorted.last()

                val nextTrip = setOfTrips.filter { it.departureTime >= last.arrivalTime }.minByOrNull { last.arrivalTime.until(it.departureTime, ChronoUnit.MINUTES) }!!

                sorted.add(nextTrip)
                setOfTrips.remove(nextTrip)
            }

            return sorted
        }
    }

    val arrivalTime: ZonedDateTime = arrivalIsoDate.atZone(pierTimezones[pierTo])
    val departureTime: ZonedDateTime = departureIsoDate.atZone(pierTimezones[pierFrom])

    /**
     * Returns true if these trips are part of the same voyage (e.g. Tallinn - Stockholm and Tallinn - Mariehamn)
     */
    fun isSameVoyage(other: Trip): Boolean = ((departureIsoDate == other.departureIsoDate && pierFrom == other.pierFrom) || (arrivalIsoDate == other.arrivalIsoDate && pierTo == other.pierTo))  && shipCode == other.shipCode
}