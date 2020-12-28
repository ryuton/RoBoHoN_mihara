package com.example.testhvml

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.generateViewId
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class MainActivity : AppCompatActivity(), HVMLPlacement.HVMLPlacementListener{
    lateinit var placement: HVMLPlacement
    lateinit var button: Button
    lateinit var horizontalScrollView: HorizontalScrollView
    lateinit var scrollView: ScrollView
    var flag = true
    private val topicIds = arrayOf("t1",  "t2", "t3", "t4", "t5", "t11", "t10")
    private var now = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val paser = HVMLParser(resources, "hvml/other/jp_co_sharp_sample_simple_talk.hvml")
        val model = paser.parse()

        placement = HVMLPlacement(model)
        placement.setHVMLPlacementListener(this)
        placement.createTree()

        horizontalScrollView = findViewById(R.id.horizontal)
        scrollView = findViewById(R.id.vertical)

        button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            val point = placement.setTopicViewBackground(topicIds[now], Color.BLUE)
            Log.d("-------points-------", point.toString())
            if (now != 0) placement.setTopicViewBackground(topicIds[now - 1], Color.WHITE)
            else placement.setTopicViewBackground(topicIds[topicIds.count() - 1], Color.WHITE)
            if (now == topicIds.count() - 1) now = 0
            else now++

            if (point != null) {
                horizontalScrollView.smoothScrollBy(point.x, point.y)
                scrollView.smoothScrollBy(point.x, point.y)
            }

        }
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