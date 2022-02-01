package com.zypex.piflowapp.Input

import java.lang.Runnable
import java.util.ArrayList

class Button {
    var pressEvents: MutableList<() -> Unit> = ArrayList()
    var releaseEvents: MutableList<() -> Unit> = ArrayList()
    var isPressed = false
    var buffer = false
}