package xyz.malkki.tallinkgtfsscraper

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.commons.codec.digest.MurmurHash3
import org.geojson.FeatureCollection
import org.geojson.LineString
import xyz.malkki.gtfs.model.Shape
import java.nio.charset.StandardCharsets
import java.util.*

class ShapeCreator {
    private val objectMapper = jacksonObjectMapper()

    private fun getPointsForStopInterval(from: String, to: String): List<Pair<Double, Double>>? {
        return javaClass.classLoader.getResourceAsStream("shapes/${from.uppercase(Locale.ROOT)}_${to.uppercase(Locale.ROOT)}.geojson")?.let { inputStream ->
            inputStream.use {
                val featureCollection = objectMapper.readValue(it, FeatureCollection::class.java)
                featureCollection.features.flatMap { feature ->
                    if (feature.geometry is LineString) {
                        (feature.geometry as LineString).coordinates.map { coordinate ->
                            coordinate.latitude to coordinate.longitude
                        }
                    } else {
                        emptyList()
                    }
                }
            }
        }
    }

    private fun createShapeId(stops: List<String>): String {
        val hasher = MurmurHash3.IncrementalHash32x86()

        hasher.start(0)
        stops.forEach {
            hasher.add(it.toByteArray(StandardCharsets.UTF_8))
        }

        return hasher.end().toUInt().toString(16)
    }

    private fun MurmurHash3.IncrementalHash32x86.add(bytes: ByteArray) = add(bytes, 0, bytes.size)

    fun createShapesForStopIntervals(stops: List<String>): List<Shape> {
        val pointsByStopInterval = stops
            .mapIndexedNotNull { index, stop ->
                if (index == 0) {
                    null
                } else {
                    val prev = stops[index - 1]

                    getPointsForStopInterval(prev, stop) ?: emptyList()
                }
            }

        if (pointsByStopInterval.any { it.isEmpty() }) {
            //Cannot generate shape because there was no data for some stop interval
            return emptyList()
        }

        val shapeId = createShapeId(stops)

        return pointsByStopInterval
            .flatten()
            .mapIndexed { index, (lat, lng) ->
                Shape(shapeId, lat, lng, index)
            }
    }
}