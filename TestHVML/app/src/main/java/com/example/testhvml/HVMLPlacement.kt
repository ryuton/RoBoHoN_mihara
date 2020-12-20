package com.example.testhvml

import android.view.View
import android.view.View.generateViewId
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet


class HVMLPlace(view: View, startId: Int?, topId: Int?, bottomId: Int?, arrowView: View?)

class HVMLPlacement (rootLayoutId: Int,
                     private val topicLayoutId: Int,
                     private val topicNameId: Int,
                     private val anchorNameId: Int,
                     private val nextNameId: Int,
                     private val activity: AppCompatActivity,
                     private val hvmlModel: HvmlModel
                     ) {

    private var rootLayout = activity.findViewById<ConstraintLayout>(rootLayoutId)
    var treedTopics = createTree()

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

    private fun newTopicLayout(topic: Topic,
                               topicLayoutId: Int,
                               topicNameId: Int,
                               anchorNameId: Int,
                               nextNameId: Int
    ) : View {
        val topicLayout = this.activity.findViewById<View>(topicLayoutId)
        topicLayout.id = generateViewId()

        val topicName = topicLayout.findViewById<TextView>(topicNameId)
        topicName.text = topic.actions.first().speech

        val anchorName = topicLayout.findViewById<TextView>(anchorNameId)
        var anchorText = ""
        topic.anchors.forEach { anchor ->
            anchorText += anchor.href
        }
        anchorName.text = anchorText

        val nextName = topicLayout.findViewById<TextView>(nextNameId)
        nextName.text = topic.next?.href

        return topicLayout
    }

    /**
     * パースしたHVMLをレイアウト
     *
     * @param topic Topic
     * @param col 列
     */
    @Suppress("NAME_SHADOWING")
    private fun addTopics(topic: Topic, col: Int) : MutableList<MutableList<Topic>>  {
        val treedTopics = mutableListOf<MutableList<Topic>>()
        val topicLayout = newTopicLayout(topic, topicLayoutId, )
        topicLayout.id = generateViewId()
        rootLayout.addView(topicLayout)
        treedTopics[col].add(topic)


        val segues: List<Topic.Segue?> = topic.anchors + topic.next

        segues.forEachIndexed { index, segue ->
            segue?.href?.let { href ->
               val topic = topicFromId(href)
                if (topic != null) addTopics(topic, col + 1)
            }
        }

        return treedTopics
    }

    /**
     * Constraint Layoutを計算する
     *
     * @param view      対象のTopic
     * @param startId   対象のTopicの左にあるTopicのID
     * @param topId     対象のTopicの上にあるTopicのID
     * @param bottomId　対象のTopicの下にあるTopicのID
     * @param arrowView 対象のTopicと親のTopicの関係を表す矢印
     */
    private fun addConstraint(view: View, startId: Int?, topId: Int?, bottomId: Int?, arrowView: View?) {
        this.rootLayout.addView(view)
        // ConstraintSetを生成してConstraintLayoutから制約を複製する
        val constraintSet = ConstraintSet()
        constraintSet.clone(rootLayout)

        //Topicの左
        if (startId == null) constraintSet.connect(view.id, ConstraintSet.START, this.rootLayout.id, ConstraintSet.START)
        else constraintSet.connect(view.id, ConstraintSet.START, startId, ConstraintSet.START)

        //Topicの上
        if (topId == null) constraintSet.connect(view.id, ConstraintSet.TOP, this.rootLayout.id, ConstraintSet.TOP)
        else constraintSet.connect(view.id, ConstraintSet.TOP, topId, ConstraintSet.END)

        //Topicの下
        if (bottomId == null) constraintSet.connect(view.id, ConstraintSet.BOTTOM, this.rootLayout.id, ConstraintSet. BOTTOM)
        else constraintSet.connect(view.id, ConstraintSet.BOTTOM, bottomId, ConstraintSet.TOP)

        //矢印のConstraint
        if (arrowView != null && startId != null) {
            constraintSet.connect(arrowView.id, ConstraintSet.BOTTOM, startId, ConstraintSet.BOTTOM)
            constraintSet.connect(arrowView.id, ConstraintSet.START, startId, ConstraintSet.END)
            constraintSet.connect(arrowView.id, ConstraintSet.TOP, view.id, ConstraintSet.TOP)
            constraintSet.connect(arrowView.id, ConstraintSet.END, view.id, ConstraintSet.START)
        }

        //制約を実行
        constraintSet.applyTo(this.rootLayout)
    }

    /**
     * ListofListをマージする関数
     *
     * @param preListList ListOfList
     * @param dstListList ListOfList
     *
     * @return            マージ後のListOfList
     */
    private fun <T> mergeListList(preListList: MutableList<MutableList<T>>, dstListList: MutableList<MutableList<T>>): MutableList<MutableList<T>> {
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

    /**
     * IDを指定してTopicsの配列からTopic一つ取り出す
     *
     * @param id TopicのID
     */
    private fun topicFromId(id: String): Topic? {
        this.hvmlModel.topics.forEach {
            if (it.id == id) return  it
        }

        return null
    }
}




