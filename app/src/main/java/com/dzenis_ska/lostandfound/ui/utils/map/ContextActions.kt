package com.dzenis_ska.lostandfound.ui.utils.map

import android.content.Context
import android.util.Log


typealias ContextAction = (Context) -> Unit

class ContextActions {

    var contextAction: Context? = null
        set(context) {
            field = context
            Log.d("!!!contextSet", "${context}")
            if (context != null) {
                actions.forEach {
                    it(context)
                }
                actions.clear()
            }
        }

    private val actions = mutableListOf<ContextAction>()

    /*suspend */operator  fun invoke(action: ContextAction) {
        val context = this.contextAction
        if (context == null) {
            actions += action
        } else {
            action(context)
        }
    }

    fun clear() {
        actions.clear()
    }
}