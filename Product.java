public class Product {
    private final String name;
    private int price;
    private int discountPercent;

    public Product(String name, int price, int discountPercent) {
        this.name = name;
        this.price = price;
        this.discountPercent = discountPercent;
    }

    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getDiscountPercent() { return discountPercent; }
    public void setPrice(int price) { this.price = price; }
    public void setDiscountPercent(int discountPercent) { this.discountPercent = discountPercent; }

    /** Price after the shop owner's product discount. */
    public int getFinalPrice() {
        return (int) Math.round(price * (100 - discountPercent) / 100.0);
    }
}
