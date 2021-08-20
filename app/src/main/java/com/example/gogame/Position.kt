package com.example.gogame

import android.graphics.Paint

class Position {
    var x = 0f
    var y = 0f
    private var color: Paint? = null

    constructor() {}
    constructor(x: Float, y: Float, color: Paint?) {
        this.x = x
        this.y = y
        this.color = color
    }

    constructor(color: Paint?) {
        this.color = color
    }

    fun getColor(): Paint? {
        return color
    }

    fun setColor(color: Paint?) {
        this.color = color
    }

    fun setLocation(x: Float, y: Float) {
        this.x = x
        this.y = y
    }
}
