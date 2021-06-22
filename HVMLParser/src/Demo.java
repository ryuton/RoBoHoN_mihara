import hvml.HVMLParser;
import hvml.tag.HVML;

import java.io.File;

public class Demo {

    public static void main(String[] args) throws Exception {

        File file = new File("C:\\Users\\alpine\\Documents\\RoBoHoN_mihara\\HVMLParser\\assets\\jp_co_sharp_sample_simple_talk.hvml");

        HVMLParser parser = new HVMLParser(file);
        System.out.println("ok");
    }

}