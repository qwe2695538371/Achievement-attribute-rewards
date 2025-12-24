package com.playerattributemanagement.common.reward;

import java.util.Locale;

public final class RewardFormat {
    public static final double EPSILON = 1.0E-6;

    private RewardFormat() {}

    public static boolean isZero(double value) {
        return Math.abs(value) < EPSILON;
    }

    public static String formatSigned(double value) {
        String formatted = String.format(Locale.ROOT, "%.2f", value);
        return value >= 0 ? "+" + formatted : formatted;
    }
}
