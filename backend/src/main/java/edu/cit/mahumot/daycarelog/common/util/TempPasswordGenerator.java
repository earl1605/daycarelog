package edu.cit.mahumot.daycarelog.common.util;

import java.security.SecureRandom;

// Shared by UserService (staff/admin creation) and GuardianService (parent account
// creation) so both produce temp passwords the same way instead of duplicating it.
public final class TempPasswordGenerator {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private TempPasswordGenerator() {}

    public static String generate() {
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
