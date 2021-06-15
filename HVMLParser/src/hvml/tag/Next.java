package hvml.tag;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "next")
public class Next {
    //遷移先の HVML ファイル名と topic id を指定
    @Attribute
    private String href;
    public String getHref() { return href; }
    public void setHref(String href) { this.href = href; }

    //type="default"を指定
    @Attribute
    private String type;
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}