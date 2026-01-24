package com.fawry.paymentroutingengine.constant;

import java.util.Arrays;

public enum DayType {
    MON, TUE, WED, THU, FRI, SAT, SUN, ALL;

    public static DayType fromString(String dayStr) {
        return Arrays.stream(DayType.values())
                .filter(day -> day.name().equalsIgnoreCase(dayStr))
                .findFirst()
                .orElse(ALL); // Default to ALL if no match is found
    }
}

