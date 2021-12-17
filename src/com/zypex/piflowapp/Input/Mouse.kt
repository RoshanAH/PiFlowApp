package com.zypex.piflowapp.Input

import utils.math.Vector

object Mouse {
    var position = Vector(0.0, 0.0)
    var leftButton = false
    var rightButton = false
    private val leftClickEvents: MutableList<Runnable> = ArrayList()
    private val rightClickEvents: MutableList<Runnable> = ArrayList()
    fun callLeftClick() {
        for (r in leftClickEvents) r.run()
    }

    fun callRightClick() {
        for (r in rightClickEvents) r.run()
    }

    fun addOnLeftClick(runnable: Runnable) {
        leftClickEvents.add(runnable)
    }

    fun addOnRightClick(runnable: Runnable) {
        rightClickEvents.add(runnable)
    }
}