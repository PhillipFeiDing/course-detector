import java.util.HashMap;
import java.util.Map;

public class CourseTitle {
    private Map<String, String> map;

    public CourseTitle() {
        map = new HashMap<>();
        map.put("MATH", "Mathematics");
        map.put("CS", "Computer Science");
        map.put("APPH", "Applied Physiology");
        map.put("ISYE", "Industrial & Systems Engr");
        map.put("PSYC", "Psychology");
    }

    public String get(String abbr) {
        return map.get(abbr);
    }
}
