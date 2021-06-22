package hvml.tag;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "memory")
public class Memory {
    //情報破棄タイミングの指定、temporary または　permanent
    @Attribute
    private String type;
    public String getType() { return type; }
    public void setType(String type) { if ( type.equals("temporary") || type.equals("permanent")) this.type = type; }

    //保存した情報を呼び出す際に使用
    @Attribute
    private String key;
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    //記憶する値を指定
    @Attribute(required = false)
    private String value;
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    //操作種別を指定 set または delete
    @Attribute(required = false)
    private String operation = "set";
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
}
