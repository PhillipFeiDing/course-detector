import java.util.List;

public class CourseCombo implements Comparable<CourseCombo> {
    private int[] indices;
    private double averageGPA;

    public CourseCombo(List<Course> courses, int[] indices,
                       boolean countNullAverage) {
        if (courses == null || indices == null
                || courses.size() != indices.length) {
            throw new IllegalArgumentException("The course list, indices, or"
             + " their length do not match.");
        }
        this.indices = indices;
        averageGPA = getAverageGPA(courses, indices, countNullAverage);
    }

    public int[] getCourseIndices() {
        return indices;
    }

    public double getAverageGPA() {
        return averageGPA;
    }

    @Override
    public int compareTo(CourseCombo other) {
        double diff = this.averageGPA - other.averageGPA;
        if (diff == 0) {
            return 0;
        } else if (diff > 0) {
            return -1;
        } else {
            return 1;
        }
    }

    /**
     * Get the composite score for this combination
     * @param combo a combination of sections
     * @param countNullAverage whether count instructor without GPA information
     *                         as average for all sections, or 0 otherwise;
     * @return the composite score for this combination
     */
    private double getAverageGPA(List<Course> courses, int[] combo,
                                 boolean countNullAverage) {
        double totalGPA = 0;
        int totalCredit = 0;
        for (int i = 0; i < combo.length; i++) {
            Double gpa = courses.get(i).getSectionGPA(combo[i]);
            int credit = courses.get(i).getCredit();
            totalCredit += credit;
            if (gpa != null) {
                totalGPA += gpa * credit;
            } else {
                if (countNullAverage) {
                    totalGPA += courses.get(i).getInstructorPool().getAvgGPA()
                            * credit;
                }
            }
        }
        return totalCredit != 0 ? totalGPA / totalCredit : 0;
    }
}
