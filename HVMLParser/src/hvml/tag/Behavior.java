package hvml.tag;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "behavior")
public class Behavior {
    @Attribute
    private String type;
    public String getType() { return type; }
    public void setType(String type) {
        if (!type.equals("normal")) throw new Error();
        this.type = type;
    }

    //モーションを指定
    @Attribute
    private String id;
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}
