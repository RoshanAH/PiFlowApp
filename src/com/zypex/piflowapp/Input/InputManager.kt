package com.zypex.piflowapp.Input

import javafx.event.EventHandler
import javafx.scene.canvas.Canvas
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent

val keyboard = Keyboard()
val mouse = Mouse()

fun configure(canvas: Canvas) {
    canvas.onMouseMoved = EventHandler { e: MouseEvent ->
        mouse.x = e.x
        mouse.y = e.y
    }
    canvas.onMouseDragged = EventHandler { e: MouseEvent ->
        mouse.x = e.x
        mouse.y = e.y
    }
    canvas.onMousePressed = EventHandler { e: MouseEvent ->
        if (e.button == MouseButton.PRIMARY) mouse.updateLeft(true)
        if (e.button == MouseButton.SECONDARY) mouse.updateRight(true)
    }
    canvas.onMouseReleased = EventHandler { e: MouseEvent ->
        if (e.button == MouseButton.PRIMARY) mouse.updateLeft(false)
        if (e.button == MouseButton.SECONDARY) mouse.updateRight(false)
    }
    canvas.isFocusTraversable = true
    canvas.onKeyPressed = EventHandler { e: KeyEvent -> keyboard.updateKey(e.code, true) }
    canvas.onKeyReleased = EventHandler { e: KeyEvent -> keyboard.updateKey(e.code, false) }
}