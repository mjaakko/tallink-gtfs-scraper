package xyz.malkki.tallinkgtfsscraper.constants

//List of trips by city codes that form trips that can be queried from Tallink API
val tallinkRoutesByCities = listOf(
    listOf("HEL", "ALA", "STO"),
    listOf("TAL", "ALA", "STO"),
    listOf("TUR", "ALA", "STO"),
    listOf("TUR", "ALA", "KAP"),
    listOf("HEL", "TAL"),
).flatMap {
    listOf(it, it.reversed())
}

//Actual routes by the piers they go through (e.g. query by city pair STO-ALA will return trips from KAPE to LNAS)
val tallinkRoutesByPiers = listOf(
    listOf("LSA2", "DTER"),
    listOf("DTER", "MHAM", "VHAM"),
    listOf("OLYM", "MHAM", "VHAM"),
    listOf("TSAT", "MHAM", "VHAM"),
    listOf("TSAT", "LNAS", "VHAM"),
    listOf("TSAT", "LNAS", "KAPE"),
    listOf("TSAT", "MHAM", "KAPE"),
).flatMap {
    listOf(it, it.reversed())
}