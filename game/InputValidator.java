package game;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Robust input helpers for integers and strings with range checking.
 
 * Author: Xiaoxi J
 * Date: 2025-10-01
 */

public final class InputValidator {
    private final Scanner sc;
    private final PrintStream out;

    public InputValidator(InputStream in, PrintStream out) {
        this.sc = new Scanner(in);
        this.out = out;
    }

    public int readIntInRange(String prompt, int lo, int hi) {
        while (true) {
            out.print(prompt);
            try {
                String token = sc.nextLine().trim();
                int val = Integer.parseInt(token);
                if (val < lo || val > hi) {
                    out.println("Please enter a number between " + lo + " and " + hi + ".");
                } else {
                    return val;
                }
            } catch (NumberFormatException | NoSuchElementException e) {
                out.println("Invalid integer. Try again.");
            }
        }
    }

    public String readNonEmpty(String prompt) {
        while (true) {
            out.print(prompt);
            String line = sc.nextLine();
            if (line != null && !line.trim().isEmpty()) return line.trim();
            out.println("Please enter a non-empty string.");
        }
    }
}
