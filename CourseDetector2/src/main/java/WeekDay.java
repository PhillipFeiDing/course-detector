public enum WeekDay {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY;

    /**
     *
     * The static method returns the enum representation given a capitalized
     * char representation.
     * @param c the char representation of a weekday
     * @return the WeekDay enum associated with the given char representation
     * @throws IllegalArgumentException when the given char is not associated
     * with a valid WeekDay enum
     */
    public static WeekDay getDay(char c) {
        if (c == 'M') {
            return MONDAY;
        } else if (c == 'T') {
            return TUESDAY;
        } else if (c == 'W') {
            return WEDNESDAY;
        } else if (c == 'R') {
            return THURSDAY;
        } else if (c == 'F') {
            return FRIDAY;
        }

        throw new IllegalArgumentException("Given char does not translate to"
        + " a weekday!" + " Given: " + c);
    }
}
