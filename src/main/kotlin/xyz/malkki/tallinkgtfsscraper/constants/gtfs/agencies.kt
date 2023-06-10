package xyz.malkki.tallinkgtfsscraper.constants.gtfs

import xyz.malkki.gtfs.model.Agency
import xyz.malkki.tallinkgtfsscraper.constants.HELSINKI_TIMEZONE

val agency = Agency(
    agencyId = "tallink",
    agencyName = "Tallink",
    agencyUrl = "https://www.tallink.com/",
    agencyTimezone = HELSINKI_TIMEZONE
)

val agencies = listOf(agency)