package edu.cit.mahumot.daycarelog.features.health;

import java.math.BigDecimal;
import java.time.LocalDate;

// Mirrors web/src/utils/nutritionalStatus.js classifyNutritionalStatus (WHO weight-for-age
// median tables, 0-60 months) -- keep both in sync if the thresholds or tables ever change.
// Age is computed as of the measurement date (not "today"), since this backs a dated,
// historical record rather than a live preview.
final class NutritionalStatusCalculator {

    static final String NORMAL = "NORMAL";
    static final String UNDERWEIGHT = "UNDERWEIGHT";
    static final String SEVERELY_UNDERWEIGHT = "SEVERELY_UNDERWEIGHT";
    static final String OVERWEIGHT = "OVERWEIGHT";

    private static final double[] MEDIAN_MALE = {
        3.3,4.5,5.6,6.4,7.0,7.5,7.9,8.3,8.6,8.9,9.2,9.4,9.6,10.0,10.3,10.6,10.9,11.1,11.4,11.6,
        11.8,12.0,12.2,12.4,12.6,12.8,13.0,13.2,13.4,13.6,13.8,14.0,14.2,14.4,14.6,14.8,14.9,15.1,
        15.3,15.5,15.7,15.9,16.1,16.2,16.4,16.6,16.8,17.0,17.2,17.4,17.5,17.7,17.9,18.1,18.3,18.5,
        18.7,18.9,19.1,19.3,19.5
    };

    private static final double[] MEDIAN_FEMALE = {
        3.2,4.2,5.1,5.8,6.4,6.9,7.3,7.6,7.9,8.2,8.5,8.7,8.9,9.2,9.5,9.8,10.0,10.2,10.5,10.7,
        10.9,11.1,11.3,11.5,11.7,11.9,12.1,12.3,12.5,12.7,12.9,13.1,13.3,13.5,13.7,13.9,14.1,14.2,
        14.4,14.6,14.8,15.0,15.1,15.3,15.5,15.7,15.9,16.1,16.2,16.4,16.6,16.8,17.0,17.2,17.4,17.6,
        17.8,18.0,18.2,18.4,18.6
    };

    private NutritionalStatusCalculator() {}

    static String classify(BigDecimal weightKg, LocalDate dateOfBirth, String sex, LocalDate asOf) {
        if (weightKg == null || dateOfBirth == null || asOf == null) return null;

        int months = (asOf.getYear() - dateOfBirth.getYear()) * 12 + (asOf.getMonthValue() - dateOfBirth.getMonthValue());
        if (months < 0 || months > 60) return null;

        double[] table = "female".equalsIgnoreCase(sex) ? MEDIAN_FEMALE : MEDIAN_MALE;
        double median = table[Math.min(months, 60)];
        double ratio = weightKg.doubleValue() / median;

        if (ratio >= 1.20) return OVERWEIGHT;
        if (ratio >= 0.90) return NORMAL;
        if (ratio >= 0.75) return UNDERWEIGHT;
        return SEVERELY_UNDERWEIGHT;
    }
}
