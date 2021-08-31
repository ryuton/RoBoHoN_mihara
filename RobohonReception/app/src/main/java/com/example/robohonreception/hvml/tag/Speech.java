package com.example.robohonreception.hvml.tag;

;
import org.simpleframework.xml.*;
import org.simpleframework.xml.convert.Convert;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

import java.util.ArrayList;
import java.util.List;

@Root(name = "speech")
@Convert(value = Speech.SpeechConvert.class)
public class Speech {

//    public String getSpeechText() {
//        if (emotions == null) {
//            if (value == null) return "";
//            else return value.get(0);
//        } else {
//            return emotions.get(emotions.size() -1).getValue();
//        }
//    }

    //発話する際の感情(TTS のパラメータ)を設定
    @ElementList(inline = true, required = false)
    private List<Emotion> emotions;
    public List<Emotion> getEmotions() { return emotions; }
    public void setEmotions(List<Emotion> emotions) { this.emotions = emotions; }

    @ElementList(inline = true, required = false)
    private List<Wait> waits;
    public List<Wait> getWaits() { return waits; }
    public void setWaits(List<Wait> waits) { this.waits = waits; }

    @Text(required = false)
    private String value;
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    static class SpeechConvert implements Converter<Speech> {
        private final Serializer ser = new Persister();


        @Override
        public Speech read(InputNode node) throws Exception {
            Speech speech = new Speech();
            speech.setValue(node.getValue());

            List<Emotion> emotions = new ArrayList<>();
            InputNode emotion = node.getNext("emotion");

            while( emotion != null )
            {
                emotions.add(ser.read(Emotion.class, emotion));
                emotion = node.getNext("emotion");
            }

            speech.setEmotions(emotions);

            List<Wait> waits = new ArrayList<>();
            InputNode wait = node.getNext("wait");

            while ( wait != null) {
                waits.add(ser.read(Wait.class, wait));
                wait = node.getNext("wait");
            }

            speech.setWaits(waits);

            return speech;
        }


        @Override
        public void write(OutputNode node, Speech value) throws Exception {
            node.setValue(value.value);

            for( Emotion c : value.getEmotions() ) {
                ser.write(c, node);
            }

            for( Wait c : value.getWaits() ) {
                ser.write(c, node);
            }
        }
    }
}
