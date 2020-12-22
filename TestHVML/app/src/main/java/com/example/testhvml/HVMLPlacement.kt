package com.example.testhvml

import android.util.Log
import android.view.View
import android.view.View.generateViewId
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat

class HVMLPlacement (private val hvmlModel: HvmlModel) {

    interface HVMLPlacementListener{
        fun newTopicLayout(topic: Topic) : View
        fun newArrowView(): View
        fun getRootLayout(): ConstraintLayout
        fun addArrowView(arrowView: View)
    }

    private var mHVMLPlacementListener: HVMLPlacementListener? = null

    /**
     * HVMLPlacementListenerのゲッター
     *
     * @param listener HVMLPlacementListener
     */
    fun setHVMLPlacementListener(listener: HVMLPlacementListener) {
        this.mHVMLPlacementListener = listener
    }

    fun createTree(): List<List<View>> {
        var treedTopics = listOf<List<View>>()

        hvmlModel.head?.situation?.topicId?.let {
            val topic =  topicFromId(it)
            if (topic != null) {
                treedTopics = addTopics(topic, 0)
            }
        }

        setRootConstraint(treedTopics)

        return treedTopics
    }

    /**
     * パースしたHVMLをレイアウト
     *
     * @param topic Topic
     * @param col   列
     */
    @Suppress("NAME_SHADOWING")
    private fun addTopics(topic: Topic, col: Int) : MutableList<MutableList<View>>  {
        var treedTopics = mutableListOf<MutableList<View>>()
        val topicLayout = mHVMLPlacementListener?.newTopicLayout(topic)
        (0..col + 1).forEach {
            treedTopics.add( mutableListOf() )
        }
        treedTopics[col].add(topicLayout!!)

        mHVMLPlacementListener?.getRootLayout()?.addView(topicLayout)


        //topicのanchorとtopic配列を合成
        val segues: List<Topic.Segue?> = topic.anchors // + topic.next
        segues.forEachIndexed { index, segue ->
            segue?.href?.let { href ->
               val nextTopic = topicFromId(href.replace("#", ""))
                if (nextTopic != null) {
                    val dstTopics = addTopics(nextTopic, col + 1)
                    treedTopics = mergeListList(treedTopics, dstTopics)
                }
            }
        }

        val lineTreedTopics =  treedTopics[col + 1]
        (0 until segues.count()).forEach { index ->
            val now = lineTreedTopics.count() - segues.count() + index
            //行の要素が１つのとき
            if (lineTreedTopics.count() == 1) addConstraint(lineTreedTopics[now], topicLayout.id, null, null)
            else {

                if (index == 0) {
                    //行の上
                    if (now == 0) addConstraint(lineTreedTopics[now], topicLayout.id, null, null)
                    //上にまだあるとき
                    else {
                        addConstraint(lineTreedTopics[now], topicLayout.id, lineTreedTopics[now - 1].id, lineTreedTopics[now + 1].id)
                        addConstraint(lineTreedTopics[now - 1], topicLayout.id, null, lineTreedTopics[now + 1].id)
                    }
                }
                //行の一番下
                else if (index == segues.count() - 1) addConstraint(lineTreedTopics[now], topicLayout.id, lineTreedTopics[now - 1].id, null)

                else addConstraint(lineTreedTopics[now], topicLayout.id, lineTreedTopics[now - 1].id, lineTreedTopics[now + 1].id)
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
     */
    private fun addConstraint(view: View, startId: Int?, topId: Int?, bottomId: Int?) {
        val arrowView = mHVMLPlacementListener!!.newArrowView()
        mHVMLPlacementListener?.getRootLayout()?.addView(arrowView)
        // ConstraintSetを生成してConstraintLayoutから制約を複製する
        val constraintSet = ConstraintSet()
        constraintSet.clone(mHVMLPlacementListener?.getRootLayout())

        //Topicの左
        if (startId != null)  {
            //mainView
            constraintSet.connect(view.id, ConstraintSet.START, arrowView.id, ConstraintSet.END)
            //starView
            constraintSet.connect(startId, ConstraintSet.END, arrowView.id, ConstraintSet.START)
            //矢印のConstraint
            constraintSet.connect(arrowView.id, ConstraintSet.BOTTOM, startId, ConstraintSet.BOTTOM)
            constraintSet.connect(arrowView.id, ConstraintSet.START, startId, ConstraintSet.END)
            constraintSet.connect(arrowView.id, ConstraintSet.TOP, view.id, ConstraintSet.TOP)
            constraintSet.connect(arrowView.id, ConstraintSet.END, view.id, ConstraintSet.START)
        }

        //Topicの上
        if (topId != null)  constraintSet.connect(view.id, ConstraintSet.TOP, topId, ConstraintSet.BOTTOM)

        //Topicの下
        if (bottomId != null) constraintSet.connect(view.id, ConstraintSet.BOTTOM, bottomId, ConstraintSet.TOP)

        //制約を実行
        constraintSet.applyTo(mHVMLPlacementListener?.getRootLayout())
    }

    /**
     * 端にあるtopicViewとrootConstraintLayoutとのConstraintを適用
     *
     * @param treedLayout 配置済みのList<List<View>>
     */
    private fun setRootConstraint(treedLayout: List<List<View>> ) {

        val constraintSet = ConstraintSet()
        constraintSet.clone(mHVMLPlacementListener?.getRootLayout())

        treedLayout.forEachIndexed { i, lineTreedLayout ->
            lineTreedLayout.forEachIndexed { index, view ->
                if (index == 0) {
                    if (i == 0) constraintSet.connect(view.id, ConstraintSet.START, mHVMLPlacementListener?.getRootLayout()!!.id, ConstraintSet.START)
                    constraintSet.connect(view.id, ConstraintSet.TOP, mHVMLPlacementListener?.getRootLayout()!!.id, ConstraintSet.TOP)
                }
                if (index == lineTreedLayout.count()) constraintSet.connect(view.id, ConstraintSet.END, mHVMLPlacementListener?.getRootLayout()!!.id, ConstraintSet.END)
            }

        }
        //制約を実行
        constraintSet.applyTo(mHVMLPlacementListener?.getRootLayout())
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
        val returnListList = mutableListOf<MutableList<T>>()

        if ( preListList.count() < dstListList.count()) {
            (0 until dstListList.count()).forEach { index ->
                returnListList.add(mutableListOf())
                if (index < preListList.count()) returnListList[index] = (dstListList[index] + preListList[index]) as MutableList<T>
                else returnListList[index] = dstListList[index]
            }
        } else {
            (0 until preListList.count()).forEach { index ->
                returnListList.add(mutableListOf())
                if (index < dstListList.count()) returnListList[index] = (preListList[index] + dstListList[index]) as MutableList<T>
                else returnListList[index] = preListList[index]
            }
        }

        return returnListList
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

    public fun rotateArrowView() {

    }
}




