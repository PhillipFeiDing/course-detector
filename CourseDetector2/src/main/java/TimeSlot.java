public class TimeSlot implements Comparable<TimeSlot> {
    private int begin;
    private int end;
    private WeekDay weekDay;
    private String location;

    /**
     *
     * The basic constructor of the TimeSlot class, given the weekDay of the class, the
     * begin time and the end time of the class in integer representation
     * @param weekDay a WeekDay enum representing the weekDay of the class
     * @param begin an integer(absolute minute) representation of the starting
     *              time of the class
     * @param end an integer(absolute minute) representation of the ending time
     *            of the class
     */
    public TimeSlot(WeekDay weekDay, String location, int begin, int end) {
        initialize(weekDay, location, begin, end);
    }

    /**
     *
     * @param weekDay a WeekDay enum representing the weekDay of the class
     * @param raw the raw String representation of class time in a weekDay
     *            e.g. "12:00 pm-01:10 pm"
     * @throws IllegalArgumentException if the given String representation of
     * staring time and ending time is null
     */
    public TimeSlot(WeekDay weekDay, String location, String raw) {
        if (raw == null || location == null) {
            throw new IllegalArgumentException("Given raw string is null.");
        }

        int[] minutes = translateMinutes(raw);
        initialize(weekDay, location, minutes[0], minutes[1]);
    }

    /**
     * Construct a TimeSlot instance based on two given strings, one is the weekDay and
     * the other is time slot for the class period
     * @param dayStr the weekDay of the class period
     * @param raw the time slot for the class period
     *            e.g. "12:00 pm-01:10 pm"
     * @param location the location where this slot is held
     * @throws IllegalArgumentException if given null arguments
     */
    public TimeSlot(String dayStr, String location, String raw) {
        if (dayStr == null || dayStr.length() != 1) {
            throw new IllegalArgumentException("Given weekDay string is null"
            + " or contains more than one weekDay");
        }
        if (location == null) {
            throw new IllegalArgumentException("Given location is null.");
        }

        WeekDay weekDay;
        try {
            weekDay = WeekDay.getDay(dayStr.charAt(0));
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Given weekDay string is malformed");
        }

        if (weekDay != null) {
            int[] minutes = translateMinutes(raw);
            initialize(weekDay, location, minutes[0], minutes[1]);
        } else {
            throw new IllegalArgumentException("Constructed weekDay is null.");
        }
    }

    /**
     *
     * @param weekDay a WeekDay enum representing the weekDay of the class period
     * @param begin an integer(absolute minute) representation of the starting
     *              time of the class period
     * @param end an integer(absolute minute) representation of the ending time
     *            of the class period
     * @throws IllegalArgumentException if it is not possible to construct a
     * time slot object for the course.
     */
    public void initialize(WeekDay weekDay, String location, int begin, int end) {
        if (weekDay == null) {
            throw new IllegalArgumentException("Given weekDay is null.");
        }
        if (!(begin >= 0 && begin < 1440) || !(end >= 0 && end < 1440)) {
            throw new IllegalArgumentException("Given starting or ending time"
                    + " is not valid. Check if they agree with integer-minute"
                    + " representation of 24 hours.");
        }
        if (end <= begin) {
            throw new IllegalArgumentException("Attempting to instantiate a"
                    + " TimeSlot object where the staring time is larger "
                    + "than or equal to ending time.");
        }

        this.weekDay = weekDay;
        this.begin = begin;
        this.end = end;
        this.location = location;
    }

    /**
     * Overrides to super class's toString() method
     * @return a string representation of the TimeSlot object
     */
    @Override
    public String toString() {
        return  String.format("%02d", (begin / 60)) + ":"
                + String.format("%02d", (begin % 60))
                + " - " + String.format("%02d", (end / 60))
                + ":" + String.format("%02d", (end % 60))
                + " " + weekDay.toString()
                + " " + location;
    }

    /**
     * Determines if this TimeSlot slot precedes the other in terms of starting
     * time, considering the weekDay in a week
     * @param other the other TimeSlot instance
     * @return an integer to signify if this TimeSlot instance precedes the other
     * one, <0 if it does, >0 if it does not, and =0 if both have equal staring
     * time
     */
    @Override
    public int compareTo(TimeSlot other) {
        if (other == null) {
            throw new IllegalArgumentException("The other TimeSlot object is null");
        }

        int dayComp = this.weekDay.compareTo(other.weekDay);
        if (dayComp != 0) {
            return dayComp;
        } else {
            return this.begin - other.begin;
        }
    }

    /**
     * determining if two time slots are identical regardless of their location
     * @param that the other TimeSlot Object
     * @return true if they are identical, else otherwise
     */
    public boolean equalsIgnoreLocation(TimeSlot that) {
        if (that == null) {
            return false;
        }
        return this.begin == that.begin && this.end == that.end
                && this.weekDay.equals(that.weekDay);
    }

    /**
     * determining if two time slots are identical considering the location
     * @param other the other TimeSlot Object
     * @return true if they are identical, else otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TimeSlot)) {
            return false;
        }
        TimeSlot that = (TimeSlot) other;
        return equalsIgnoreLocation(that)
                && this.location.equals(that.location);
    }

    /**
     * Determines if this TimeSlot slot overlaps with the other, considering the
     * weekDay in a week
     * @param other the other TimeSlot instance
     * @return true if they overlap, and false if they don't
     */
    public boolean overlaps(TimeSlot other) {
        if (!this.weekDay.equals(other.weekDay)) {
            return false;
        } else {
            int comp = this.compareTo(other);
            if (comp == 0) {
                return true;
            } else if (comp < 0) {
                return this.end > other.begin;
            } else {
                return this.begin < other.end;
            }
        }
    }

    /**
     *
     * Calculate the length of the time slot instance
     * @return the length of the time slot in minutes
     */
    public int getLength() {
        return this.end - this.begin;
    }

    /**
     *
     * @param raw a String representation of the staring and ending time of a
     *            class period
     * @return an integer array with 2 elements: start and end time of a class
     * period
     * @throws IllegalArgumentException if given raw String is malformed.
     */
    private static int[] translateMinutes(String raw) {
        String[] clocks = raw.split("-");
        if (clocks == null || clocks.length != 2) {
            throw new IllegalArgumentException("Malformed raw string given.");
        }

        String[] start = clocks[0].split(":");
        String[] finish = clocks[1].split(":");
        if (start == null || clocks.length != 2
                || finish == null || finish.length != 2) {
            throw new IllegalArgumentException("Malformed raw string given.");
        }

        try {
            int startHour = Integer.parseInt(start[0]);
            int startMinute = Integer.parseInt(start[1].substring(0, 2));
            int finishHour = Integer.parseInt(finish[0]);
            int finishMinute = Integer.parseInt(finish[1].substring(0, 2));

            String amPm = start[1].split(" ")[1];
            if (!(amPm.equals("am") || amPm.equals("pm"))) {
                throw new IllegalArgumentException();
            }
            if (amPm.equals("pm") && startHour != 12) {
                startHour += 12;
            }
            if (amPm.equals("am") && startHour == 12) {
                startHour -= 12;
            }
            amPm = finish[1].split(" ")[1];
            if (amPm.equals("pm") && finishHour != 12) {
                finishHour += 12;
            }
            if (amPm.equals("am") && finishHour == 12) {
                finishHour -= 12;
            }

            int[] ret = new int[2];
            ret[0] = startHour * 60 + startMinute;
            ret[1] = finishHour * 60 + finishMinute;

            return ret;
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Malformed raw string given.");
        }
    }
}
