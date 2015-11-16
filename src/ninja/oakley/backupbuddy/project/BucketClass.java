package ninja.oakley.backupbuddy.project;

import java.util.ArrayList;
import java.util.List;

public enum BucketClass {
    STANDARD, NEARLINE, DURABLE_REDUCED_AVAILABILITY;

    public static List<String> getStrings() {
        List<String> rt = new ArrayList<>();

        for (BucketClass clazz : BucketClass.values()) {
            rt.add(clazz.toString());
        }

        return rt;
    }
}
