package xyz.malkki.tallinkgtfsscraper.tallinkapi

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import xyz.malkki.tallinkgtfsscraper.tallinkapi.model.Timetables
import xyz.malkki.tallinkgtfsscraper.tallinkapi.model.Trips
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TallinkApi(private val httpClient: HttpClient, private val endpoint: String = "https://booking.tallink.com") {
    companion object {
        private val OBJECT_MAPPER = jacksonObjectMapper().registerModule(JavaTimeModule())
    }

    fun getTrips(from: String, to: String, startDate: LocalDate, endDate: LocalDate): Trips {
        val dateFrom = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val dateTo = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val httpRequest = HttpRequest.newBuilder()
            .uri(
                URI.create(
                    "$endpoint/api/timetables?from=${from.lowercase()}&to=${to.lowercase()}&oneWay=true&dateFrom=$dateFrom&dateTo=$dateTo&voyageType=SHUTTLE&includeOvernight=true&searchFutureSails=false"
                )
            )
            .GET()
            .timeout(Duration.ofMinutes(1))
            .build()

        val httpResponse = httpClient.send(httpRequest, BodyHandlers.ofInputStream())

        BufferedReader(InputStreamReader(httpResponse.body(), StandardCharsets.UTF_8)).use {
            return OBJECT_MAPPER.readValue(it, Timetables::class.java).trips
        }
    }
}