import java.util.Scanner;

public final class InputHelper {
    // Valid BD mobile: 11 digits, must start with 014-019
    public static final String BD_PHONE_REGEX = "01[4-9]\\d{8}";
    public static final String BD_PHONE_HINT =
            "You must use a valid BD mobile number.";

    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final String PASSWORD_HINT =
            "min " + MIN_PASSWORD_LENGTH + " characters, with uppercase, lowercase, number & special character";

    private InputHelper() {}

    /** Returns null if the password is strong enough, otherwise a message saying what is missing. */
    public static String passwordProblem(String p) {
        StringBuilder sb = new StringBuilder();
        if (p.length() < MIN_PASSWORD_LENGTH) sb.append("\n - at least ").append(MIN_PASSWORD_LENGTH).append(" characters");
        if (!p.matches(".*[a-z].*")) sb.append("\n - a lowercase letter");
        if (!p.matches(".*[A-Z].*")) sb.append("\n - an uppercase letter");
        if (!p.matches(".*\\d.*")) sb.append("\n - a number");
        if (!p.matches(".*[^A-Za-z0-9\\s].*")) sb.append("\n - a special character (e.g. @ # $ % !)");
        return sb.length() == 0 ? null : "Weak password! It must have:" + sb;
    }

    /** Asks for a new password until it meets the policy and is confirmed. */
    public static String readNewPassword(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String pw = scanner.nextLine();
            String problem = passwordProblem(pw);
            if (problem != null) {
                System.out.println(problem);
                continue;
            }
            System.out.print("Confirm password: ");
            if (pw.equals(scanner.nextLine())) return pw;
            System.out.println("Passwords do not match. Try again.");
        }
    }

    public static boolean isValidBdPhone(String s) {
        return s != null && s.matches(BD_PHONE_REGEX);
    }

    public static String readBdPhone(Scanner scanner, String prompt, int attempts) {
        for (int attempt = 1; attempt <= attempts; attempt++) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (isValidBdPhone(input)) return input;

            System.out.println("\nInvalid number! " + BD_PHONE_HINT);
            if (attempt < attempts) System.out.println("Attempts remaining: " + (attempts - attempt) + "\n");
        }
        return null;
    }

    public static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) return value;
                System.out.println("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number.");
            }
        }
    }

    public static String readDigits(Scanner scanner, String prompt, int minLen, int maxLen, int attempts) {
        for (int attempt = 1; attempt <= attempts; attempt++) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.matches("\\d{" + minLen + "," + maxLen + "}")) return input;

            String need = (minLen == maxLen) ? "exactly " + minLen : minLen + "-" + maxLen;
            System.out.println("\nInvalid input! Must be " + need + " digits (numbers only).");
            if (attempt < attempts) System.out.println("Attempts remaining: " + (attempts - attempt) + "\n");
        }
        return null;
    }
}