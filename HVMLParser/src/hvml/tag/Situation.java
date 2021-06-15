package hvml.tag;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "situation")
public class Situation {

    @Attribute
    private String trigger;
    public String getTrigger() { return trigger; }
    public void setTrigger(String trigger) { this.trigger = trigger; }

    @Attribute(required = false)
    private int priority;
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    @Attribute(name = "topic_id", required = false)
    private String topicID;
    public String getTopicID() { return topicID; }
    public void setTopicID(String topicID) { this.topicID = topicID; }

    @Text
    private String value;
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
