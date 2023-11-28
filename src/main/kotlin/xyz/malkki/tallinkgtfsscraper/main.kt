package xyz.malkki.tallinkgtfsscraper

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import xyz.malkki.gtfs.model.Shape
import xyz.malkki.gtfs.serialization.writer.ZipGtfsFeedWriter
import xyz.malkki.tallinkgtfsscraper.constants.gtfs.agencies
import xyz.malkki.tallinkgtfsscraper.constants.gtfs.agency
import xyz.malkki.tallinkgtfsscraper.constants.gtfs.routes
import xyz.malkki.tallinkgtfsscraper.constants.gtfs.stops
import xyz.malkki.tallinkgtfsscraper.constants.tallinkRoutesByCities
import xyz.malkki.tallinkgtfsscraper.tallinkapi.TallinkApi
import xyz.malkki.tallinkgtfsscraper.tallinkapi.model.Trip
import xyz.malkki.tallinkgtfsscraper.tallinkapi.model.Trips
import java.net.http.HttpClient
import java.nio.file.Path
import java.time.LocalDate
import kotlin.system.exitProcess
import kotlin.time.DurationUnit
import kotlin.time.measureTime

/**
 * Creates pairs from the list, e.g. (A, B, C) -> (A-B, A-C, B-C)
 */
fun <T> getPairs(list: List<T>): List<Pair<T, T>> {
    return list.flatMapIndexed { index, from ->
        if (index + 1 > list.size) {
            emptyList()
        } else {
            list.subList(index + 1, list.size).map { from to it }
        }
    }
}

/**
 * Queries all possible Tallink frips for given dates
 */
fun getTallinkTrips(tallinkApi: TallinkApi, fromDate: LocalDate, toDate: LocalDate): List<Trip> {
    val cache = mutableMapOf<Pair<String, String>, Trips>()

    fun getTrips(fromCity: String, toCity: String): Trips {
        return cache.computeIfAbsent(fromCity to toCity) {
            tallinkApi.getTrips(fromCity, toCity, fromDate, toDate.plusDays(1))
        }
    }

    return tallinkRoutesByCities
        .flatMap { getPairs(it) }
        .flatMap { (from, to) ->
            getTrips(from, to).tripsByDate.values.flatMap { it.outwards }.filter { it.arrivalIsoDate.toLocalDate() <= toDate }
        }
}

fun createTallinkGtfs(httpClient: HttpClient, file: Path, fromDate: LocalDate, toDate: LocalDate) {
    val tallinkApi = TallinkApi(httpClient)

    val tallinkTrips = getTallinkTrips(tallinkApi, fromDate, toDate)

    fun findSameTrips(trip: Trip, foundTrips: Set<Trip>): List<Trip> {
        val output = mutableSetOf(trip)

        tallinkTrips.forEach {
            if (it != trip && it.isSameVoyage(trip) && it !in foundTrips) {
                output += findSameTrips(it, output.toSet())
            }
        }

        return output.sortedBy { it.departureIsoDate }.sortedBy { it.arrivalIsoDate }
    }

    val shapeCreator = ShapeCreator()

    val shapeCache = mutableMapOf<List<String>, List<Shape>>()

    fun createShape(stops: List<String>): String? {
        return if (stops in shapeCache) {
            shapeCache[stops]!!.first().shapeId
        } else {
            val shape = shapeCreator.createShapesForStopIntervals(stops)

            if (shape.isNotEmpty()) {
                shapeCache[stops] = shape

                shape.first().shapeId
            } else {
                null
            }
        }
    }

    val tallinkGtfsData = tallinkTrips
        .asSequence()
        .map { trip -> findSameTrips(trip, emptySet()) }
        .map {
            TripSet.buildFromTrips(it)
        }
        .filter { it.getCities() in tallinkRoutesByCities } //Only create full trips, because the API can return "partial" trips when the ship has already departed from the first port
        .groupBy { it.getIdentity() }
        .mapNotNull { (identity, tripSet) ->
            val dates = tripSet.map { it.getDate() }.toList()

            val routeId = tripSet.first().getPiers().first() + "_" + tripSet.first().getPiers().last()

            val serviceIdAndTripId = "$routeId-$identity"

            val calendarAndCalendarDates = getCalendarAndCalendarDates(serviceIdAndTripId, dates)
            val stopTimes = getStopTimes(serviceIdAndTripId, agency.agencyTimezone, tripSet.first())

            val shapeId = createShape(stopTimes.map { it.stopId })

            val trip = xyz.malkki.gtfs.model.Trip(
                routeId = routeId,
                serviceId = serviceIdAndTripId,
                tripId = serviceIdAndTripId,
                tripHeadsign = stops.find { it.stopId == stopTimes.last().stopId }?.stopName,
                shapeId = shapeId,
                blockId = serviceIdAndTripId //TODO: think about using ship name in block ID so that we could create estimates for the next trip that the ship will serve
            )

            Triple(trip, calendarAndCalendarDates, stopTimes)
        }
        .toList()

    val trips = tallinkGtfsData.map { it.first }

    //Filter routes that do not have any trips
    val routeIds = trips.map { it.routeId }.toSet()
    val routesFiltered = routes.filter { it.routeId in routeIds }

    val calendars = tallinkGtfsData.mapNotNull { it.second.first }
    val calendarDates = tallinkGtfsData.flatMap { it.second.second }
    val stopTimes = tallinkGtfsData.flatMap { it.third }

    val shapes = shapeCache.values.flatten()

    ZipGtfsFeedWriter(file).use {
        it.writeAgencies(agencies)
        it.writeStops(stops)
        it.writeRoutes(routesFiltered)
        it.writeTrips(trips)
        it.writeCalendars(calendars)
        it.writeCalendarDates(calendarDates)
        it.writeStopTimes(stopTimes)
        it.writeShapes(shapes)
    }
}

private const val DEFAULT_DAYS = 31L

data class Params(val days: Long, val outputPath: String)

fun main(vararg args: String) {
    val params = when (args.size) {
        0 -> Params(DEFAULT_DAYS, "tallink.zip",)
        1 -> Params(DEFAULT_DAYS, args[0],)
        else -> {
            val options = Options().apply {
                addRequiredOption("o", "output", true, "Path to the output file")
                addRequiredOption("d", "days", true, "Number of days to include in the GTFS")
            }
            val cliParser = DefaultParser()

            val cli = cliParser.parse(options, args)

            Params(cli.getOptionValue("d").toLong(), cli.getOptionValue("o"),)
        }
    }

    if (params.days < 2) {
        println("Cannot generate GTFS for less than 2 days")
        exitProcess(1)
    }

    val outputPath = Path.of(params.outputPath)

    println("Writing GTFS file to ${outputPath.toAbsolutePath()}")

    val tomorrow = LocalDate.now().plusDays(1)

    val from = tomorrow
    val to = tomorrow.plusDays(params.days)

    println("GTFS will contain data for period from $from to $to")

    val httpClient: HttpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()

    val duration = measureTime {
        createTallinkGtfs(httpClient = httpClient, file = outputPath, fromDate = from, toDate = to)
    }

    println("GTFS file created in ${duration.toString(DurationUnit.SECONDS, 2)}")
}