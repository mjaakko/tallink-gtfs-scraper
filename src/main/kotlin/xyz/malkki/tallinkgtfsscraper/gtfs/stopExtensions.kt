package xyz.malkki.tallinkgtfsscraper.gtfs

import xyz.malkki.gtfs.model.Stop

val Stop.sanitizedName: String
    get() {
        val i = stopName!!.indexOf('(')
        return if (i == -1) {
            return stopName!!
        } else {
            stopName!!.substring(0, i).trim()
        }
    }