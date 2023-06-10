package xyz.malkki.tallinkgtfsscraper.constants.gtfs

import xyz.malkki.gtfs.model.Route
import xyz.malkki.tallinkgtfsscraper.constants.tallinkRoutesByPiers
import xyz.malkki.tallinkgtfsscraper.gtfs.sanitizedName

val routes = tallinkRoutesByPiers
    .map { it.first() to it.last() }
    .map { (from, to) ->
        Route(
            routeId = "${from}_${to}",
            agencyId = "tallink",
            routeLongName = "${stopsById[from]!!.sanitizedName} - ${stopsById[to]!!.sanitizedName}",
            routeType = 4
        )
    }