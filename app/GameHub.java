package app;

import dots.DotsAndBoxes;
import game.InputValidator;
import puzzle.SlidingPuzzle;
import quoridor.QuoridorGame;

public class GameHub {
    private final InputValidator v;
    public GameHub(InputValidator validator){
        this.v = validator;
    }
    public void run(){
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
