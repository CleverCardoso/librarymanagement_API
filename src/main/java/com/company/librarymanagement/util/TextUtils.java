package com.company.librarymanagement.util;

public final class TextUtils {

    private TextUtils() {
    }

    public static String cleanBasic(String s) {
        if (s == null) return null;
        s = s.trim();
        s = s.replaceAll("\\s+", " ");
        return s.isEmpty() ? null : s;
    }

    public static String cleanName(String s) {
        s = cleanBasic(s);
        if (s == null) return null;

        s = s.toLowerCase();
        String[] parts = s.split(" ");
        StringBuilder out = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            if (p.isEmpty()) continue;

            out.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) {
                out.append(p.substring(1));
            }

            if (i < parts.length - 1) {
                out.append(" ");
            }
        }

        return out.toString();
    }

    // --- MÉTODOS NOVOS ADICIONADOS ---

    public static String cleanEmail(String email) {
        if (email == null) return null;
        return email.toLowerCase().trim();
    }

    public static String cleanPhone(String phone) {
        // Usa o cleanBasic para tirar espaços duplos e nas pontas
        return cleanBasic(phone);
    }
}