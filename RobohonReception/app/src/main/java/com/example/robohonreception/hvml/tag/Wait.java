package com.example.robohonreception.hvml.tag;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "wait")
public class Wait {
    @Attribute
    private int ms;
    public int getMs() { return ms; }
    public void setMs(int ms) { this.ms = ms; }
}
