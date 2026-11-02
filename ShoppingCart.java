import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class ShoppingCart {
    private final Catalog catalog;
    private final boolean member;
    private final Map<Product, Integer> quantities = new LinkedHashMap<>();

    public ShoppingCart(Catalog catalog, boolean member) {
        this.catalog = catalog;
        this.member = member;
    }

    public enum Result { CHECKOUT, LOGOUT }

    public Result shop(Scanner scanner) {
        while (true) {
            int n = catalog.size();
            System.out.println("\n==============================================");
            System.out.println("            CHOCOLALA LAND SHOP");
            System.out.println("==============================================");
            if (member) {
                System.out.println("* Member: extra " + Membership.DISCOUNT_PERCENT + "% off at checkout *");
            }
            if (n == 0) System.out.println("(no products available right now)");
            for (int i = 0; i < n; i++) {
                System.out.println((i + 1) + ". " + ShopOwner.describe(catalog.get(i)));
            }
            System.out.println((n + 1) + ". Remove Item from Cart");
            System.out.println((n + 2) + ". Logout");
            System.out.println("0. Checkout & Pay");

            int choice = InputHelper.readInt(scanner, "\nSelect an option: ", 0, n + 2);

            if (choice == 0) {
                if (getItemCount() == 0) {
                    System.out.println("\nYour cart is empty! Please select at least one item.\n");
                    continue;
                }
                return Result.CHECKOUT;
            }
            if (choice == n + 2) {
                if (getItemCount() > 0) {
                    int confirm = InputHelper.readInt(scanner,
                            "\nYour cart will be cleared. Logout anyway? (1 = Yes, 2 = No): ", 1, 2);
                    if (confirm == 2) continue;
                }
                return Result.LOGOUT;
            }
            if (choice == n + 1) {
                removeItem(scanner);
            } else {
                addItem(catalog.get(choice - 1));
            }
        }
    }

    private void addItem(Product p) {
        quantities.merge(p, 1, Integer::sum);
        System.out.println("\n--> Added " + p.getName() + " (" + p.getFinalPrice() + " Taka) to Cart.\n");
        printCartFormat();
    }

    private void removeItem(Scanner scanner) {
        if (getItemCount() == 0) {
            System.out.println("\nYour cart is already empty!");
            return;
        }

        System.out.println("\n--- Current Items in Cart ---");
        for (Map.Entry<Product, Integer> e : quantities.entrySet()) {
            System.out.println((catalog.indexOf(e.getKey()) + 1) + ". " + e.getKey().getName()
                    + " - Quantity: " + e.getValue());
        }

        int n = catalog.size();
        int removeChoice = InputHelper.readInt(scanner,
                "\nEnter the product number (1-" + n + ") you want to remove (0 to cancel): ", 0, n);
        if (removeChoice == 0) return;

        Product p = catalog.get(removeChoice - 1);
        int qty = quantities.getOrDefault(p, 0);
        if (qty > 0) {
            if (qty == 1) quantities.remove(p); else quantities.put(p, qty - 1);
            System.out.println("\n--> Removed 1 " + p.getName() + " from Cart.\n");
            printCartFormat();
        } else {
            System.out.println("\nThat item is not in your cart!");
        }
    }

    public void printCartFormat() {
        System.out.println("CURRENT CART");
        System.out.println("------------------------------------------");
        for (Map.Entry<Product, Integer> e : quantities.entrySet()) {
            Product p = e.getKey();
            int qty = e.getValue();
            System.out.printf("%-24s x%-2d = %d Taka%n", p.getName(), qty, qty * p.getFinalPrice());
        }
        System.out.println("------------------------------------------");
        System.out.println("Total items: " + getItemCount());
        if (member) {
            System.out.println("Subtotal: " + getSubtotal() + " Taka");
            System.out.println("Member discount (" + Membership.DISCOUNT_PERCENT + "%): -" + getMemberDiscount() + " Taka");
        }
        System.out.println("Total price: " + getTotalPrice() + " Taka");
    }

    public int getItemCount() {
        int count = 0;
        for (int q : quantities.values()) count += q;
        return count;
    }

    /** Sum after product discounts, before membership discount. */
    public int getSubtotal() {
        int sum = 0;
        for (Map.Entry<Product, Integer> e : quantities.entrySet()) {
            sum += e.getKey().getFinalPrice() * e.getValue();
        }
        return sum;
    }

    public int getMemberDiscount() {
        return member ? (int) Math.round(getSubtotal() * Membership.DISCOUNT_PERCENT / 100.0) : 0;
    }

    public int getTotalPrice() { return getSubtotal() - getMemberDiscount(); }
}