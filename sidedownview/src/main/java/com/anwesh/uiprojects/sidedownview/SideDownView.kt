package com.anwesh.uiprojects.sidedownview

/**
 * Created by anweshmishra on 04/09/18.
 */

import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Paint
import android.content.Context
import android.graphics.Color

val nodes : Int = 5

fun Canvas.drawSideDownNode(i : Int, scale : Float, currI : Int, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = (w * 0.9f) / nodes
    val sc1 : Float = Math.min(0.5f, scale) * 2
    val sc2 : Float = Math.min(0.5f, Math.max(0f, scale - 0.5f)) * 2
    paint.strokeWidth = Math.min(w, h) / 60
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = Color.parseColor("#283593")
    save()
    translate(gap * i + gap/2, h/2)
    scale(1f, 1f - 2 * (i % 2))
    save()
    translate(-gap/2, -gap/2)
    drawLine(0f, 0f, gap * sc1, 0f, paint)
    restore()
    save()
    translate(gap/2, -gap/2)
    drawLine(0f, 0f, 0f, gap * sc2, paint)
    restore()
    if (currI == i) {
        paint.color = Color.WHITE
        drawCircle(-gap/2 + gap * sc1, -gap/2 + gap * sc2, gap/18, paint)
    }
    restore()
}

class SideDownView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += 0.05f * this.dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }
    }

    data class SideDownNode(var i : Int, val state : State = State()) {
        private var next : SideDownNode? = null
        private var prev : SideDownNode? = null

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = SideDownNode(i + 1)
                next?.prev = this
            }
        }

        init {
            addNeighbor()
        }

        fun draw(canvas : Canvas, currI : Int, paint : Paint) {
            prev?.draw(canvas, currI, paint)
            canvas.drawSideDownNode(i, state.scale, currI, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : SideDownNode {
            var curr : SideDownNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LinkedSideDown(var i : Int) {

        private var curr : SideDownNode = SideDownNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, curr.i, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : SideDownView) {
        private val animator : Animator = Animator(view)
        private val sideDown : LinkedSideDown = LinkedSideDown(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#263238"))
            sideDown.draw(canvas, paint)
            animator.animate {
                sideDown.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            sideDown.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : SideDownView {
            val view : SideDownView = SideDownView(activity)
            activity.setContentView(view)
            return view
        }
    }
}