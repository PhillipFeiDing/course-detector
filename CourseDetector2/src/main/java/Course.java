import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Course {
    private String title;
    private List<Section> allSections;
    private List<Section> sections;
    private int credit;
    private InstructorPool instructors;

    /**
     * full constructor for Course class
     * @param title the title for the course
     * @param fileName the file name to the html file containing all section
     *                 information
     * @throws IllegalArgumentException if given parameters are null or illegal
     */
    public Course(String title, String fileName) {
        if (title == null || fileName == null) {
            throw new IllegalArgumentException("Cannot construct a course"
            + " using null title, null importance, or non-positive importance");
        }
        this.title = title;
        HtmlParser parser = new HtmlParser();
        this.allSections = parser.parseCourse(fileName);
        if (allSections == null || allSections.size() == 0) {
            throw new IllegalArgumentException("Fail to construct all sections"
            + " of a course for unknown reasons." + " File: " + fileName);
        }
        this.credit = allSections.get(0).getCredit();
        instructors = new InstructorPool(title);

        for (Section section: allSections) {
            section.setGPA(instructors.getGPA(section.getInstructor()));
        }

        Collections.sort(allSections, new Comparator<Section>() {
            @Override
            public int compare(Section s1, Section s2) {
                Double s1GPA = s1.getGPA();
                Double s2GPA = s2.getGPA();
                if (s1GPA == null && s2GPA != null) {
                    return 1;
                } else if (s1GPA != null && s2GPA == null) {
                    return -1;
                } else if (s1GPA != null && s2GPA != null) {
                    double diff = s2GPA - s1GPA;
                    if (diff > 0) {
                        return 1;
                    } else if (diff < 0) {
                        return -1;
                    }
                }
                return s1.compareTo(s2);
            }
        });

        this.sections = new ArrayList<>();
        for (Section section: allSections) {
            sections.add(section);
        }
    }

    /**
     * getter for credit
     * @return credit hour of this course
     */
    public int getCredit() {
        return credit;
    }

    /**
     * getter for instructors
     * @return InstructorPool for this course
     */
    public InstructorPool getInstructorPool() {
        return instructors;
    }

    /**
     * getter for the title
     * @return the title for the course
     */
    public String getTitle() {
        return title;
    }

    /**
     * Overrides the superclass's toString() method
     * @return a string representation of all sections
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("**********");
        builder.append(title);
        builder.append("; " + credit + "credit hour(s)"
                + "**********");
        int count = 0;
        for (Section section : sections) {
            builder.append("\n-->Section " + String.format("%02d", count)
                    + " GPA: ");
            Double gpa = instructors.getGPA(section.getInstructor());
            builder.append(gpa != null ? String.format("%.2f", gpa)
                    : "unknown");
            builder.append("\n");
            String sect = section.toString();
            sect = sect.replaceAll("\n", "\n          ");
            builder.append(sect + "\n");
            count++;
        }
        builder.append("\n");
        return builder.toString();
    }

    /**
     * filter out the sections not in consideration
     * @param predicate a Predicate used to filter out unwanted sections
     */
    public void filterSections(Predicate<Section> predicate) {
        this.sections = sections.stream()
                .filter(predicate).collect(Collectors.toList());
    }

    /**
     * Recover all sections including those have been filtered out previously
     */
    public void recoverAllSections() {
        this.sections = new ArrayList<>();
        for (Section section: allSections) {
            sections.add(section);
        }
    }

    /**
     * merge all sections with same professor and time schedule
     * @return the indices of merged sections, each representing a unique
     * section
     */
    public ArrayList<Integer> getUniqueSectionIndices() {
        ArrayList<Integer> sectionIndices = new ArrayList<>();
        if (sections.size() == 0) {
            return sectionIndices;
        }
        sectionIndices.add(0);
        for (int i = 1; i < sections.size(); i++) {
            if (!sections.get(i).equivalentTo(sections.get(i - 1))) {
                sectionIndices.add(i);
            }
        }
        return sectionIndices;
    }

    /**
     * Give the section at the specified index
     * @param index the index specifying the section to get
     * @return the section at the specified section
     */
    public Section getSection(int index) {
        if (index < 0 || index >= sections.size()) {
            throw new IllegalArgumentException("Given index exceeds the bounds"
            + " for sections list.");
        }
        return sections.get(index);
    }

    /**
     * Give the gpa of the indexed section
     * @param index the index specifying the section for getting GPA
     * @return the GPA of that section
     */
    public Double getSectionGPA(int index) {
        if (index < 0 || index >= sections.size()) {
            throw new IllegalArgumentException("Given index exceeds the bounds"
                    + " for sections list.");
        }
        return instructors.getGPA(sections.get(index).getInstructor());
    }

    /**
     * Sort out all equivalent section numbers
     * @param index the index in sections list
     * @return equivalent section numbers
     */
    public String getAllEquivalentSectionNumbers(int index) {
        ArrayList<Integer> indices = getUniqueSectionIndices();
        int upIdx = 0;
        while (upIdx < indices.size() && indices.get(upIdx) <= index) {
            upIdx++;
        }
        int up;
        if (upIdx == indices.size()) {
            up = sections.size();
        } else {
            up = indices.get(upIdx);
        }
        int low = indices.get(upIdx - 1);
        String sectionNumbers = "";
        for (int i = low; i < up; i++) {
            sectionNumbers += sections.get(i).getSectionNumber() + " ";
        }
        return sectionNumbers;
    }

    /**
     * Sort out all equivalent section CRNs
     * @param index the index in sections list
     * @return equivalent section numbers
     */
    public String getAllEquivalentSectionCrns(int index) {
        ArrayList<Integer> indices = getUniqueSectionIndices();
        int upIdx = 0;
        while (upIdx < indices.size() && indices.get(upIdx) <= index) {
            upIdx++;
        }
        int up;
        if (upIdx == indices.size()) {
            up = sections.size();
        } else {
            up = indices.get(upIdx);
        }
        int low = indices.get(upIdx - 1);
        String sectionCRNs = "";
        for (int i = low; i < up; i++) {
            sectionCRNs += sections.get(i).getCrn() + " ";
        }
        return sectionCRNs;
    }
}
