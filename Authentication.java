import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Authentication {
    private static final Path FILE = Paths.get("users.txt");
    private static final String SEP = "\t";
    private static final int F_NAME = 0, F_PHONE = 1, F_USERNAME = 2, F_SALT = 3, F_HASH = 4;
    private static final int MAX_ATTEMPTS = 3;
    private static final SecureRandom RANDOM = new SecureRandom();

    public User registerUser(Scanner scanner) {
        System.out.println("\n----------- USER REGISTRATION -----------");

        String name = readValid(scanner, "Enter your full name: ",
                s -> s.length() >= 2, "Name must be at least 2 characters.");

        try {
            String phone;
            while (true) {
                phone = readValid(scanner, "Enter your 11-digit mobile number: ",
                        this::isValidPhone, InputHelper.BD_PHONE_HINT);
                if (findRecord(F_PHONE, phone) == null) break;
                System.out.println("This phone number is already registered. Use another one or log in.");
            }

            String username;
            while (true) {
                username = readValid(scanner, "Create a username (3-20 letters/numbers/_): ",
                        s -> s.matches("[A-Za-z0-9_]{3,20}") && !s.matches("\\d{11}"),
                        "Username must be 3-20 characters (letters, numbers, underscore) and cannot look like a phone number.");
                if (findRecord(F_USERNAME, username) == null) break;
                System.out.println("That username is already taken. Try another one.");
            }

            String password = readPassword(scanner, "Create a password (" + InputHelper.PASSWORD_HINT + "): ");

            byte[] salt = newSalt();
            String line = String.join(SEP, clean(name), phone, username,
                    Base64.getEncoder().encodeToString(salt), hash(password, salt));
            Files.write(FILE, List.of(line), StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            System.out.println("\nRegistration Successful!");
            System.out.println("Your account has been created.");
            return new User(clean(name), phone, username);
        } catch (IOException | GeneralSecurityException e) {
            System.out.println("An error occurred while saving user data.");
            return null;
        }
    }

    public User login(Scanner scanner) {
        System.out.println("\n----------- USER LOGIN -----------");
        if (!Files.exists(FILE)) {
            System.out.println("\nNo registered accounts found. Please register first.");
            return null;
        }

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            System.out.print("Enter username or phone number: ");
            String id = scanner.nextLine().trim();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            try {
                String[] rec = findRecord(F_USERNAME, id);
                if (rec == null && isValidPhone(id)) rec = findRecord(F_PHONE, id);
                if (rec != null && verify(password, rec[F_SALT], rec[F_HASH])) {
                    System.out.println("\nLogin Successful!");
                    System.out.println("Welcome, " + rec[F_NAME] + "!");
                    return new User(rec[F_NAME], rec[F_PHONE], rec[F_USERNAME]);
                }
            } catch (IOException | GeneralSecurityException e) {
                System.out.println("Error reading users file.");
                return null;
            }

            System.out.println("Incorrect username/phone or password.");
            if (attempt < MAX_ATTEMPTS) {
                System.out.println("Attempts remaining: " + (MAX_ATTEMPTS - attempt) + "\n");
            }
        }
        return null;
    }

    public boolean forgotPassword(Scanner scanner) {
        System.out.println("\n----------- FORGOT PASSWORD -----------");
        if (!Files.exists(FILE)) {
            System.out.println("\nNo registered accounts found. Please register first.");
            return false;
        }

        String phone = readValid(scanner, "Enter the mobile number you registered with: ",
                this::isValidPhone, InputHelper.BD_PHONE_HINT);

        try {
            List<String> lines = Files.readAllLines(FILE);
            int index = -1;
            String[] rec = null;
            for (int i = 0; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(SEP, -1);
                if (parts.length == 5 && parts[F_PHONE].equals(phone)) {
                    index = i;
                    rec = parts;
                    break;
                }
            }
            if (rec == null) {
                System.out.println("\nNo account found with that phone number.");
                return false;
            }

            playSendingOTPAnimation(maskPhone(phone));
            int otp = 1000 + RANDOM.nextInt(9000);
            System.out.println("Simulation - Your OTP is: " + otp);

            boolean verified = false;
            for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
                int entered = InputHelper.readInt(scanner, "Enter the OTP: ", 0, 999999);
                if (entered == otp) {
                    verified = true;
                    break;
                }
                System.out.println("Incorrect OTP.");
                if (attempt < MAX_ATTEMPTS) {
                    System.out.println("Attempts remaining: " + (MAX_ATTEMPTS - attempt) + "\n");
                }
            }
            if (!verified) {
                System.out.println("\nPassword reset failed! Too many incorrect OTP attempts.");
                return false;
            }

            System.out.println("\nOTP verified! Your username is: " + rec[F_USERNAME]);

            String newPassword;
            while (true) {
                newPassword = readPassword(scanner, "Enter new password (" + InputHelper.PASSWORD_HINT + "): ");
                System.out.print("Confirm new password: ");
                String confirm = scanner.nextLine();
                if (newPassword.equals(confirm)) break;
                System.out.println("Passwords do not match. Try again.");
            }

            byte[] salt = newSalt();
            lines.set(index, String.join(SEP, rec[F_NAME], rec[F_PHONE], rec[F_USERNAME],
                    Base64.getEncoder().encodeToString(salt), hash(newPassword, salt)));
            Files.write(FILE, lines);

            System.out.println("\nPassword changed successfully! Please log in with your new password.");
            return true;
        } catch (IOException | GeneralSecurityException e) {
            System.out.println("An error occurred while resetting the password.");
            return false;
        }
    }

    private boolean isValidPhone(String s) {
        return InputHelper.isValidBdPhone(s);
    }

    private String maskPhone(String phone) {
        return phone.substring(0, 3) + "*****" + phone.substring(8);
    }

    private void playSendingOTPAnimation(String destination) {
        System.out.println();
        System.out.print("Sending OTP to " + destination + " ");
        try {
            for (int i = 0; i < 5; i++) {
                System.out.print(".");
                System.out.flush();
                Thread.sleep(350);
            }
            System.out.println(" Sent!\n");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String readValid(Scanner scanner, String prompt, Predicate<String> rule, String error) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (rule.test(input)) return input;
            System.out.println(error);
        }
    }

    private String readPassword(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            String problem = InputHelper.passwordProblem(input);
            if (problem == null) return input;
            System.out.println(problem);
        }
    }

    private String clean(String s) {
        return s.replace("\t", " ");
    }

    private String[] findRecord(int field, String value) throws IOException {
        if (!Files.exists(FILE)) return null;
        for (String line : Files.readAllLines(FILE)) {
            String[] parts = line.split(SEP, -1);
            if (parts.length == 5 && parts[field].equalsIgnoreCase(value)) return parts;
        }
        return null;
    }

    static byte[] newSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return salt;
    }

    static String hash(String password, byte[] salt) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        byte[] h = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(h);
    }

    static boolean verify(String password, String saltB64, String expectedHash)
            throws GeneralSecurityException {
        byte[] salt = Base64.getDecoder().decode(saltB64);
        return MessageDigest.isEqual(hash(password, salt).getBytes(), expectedHash.getBytes());
    }
}