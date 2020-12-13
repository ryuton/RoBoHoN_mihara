package com.example.testhvml

data class HvmlModel (
    var head: Head?,
    var topics: List<Topic>
)

data class Head (
    var producer: String?,
    var description: String?,
    var toolVersion: String?,
    var scene: Scene?,
    var situation: Situation?,
    var accost: Accost?
){
    data class Scene (
        val value: String?
    )

    data class Situation (
        val priority: String?,
        val topicId: String?,
        val trigger: String?
    )

    data class Accost (
        val priority: String?,
        val topicId: String?,
        val word: String?
    )
}

data class Topic(
    var id: String?,
    var case: Case?,
    var rule: Rule?,
    var actions: List<Action>,
    var anchors: List<Anchor>,
    var next: Next?
) {
    data class Rule (
        var conditions: List<Condition>
    ) {
        data class Condition(
            var caseId: String,
            var text: String
        )
    }

    data class Case (
        var id: String?,
        var actions: List<Action>,
        var anchors: List<Anchor>,
        var next: Next?
    )

    data class Action(
        var index: String?
    )

    data class Anchor (
            override var href: String?,
            override var type: String?
    ): Segue

    data class Next (
            override var href: String?,
            override var type: String?
    ): Segue

    interface Segue {
        var href: String?
        var type: String?
    }
}