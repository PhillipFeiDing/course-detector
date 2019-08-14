import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class Schedule {
    private List<Course> courses;
    private boolean countNullAverage;

    /**
     * The standard constructor for a schedule giving all courses taking
     * @param titles the titles of the courses
     * @param fileNames the file names to the html files of the courses taking
     * @param importances the importances of corresponding courses
     */
    public Schedule(String[] titles, String[] fileNames) {
        if (fileNames == null || fileNames.length == 0 || titles == null
        || titles.length == 0 || !(titles.length == fileNames.length)) {
            throw new IllegalArgumentException("Given array of file names for"
            + " the course titles/files/importance is null or contains 0 files"
            + " or are of different length");
        }

        courses = new ArrayList<>();
        for (int i = 0; i < titles.length; i++) {
            courses.add(new Course(titles[i], fileNames[i]));
        }

        countNullAverage = false;
    }

    /**
     * a setter switch for the property countNullAverage
     * @param flag if count null as average
     */
    public void setCountNullAverage(boolean flag) {
        countNullAverage = flag;
    }

    /**
     * getter for number of courses
     * @return number of courses
     */
    public int getNumberOfCourses() {
        return courses.size();
    }

    /**
     * getter for the indexed course
     * @param index the index of the course
     * @return the indexed course
     */
    public Course getCourse(int index) {
        return courses.get(index);
    }

    /**
     * filter an indexed course's all sections
     * @param index the index of the course we apply the filter
     * @param predicate the criteria used for filtering
     */
    public void filter(int index, Predicate<Section> predicate) {
        courses.get(index).filterSections(predicate);
    }

    /**
     * Calculate all possible combinations of sections
     * @return a list of all possible combinations
     */
    public List<CourseCombo> getAllSectionCombos() {
        List<CourseCombo> combos = new ArrayList<>();
        int[] currCombo = new int[courses.size()];
        comboHelper(0, currCombo, combos);
        return combos;
    }

    /**
     * Uses back-tracing to find all combinations that do not lead to time
     * conflict
     * @param currCourse the index of current course
     * @param currCombo the combo workspace
     * @param combos a list of all possible combos
     */
    private void comboHelper(int currCourse, int[] currCombo,
                             List<CourseCombo> combos) {
        if (currCourse == courses.size()) {
            combos.add(new CourseCombo(courses,
                    Arrays.copyOf(currCombo, currCombo.length),
                    countNullAverage));
            return;
        }
        ArrayList<Integer> currUniqueSectionIndices
                = courses.get(currCourse).getUniqueSectionIndices();
        for (int sectionIndex: currUniqueSectionIndices) {
            boolean conflict = false;
            for (int i = 0; i < currCourse && !conflict; i++) {
                Section thisSection
                        = courses.get(currCourse).getSection(sectionIndex);
                Section thatSection = courses.get(i).getSection(currCombo[i]);
                if (thisSection.conflictsWith(thatSection)) {
                    conflict = true;
                }
            }
            if (!conflict) {
                currCombo[currCourse] = sectionIndex;
                comboHelper(currCourse + 1, currCombo, combos);
            }

        }
    }

    /**
     * Overrides the superclass's toString() method
     * @return a String representation of all courses to take
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Course count: " + courses.size() + "\n");
        for (Course course: courses) {
            builder.append(course.toString());
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        // 从这里改数据
        String semester = "2019_fall"; // 学期
        String[] titles
                = {
                "MATH3012",
                "CS2110",
                "CS2340",
                "CS3600",
                "CS3510",
                "MATH3670",
                "MATH3215",
                "ISYE3770",
                "CS4400",
                "CS4641",
                "CS3790",
                "PSYC3040",
                "CS1100",
                "APPH1050"
        }; // 课程

        // String[] titles = {"MATH3012", "CS2050", "CS1332", "CS1100"};
        // 用Chrome浏览器访问GT登陆访问GT的Buzzport
//        try {
//            SeleniumChrome.start();
//            SeleniumChrome.pullAllPages("src/main/resources/data", titles);
//            SeleniumChrome.close();
//        } catch (IOException | InterruptedException | IllegalStateException e) {
//            System.out.println(e.getMessage());
//        }

        // 筛选标准 默认：Section 全选
        // 现行：只要waitlist 不满或者还section还开着
        Predicate<Section>[] predicates = new Predicate[titles.length];
        for (int i = 0; i < predicates.length; i++) {
            predicates[i] = section -> !section.wlFull() || section.isOpen();
        }
        // 特殊的标在下面，顺序和课程titles相同
        // 例子： predicates[0] = section -> section.getInstructor().equals(myFavorite);
        predicates[1] = predicates[1].and(section -> !section.getSectionNumber().equals("GR")); // CS2110
        predicates[2] = predicates[2].and(section -> !section.getSectionNumber().equals("GR")); // CS2340
        predicates[5] = predicates[5].and(section -> !section.getSectionNumber().equals("UG")); // MATH3670
        predicates[8] = predicates[8].and(section -> !section.getSectionNumber().equals("B")); // CS4400
        predicates[12] = predicates[12].and(section -> !section.getSectionNumber().equals("B1")); // CS1100
        predicates[13] = predicates[13].and(section -> {
            return !section.getSectionNumber().equals("RBS")
                    && !section.getSectionNumber().equals("HPW")
                    && !section.getSectionNumber().equals("HPY")
                    && !section.getSectionNumber().equals("HPF");
        }); // APPH1050

        // 生成文件名，不要随便改
        String[] fileNames = new String[titles.length];
        for (int i = 0; i < titles.length; i++) {
            fileNames[i] = "src/main/resources/data/" + titles[i] + ".html";
        }

        // 读取文件，筛选section
        Schedule schedule = new Schedule(titles, fileNames);
        for (int i = 0; i < schedule.getNumberOfCourses(); i++) {
            schedule.filter(i, predicates[i]);
        }

        // 打印所有课程所有section
        try {
            for (int i = 0; i < schedule.getNumberOfCourses(); i++) {
                String title = schedule.getCourse(i).getTitle();
                BufferedWriter writer = new BufferedWriter(
                        new FileWriter("src/main/resources/output/" + title + ".txt"));
                writer.write(schedule.getCourse(i).toString());
                writer.close();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Writing interrupted.");
        }

        // 查找组合
        // 这里注意,countNullAverage 表示在计算GPA时如果遇到一个instructor没有GPA信息，
        // 要么取所有instructors平均分，要么算作0分，true为前者，false后者
        schedule.setCountNullAverage(true);
        List<CourseCombo> combos = schedule.getAllSectionCombos();
        // 排序组合：GPA标准
        Collections.sort(combos);

        // 打印所有组合
        try {
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter("src/main/resources/output/course_combo.txt"));

            for (String title : titles) {
                writer.write(String.format("%-25s", title));
            }
            writer.write(String.format("%-25s\n", "Average GPA"));

            for (CourseCombo combo : combos) {
                for (int i = 0; i < schedule.getNumberOfCourses(); i++) {
                    writer.write(String.format("%25s", schedule.getCourse(i)
                            .getAllEquivalentSectionNumbers(
                                    combo.getCourseIndices()[i])));
                }
                writer.write(String.format("%25.4f\n", combo.getAverageGPA()));
            }
            writer.write(String.format("Total Combinations: " + combos.size()));
            writer.close();
        } catch (IOException e) {
            throw new IllegalArgumentException("Writing interrupted.");
        }
    }
}
