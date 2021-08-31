package com.example.robohonreception.hvml.tag;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "topic")
public class Topic {

    //situation,accost タグで遷移する topic を指定
    @Attribute
    private String id;
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    //ユーザからの発話を受け付けるかどうかを指定
    @Attribute
    private String listen;
    public String getListen() { return listen; }
    public void setListen(String listen) { this.listen = listen; }

    //listen="true"の場合に、ユーザ発話を受け付ける最大時間を msec 単位で指定
    @Attribute(name = "listen_ms", required = false)
    private int listenMs = 10;
    public int getListenMs() { return listenMs; }
    public void setListenMs(int listenMs) { this.listenMs = listenMs; }

    //肯定/否定の聞き取りを行う
    @Attribute(required = false)
    private String dict;
    public String getDict() { return dict; }
    public void setDict(String dict) { this.dict = dict; }

    //topic 実行時に、その時の条件によって実行するアクション等を変えたい場合に使用
    @Element(required = false)
    private Rule rule;
    public Rule getRule() { return rule; }
    public void setRule(Rule rule) { this.rule = rule; }

    //rule タグに対応する操作を記述します
    @ElementList(inline = true, required = false)
    private List<Case> cases;
    public List<Case> getCases() { return cases; }
    public void setCases(List<Case> cases) { this.cases = cases; }

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
