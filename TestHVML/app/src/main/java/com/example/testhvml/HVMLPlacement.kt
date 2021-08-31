package com.example.testhvml

import android.graphics.Point
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import kotlin.math.atan2

class HVMLPlacement (private val hvmlModel: HvmlModel) {

    object TreeSingleton{

        /**
         * arrowViewの親子関係
         *
         * @param startView  arrowViewの親（arrowViewの左）
         * @param targetView arrowViewの子（arrowViewの右）
         * @param arrowView  arrowView
         */
        class ArrowRelation(
            val startView: View,
            val targetView: View,
            val arrowView: View
        )

        /**
         * topicViewと対応するview
         *
         * @param topic
         * @param topicView
         */
        class TopicRelation(
                val topic: Topic,
                val topicView: View
        )

        private var topicLayoutTree = mutableListOf<MutableList<TopicRelation>>()
        var arrowRelations = mutableListOf<ArrowRelation>()

        private fun addCol(col: Int) {
            while (topicLayoutTree.count() <= col) {
                topicLayoutTree.add( mutableListOf() )
            }
        }

        fun addValue(col: Int, view: View, topic: Topic) {
            if (topicLayoutTree.count() <= col) addCol(col)
            topicLayoutTree[col].add(TopicRelation(topic, view))
        }

        /**
         * 新しいArrowRelationを作成
         *
         * @param startView
         * @param targetView
         * @param arrowView
         */
        fun addArrowRelation(startView: View, targetView: View, arrowView: View) {
            arrowRelations.add(
                ArrowRelation(startView, targetView, arrowView)
            )
        }

        /**
         * 全部返す
         */
        fun getTree(): List<List<TopicRelation>> {
            return topicLayoutTree
        }

        /**
         * 行を返す
         *
         * @param col 行のInt
         */
        fun getCol(col: Int): List<TopicRelation> {
            if (topicLayoutTree.count() <= col) addCol(col)
            return topicLayoutTree[col]
        }

        /**
         * topicRelationを返す
         */
        fun  getTopicRelation(col: Int, row: Int): TopicRelation {
            return topicLayoutTree[col][row]
        }

        fun  getTopicRelation(topicId: String): TopicRelation? {
            topicLayoutTree.forEach { lineTopicLayout ->
                lineTopicLayout.forEach { topicRelation ->
                    if (topicRelation.topic.id == topicId) return topicRelation
                }
            }

            return null
        }
    }

    /**
     * layout系のListener
     */
    interface HVMLPlacementListener{
        fun newTopicLayout(topic: Topic) : View
        fun newArrowView(): View
        fun getRootLayout(): ConstraintLayout
        fun addArrowView(arrowView: View)
    }

    private var mHVMLPlacementListener: HVMLPlacementListener? = null
    private var mTreeSingleton = TreeSingleton

    /**
     * HVMLPlacementListenerのゲッター
     *
     * @param listener HVMLPlacementListener
     */
    fun setHVMLPlacementListener(listener: HVMLPlacementListener) {
        this.mHVMLPlacementListener = listener
    }

    fun createTree() {
        hvmlModel.head?.situation?.topicId?.let {
            val topic =  this.hvmlModel.topicFromId(it)
            if (topic != null) {
                addTopics(topic, 0)
            }
        }

        setRootConstraint(mTreeSingleton.getTree())
    }

    /**
     * パースしたHVMLをレイアウト
     *
     * @param topic Topic
     * @param col   列
     */
    @Suppress("NAME_SHADOWING")
    private fun addTopics(topic: Topic, col: Int) {
        val topicLayout = mHVMLPlacementListener?.newTopicLayout(topic)

        mTreeSingleton.addValue(col, topicLayout!!, topic)
        mHVMLPlacementListener?.getRootLayout()?.addView(topicLayout)


        //topicのanchorとtopic配列を合成
        val segues: List<Topic.Segue?> = topic.anchors + topic.nexts
        segues.forEachIndexed { _, segue ->
            segue?.href?.let { href ->
               val nextTopic = this.hvmlModel.topicFromId(href.replace("#", ""))
                if (nextTopic != null) {
                    addTopics(nextTopic, col + 1)
                }
            }
        }

        val lineTreedTopics = mTreeSingleton.getCol(col + 1)

        (0 until segues.count()).forEach { index ->
            val arrowView = mHVMLPlacementListener!!.newArrowView()
            mHVMLPlacementListener?.getRootLayout()?.addView(arrowView)

            val now = lineTreedTopics.count() - segues.count() + index
            val targetId = lineTreedTopics[now].topicView.id
            val startId = topicLayout.id
            val topId = lineTreedTopics.getOrNull(now - 1)?.topicView?.id
            val bottomId = lineTreedTopics.getOrNull(now + 1)?.topicView?.id

            addConstraint(targetId, startId, topId, bottomId, arrowView.id)
            if (index == 0 && now != 0 && topId != null) addConstraint(topId, null, null, targetId, null)
            mTreeSingleton.addArrowRelation(topicLayout, lineTreedTopics[now].topicView, arrowView)
        }
    }

    /**
     * Constraint Layoutを計算する
     *
     * @param targetId      対象のTopic
     * @param startId   対象のTopicの左にあるTopicのID
     * @param topId     対象のTopicの上にあるTopicのID
     * @param bottomId　対象のTopicの下にあるTopicのID
     */
    private fun addConstraint(targetId: Int, startId: Int?, topId: Int?, bottomId: Int?, arrowViewId: Int?) {
        // ConstraintSetを生成してConstraintLayoutから制約を複製する
        val constraintSet = ConstraintSet()
        constraintSet.clone(mHVMLPlacementListener?.getRootLayout())

        //Topicの左
        if (startId != null && arrowViewId != null )  {
            //mainView
            constraintSet.connect(targetId, ConstraintSet.START, arrowViewId, ConstraintSet.END)
            //starView
            constraintSet.connect(startId, ConstraintSet.END, arrowViewId, ConstraintSet.START)
            //矢印のConstraint
            constraintSet.connect(arrowViewId, ConstraintSet.BOTTOM, startId, ConstraintSet.BOTTOM)
            constraintSet.connect(arrowViewId, ConstraintSet.START, startId, ConstraintSet.END)
            constraintSet.connect(arrowViewId, ConstraintSet.TOP, targetId, ConstraintSet.TOP)
            constraintSet.connect(arrowViewId, ConstraintSet.END, targetId, ConstraintSet.START)
        }

        //Topicの上
        if (topId != null)  constraintSet.connect(targetId, ConstraintSet.TOP, topId, ConstraintSet.BOTTOM)

        //Topicの下
        if (bottomId != null) constraintSet.connect(targetId, ConstraintSet.BOTTOM, bottomId, ConstraintSet.TOP)

        //制約を実行
        constraintSet.applyTo(mHVMLPlacementListener?.getRootLayout())
    }

    /**
     * 端にあるtopicViewとrootConstraintLayoutとのConstraintを適用
     *
     * @param treedLayout 配置済みのList<List<View>>
     */
    private fun setRootConstraint(treedLayout: List<List<TreeSingleton.TopicRelation>> ) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(mHVMLPlacementListener?.getRootLayout())

        treedLayout.forEachIndexed { i, lineTreedLayout ->
            lineTreedLayout.forEachIndexed { index, topicRelation ->
                if (index == 0) {
                    if (i == 0) constraintSet.connect(topicRelation.topicView.id, ConstraintSet.START, mHVMLPlacementListener?.getRootLayout()!!.id, ConstraintSet.START)
                    constraintSet.connect(topicRelation.topicView.id, ConstraintSet.TOP, mHVMLPlacementListener?.getRootLayout()!!.id, ConstraintSet.TOP)
                }
                if (index == lineTreedLayout.count() - 1) constraintSet.connect(topicRelation.topicView.id, ConstraintSet.BOTTOM, mHVMLPlacementListener?.getRootLayout()!!.id, ConstraintSet.BOTTOM)
            }

        }
        //制約を実行
        constraintSet.applyTo(mHVMLPlacementListener?.getRootLayout())
    }

    fun rotateArrowView() {
        mTreeSingleton.arrowRelations.forEach { arrowRelation ->
            val preLayout = arrowRelation.startView.findViewById<TextView>(R.id.TopicName)
            val dstLayout = arrowRelation.targetView.findViewById<TextView>(R.id.TopicName)
            val rad = getRadianDegree(
                arrowRelation.targetView.getLocationPointInWindow(),
                arrowRelation.startView.getLocationPointInWindow()).toString()

            Log.d("------pre-------", preLayout.text as String)
            Log.d("------dst-------", dstLayout.text as String)
            Log.d("------Rad-------", rad)

            arrowRelation.arrowView.rotation = getRadianDegree(
                arrowRelation.startView.getLocationPointInWindow(),
                arrowRelation.targetView.getLocationPointInWindow()
            )
        }
    }

    fun setTopicViewBackground(topicId: String, color: Int): Point? {
        val topicRelation = this.mTreeSingleton.getTopicRelation(topicId)
        topicRelation?.topicView?.setBackgroundColor(color)
        return topicRelation?.topicView?.getLocationPointInWindow()
    }

    private fun getRadianDegree(pre: Point, dst: Point): Float {
        return (atan2(((dst.y - pre.y).toDouble()), ((dst.x - pre.x).toDouble())) * 180.0 / Math.PI ).toFloat() * 2
    }

    private fun View.getLocationPointInWindow(): Point {
        val array = IntArray(2)
        this.getLocationInWindow(array)
        return Point(array[0], array[1])
    }

}




