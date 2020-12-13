package com.example.testhvml

import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.view.View.generateViewId
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import kotlin.math.atan2

class MainActivity : AppCompatActivity() {
    var topicView: View? = null
    var topicView1: View? = null
    var arrowView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val root = findViewById<ConstraintLayout>(R.id.root)

        topicView = layoutInflater.inflate(R.layout.layout_topic, null)
        topicView?.layoutParams = FrameLayout.LayoutParams(350, 350)
        topicView?.id = generateViewId()

        topicView1 = layoutInflater.inflate(R.layout.layout_topic, null)
        topicView1?.layoutParams = FrameLayout.LayoutParams(350, 350)
        topicView1?.id = generateViewId()
        val topicName1 = topicView1?.findViewById<TextView>(R.id.TopicName)
        topicName1?.text = "########"

        arrowView = View(baseContext)
        arrowView?.background = ContextCompat.getDrawable(baseContext, R.drawable.ic_down_arrow)
        arrowView?.layoutParams = FrameLayout.LayoutParams(350, 350)
        arrowView?.id = generateViewId()

        root.addView(topicView)
        root.addView(topicView1)
        root.addView(arrowView)

        ConstraintSet().apply {
            this.clone(root)

            topicView?.let { topicView ->
                topicView1?.let { topicView1 ->
                    arrowView?.let { arrowView ->
                        this.connect(topicView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                        this.connect(topicView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                        this.connect(topicView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

                        this.connect(topicView1.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                        this.connect(topicView1.id, ConstraintSet.START, arrowView.id, ConstraintSet.END)

                        this.connect(arrowView.id, ConstraintSet.TOP, topicView1.id, ConstraintSet.TOP)
                        this.connect(arrowView.id, ConstraintSet.END, topicView1.id, ConstraintSet.START)
                        this.connect(arrowView.id, ConstraintSet.BOTTOM, topicView.id, ConstraintSet.BOTTOM)
                        this.connect(arrowView.id, ConstraintSet.START, topicView.id, ConstraintSet.END)
                    }
                }
            }

            this.applyTo(root)
        }
    }

    override fun onWindowFocusChanged(p0: Boolean) {
        arrowView?.rotation = getRadianDegree(topicView!!.getLocationPointInWindow(), topicView1!!.getLocationPointInWindow()).toFloat()
    }

    fun getRadianDegree(pre: Point, dst: Point): Double {
        return atan2((dst.x - pre.y).toDouble(), (dst.x - pre.y).toDouble()) * 180.0 / Math.PI
    }

}



fun View.getLocationPointInWindow(): Point {
    val array = IntArray(2)
    this.getLocationInWindow(array)
    return Point(array[0], array[1])
}

fun View.getLocationPointOnScreen(): Point {
    val array = IntArray(2)
    this.getLocationOnScreen(array)
    return Point(array[0], array[1])
}