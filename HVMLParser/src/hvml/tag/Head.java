package hvml.tag;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root
class Head {
    @Element
    public String producer;
    @Element
    public String description;
    @Element(name = "tool_version")
    public String toolVersion;
    @Element
    public Scene scene;
    @Element
    public Situation situation;
    @ElementList(inline = true)
    public List<Accost> accosts;
    @Element
    public Version version;

    @Element
    static class Scene {
        @Attribute
        public String value;
    }

    @Element
    static class Version {
        @Attribute
        public String value;
    }
}
