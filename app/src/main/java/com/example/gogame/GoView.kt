package com.example.gogame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.View
import kotlin.math.roundToInt
import kotlin.math.sqrt


class GoView : View {
    private var width = 0f
    private var height = 0f
    private var length = 0f
    private var radius = 0f
    private var black: Paint? = null
    private var white: Paint? = null
    private var empty: Paint? = null
    private var grey: Paint? = null
    private var positions: ArrayList<Position>? = null
    private var previous_position: ArrayList<Position>? = null
    private var screen_shot: ArrayList<Position>? = null
    private var chain: ArrayList<Int>? = null
    private var territory_chain: ArrayList<Int>? = null
    private var black_territories: ArrayList<Int>? = null
    private var white_territories: ArrayList<Int>? = null
    private var game_over = false
    private var territory = false
    private var last_move_pass = false
    private var suicide_move = false
    private var touchX = 0f
    private var touchY = 0f
    var turn_black = false
    var black_timer: MyCountDownTimer? = null
    var white_timer: MyCountDownTimer? = null
    private var black_start_time: Long = 0
    private var white_start_time: Long = 0
    private var prisoners_of_white = 0
    private var prisoners_of_black = 0
    private var white_score = 0
    private var black_score = 0

    constructor(c: Context?) : super(c) {
        init()
    }

    constructor(c: Context?, `as`: AttributeSet?) : super(c, `as`) {
        init()
    }

    constructor(c: Context?, `as`: AttributeSet?, default_style: Int) : super(
        c,
        `as`,
        default_style
    ) {
        init()
    }

    inner class MyCountDownTimer(var time_left: Long, interval: Long) :
        CountDownTimer(time_left, interval) {
        override fun onFinish() {
            endGame()
            time_left = 0
            invalidate()
        }

        override fun onTick(millisUntilFinished: Long) {
            time_left = millisUntilFinished
            invalidate()
        }
    }

    private fun init() {
        var j: Int
        black = Paint(Paint.ANTI_ALIAS_FLAG)
        black!!.color = Color.BLACK
        black!!.strokeWidth = 4F
        white = Paint(Paint.ANTI_ALIAS_FLAG)
        white!!.color = -0x1
        white!!.strokeWidth = 4F
        empty = Paint(Paint.ANTI_ALIAS_FLAG)
        empty!!.color = Color.GRAY
        grey = Paint(Paint.ANTI_ALIAS_FLAG)
        grey!!.color = Color.LTGRAY
        positions = ArrayList<Position>()
        previous_position = ArrayList<Position>()
        screen_shot = ArrayList<Position>()
        var i: Int = 0
        while (i < 81) {
            positions!!.add(Position())
            i++
        }
        i = 0
        while (i < 81) {
            previous_position!!.add(Position())
            i++
        }
        i = 0
        while (i < 81) {
            screen_shot!!.add(Position())
            i++
        }
        newGame()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var i: Int
        i = 0
        while (i < 81) {
            black?.let {
                canvas.drawLine(
                    positions!![i].x,
                    positions!![i].y,
                    positions!![i + 8].x,
                    positions!![i + 8].y,
                    it
                )
            }
            i = i + 9
        }
        i = 0
        while (i < 9) {
            black?.let {
                canvas.drawLine(
                    positions!![i].x,
                    positions!![i].y,
                    positions!![72 + i].x,
                    positions!![72 + i].y,
                    it
                )
            }
            i++
        }
        i = 0
        while (i < 81) {
            if (i == 20 || i == 22 || i == 24 || i == 38 || i == 40 || i == 42 || i == 56 || i == 58 || i == 60) // reference points
            {
                black?.let { canvas.drawCircle(positions!![i].x, positions!![i].y, radius / 4, it) }
            }
            if (!positions!![i].getColor()?.equals(empty)!!) {
                positions!![i].getColor()?.let {
                    canvas.drawCircle(
                        positions!![i].x,
                        positions!![i].y,
                        radius,
                        it
                    )
                }
            }
            if (territory) {
                if (black_territories!!.contains(i)) {
                    black?.let {
                        canvas.drawRect(
                            positions!![i].x - radius / 2,
                            positions!![i].y - radius / 2,
                            positions!![i].x + radius / 2,
                            positions!![i].y + radius / 2,
                            it
                        )
                    }
                } else if (white_territories!!.contains(i)) {
                    white?.let {
                        canvas.drawRect(
                            positions!![i].x - radius / 2,
                            positions!![i].y - radius / 2,
                            positions!![i].x + radius / 2,
                            positions!![i].y + radius / 2,
                            it
                        )
                    }
                }
            }
            i++
        }
        if (turn_black) {
            black?.let { canvas.drawCircle(0.50f * length, 0.65f * height, radius * 1.5f, it) }
        } else {
            white?.let { canvas.drawCircle(0.50f * length, 0.65f * height, radius * 1.5f, it) }
        }
        grey?.let {
            canvas.drawRect(0.10f * length, 0.62f * height, 0.40f * length, 0.70f * height,
                it
            )
        }
        black?.let { canvas.drawText("Pass", 0.15f * length, 0.68f * height, it) }
        grey?.let {
            canvas.drawRect(0.60f * length, 0.62f * height, 0.90f * length, 0.70f * height,
                it
            )
        }
        black?.let { canvas.drawText("Territory", 0.62f * length, 0.68f * height, it) }
        grey?.let {
            canvas.drawRect(0.10f * length, 0.72f * height, 0.40f * length, 0.80f * height,
                it
            )
        }
        black?.let { canvas.drawText("Reset", 0.15f * length, 0.78f * height, it) }
        grey?.let {
            canvas.drawRect(0.60f * length, 0.72f * height, 0.90f * length, 0.80f * height,
                it
            )
        }
        black?.let { canvas.drawText("End", 0.70f * length, 0.78f * height, it) }
        var seconds = (black_timer!!.time_left / 1000).toInt()
        var minutes = seconds / 60
        seconds = seconds % 60
        black?.let {
            canvas.drawText(
                "Black " + String.format("%d:%02d", minutes, seconds),
                0.10f * length,
                0.85f * height,
                it
            )
        }
        seconds = (white_timer!!.time_left / 1000).toInt()
        minutes = seconds / 60
        seconds = seconds % 60
        white?.let {
            canvas.drawText(
                "White " + String.format("%d:%02d", minutes, seconds),
                0.60f * length,
                0.85f * height,
                it
            )
        }
        black?.let {
            canvas.drawText("Captured: $prisoners_of_black", 0.10f * length, 0.90f * height,
                it
            )
        }
        white?.let {
            canvas.drawText("Captured: $prisoners_of_white", 0.60f * length, 0.90f * height,
                it
            )
        }
        if (game_over) {
            if (black_score > white_score) {
                black?.let {
                    canvas.drawText(
                        "Winner: Black by " + (black_score - white_score) + " points",
                        0.20f * length,
                        0.59f * height,
                        it
                    )
                }
            } else if (black_score < white_score) {
                white?.let {
                    canvas.drawText(
                        "Winner: White by " + (white_score - black_score) + " points",
                        0.20f * length,
                        0.59f * height,
                        it
                    )
                }
            } else {
                black?.let { canvas.drawText("Tie", 0.47f * length, 0.59f * height, it) }
            }
        }
    }

    fun addItem(pos: Int, color: Paint?) {
        for (i in 0..80) {
            screen_shot!![i].setColor(positions!![i].getColor())
        }
        positions!![pos].setColor(color)
        chain = ArrayList()
        val neighbours = getNeighboours(pos)
        var remove_successfull = true
        for (i in 0 until neighbours.size) {
            if (color != null) {
                if (!color.equals(positions!![neighbours[i]].getColor()) && !positions!![neighbours[i]].getColor()
                        ?.equals(empty)!!
                ) {
                    if (checkForSurrounded(neighbours[i]) && chain!!.size > 0) {
                        remove_successfull = !removeCapturedItems()
                    }
                    chain = ArrayList()
                }
            }
        }
        if (remove_successfull) {
            if (checkForSurrounded(pos) && chain!!.size > 0) { // check for suicide
                suicide_move = true
                removeCapturedItems()
            }
            for (i in 0..80) {
                previous_position!![i].setColor(screen_shot!![i].getColor())
            }
            switchPlayer()
        } else {
            positions!![pos].setColor(empty)
        }
    }

    fun removeCapturedItems(): Boolean {
        for (i in 0 until chain!!.size) {
            positions!![chain!![i]].setColor(empty)
        }
        var repeated_moves = true
        for (i in 0..80) {
            if (!previous_position!![i].getColor()?.equals(positions!![i].getColor())!!) {
                repeated_moves = false
                break
            }
        }
        if (repeated_moves) {  // dont allow
            for (i in 0..80) {
                positions!![i].setColor(screen_shot!![i].getColor())
            }
            return true
        }
        if (suicide_move) {
            if (positions!![chain!![0]].getColor()?.equals(empty) == true) {
                if (turn_black) {
                    prisoners_of_white = prisoners_of_white + chain!!.size
                } else {
                    prisoners_of_black = prisoners_of_black + chain!!.size
                }
            }
            suicide_move = false
        } else {
            if (positions!![chain!![0]].getColor()?.equals(empty) == true) {
                if (turn_black) {
                    prisoners_of_black = prisoners_of_black + chain!!.size
                } else {
                    prisoners_of_white = prisoners_of_white + chain!!.size
                }
            }
        }
        return false
    }

    private fun switchPlayer() {
        if (turn_black) {
            black_start_time = black_timer!!.time_left
            black_timer!!.cancel()
            white_timer = MyCountDownTimer(white_start_time, 1000)
            white_timer!!.start()
        } else {
            white_start_time = white_timer!!.time_left
            white_timer!!.cancel()
            black_timer = MyCountDownTimer(black_start_time, 1000)
            black_timer!!.start()
        }
        turn_black = !turn_black
    }

    fun endGame() {
        calculateTerritory()
        game_over = true
        black_score = prisoners_of_black + black_territories!!.size
        white_score = prisoners_of_white + white_territories!!.size
        white_timer!!.cancel()
        black_timer!!.cancel()
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            touchX = event.x
            touchY = event.y
            if (touchX > 0 || touchY > 0) {
                territory = false
                if (!game_over) {
                    for (i in 0..80) {
                        val distance =
                            sqrt((touchX - positions!![i].x) * (touchX - positions!![i].x) + (touchY - positions!![i].y) * (touchY - positions!![i].y).toDouble())
                                .toFloat()
                        if (distance <= radius && positions!![i].getColor()?.equals(empty) == true) {
                            if (turn_black) {
                                addItem(i, black)
                            } else {
                                addItem(i, white)
                            }
                            invalidate()
                        }
                    }
                }
                if (touchX > 0.10 * length && touchX < 0.40 * length && touchY > 0.62 * height && touchY < 0.70 * height) {
                    if (last_move_pass) {
                        endGame()
                    } else {
                        last_move_pass = true
                        switchPlayer()
                    }
                } else if (touchX > 0.60 * length && touchX < 0.90 * length && touchY > 0.62 * height && touchY < 0.70 * height) {
                    calculateTerritory()
                } else if (touchX > 0.10 * length && touchX < 0.40 * length && touchY > 0.72 * height && touchY < 0.80 * height) {
                    newGame()
                } else if (touchX > 0.60 * length && touchX < 0.90 * length && touchY > 0.72 * height && touchY < 0.80 * height) {
                    endGame()
                }
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        width = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        height = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        length = width
        length = if (width > height * 0.60f) height * 0.60f else width
        radius = length / 27
        var i: Float
        var j: Float
        var count = 0
        i = 0.10f
        while (i < 1.0) {
            j = 0.10f
            while (j < 1.0) {
                positions!![count].setLocation(
                    (length * j).roundToInt().toFloat(),
                    (length * i).roundToInt().toFloat()
                )
                count++
                j = j + 0.10f
            }
            i = i + 0.10f
        }
        black?.setTextSize(length / 17)
        white?.setTextSize(length / 17)
        invalidate()
    }

    private fun calculateTerritory() {
        black_territories = ArrayList()
        white_territories = ArrayList()
        for (i in 0..80) {
            if (positions!![i].getColor()?.equals(empty) == true) {
                territory_chain = ArrayList()
                if (findTerritory(i, black)) {
                    for (j in 0 until territory_chain!!.size) {
                        if (!black_territories!!.contains(territory_chain!![j])) {
                            black_territories!!.add(territory_chain!![j])
                        }
                    }
                }
                territory_chain = ArrayList()
                if (findTerritory(i, white)) {
                    for (j in 0 until territory_chain!!.size) {
                        if (!white_territories!!.contains(territory_chain!![j])) {
                            white_territories!!.add(territory_chain!![j])
                        }
                    }
                }
            }
        }
        territory = true
    }

    fun findTerritory(pos: Int, color: Paint?): Boolean {
        territory_chain!!.add(pos)
        val neighbours = getNeighboours(pos)
        for (i in 0 until neighbours.size) {
            if (positions!![neighbours[i]].getColor()
                    ?.equals(positions!![pos].getColor()) == true && !territory_chain!!.contains(
                    neighbours[i]
                )
            ) {
                if (!findTerritory(neighbours[i], color)) {
                    return false
                }
            }
            if (!positions!![neighbours[i]].getColor()
                    ?.equals(color)!! && !positions!![neighbours[i]].getColor()?.equals(empty)!!
            ) {
                return false
            }
        }
        return true
    }

    fun checkForSurrounded(pos: Int): Boolean {
        var i: Int
        chain!!.add(pos)
        var neighbours = ArrayList<Int>()
        neighbours = getNeighboours(pos)
        i = 0
        while (i < neighbours.size) {
            if (positions!![pos].getColor()
                    ?.equals(positions!![neighbours[i]].getColor()) == true && !chain!!.contains(
                    neighbours[i]
                )
            ) {
                if (!checkForSurrounded(neighbours[i])) {
                    return false
                }
            }
            if (positions!![neighbours[i]].getColor()?.equals(empty) == true) {
                return false
            }
            i++
        }
        return true
    }

    private fun getNeighboours(pos: Int): ArrayList<Int> {
        val neighbours = ArrayList<Int>()
        if (pos < 72) {
            neighbours.add(pos + 9)
        }
        if (pos > 8) {
            neighbours.add(pos - 9)
        }
        if ((pos + 1) % 9 != 0) {
            neighbours.add(pos + 1)
        }
        if (pos % 9 != 0) {
            neighbours.add(pos - 1)
        }
        return neighbours
    }

    private fun newGame() {
        var i: Int = 0
        while (i < 81) {
            positions!![i].setColor(empty)
            i++
        }
        i = 0
        while (i < 81) {
            previous_position!![i].setColor(empty)
            i++
        }
        turn_black = true
        game_over = false
        last_move_pass = false
        prisoners_of_white = 0
        prisoners_of_black = prisoners_of_white
        white_start_time = 900000
        black_start_time = white_start_time
        black_timer = MyCountDownTimer(black_start_time, 1000)
        black_timer!!.start()
        white_timer = MyCountDownTimer(white_start_time, 1000)
        black_territories = ArrayList()
        white_territories = ArrayList()
        territory = false
        suicide_move = false
    }
}