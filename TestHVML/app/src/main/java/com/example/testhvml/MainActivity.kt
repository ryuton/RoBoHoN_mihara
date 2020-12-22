package com.example.testhvml

import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.generateViewId
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import kotlin.math.atan2

class MainActivity : AppCompatActivity(), HVMLPlacement.HVMLPlacementListener{
    lateinit var placement: HVMLPlacement
    var flag = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val paser = HVMLParser(resources, "hvml/other/jp_co_sharp_sample_simple_talk.hvml")
        val model = paser.parse()

        placement = HVMLPlacement(model)
        placement.setHVMLPlacementListener(this)
        placement.createTree()
    }

    override fun onWindowFocusChanged(p0: Boolean) {
        if (flag) {
            placement.rotateArrowView()
            flag = false
        }

    }

    override fun newTopicLayout(topic: Topic): View {
        val topicLayout = layoutInflater.inflate(R.layout.layout_topic, null)
        topicLayout.id = generateViewId()
        topicLayout.layoutParams = FrameLayout.LayoutParams(500, 500)

        val topicTextView = topicLayout.findViewById<TextView>(R.id.TopicName)
        if (!topic.actions.isNullOrEmpty()){
            topicTextView.text = topic.actions.first().speech
        }

        val anchorTextView = topicLayout.findViewById<TextView>(R.id.AnchorName)
        var anchorText = ""
        topic.anchors.forEach { anchor ->
            anchorText += anchor.href
        }
        anchorTextView.text = anchorText

        val nextTextView = topicLayout.findViewById<TextView>(R.id.NextName)
        var nextText = ""
        topic.nexts.forEach { next ->
            nextText += next
        }
        nextTextView.text = nextText

        return topicLayout
    }

    override fun newArrowView(): View {
        val arrowView = layoutInflater.inflate(R.layout.layout_arrow, null)
        arrowView.layoutParams = FrameLayout.LayoutParams(400, 400)
        arrowView.id = generateViewId()

        return arrowView
    }

    override fun getRootLayout(): ConstraintLayout {
        return findViewById(R.id.root)
    }

    override fun addArrowView(arrowView: View) {
        addContentView(arrowView, FrameLayout.LayoutParams(350, 350))
    }

}

//val root = findViewById<ConstraintLayout>(R.id.root)
//
//topicView = layoutInflater.inflate(R.layout.layout_topic, null)
//topicView?.layoutParams = FrameLayout.LayoutParams(350, 350)
//topicView?.id = generateViewId()
//
//topicView1 = layoutInflater.inflate(R.layout.layout_topic, null)
//topicView1?.layoutParams = FrameLayout.LayoutParams(350, 350)
//topicView1?.id = generateViewId()
//val topicName1 = topicView1?.findViewById<TextView>(R.id.TopicName)
//topicName1?.text = "########"
//
//arrowView = View(baseContext)
//arrowView?.background = ContextCompat.getDrawable(baseContext, R.drawable.ic_down_arrow)
//arrowView?.layoutParams = FrameLayout.LayoutParams(350, 350)
//arrowView?.id = generateViewId()
//
//root.addView(topicView)
//root.addView(topicView1)
//root.addView(arrowView)
//
//ConstraintSet().apply {
//    this.clone(root)
//
//    topicView?.let { topicView ->
//        topicView1?.let { topicView1 ->
//            arrowView?.let { arrowView ->
//                this.connect(topicView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
//                this.connect(topicView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
//                this.connect(topicView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
//
//                this.connect(topicView1.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
//                this.connect(topicView1.id, ConstraintSet.START, arrowView.id, ConstraintSet.END)
//
//                this.connect(arrowView.id, ConstraintSet.TOP, topicView1.id, ConstraintSet.TOP)
//                this.connect(arrowView.id, ConstraintSet.END, topicView1.id, ConstraintSet.START)
//                this.connect(arrowView.id, ConstraintSet.BOTTOM, topicView.id, ConstraintSet.BOTTOM)
//                this.connect(arrowView.id, ConstraintSet.START, topicView.id, ConstraintSet.END)
//            }
//        }
//    }
//
//    this.applyTo(root)
//}