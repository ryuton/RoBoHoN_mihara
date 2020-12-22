package com.example.testhvml

import android.content.res.Resources
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

const val TAG = "PARSE_HVML"
private val ns: String? = null

class HVMLParser(resources: Resources, fileName: String) {

    val hvmlString = resources.assets.open(fileName)

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(): HvmlModel {
        hvmlString.use { inputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            var head: Head? = null
            var topics = listOf<Topic>()
            parser.nextTag() // <?xml version="1.0" ?>
            parser.require(XmlPullParser.START_TAG, ns, "hvml")
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) {
                    continue
                }
                when (parser.name) {
                    "body" -> topics = readTopics(parser)
                    "head" -> head = readHead(parser)
                    else -> skip(parser)
                }
            }
            return HvmlModel(head, topics)
            //return readHead(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readTopics (parser: XmlPullParser): List<Topic> {
        val topics = mutableListOf<Topic>()
        parser.require(XmlPullParser.START_TAG, ns, "body")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            // Starts by looking for the entry tag
            if (parser.name == "topic") {
                topics.add(readTopic(parser))
            } else {
                skip(parser)
            }
        }

        return topics
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readTopic (parser: XmlPullParser): Topic {
        parser.require(XmlPullParser.START_TAG, ns, "topic")

        var id: String? = null
        var case: Topic.Case? = null
        var rule: Topic.Rule? = null
        val actions = mutableListOf<Topic.Action>()
        val anchors = mutableListOf<Topic.Anchor>()
        var next: Topic.Next? = null

        id = parser.getAttributeValue(null, "id")

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "case" -> case = readCase(parser)
                "role" -> rule = readRule(parser)
                "action" -> actions.add(readAction(parser))
                "a" -> anchors.add(readAnchor(parser))
                "next" -> next = readNext(parser)
                else -> skip(parser)
            }
        }

        val topic = Topic(id, case, rule, actions, anchors, next)

        return topic
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readCase (parser: XmlPullParser): Topic.Case {
        parser.require(XmlPullParser.START_TAG, ns, "case")

        var id: String? = null
        val actions = mutableListOf<Topic.Action>()
        val anchors = mutableListOf<Topic.Anchor>()
        var next: Topic.Next? = null

        id = parser.getAttributeValue(null, "id")

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "action" -> actions.add(readAction(parser))
                "a" -> anchors.add(readAnchor(parser))
                "next" -> next = readNext(parser)
                else -> skip(parser)
            }
        }

        val case = Topic.Case(id, actions, anchors, next)

        return case
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readRule (parser: XmlPullParser): Topic.Rule {
        parser.require(XmlPullParser.START_TAG, ns, "rule")
        var conditions = mutableListOf<Topic.Rule.Condition>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "action" -> conditions.add(readCondition(parser))
                else -> skip(parser)
            }
        }

        return Topic.Rule(conditions)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readAction (parser: XmlPullParser): Topic.Action {
        parser.require(XmlPullParser.START_TAG, ns, "action")

        var index: String? = null
        var speech: String? = null
        index = parser.getAttributeValue(null, "index")

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "speech" -> speech = readText(parser, "speech")
                else -> skip(parser)
            }
        }

        return Topic.Action(index, speech)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readAnchor (parser: XmlPullParser): Topic.Anchor {
        parser.require(XmlPullParser.START_TAG, ns, "a")

        var href: String? = null
        var type: String? = null
        href = parser.getAttributeValue(null, "href")
        type = parser.getAttributeValue(null, "type")

        skip(parser)

        parser.require(XmlPullParser.END_TAG, ns, "a")

        return Topic.Anchor(href, type)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readNext (parser: XmlPullParser): Topic.Next {
        parser.require(XmlPullParser.START_TAG, ns, "next")

        var href: String? = null
        var type: String? = null
        href = parser.getAttributeValue(null, "href")
        type = parser.getAttributeValue(null, "type")

        if (parser.next() != XmlPullParser.END_TAG) {
            parser.nextTag()
        }

        parser.require(XmlPullParser.END_TAG, ns, "next")

        return Topic.Next(href, type)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readCondition (parser: XmlPullParser): Topic.Rule.Condition {
        parser.require(XmlPullParser.START_TAG, ns, "a")

        var caseId: String? = null
        var text: String? = null
        caseId = parser.getAttributeValue(null, "case_id")
        text = readText(parser, "condition")

        if (parser.next() != XmlPullParser.END_TAG) {
            parser.nextTag()
        }

        parser.require(XmlPullParser.END_TAG, ns, "a")

        return Topic.Rule.Condition(caseId, text)
    }


    @Throws(XmlPullParserException::class, IOException::class)
    fun readHead (parser: XmlPullParser): Head {
        parser.require(XmlPullParser.START_TAG, ns, "head")
        var producer: String? = null
        var description: String? = null
        var toolVersion: String? = null
        var scene: Head.Scene? = null
        var accost: Head.Accost? = null
        var situation: Head.Situation? = null
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "producer" -> producer = readText(parser, "producer")
                "description" -> description = readText(parser, "description")
                "tool_version" -> toolVersion = readText(parser, "tool_version")
                "scene" -> scene = readScene(parser)
                "situation" -> situation = readSituation(parser)
                "accost" -> accost = readAccost(parser)
                else -> skip(parser)
            }
        }
        return Head(producer, description, toolVersion, scene, situation, accost)

    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readScene(parser: XmlPullParser): Head.Scene {
        var value: String? = null
        parser.require(XmlPullParser.START_TAG, ns, "scene")
        value = parser.getAttributeValue("", "value")
        parser.nextTag()
        parser.require(XmlPullParser.END_TAG, ns, "scene")
        return Head.Scene(value)

    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readSituation(parser: XmlPullParser): Head.Situation {
        var priority: String? = null
        var topicId: String? = null
        var trigger: String? = null

        parser.require(XmlPullParser.START_TAG, ns, "situation")
        priority = parser.getAttributeValue(null, "priority")
        topicId = parser.getAttributeValue(null, "topic_id")
        trigger = parser.getAttributeValue(null, "trigger")

        if (parser.next() != XmlPullParser.END_TAG) {
            parser.nextTag()
        }

        parser.require(XmlPullParser.END_TAG, ns, "situation")

        return Head.Situation(priority, topicId, trigger)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readAccost(parser: XmlPullParser): Head.Accost {
        var priority: String? = null
        var topicId: String? = null
        var word: String? = null

        parser.require(XmlPullParser.START_TAG, ns, "accost")
        priority = parser.getAttributeValue(null, "priority")
        topicId = parser.getAttributeValue(null, "topic_id")
        word = parser.getAttributeValue(null, "word")

        if (parser.next() != XmlPullParser.END_TAG) {
            parser.nextTag()
        }

        parser.require(XmlPullParser.END_TAG, ns, "accost")

        return Head.Accost(priority, topicId, word)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    fun readText(parser: XmlPullParser, tagName: String): String {
        parser.require(XmlPullParser.START_TAG, ns, tagName)

        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }

        parser.require(XmlPullParser.END_TAG, ns, tagName)
        return result
    }
}