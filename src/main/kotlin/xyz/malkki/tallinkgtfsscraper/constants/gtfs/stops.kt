package xyz.malkki.tallinkgtfsscraper.constants.gtfs

import xyz.malkki.gtfs.model.Stop
import xyz.malkki.tallinkgtfsscraper.constants.HELSINKI_TIMEZONE
import xyz.malkki.tallinkgtfsscraper.constants.MARIEHAMN_TIMEZONE
import xyz.malkki.tallinkgtfsscraper.constants.STOCKHOLM_TIMEZONE
import xyz.malkki.tallinkgtfsscraper.constants.TALLINN_TIMEZONE


val stops = listOf(
    Stop(
        stopId = "LSA2",
        stopName = "Helsinki (West Harbour)",
        stopLat = 60.14938,
        stopLon = 24.91470,
        stopTimezone = HELSINKI_TIMEZONE,
        stopUrl = "https://en.tallink.com/en/helsinki-west-harbour-terminal-2-t2-"
    ),
    Stop(
        stopId = "OLYM",
        stopName = "Helsinki (Olympia Terminal)",
        stopLat = 60.16077,
        stopLon = 24.95923,
        stopTimezone = HELSINKI_TIMEZONE,
        stopUrl = "https://en.tallink.com/en/helsinki-olympia-terminal"
    ),
    Stop(
        stopId = "TSAT",
        stopName = "Turku",
        stopLat = 60.43527,
        stopLon = 22.21974,
        stopTimezone = HELSINKI_TIMEZONE,
        stopUrl = "https://en.tallink.com/en/turku-harbour"
    ),
    Stop(
        stopId = "LNAS",
        stopName = "Långnäs",
        stopLat = 60.11655,
        stopLon = 20.29821,
        stopTimezone = MARIEHAMN_TIMEZONE,
        stopUrl = "https://en.tallink.com/en/langnas-terminal"
    ),
    Stop(
        stopId = "MHAM",
        stopName = "Mariehamn",
        stopLat = 60.09204,
        stopLon = 19.92919,
        stopTimezone = MARIEHAMN_TIMEZONE,
        stopUrl = "https://en.tallink.com/en/mariehamn-terminal"
    ),
    Stop(
        stopId = "VHAM",
        stopName = "Stockholm",
        stopLat = 59.35129,
        stopLon = 18.11178,
        stopTimezone = STOCKHOLM_TIMEZONE,
        stopUrl = "https://en.tallink.com/en/stockholm-vartahamnen"
    ),
    Stop(
        stopId = "KAPE",
        stopName = "Kapellskär",
        stopLat = 59.72182,
        stopLon = 19.06443,
        stopTimezone = STOCKHOLM_TIMEZONE,
        stopUrl = "https://en.tallink.com/en/kapellskar"
    ),
    Stop(
        stopId = "DTER",
        stopName = "Tallinn",
        stopLat = 59.44354,
        stopLon = 24.76749,
        stopTimezone = TALLINN_TIMEZONE,
        stopUrl = "https://en.tallink.com/en/tallinn-d-terminal"
    ),
    /*Stop(
        stopId = "rig",
        stopName = "Riga",
        stopLat = 56.95932,
        stopLon = 24.09519,
        stopTimezone = RIGA_TIMEZONE,
    ),*/
)
val stopsById = stops.associateBy { it.stopId }