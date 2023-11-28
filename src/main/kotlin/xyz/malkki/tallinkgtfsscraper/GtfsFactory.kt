package xyz.malkki.tallinkgtfsscraper

import xyz.malkki.gtfs.model.Calendar
import xyz.malkki.gtfs.model.CalendarDate
import xyz.malkki.gtfs.model.StopTime
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Creates a GTFS calendar and/or a list of GTFS calendar dates representing the list of dates when the service runs
 *
 * @param serviceId Service ID to use
 * @param dates List of dates
 */
fun getCalendarAndCalendarDates(serviceId: String, dates: List<LocalDate>): Pair<Calendar?, List<CalendarDate>> {
    val sortedDates = dates.toSortedSet()

    val daysOfWeek = sortedDates.map { it.dayOfWeek }.toSet()

    val exceptions = mutableListOf<CalendarDate>()

    val from = sortedDates.first()
    val to = sortedDates.last()

    var date = from
    while (date <= to) {
        if (date.dayOfWeek !in daysOfWeek) {
            date = date.plusDays(1)
            continue
        }

        if (date !in sortedDates) {
            exceptions += CalendarDate(serviceId, date, CalendarDate.EXCEPTION_TYPE_REMOVED)
        }

        date = date.plusDays(1)
    }

    //If the service runs for more than a week and it would be simpler to represent as a calendar, create a calendar and a list of exceptions
    if (exceptions.size < sortedDates.size / 2  - 1 && from.until(to, ChronoUnit.DAYS) > 7) {
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

        return calendar to exceptions
    } else {
        //Otherwise just create a list of dates when the service runs
        return null to sortedDates.map { date ->
            CalendarDate(serviceId, date, CalendarDate.EXCEPTION_TYPE_ADDED)
        }
    }
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