package com.dzenis_ska.lostandfound.ui.utils.mathUtils

fun Long.divideToPercent(divideTo: Long): Long {
    return if (divideTo == 0L) 0L
    else (divideTo*100/this).toLong()
}