package com.rhetorical.auth.signing;

public class Obfuscator {

    private static final String key = "Zx" + Math.log(2) / 3;

    public static String obfuscate(String s) {
        char[] result = new char[s.length()];
        for (int i = 0; i < s.length(); i++) {
            result[i] = (char) (s.charAt(i) + key.charAt(i % key.length()));
        }

        return new String(result);
    }

    public static String unobfuscate(String s) {
        char[] result = new char[s.length()];
        for (int i = 0; i < s.length(); i++) {
            result[i] = (char) (s.charAt(i) - key.charAt(i % key.length()));
        }

        return new String(result);
    }
}
