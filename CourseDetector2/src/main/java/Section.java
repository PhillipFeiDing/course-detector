import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Section implements Comparable<Section> {
    private List<TimeSlot> timeSlots;
    private String sectionNumber;
    private String instructor;
    private String crn;
    private String status;
    private int capacity;
    private int actual;
    private int wlCapacity;
    private int wlActual;
    private int credit;
    private Double gpa;

    /**
     * getter for credit
     * @return credit
     */
    public int getCredit() {
        return credit;
    }

    /**
     * getter for sectionNumber
     * @return serctionNumber
     */
    public String getSectionNumber() {
        return sectionNumber;
    }

    /**
     * getter for CRN
     * @return crn
     */
    public String getCrn() {
        return crn;
    }

    /**
     * getter for instructor
     * @return instructor
     */
    public String getInstructor() {
        return instructor;
    }

    /**
     * setter for instructor
     * @param instructor the instructor
     */
    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    /**
     * getter for gpa
     * @return gpa for this section
     */
    public Double getGPA() {
        return gpa;
    }

    /**
     * setter for gpa
     * @param gpa for this section
     */
    public void setGPA(Double gpa) {
        this.gpa = gpa;
    }

    /**
     * test if the wait list is full
     * @return true if the wait list is full
     */
    public boolean wlFull() {
        return wlCapacity - wlActual >= 0;
    }

    /**
     * The full constructor of a section for a course
     * @param crn the course registration number
     * @param sectionNumber the section number
     * @param credit credit hours
     * @param capacity capacity of the section
     * @param actual actual enrollment in that section
     * @param wlCapacity wait list capacity
     * @param wlActual wait list actual
     * @param instructor the instructor of that section for the course
     * @param status status of the course, closed or open
     * @throws IllegalArgumentException if some reference arguments given are
     * null
     */
    public Section(String crn, String sectionNumber, int credit, int capacity,
                   int actual, int wlCapacity, int wlActual, String instructor,
                   String status) {
        if (crn == null || sectionNumber == null || instructor == null) {
            throw new IllegalArgumentException("Some arguments are null.");
        }

        this.sectionNumber = sectionNumber;
        this.instructor = instructor;
        this.crn = crn;
        this.capacity = capacity;
        this.actual = actual;
        this.wlCapacity = wlCapacity;
        this.wlActual = wlActual;
        this.credit = credit;
        this.status = status;
        this.timeSlots = new ArrayList<>();
    }

    /**
     * Add time slots for the section given on which day they are taught, and
     * at which hour (assumed same for everyday).
     * @param days the days on which the section is taught
     * @param time the time in a day when the section is taught
     * @param location where the section is taught
     */
    public void addTimeSlots(String days, String time, String location) {
        for (int i = 0; i < days.length(); i++) {
            timeSlots.add(
                    new TimeSlot(days.substring(i, i + 1), location, time));
        }
        checkInternalConflict();
    }

    /**
     * Check if two sections are equivalent in terms of time and instructor,
     * disregarding location, section number, and crn
     * @param that the other section to check for equivalency
     * @return if this section is equivalent to the other specifically in terms
     * of time and instructor
     */
    public boolean equivalentTo(Section that) {
        if (that == null || !that.instructor.equals(this.instructor)
            || that.timeSlots.size() != this.timeSlots.size()
            || this.credit != that.credit) {
            return false;
        }
        for (int i = 0; i < this.timeSlots.size(); i++) {
            if (!this.timeSlots.get(i).equalsIgnoreLocation(
                    that.timeSlots.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Test if this section time conflicts with the other section
     * @param that the other section
     * @return true if they have conflicts, false otherwise
     */
    public boolean conflictsWith(Section that) {
        for (TimeSlot thisTime: this.timeSlots) {
            for (TimeSlot thatTime: that.timeSlots) {
                if (thisTime.overlaps(thatTime)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Overrides the super class's compareTo() method;
     * ordering: instructor, timeSlots.size(), each entry in timeSlots
     * @param that the other section of the same course
     * @return an integer to signify the ordering, negative means smaller, and
     * positive means larger than
     */
    @Override
    public int compareTo(Section that) {
        int comp = this.instructor.compareTo(that.instructor);
        if (comp != 0) {
            return comp;
        }
        comp = this.timeSlots.size() - that.timeSlots.size();
        if (comp != 0) {
            return comp;
        }
        int i = 0;
        while (comp == 0 && i < timeSlots.size()) {
            comp = this.timeSlots.get(i).compareTo(that.timeSlots.get(i));
            i++;
        }
        if (comp != 0) {
            return comp;
        }
        return this.sectionNumber.compareTo(that.sectionNumber);
    }

    /**
     * Override the super class's toString() method
     * @return the string representation of the section
     */
    @Override
    public String toString() {
        String res = "Status: " + status + "; CRN: " + crn + "; Section: "
                + sectionNumber + "; Hour: " + credit + "; Registered: "
                + actual + "/" + capacity + "; Wait_listed:" + wlActual
                + "/" + wlCapacity + "; Instructor: " + instructor;
        for (TimeSlot timeSlot : timeSlots) {
            res += "\n" + timeSlot.toString();
        }
        return res;
    }

    /**
     * test if the section is still open
     * @return true if the section is still open, false otherwise
     */
    public boolean isOpen() {
        return !status.equals("C");
    }

    /**
     * check if the time slots for the course is valid
     * @throws IllegalArgumentException if there is an internal conflict, some
     * time slots overlap even for the same section
     */
    private void checkInternalConflict() {
        Collections.sort(timeSlots);
        for (int i = 0; i < timeSlots.size() - 1; i++) {
            if (timeSlots.get(i).overlaps(timeSlots.get(i + 1))) {
                throw new IllegalArgumentException("There is an internal"
                + " conflict in the section schedule; check if duplicate time"
                + " / overlapping time slots are added;");
            }
        }
    }
}
