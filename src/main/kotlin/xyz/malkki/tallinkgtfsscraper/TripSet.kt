package xyz.malkki.tallinkgtfsscraper

import org.apache.commons.codec.digest.MurmurHash3
import xyz.malkki.tallinkgtfsscraper.tallinkapi.model.Trip
import xyz.malkki.tallinkgtfsscraper.tallinkapi.model.Trip.Companion.createVoyage
import java.nio.charset.StandardCharsets
import java.time.LocalDate

/**
 * Class describing set of trips making up a voyage
 */
class TripSet private constructor(val trips: List<Trip>) {
    companion object {
        fun buildFromTrips(trips: List<Trip>): TripSet {
            return TripSet(trips.createVoyage())
        }
    }

    fun getDate(): LocalDate = trips.first().departureTime.toLocalDate()

    fun getCities(): List<String> {
        return listOf(trips.first().cityFrom) + trips.map { it.cityTo }
    }

    fun getPiers(): List<String> {
        return listOf(trips.first().pierFrom) + trips.map { it.pierTo }
    }

    fun getShip(): String {
        return trips.first().shipCode
    }

    /**
     * Gets trip identity which is generated from pier names and arrival and departure times
     */
    fun getIdentity(): String {
        val hasher = MurmurHash3.IncrementalHash32x86()

        hasher.start(0)
        trips.forEach {
            hasher.add(it.pierFrom.toByteArray(StandardCharsets.UTF_8))
            hasher.add(it.pierTo.toByteArray(StandardCharsets.UTF_8))
            hasher.add(it.departureIsoDate.toLocalTime().toSecondOfDay().toBigInteger().toByteArray())
            hasher.add(it.arrivalIsoDate.toLocalTime().toSecondOfDay().toBigInteger().toByteArray())
        }

        return hasher.end().toUInt().toString(16)
    }

    private fun MurmurHash3.IncrementalHash32x86.add(bytes: ByteArray) = add(bytes, 0, bytes.size)
}
