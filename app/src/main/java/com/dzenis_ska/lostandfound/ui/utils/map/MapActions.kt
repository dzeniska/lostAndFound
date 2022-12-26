package com.dzenis_ska.lostandfound.ui.utils.map

import com.google.android.gms.maps.GoogleMap

typealias MapAction = (GoogleMap) -> Unit

class MapActions {

    var mMap: GoogleMap? = null
        set(map) {
            field = map
            if (map != null) {
                actions.forEach {
                    it(map)
                }
                actions.clear()
            }
        }

    private val actions = mutableListOf<MapAction>()

    operator fun invoke(action: MapAction) {
        val map = this.mMap
        if (map == null) {
            actions += action
        } else {
            action(map)
        }
    }

    fun clear() {
        actions.clear()
    }
}