import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

public class ShopOwner {
    private static final Path FILE = Paths.get("owner.txt");   // name \t salt \t hash
    private static final String SEP = "\t";
    private static final int MAX_ATTEMPTS = 3;

    public void run(Scanner scanner, Catalog catalog) {
        try {
            // First time only: register the owner. After that, password-only login.
            if (!Files.exists(FILE)) {
                if (!register(scanner)) return;
            }
            String[] rec = login(scanner);
            if (rec == null) return;
            System.out.println("\nLogin Successful! Welcome, " + rec[0] + "!");
            panel(scanner, catalog);
        } catch (IOException | GeneralSecurityException e) {
            System.out.println("An error occurred while accessing owner data.");
        }
    }

    private void panel(Scanner scanner, Catalog catalog) throws IOException, GeneralSecurityException {
        while (true) {
            System.out.println("\n==============================================");
            System.out.println("               SHOP OWNER PANEL");
            System.out.println("==============================================");
            System.out.println("1. View Products");
            System.out.println("2. Add Product");
            System.out.println("3. Remove Product");
            System.out.println("4. Set / Remove Product Discount");
            System.out.println("5. Change Password");
            System.out.println("0. Logout");

            int choice = InputHelper.readInt(scanner, "Choose an option: ", 0, 5);
            switch (choice) {
                case 1 -> printProducts(catalog);
                case 2 -> addProduct(scanner, catalog);
                case 3 -> removeProduct(scanner, catalog);
                case 4 -> setDiscount(scanner, catalog);
                case 5 -> changePassword(scanner);
                case 0 -> {
                    System.out.println("\nLogged out from owner panel.");
                    return;
                }
            }
        }
    }

    private boolean register(Scanner scanner) throws IOException, GeneralSecurityException {
        System.out.println("\n----------- SHOP OWNER REGISTRATION (first time) -----------");
        String name;
        while (true) {
            System.out.print("Enter owner name: ");
            name = scanner.nextLine().trim().replace("\t", " ");
            if (name.length() >= 2) break;
            System.out.println("Name must be at least 2 characters.");
        }
        String password = InputHelper.readNewPassword(scanner,
                "Create owner password (" + InputHelper.PASSWORD_HINT + "): ");
        save(name, password);
        System.out.println("\nOwner registration successful! Please log in with your password.");
        return true;
    }

    /** Password-only login. Returns the owner record, or null after too many wrong attempts. */
    private String[] login(Scanner scanner) throws IOException, GeneralSecurityException {
        System.out.println("\n----------- SHOP OWNER LOGIN -----------");
        String[] rec = read();
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            System.out.print("Enter owner password: ");
            if (Authentication.verify(scanner.nextLine(), rec[1], rec[2])) return rec;
            System.out.println("Incorrect password.");
            if (attempt < MAX_ATTEMPTS) System.out.println("Attempts remaining: " + (MAX_ATTEMPTS - attempt) + "\n");
        }
        System.out.println("\nToo many wrong attempts. Access denied.");
        return null;
    }

    private void changePassword(Scanner scanner) throws IOException, GeneralSecurityException {
        System.out.println("\n----------- CHANGE OWNER PASSWORD -----------");
        String[] rec = read();
        boolean ok = false;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            System.out.print("Enter current password: ");
            if (Authentication.verify(scanner.nextLine(), rec[1], rec[2])) {
                ok = true;
                break;
            }
            System.out.println("Incorrect password.");
            if (attempt < MAX_ATTEMPTS) System.out.println("Attempts remaining: " + (MAX_ATTEMPTS - attempt) + "\n");
        }
        if (!ok) {
            System.out.println("\nPassword change failed! Too many incorrect attempts.");
            return;
        }
        String newPassword = InputHelper.readNewPassword(scanner,
                "Enter new password (" + InputHelper.PASSWORD_HINT + "): ");
        save(rec[0], newPassword);
        System.out.println("\nPassword changed successfully!");
    }

    private String[] read() throws IOException {
        return Files.readAllLines(FILE).get(0).split(SEP, -1);
    }

    private void save(String name, String password) throws IOException, GeneralSecurityException {
        byte[] salt = Authentication.newSalt();
        String line = String.join(SEP, name, Base64.getEncoder().encodeToString(salt),
                Authentication.hash(password, salt));
        Files.write(FILE, List.of(line));
    }

    public static void printProducts(Catalog catalog) {
        System.out.println("\n--- Products ---");
        if (catalog.size() == 0) {
            System.out.println("(no products)");
            return;
        }
        for (int i = 0; i < catalog.size(); i++) {
            Product p = catalog.get(i);
            System.out.println((i + 1) + ". " + describe(p));
        }
    }

    public static String describe(Product p) {
        if (p.getDiscountPercent() > 0) {
            return p.getName() + " - " + p.getFinalPrice() + " Taka (" + p.getDiscountPercent()
                    + "% OFF, was " + p.getPrice() + ")";
        }
        return p.getName() + " - " + p.getPrice() + " Taka";
    }

    private void addProduct(Scanner scanner, Catalog catalog) {
        String name;
        while (true) {
            System.out.print("Enter product name: ");
            name = scanner.nextLine().trim();
            if (!name.isEmpty()) break;
            System.out.println("Name cannot be empty.");
        }
        int price = InputHelper.readInt(scanner, "Enter price (Taka): ", 1, 1_000_000);
        catalog.add(name, price);
        System.out.println("\n--> Added " + name + " (" + price + " Taka).");
    }

    private void removeProduct(Scanner scanner, Catalog catalog) {
        if (catalog.size() == 0) {
            System.out.println("\nNo products to remove.");
            return;
        }
        printProducts(catalog);
        int n = InputHelper.readInt(scanner, "\nProduct number to remove (0 to cancel): ", 0, catalog.size());
        if (n == 0) return;
        String name = catalog.get(n - 1).getName();
        catalog.remove(n - 1);
        System.out.println("\n--> Removed " + name + ".");
    }

    private void setDiscount(Scanner scanner, Catalog catalog) {
        if (catalog.size() == 0) {
            System.out.println("\nNo products available.");
            return;
        }
        printProducts(catalog);
        int n = InputHelper.readInt(scanner, "\nProduct number (0 to cancel): ", 0, catalog.size());
        if (n == 0) return;
        int percent = InputHelper.readInt(scanner, "Discount percent (0-90, 0 = remove discount): ", 0, 90);
        catalog.setDiscount(n - 1, percent);
        System.out.println("\n--> Updated: " + describe(catalog.get(n - 1)));
    }
}