package com.zypex.piflowapp.Input

import javafx.scene.input.KeyCode

class Keyboard {
    var keys: MutableMap<KeyCode, Button> = mutableMapOf()

    fun updateKey(key: KeyCode, pressed: Boolean) {
        keys.putIfAbsent(key, Button())
        val b = keys[key]
        b!!.isPressed = pressed
        if (pressed) {
            if (!b.buffer) {
                b.pressEvents.forEach { it() }
                b.buffer = true
            }
        } else {
            b.releaseEvents.forEach { it() }
            b.buffer = false
        }
    }

    fun addPressEvent(key: KeyCode, event: () -> Unit) {
        keys.putIfAbsent(key, Button())
        keys[key]!!.pressEvents.add(event)
    }

    fun addReleaseEvent(key: KeyCode, event: () -> Unit) {
        keys.putIfAbsent(key, Button())
        keys[key]!!.releaseEvents.add(event)
    }

    fun getKey(key: KeyCode): Boolean {
        return (keys[key] ?: keys.putIfAbsent(key, Button()))!!.isPressed
    }

    fun keyString(): String {
        var out = ""
        for ((key, value) in keys) {
            out += """
                ${key.name}: ${value.isPressed}
                
                """.trimIndent()
        }
        return out
    }
}