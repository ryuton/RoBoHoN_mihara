package com.example.robohonreception.hvml.tag;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "accost")
class Accost {
    @Attribute
    public String priority;
    @Attribute(name = "topic_id")
    public String topicID;
    @Attribute
    public String word;
}