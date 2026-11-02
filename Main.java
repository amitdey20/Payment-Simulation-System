import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Authentication auth = new Authentication();
        Catalog catalog = new Catalog();
        Membership membership = new Membership();
        ShopOwner owner = new ShopOwner();
        PaymentProcessor paymentProcessor = new PaymentProcessor();

        System.out.println("==============================================");
        System.out.println("         WELCOME TO CHOCOLALA LAND");
        System.out.println("==============================================");

        while (true) {
            User user = loginMenu(scanner, auth, owner, catalog);
            if (user == null) break;                       // Exit chosen
            boolean finished = customerMenu(scanner, user, catalog, membership, paymentProcessor);
            if (finished) break;                           // payment done
            System.out.println("\nYou have been logged out.");
        }
        scanner.close();
    }

    /** Returns the logged-in user, or null if the person chose Exit. */
    private static User loginMenu(Scanner scanner, Authentication auth, ShopOwner owner, Catalog catalog) {
        User currentUser = null;
        while (currentUser == null) {
            System.out.println("\n1. Register");
            System.out.println("2. Login");
            System.out.println("3. Forgot Password");
            System.out.println("4. Exit");
            System.out.println("5. Shop Owner Login");
            int choice = InputHelper.readInt(scanner, "Choose an option: ", 1, 5);

            switch (choice) {
                case 1 -> {
                    if (auth.registerUser(scanner) != null) {
                        currentUser = auth.login(scanner);
                        if (currentUser == null) {
                            System.out.println("\nLogin failed. Choose Login or Forgot Password from the menu.");
                        }
                    }
                }
                case 2 -> {
                    currentUser = auth.login(scanner);
                    if (currentUser == null) {
                        System.out.println("\nLogin failed. If you forgot your password, choose 'Forgot Password'.");
                    }
                }
                case 3 -> auth.forgotPassword(scanner);
                case 4 -> {
                    System.out.println("\nGoodbye!");
                    return null;
                }
                case 5 -> owner.run(scanner, catalog);
            }
        }
        return currentUser;
    }

    /** Returns true when the order is paid (program ends), false when the user logs out. */
    private static boolean customerMenu(Scanner scanner, User user, Catalog catalog,
                                        Membership membership, PaymentProcessor paymentProcessor) {
        while (true) {
            boolean member = membership.isMember(user.getPhone());
            System.out.println("\n==============================================");
            System.out.println("                 MAIN MENU");
            System.out.println("==============================================");
            System.out.println("Membership: " + (member
                    ? "ACTIVE (" + Membership.DISCOUNT_PERCENT + "% off every order)" : "Not a member"));
            System.out.println("1. Shop");
            System.out.println(member ? "2. Membership (already active)"
                    : "2. Buy Membership (" + Membership.FEE + " Taka, " + Membership.DISCOUNT_PERCENT + "% off all orders)");
            System.out.println("0. Logout");

            int choice = InputHelper.readInt(scanner, "Choose an option: ", 0, 2);
            switch (choice) {
                case 1 -> {
                    ShoppingCart cart = new ShoppingCart(catalog, member);
                    if (cart.shop(scanner) == ShoppingCart.Result.LOGOUT) return false;
                    printSummary(user, cart, member);
                    if (collectPayment(scanner, paymentProcessor, cart.getTotalPrice(), user)) return true;
                }
                case 2 -> {
                    if (member) {
                        System.out.println("\nYou are already a member!");
                        break;
                    }
                    System.out.println("\nMembership fee: " + Membership.FEE + " Taka. You get "
                            + Membership.DISCOUNT_PERCENT + "% off every order on this phone number ("
                            + user.getPhone() + ").");
                    int confirm = InputHelper.readInt(scanner, "Buy membership? (1 = Yes, 2 = No): ", 1, 2);
                    if (confirm == 1
                            && collectPayment(scanner, paymentProcessor, Membership.FEE, user)
                            && membership.join(user.getPhone())) {
                        System.out.println("\nCongratulations! You are now a member. "
                                + Membership.DISCOUNT_PERCENT + "% discount will apply to your orders.");
                    }
                }
                case 0 -> {
                    return false;
                }
            }
        }
    }

    /** Asks for a payment method and retries until paid or the user gives up. */
    private static boolean collectPayment(Scanner scanner, PaymentProcessor processor, int amount, User user) {
        while (true) {
            System.out.println("\n==============================================");
            System.out.println("               Payment Methods");
            System.out.println("==============================================");
            System.out.println("Amount to pay: " + amount + " Taka");
            System.out.println("1. bKash  2. Nagad  3. Upay  4. Rocket  5. CellFin  6. Card Payment");
            int method = InputHelper.readInt(scanner, "\nChoose option (1-6): ", 1, 6);

            if (processor.processPayment(scanner, method, amount, user)) return true;

            int retry = InputHelper.readInt(scanner,
                    "\nWould you like to try paying again? (1 = Yes, 2 = No): ", 1, 2);
            if (retry == 2) {
                System.out.println("\nPayment not completed.");
                return false;
            }
        }
    }

    private static void printSummary(User user, ShoppingCart cart, boolean member) {
        System.out.println("\n==============================================");
        System.out.println("               CART SUMMARY");
        System.out.println("==============================================");
        System.out.println("Customer Name : " + user.getName());
        System.out.println("Phone         : " + user.getPhone());
        System.out.println("Membership    : " + (member ? "Member (" + Membership.DISCOUNT_PERCENT + "% off)" : "No"));
        System.out.println("Total Amount  : " + cart.getTotalPrice() + " Taka");
        System.out.println("\nFinal Items:");
        cart.printCartFormat();

        System.out.println("\n==============================================");
        System.out.println("               SHOP DETAILS");
        System.out.println("==============================================");
        System.out.println("Shop Name     : CHOCOLALA LAND");
        System.out.println("Shop Location : USTC, Foy's Lake, Zakir Hossain Road, Chattogram 4202");
        System.out.println("Email         : registrar@digdig.org");
        System.out.println("Phone Number  : +8809678632793");
    }
}