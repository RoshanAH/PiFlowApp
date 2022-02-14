package com.zypex.piflowapp.Input

import com.zypex.piflow.math.Vector

class Mouse : Vector(0.0, 0.0) {

    var left = Button()
    var right = Button()

    fun rightPressed(): Boolean {
        return right.isPressed
    }

    fun leftPressed(): Boolean {
        return left.isPressed
    }

    fun addOnRightPressed(event: () -> Unit) {
        right.pressEvents.add(event)
    }

    fun addOnRightReleased(event: () -> Unit) {
        right.releaseEvents.add(event)
    }

    fun addOnLeftPressed(event: () -> Unit) {
        left.pressEvents.add(event)
    }

    fun addOnLeftReleased(event: () -> Unit) {
        left.releaseEvents.add(event)
    }

    fun updateRight(pressed: Boolean) {
        right.isPressed = pressed
        if (pressed) {
            if (!right.buffer) {
                right.buffer = true
                right.pressEvents.forEach { it() }
            }
        } else {
            right.buffer = false
            right.releaseEvents.forEach { it() }
        }
    }

    fun updateLeft(pressed: Boolean) {
        left.isPressed = pressed
        if (pressed) {
            if (!left.buffer) {
                left.buffer = true
                left.pressEvents.forEach { it() }
            }
        } else {
            left.buffer = false
            left.releaseEvents.forEach { it() }
        }
    }
}