import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

/** Members are stored by phone number in members.txt. */
public class Membership {
    public static final int FEE = 500;               // Taka
    public static final int DISCOUNT_PERCENT = 10;   // off the whole cart
    private static final Path FILE = Paths.get("members.txt");

    public boolean isMember(String phone) {
        if (!Files.exists(FILE)) return false;
        try {
            for (String line : Files.readAllLines(FILE)) {
                if (line.trim().equals(phone)) return true;
            }
        } catch (IOException e) {
            System.out.println("Warning: could not read members file.");
        }
        return false;
    }

    public boolean join(String phone) {
        try {
            Files.write(FILE, List.of(phone), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            System.out.println("Could not save membership.");
            return false;
        }
    }
}
