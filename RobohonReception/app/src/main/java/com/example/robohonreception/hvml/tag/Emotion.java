package com.example.robohonreception.hvml.tag;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "emotion")
public class Emotion {

    //感情の種別を指定
    @Attribute
    private EmotionType type = EmotionType.happiness;
    public EmotionType getType() { return type; }
    public void setType(String type) {
        try {
            this.type = EmotionType.valueOf(type);
        }catch (IllegalArgumentException e) {
            System.out.println("emotion.type is illegal");
        }
    }

    //type で指定した感情の度合いを指定
    @Attribute(required = false)
    private int level = 1;
    public int getLevel() { return level; }
    public void setLevel(int level) { if ( 0 < level && level < 5) this.level = level; }

    @Text
    private String value;
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}