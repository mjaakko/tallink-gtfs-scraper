package xyz.malkki.tallinkgtfsscraper

import xyz.malkki.gtfs.model.Calendar
import xyz.malkki.gtfs.model.CalendarDate
import xyz.malkki.gtfs.model.StopTime
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields

/**
 * Calculates either a single Calendar or a list of CalendarDates depending on whether the list of given dates has a regular weekly pattern
 *
 * @param serviceId Service ID to use
 * @param dates List of dates
 */
fun getCalendarOrCalendarDates(serviceId: String, dates: List<LocalDate>): Pair<Calendar?, List<CalendarDate>?> {
    val sortedDates = dates.toSortedSet()

    if (sortedDates.first().until(sortedDates.last()).days > 14) {
        val firstWholeWeekFirstDay = sortedDates.first().plusWeeks(1).with(ChronoField.DAY_OF_WEEK, 1)
        val lastWholeWeekLastDay = sortedDates.last().minusWeeks(1).with(ChronoField.DAY_OF_WEEK, 7)

        val daysOfWeekByWeek = sortedDates.subSet(firstWholeWeekFirstDay, lastWholeWeekLastDay.plusDays(1))
            .groupBy { it.get(WeekFields.ISO.weekBasedYear()) to it.get(WeekFields.ISO.weekOfWeekBasedYear()) }
            .mapValues { (_, days) -> days.map { it.dayOfWeek }.toSet() }

        if (daysOfWeekByWeek.values.distinct().count() == 1) {
            val daysOfWeek = daysOfWeekByWeek.values.first()

            val firstWeek = sortedDates.headSet(firstWholeWeekFirstDay)
            val lastWeek = sortedDates.tailSet(lastWholeWeekLastDay.plusDays(1))

            if (firstWeek.map { it.dayOfWeek }.all { it in daysOfWeek } && lastWeek.map { it.dayOfWeek }.all { it in daysOfWeek }) {
                val calendar = Calendar(
                    serviceId,
                    DayOfWeek.MONDAY in daysOfWeek,
                    DayOfWeek.TUESDAY in daysOfWeek,
                    DayOfWeek.WEDNESDAY in daysOfWeek,
                    DayOfWeek.THURSDAY in daysOfWeek,
                    DayOfWeek.FRIDAY in daysOfWeek,
                    DayOfWeek.SATURDAY in daysOfWeek,
                    DayOfWeek.SUNDAY in daysOfWeek,
                    sortedDates.first(),
                    sortedDates.last()
                )

                return calendar to null
            }
        }
    }

    return null to sortedDates.map { CalendarDate(serviceId, it, CalendarDate.EXCEPTION_TYPE_ADDED) }
}

fun getStopTimes(tripId: String, baseTimezone: ZoneId, tripSet: TripSet): List<StopTime> {
    val stops = mutableListOf<Triple<String, Long, Long>>()

    val tripStart = tripSet.trips.first().departureTime
    val tripStartSeconds = tripStart.withZoneSameInstant(baseTimezone).toLocalTime().toSecondOfDay().toLong()

    stops.add(Triple(tripSet.trips.first().pierFrom, tripStartSeconds, tripStartSeconds))

    tripSet.trips.mapIndexed { index, trip ->
        val next = tripSet.trips.getOrNull(index+1)

        stops.add(Triple(
            trip.pierTo,
            tripStartSeconds + tripStart.until(trip.arrivalTime, ChronoUnit.SECONDS),
            tripStartSeconds + tripStart.until(next?.departureTime ?: trip.arrivalTime, ChronoUnit.SECONDS)
        ))
    }

    return stops.mapIndexed { index, stop ->
        StopTime(
            tripId = tripId,
            arrivalTime = stop.second.toInt(),
            departureTime = stop.third.toInt(),
            stopId = stop.first,
            stopSequence = index + 1,
            timepoint = true
        )
    }
}