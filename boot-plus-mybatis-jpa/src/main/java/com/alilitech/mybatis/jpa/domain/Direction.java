package com.alilitech.mybatis.jpa.domain;

import java.util.Locale;

public enum Direction {

    ASC, DESC;

    public static Direction fromString(String value) {

        try {
            return Direction.valueOf(value.toUpperCase(Locale.US));
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format(
                    "Invalid value '%s' for orders given! Has to be either 'desc' or 'asc' (case insensitive).", value), e);
        }
    }

    public static Direction fromStringOrNull(String value) {

        try {
            return fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String toMethodNameString() {
        if(this == ASC) {
            return "Asc";
        } else {
            return "Desc";
        }
    }

}
