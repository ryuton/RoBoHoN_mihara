package com.example.testhvml

import android.view.View
import android.view.View.generateViewId
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

class HVMLPlacement (private val rootLayoutId: Int,
                     private val topicLayoutId: Int,
                     private val activity: AppCompatActivity,
                     private val hvmlModel: HvmlModel
                     ) {

    private var rootLayout = activity.findViewById<ConstraintLayout>(rootLayoutId)
    var treedTopics = createTree()

    fun placement(){

    }

    private fun createTree(): List<List<Topic>> {

        var treedTopics = listOf<List<Topic>>()

        hvmlModel.head?.situation?.topicId?.let {
            val topic =  topicFromId(it)
            if (topic != null) {
                treedTopics = addTopics(topic, 0)
            }
        }

        return treedTopics
    }



    private fun addTopics(topic: Topic, col: Int) : MutableList<MutableList<Topic>>  {
        var treedTopics = mutableListOf<MutableList<Topic>>()
        var topicLayout = this.activity.findViewById<View>(topicLayoutId)
        topicLayout.id = generateViewId()
        treedTopics[col].add(topic)
        rootLayout.addView(topicLayout)

        val segue: List<Topic.Segue?> = topic.anchors + topic.next

        segue.forEach {
            it?.href?.let { href ->
               val topic = topicFromId(href)
                if (topic != null) addTopics(topic, col + 1)
            }
        }

        return treedTopics
    }

    private fun mergeListList(preListList: MutableList<MutableList<Topic>>, dstListList: MutableList<MutableList<Topic>>) {
        var returnListList = mutableListOf<MutableList<Topic>>()

        if ( preListList.count() < dstListList.count()) {
            (0..preListList.count()).forEach { index ->
                returnListList[index] = (preListList[index] + dstListList[index]) as MutableList<Topic>
            }
        } else {
            (0..dstListList.count()).forEach { index ->
                returnListList[index] = (preListList[index] + dstListList[index]) as MutableList<Topic>
            }
        }
    }

    private fun addConstraint(view: View, startId: Int?, topId: Int?, bottomId: Int?, arrowView: View?) {
        this.rootLayout.addView(view)
        // ConstraintSetを生成してConstraintLayoutから制約を複製する
        val constraintSet = ConstraintSet()
        constraintSet.clone(rootLayout)

        if (startId == null) constraintSet.connect(view.id, ConstraintSet.START, this.rootLayout.id, ConstraintSet.START)
        else constraintSet.connect(view.id, ConstraintSet.START, startId, ConstraintSet.START)

        if (topId == null) constraintSet.connect(view.id, ConstraintSet.TOP, this.rootLayout.id, ConstraintSet.TOP)
        else constraintSet.connect(view.id, ConstraintSet.TOP, topId, ConstraintSet.END)

        if (bottomId == null) constraintSet.connect(view.id, ConstraintSet.BOTTOM, this.rootLayout.id, ConstraintSet. BOTTOM)
        else constraintSet.connect(view.id, ConstraintSet.BOTTOM, bottomId, ConstraintSet.TOP)

        if (arrowView != null && startId != null) {
            constraintSet.connect(arrowView.id, ConstraintSet.BOTTOM, startId, ConstraintSet.BOTTOM)
            constraintSet.connect(arrowView.id, ConstraintSet.START, startId, ConstraintSet.END)
            constraintSet.connect(arrowView.id, ConstraintSet.TOP, view.id, ConstraintSet.TOP)
            constraintSet.connect(arrowView.id, ConstraintSet.END, view.id, ConstraintSet.START)
        }

        constraintSet.applyTo(this.rootLayout)
    }

    private fun topicFromId(id: String): Topic? {
        this.hvmlModel.topics.forEach {
            if (it.id == id) return  it
        }

        return null
    }
}




