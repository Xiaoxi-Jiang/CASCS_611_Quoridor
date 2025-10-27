package puzzle;

import game.Game;
import game.InputValidator;
import game.Player;

import java.util.Collections;

/**
 * Console UI for the sliding puzzle: size selection, instructions, and move loop.
 *
 * Author: Jigar K
 * Date: 2025-09-29
 */
public final class SlidingPuzzle extends Game {

    @Override
    public void start() {
        println("\n=== Sliding Puzzle ===");
        println("How to play:");
        println(" - Choose board size (rows x cols).");
        println(" - Each turn, type the TILE NUMBER you want to slide into the empty.");
        println("   (Only tiles directly adjacent to the empty can move.)");
        println(" - Type 'R' to reshuffle, 'Q' to quit.\n");

        InputValidator v = this.io;
        int rows = v.readIntInRange("Rows (2..10): ", 2, 10);
        int cols = v.readIntInRange("Cols (2..10): ", 2, 10);
        SlidingPuzzleBoard board = new SlidingPuzzleBoard(rows, cols);
        registerBoard(board);
        registerPlayers(Collections.singletonList(new Player("Solo Player")));

        int moves = 0;
        while (true) {
            println(board.render());
            if (board.isSolved()) {
                println("congratulations");
                println("moves: " + moves + "\n");
                return;
            }

            String cmd = v.readNonEmpty("Enter tile number, or R to reshuffle, Q to quit: ");
            String t = cmd.trim().toLowerCase();
            if (t.equals("q")) return;
            if (t.equals("r")) { board.reset(); moves = 0; continue; }

            Integer val = null;
            try { val = Integer.parseInt(cmd.trim()); } catch (NumberFormatException ignored) {}
            if (val == null) { println("Please enter a valid number, or 'R'/'Q'."); continue; }

            boolean ok = board.moveNumber(val);
            if (!ok) { println("Illegal move. That tile must be adjacent to the empty."); }
            else { moves++; }
        }
    }
}
