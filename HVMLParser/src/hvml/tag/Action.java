package hvml.tag;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "action")
public class Action {
    //数字が小さい順に再生
    @Attribute
    private int index;
    public int getIndex() { return index; }
    public void setIndex(int index) { if( 0 < index)this.index = index; }

    //TTS を使用してロボホンに発話
    @Element(required = false)
    private Speech speech;
    public Speech getSpeech() { return speech; }
    public void setSpeech(Speech speech) { this.speech = speech; }

    //発話に合わせて行うモーションを指定
    @Element(required = false)
    private Behavior behavior;
    public Behavior getBehavior() { return behavior; }
    public void setBehavior(Behavior behavior) { this.behavior = behavior; }

    //アプリとの連携の際に利用
    @ElementList(inline = true, required = false)
    private List<Control> control;
    public List<Control> getControl() { return control; }
    public void setControl(List<Control> control) { this.control = control; }

    //情報を記憶させたい場合に使用
    @ElementList(inline = true, required = false)
    private List<Memory> memories;
    public List<Memory> getMemories() { return memories; }
    public void setMemories(List<Memory> memories) { this.memories = memories; }
}
