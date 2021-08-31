package hvml.tag;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "control")
public class Control {
    //発行するイベント名
    @Attribute
    private String function;
    public String getFunction() { return function; }
    public void setFunction(String function) { this.function = function; }

    //HVML を登録したアプリの package 名
    @Attribute
    private String target;
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    //Key-Value のペアで target で指定したアプリにパラメータを引き渡す
    @ElementList(inline = true, required = false)
    private List<Data> dataList;
    public List<Data> getDataList() { return dataList; }
    public void setDataList(List<Data> dataList) { this.dataList = dataList; }
}
