package com.dzenis_ska.lostandfound.ui.utils

import android.util.Log
import android.view.MotionEvent
import android.view.View

open class MyTouchListener(val callback: ()->Unit) : View.OnTouchListener {
    var xDelta = 0.0f
    var yDelta = 0.0f

    override fun onTouch(v: View, event: MotionEvent?): Boolean {
        when(event?.action){
            MotionEvent.ACTION_DOWN -> {
                xDelta = v.x - event.rawX
                Log.d("!!!MotionEvent", "ACTION_DOWN _ ${xDelta}")
//                if (xDelta > )
//                yDelta = v.y - event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                v.x = xDelta + event.rawX
                Log.d("!!!MotionEvent", "ACTION_MOVE _ ${v.x}")
                if (v.x > 150) callback()
//                v.y = yDelta + event.rawY
            }
            MotionEvent.ACTION_UP -> callback()
        }
        return true
    }
}