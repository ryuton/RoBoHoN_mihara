package com.example.robohonreception.hvml.tag;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root()
public class Data {
    //任意の文字列、ひとつの親に一意
    @Attribute()
    private String key;
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    @Attribute()
    private String value;
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
