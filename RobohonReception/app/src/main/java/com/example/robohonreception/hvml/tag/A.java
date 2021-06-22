package com.example.robohonreception.hvml.tag;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "a")
public class A {
    //遷移先を指定
    @Attribute
    private String href;
    public String getHref() { return href; }
    public void setHref(String href) { this.href = href; }

    //他のアンカーに合致しない場合の遷移を記述したい場合は、type="default"を指定
    @Attribute(required = false)
    private String type;
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    //実行する topic を選択するための契機および条件を記述
    @ElementList(inline = true)
    private List<Situation> situations;
    public List<Situation> getSituations() { return situations; }
    public void setSituations(List<Situation> situations) { this.situations = situations; }
}
