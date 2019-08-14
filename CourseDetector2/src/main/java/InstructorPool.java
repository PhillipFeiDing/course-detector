import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstructorPool {
    private Map<String, Double> instructors;
    private double avgGPA;
    private static final String URL_HEADER
            = "https://critique.gatech.edu/course.php?id=";
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final String TABLE_BODY_MARKUP = "(<tr class=\"[A-Z\\.]+\">)(.+)(</tr>)";
    private static final String INSTRUCTOR_NAME_MARKUP = "([A-Za-z \\-\\.]+, [A-Z])([A-Za-z \\-]*)";
    private static final String GPA_MARKUP = "(\\d\\.\\d\\d)";

    /**
     * Constructor
     * @param courseTitle the title of the course
     */
    public InstructorPool(String courseTitle) {
        instructors = new HashMap<>();

        String fullURL = URL_HEADER + courseTitle;
        String html = getPageSource(fullURL, DEFAULT_ENCODING);
        Pattern pattern = Pattern.compile(TABLE_BODY_MARKUP);
        Matcher m = pattern.matcher(html);
        int count = 0;
        double totalGPA = 0;
        while (m.find()) {
            String fragment = m.group(2);
            Pattern namePattern = Pattern.compile(INSTRUCTOR_NAME_MARKUP);
            Matcher mName = namePattern.matcher(fragment);
            Pattern gpaPattern = Pattern.compile(GPA_MARKUP);
            Matcher mGPA = gpaPattern.matcher(fragment);
            if (mName.find() && mGPA.find()) {
                instructors.put(mName.group(1) + ".",
                        Double.parseDouble(mGPA.group(1)));
                count++;
                totalGPA += Double.parseDouble(mGPA.group(1));
            }
        }
        avgGPA = (count == 0)? 4 : totalGPA / count;
    }

    /**
     * getter for avgGPA
     * @return avgGPA
     */
    public double getAvgGPA() {
        return avgGPA;
    }

    /**
     * get the html String associated with the url address
     * @param pageUrl the url
     * @param encoding encoding
     * @return html of the page
     */
    private String getPageSource(String pageUrl, String encoding) {
        StringBuffer sb = new StringBuffer();
        try {
            //构建一URL对象
            URL url = new URL(pageUrl);
            //使用openStream得到一输入流并由此构造一个BufferedReader对象
            BufferedReader in = new BufferedReader(new InputStreamReader(url
                    .openStream(), encoding));
            String line;
            //读取www资源
            while ((line = in.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            in.close();
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
        return sb.toString();
    }

    /**
     *
     * @param instructorName the name of the instructor abbreviated
     * @return that instructor's course GPA
     */
    public Double getGPA(String instructorName) {
        return instructors.get(instructorName);
    }

    /**
     * Overrides to super class's toString() method
     * @return a String representation of the Map
     */
    @Override
    public String toString() {
        String ret = "";
        for (String name : instructors.keySet()) {
            ret += String.format("%25s", name)
                    + " = " + instructors.get(name) + "\n";
        }
        return ret;
    }

    public static void main(String[] args) {
        InstructorPool pool = new InstructorPool("MATH2551");
        System.out.println(pool);
    }
}
