package app;

import game.InputValidator;
import puzzle.SlidingPuzzle;
import dots.DotsAndBoxes;
import quoridor.QuoridorGame;

/**
 * Main launcher offering both games.
 * Author: Jigar K
 * Date: 2025-10-05
 */
public final class Main {
    public static void main(String[] args) {
        InputValidator v = new InputValidator(System.in, System.out);
        while (true) {
            System.out.println("=== Game Hub ===");
            System.out.println("1) Sliding Puzzle");
            System.out.println("2) Dots & Boxes");
            System.out.println("3) Quoridor");
            System.out.println("0) Quit");
            int pick = v.readIntInRange("> ", 0, 3);
            if (pick == 0) return;
            if (pick == 1) new SlidingPuzzle().start();
            if (pick == 2) new DotsAndBoxes().start();
            if (pick == 3) new QuoridorGame().start();
        }
    }
}
