package hvml;

import hvml.tag.HVML;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import java.io.File;

public class HVMLParser {
    HVML hvml;

    public HVMLParser(File file) throws Exception {
        Serializer serializer = new Persister(new AnnotationStrategy());
        this.hvml = serializer.read(HVML.class, file);
    }
}
