package com.example.robohonreception.hvml.tag;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "case")
public class Case {
    //rule で選択される case を指定するための id
    @Attribute
    private String id;
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    //回数制限を行いたい場合に指定
    @Attribute(required = false)
    private int limit;
    public int getLimit() { return limit; }
    public void setLimit(int limit) {
        if ( 0 < limit) this.limit = limit;
    }

    //回数制限を解除するまでの日数
    @Attribute(name = "cleardays", required = false)
    private int clearDays;
    public int getClearDays() { return clearDays; }
    public void setClearDays(int clearDays) { this.clearDays = clearDays; }

    //HVML 実行時に実施される操作に関する内容を記述
    @ElementList(inline = true, required = false)
    private List<Action> actions;
    public List<Action> getActions() { return actions; }
    public void setActions(List<Action> actions) { this.actions = actions; }

    //指定した遷移先にジャンプ
    @ElementList(inline = true, required = false)
    private List<A> anchors;
    public List<A> getAnchors() { return anchors; }
    public void setAnchors(List<A> anchors) { this.anchors = anchors; }

    //topic 実行後、対話を終了せずに別の topic に遷移させたい場合に遷移先を記載
    @Element(required = false)
    private Next next;
    public Next getNext() { return next; }
    public void setNext(Next next) { this.next = next; }
}
