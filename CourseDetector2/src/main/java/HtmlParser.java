import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlParser {
    private static final String MARKUP_PATTERN_GENERAL =
            "(<td class=\"dddefault\">)([^<>]+)(</td>)";
    private static final String MARKUP_PATTERN_CRN =
            "(crn_in=)(\\d\\d\\d\\d\\d)";
    private static final String MARKUP_PATTERN_STATUS =
            "(<td class=\"dddefault\"><abbr title=\"[a-zA-Z ]+\">)([A-Z]*)(</abbr></td>)";
    private static final String MARKUP_PATTERN_INSTRUCTOR =
            "(<td class=\"dddefault\">)([^<>]+)(\\(<abbr title=\"Primary\">P</abbr>\\)</td>)";
    private static final String MARKUP_PATTERN_MULTI_COL =
            "(<td colspan=\")([\\d]+)(\" class=\"dddefault\"><abbr title=\".*\">)(.*)(</abbr></td>)";
    private static final String NULL_ENTRY = " ";
    private static final String TBA = "TBA";
    private static final int TABLE_COL = 20;
    private static final int STATUS_COL = 0;
    private static final int CRN_COL = 1;
    private static final int SUBJ_COL = 2;
    private static final int CRSE_COL = 3;
    private static final int SECTION_COL = 4;
    private static final int CREDIT_COL = 7;
    private static final int DAYS_COL = 9;
    private static final int TIME_COL = 10;
    private static final int CAPACITY_COL = 11;
    private static final int ACTUAL_COL = 12;
    private static final int WL_CAPACITY_COL = 14;
    private static final int WL_ACTUAL_COL = 15;
    private static final int INSTRUCTOR_COL = 17;
    private static final int LOCATION_COL = 18;

    private static String currTitle;

    /**
     * Generate a list of all entries in the table presented in html
     * @param path the path to the html file
     * @return a list of all entries in the table
     * @throws IllegalArgumentException for any IO exceptions
     */
    private List<String> getTableEntries(String path) {
        List<String> entries = new ArrayList<>();
        File file = new File(path);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            Pattern patternGeneral = Pattern.compile(MARKUP_PATTERN_GENERAL);
            Pattern patternCrn = Pattern.compile(MARKUP_PATTERN_CRN);
            Pattern patternStatus = Pattern.compile(MARKUP_PATTERN_STATUS);
            Pattern patternInstructor = Pattern.compile(MARKUP_PATTERN_INSTRUCTOR);
            Pattern patternMultiCol = Pattern.compile(MARKUP_PATTERN_MULTI_COL);
            while (line != null) {
                Matcher m = patternGeneral.matcher(line);
                if (m.find()) {
                    entries.add(m.group(2));
                } else {
                    m = patternCrn.matcher(line);
                    if (m.find()) {
                        entries.add(m.group(2));
                    } else {
                        m = patternStatus.matcher(line);
                        if (m.find()) {
                            entries.add(m.group(2));
                        } else {
                            m = patternInstructor.matcher(line);
                            if (m.find()) {
                                entries.add(m.group(2));
                            } else {
                                m = patternMultiCol.matcher(line);
                                if (m.find()) {
                                    int col = Integer.parseInt(m.group(2));
                                    for (int i = 0; i < col; i++) {
                                        entries.add(m.group(4));
                                    }
                                }
                            }
                        }
                    }
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            throw new IllegalArgumentException("Given html file"
                    + path + "does not exist"
                    + " or unknown disruption happened.");
        }

        return entries;
    }

    /**
     * Create a list of all sections listed in the html file table
     * @param path the path to the html file
     * @return a list of all sections
     * @throws IllegalArgumentException the html table is formatted incorrectly
     * or the pattern is unidentifiable
     */
    public List<Section> parseCourse(String path) {
        List<String> entries = getTableEntries(path);

        if (entries.size() % TABLE_COL != 0) {
            int count = 0;
            for (String line: entries) {
                System.out.print(line + " ");
                count++;
                if (count % 20 == 0) {
                    System.out.println();
                }
            }
            throw new IllegalArgumentException("The given html table has"
            + " formatting errors, or unknown patterns the parser cannot"
            + " interpret. File: " + path + "; Entries: " + entries.size());
        }

        ArrayList<Section> sections;
        sections = new ArrayList<>();
        int idx = 0;
        while (idx < entries.size()) {
            Section newSection;

            try {
                String status = entries.get(idx + STATUS_COL);
                String crn = entries.get(idx + CRN_COL);
                String section = entries.get(idx + SECTION_COL);
                int credit = (int) Double.parseDouble(entries.get(idx + CREDIT_COL));
                String days = entries.get(idx + DAYS_COL);
                String time = entries.get(idx + TIME_COL);
                int capacity = Integer.parseInt(entries.get(idx + CAPACITY_COL));
                int actual = Integer.parseInt(entries.get(idx + ACTUAL_COL));
                int wlCapacity = Integer.parseInt(entries.get(idx + WL_CAPACITY_COL));
                int wlActual = Integer.parseInt(entries.get(idx + WL_ACTUAL_COL));
                String instructor = entries.get(idx + INSTRUCTOR_COL).trim();
                String location = entries.get(idx + LOCATION_COL);

                newSection = new Section(crn, section, credit, capacity,
                        actual, wlCapacity, wlActual, instructor, status);
                newSection.addTimeSlots(days, time, location);

                if (idx + TABLE_COL < entries.size()
                && hasSecondComponent(entries, idx)) {
                    String days2 = entries.get(idx + DAYS_COL + TABLE_COL);
                    String time2 = entries.get(idx + TIME_COL + TABLE_COL);
                    String location2 = entries.get(idx + LOCATION_COL + TABLE_COL);
                    if (instructor.equals(TBA)) {
                        newSection.setInstructor(entries.get(idx + TABLE_COL + INSTRUCTOR_COL).trim());
                    }
                    newSection.addTimeSlots(days2, time2, location2);
                }

                sections.add(newSection);
            } catch (IllegalArgumentException e) {
                System.out.println("Warning: unable to parseCourse one row in the"
                + " course section table in file " + path + ".");
            }

            if (idx + TABLE_COL < entries.size()
            && hasSecondComponent(entries, idx)) {
                idx += 2 * TABLE_COL;
            } else {
                idx += TABLE_COL;
            }
        }

        Collections.sort(sections);
        return sections;
    }

    private boolean hasSecondComponent(List<String> entries, int curIdx) {
        curIdx += TABLE_COL;
        return (entries.get(curIdx + SECTION_COL).equals(NULL_ENTRY)
                && entries.get(curIdx + CRN_COL).equals(NULL_ENTRY)
                && entries.get(curIdx + SECTION_COL).equals(NULL_ENTRY)
                && entries.get(curIdx + CREDIT_COL).equals(NULL_ENTRY));
    }

}
