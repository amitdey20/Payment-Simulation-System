import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The shop's product list. Saved in products.txt so owner changes are kept. */
public class Catalog {
    private static final Path FILE = Paths.get("products.txt");
    private static final String SEP = "\t";

    private final List<Product> products = new ArrayList<>();

    public Catalog() {
        load();
        if (products.isEmpty() && !Files.exists(FILE)) {
            seedDefaults();
            save();
        }
    }

    public List<Product> getAll() { return Collections.unmodifiableList(products); }
    public int size() { return products.size(); }
    public Product get(int index) { return products.get(index); }
    public int indexOf(Product p) { return products.indexOf(p); }

    public void add(String name, int price) {
        products.add(new Product(name.replace("\t", " "), price, 0));
        save();
    }

    public void remove(int index) {
        products.remove(index);
        save();
    }

    public void setDiscount(int index, int percent) {
        products.get(index).setDiscountPercent(percent);
        save();
    }

    private void seedDefaults() {
        String[] names = { "Dairy Milk", "KitKat", "Snickers", "Ferrero Rocher", "Toblerone",
                           "Mars", "Bounty", "Twix", "Cadbury Bournville", "Milky Way" };
        int[] prices = { 100, 80, 120, 350, 250, 110, 130, 90, 200, 70 };
        for (int i = 0; i < names.length; i++) products.add(new Product(names[i], prices[i], 0));
    }

    private void load() {
        if (!Files.exists(FILE)) return;
        try {
            for (String line : Files.readAllLines(FILE)) {
                String[] p = line.split(SEP, -1);
                if (p.length != 3) continue;
                products.add(new Product(p[0], Integer.parseInt(p[1]), Integer.parseInt(p[2])));
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Warning: could not read products file.");
        }
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (Product p : products) {
            lines.add(String.join(SEP, p.getName(), String.valueOf(p.getPrice()),
                    String.valueOf(p.getDiscountPercent())));
        }
        try {
            Files.write(FILE, lines);
        } catch (IOException e) {
            System.out.println("Warning: could not save products file.");
        }
    }
}
