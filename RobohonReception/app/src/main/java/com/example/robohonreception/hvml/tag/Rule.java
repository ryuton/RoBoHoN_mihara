package com.example.robohonreception.hvml.tag;


import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "rule")
public class Rule {
    @ElementList(inline = true, required = false)
    private List<Condition> Conditions;
    public List<Condition> getConditions() { return Conditions; }
    public void setConditions(List<Condition> conditions) { Conditions = conditions; }
}
