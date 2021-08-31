package hvml.tag;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "condition")
public class Condition {
    //タグ要素の内容に記載された条件に合致した場合に遷移する case の id
    @Attribute(name = "case_id")
    private String caseID;
    public String getCaseID() { return caseID; }
    public void setCaseID(String caseID) { this.caseID = caseID; }

    //case が選択されるための比率
    @Attribute(required = false)
    private int weight = 10;
    public int getWeight() { return weight; }
    public void setWeight(int weight) { if (0 < weight && weight < 100) this.weight = weight; }

    //case を選択する際の優先度
    @Attribute(required = false)
    private int priority = 10;
    public int getPriority() { return priority; }
    public void setPriority(int priority) { if ( 0 < priority && priority < 100) this.priority = priority; }

    @Text
    private String value;
    public void setValue(String value) { this.value = value; }
    public String getValue() { return value; }
}
