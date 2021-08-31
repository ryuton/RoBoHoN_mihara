package com.example.robohonreception.hvml;

import android.util.Log;

import com.example.robohonreception.hvml.tag.HVML;
import com.example.robohonreception.hvml.tag.Head;
import com.example.robohonreception.hvml.tag.Topic;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.InputStream;

public class HVMLParser {
    HVML hvml;
    String TAG = "HVMLParser";

    public HVMLParser(InputStream file) throws Exception {
        Serializer serializer = new Persister(new AnnotationStrategy());
        this.hvml = serializer.read(HVML.class, file);
        Log.d(TAG, "parsed");
    }

    public Topic getTopicFromID(String id) {
        for (Topic topic : this.hvml.getBody().getTopics() ) {
            if ( topic.getId().equals(id)) return topic;
        }

        return null;
    }
    public Head getHead() {
        return this.hvml.getHead();
    }
}
