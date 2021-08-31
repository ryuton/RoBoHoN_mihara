package com.example.robohonreception.hvml.tag;

import org.simpleframework.xml.*;

@Root(name = "hvml")
public class HVML {

    @Attribute
    private Double version;
    public Double getVersion() { return version; }
    public void setVersion(Double version) { this.version = version; }

    @Attribute(required = false)
    private String xmlns;
    public String getXmlns() { return xmlns; }
    public void setXmlns(String xmlns) { this.xmlns = xmlns; }

    @Element
    private Head head;
    public Head getHead() { return head; }
    public void setHead(Head head) { this.head = head; }

    @Element(required = false)
    private Body body;
    public Body getBody() { return body; }
    public void setBody(Body body) { this.body = body; }
}





