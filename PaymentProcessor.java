import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Scanner;

public class PaymentProcessor {
    private final SecureRandom random = new SecureRandom();

    public boolean processPayment(Scanner scanner, int method, int totalPrice, User user) {
        if (method == 6) {
            return processCardPayment(scanner, totalPrice, user);
        }
        return processMobilePayment(scanner, method, totalPrice, user);
    }

    private boolean processCardPayment(Scanner scanner, int totalPrice, User user) {
        System.out.println();
        playConnectingAnimation();

        String cardNumber = InputHelper.readDigits(scanner, "Enter your 16-digit Card Number: ", 16, 16, 3);
        if (cardNumber == null) {
            System.out.println("\nPayment Failed! Too many invalid attempts.");
            return false;
        }

        int expMonth = 1 + random.nextInt(12);
        int expYear = LocalDate.now().getYear() + random.nextInt(5);
        System.out.println("Simulation - Card Expiry Date is: " + String.format("%02d/%d", expMonth, expYear));

        int enteredMonth = InputHelper.readInt(scanner, "Enter Expiry Month (MM): ", 1, 12);
        int enteredYear = InputHelper.readInt(scanner, "Enter Expiry Year (YYYY): ", 2000, 2100);
        if (enteredMonth != expMonth || enteredYear != expYear) {
            System.out.println("\nPayment Failed! Incorrect Expiry Date.");
            return false;
        }

        String cvc = InputHelper.readDigits(scanner, "Enter 3-digit CVC/CVV: ", 3, 3, 3);
        if (cvc == null) {
            System.out.println("\nPayment Failed! Invalid CVC.");
            return false;
        }

        String maskedCardNumber = "************" + cardNumber.substring(12);
        playSendingOTPAnimation(maskedCardNumber);

        int otp = 1000 + random.nextInt(9000);
        System.out.println("Simulation - Bank OTP is: " + otp);
        int enteredOtp = InputHelper.readInt(scanner, "Enter the Bank OTP: ", 0, 999999);

        if (enteredOtp != otp) {
            System.out.println("\nPayment Cancelled! Incorrect OTP.");
            return false;
        }

        playProcessingAnimation();
        System.out.println("\nPayment Successful of " + totalPrice + " Taka via Card!");
        System.out.println("Thank you, " + user.getName() + "!");
        return true;
    }

    private boolean processMobilePayment(Scanner scanner, int method, int totalPrice, User user) {
        String serviceName = switch (method) {
            case 1 -> "bKash";
            case 2 -> "Nagad";
            case 3 -> "Upay";
            case 4 -> "Rocket";
            case 5 -> "CellFin";
            default -> "";
        };

        if (serviceName.isEmpty()) {
            System.out.println("Invalid Payment Method.");
            return false;
        }

        System.out.println();
        playConnectingAnimation();

        String accountNumber = InputHelper.readBdPhone(scanner,
                "Enter your 11-digit " + serviceName + " number: ", 3);
        if (accountNumber == null) {
            System.out.println("\nPayment Failed! Too many invalid attempts.");
            return false;
        }

        playSendingOTPAnimation(accountNumber);

        int otp = 1000 + random.nextInt(9000);
        System.out.println("Simulation - Your OTP is: " + otp);
        int enteredOtp = InputHelper.readInt(scanner, "Enter the OTP: ", 0, 999999);

        String pin = InputHelper.readDigits(scanner, "Enter your PIN (4-5 digits): ", 4, 5, 3);
        if (pin == null) {
            System.out.println("\nPayment Failed! Invalid PIN.");
            return false;
        }

        if (enteredOtp != otp) {
            System.out.println("\nPayment Cancelled! Incorrect OTP.");
            return false;
        }

        playProcessingAnimation();
        System.out.println("Payment Successful of " + totalPrice + " Taka via " + serviceName + "!");
        System.out.println("Thank you, " + user.getName() + "!");
        return true;
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

    private void playConnectingAnimation() {
        System.out.print("Connecting to payment service [");
        try {
            for (int i = 0; i < 10; i++) {
                System.out.print("=");
                System.out.flush();
                Thread.sleep(150);
            }
            System.out.println("]\n");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void playProcessingAnimation() {
        String[] steps = { "Validating payment information", "Connecting to payment service",
                           "Verifying security details", "Processing transaction", "Finalizing payment" };
        System.out.println();
        try {
            for (String step : steps) {
                System.out.print(step);
                System.out.flush();
                for (int i = 0; i < 3; i++) {
                    Thread.sleep(400);
                    System.out.print(".");
                    System.out.flush();
                }
                Thread.sleep(400);
                System.out.println(" DONE\n");
            }
            System.out.print("Payment Successful [");
            for (int i = 0; i < 10; i++) {
                System.out.print("=");
                System.out.flush();
                Thread.sleep(100);
            }
            System.out.println("]\n");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
