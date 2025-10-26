package dots;

import game.Game;
import game.Player;

import java.util.Arrays;

/**
 * Two-player Dots & Boxes with turn logic, scoring, bounds guidance, and end checks.
 * Adds: player name customization, session stats, and end-of-round menu.
 *
 * Author: Jigar K
 * Date: 2025-10-04
 */
public final class DotsAndBoxes extends Game {

    @Override
    public void start() {
        System.out.println("\n=== Dots & Boxes ===");
        System.out.println("How to play:");
        System.out.println(" - Choose board size (R x C boxes).");
        System.out.println(" - On your turn, type: H r c  (horizontal edge at row r, col c)");
        System.out.println("   or:            V r c  (vertical edge at row r, col c)");
        System.out.println(" - Coordinates are zero-based. If you complete a box, you move again.");
        System.out.println(" - Game ends when all boxes are claimed. Highest score wins.\n");

        // --- Player customization ---
        String p1Name = io.readNonEmpty("Player 1 name: ");
        String p2Name;
        while (true) {
            p2Name = io.readNonEmpty("Player 2 name: ");
            if (!p2Name.equalsIgnoreCase(p1Name)) break;
            System.out.println("Player names must be different. Please choose another name.");
        }

        Player player1 = new Player(p1Name);
        Player player2 = new Player(p2Name);
        registerPlayers(Arrays.asList(player1, player2));
        Player[] seats = new Player[]{null, player1, player2};

        // Session stats
        int p1Wins = 0, p2Wins = 0, ties = 0;

        // Outer loop lets you replay or change board dims
        int rows = -1, cols = -1;
        boolean haveBoard = false;

        while (true) {
            if (!haveBoard) {
                rows = io.readIntInRange("Rows (1..6): ", 1, 6);
                cols = io.readIntInRange("Cols (1..6): ", 1, 6);

                System.out.println("\nValid coordinate ranges for this board:");
                System.out.println(" - Horizontal edges: H r c  with  r in [0.." + rows + "], c in [0.." + (cols - 1) + "]");
                System.out.println(" - Vertical edges:   V r c  with  r in [0.." + (rows - 1) + "], c in [0.." + cols + "]");
                System.out.println("Example: H 0 0 draws the top-most edge of the top-left box.\n");

                haveBoard = true;
            }

            DotsBoard b = new DotsBoard(rows, cols);
            registerBoard(b);
            int[] pScore = new int[]{0, 0, 0}; // index 1=P1, 2=P2
            int player = 1;

            // ---- One round ----
            while (true) {
                System.out.println(b.render());
                System.out.println("Score " + seats[1].getName() + "=" + pScore[1] + "  " + seats[2].getName() + "=" + pScore[2] + "   Turn: " + seats[player].getName());

                String line = io.readNonEmpty("Move (e.g., H 0 1, V 2 3) or Q to quit round: ");
                if (line.equalsIgnoreCase("q")) {
                    // Print summary and return to main menu (exit this game)
                    System.out.println("\n=== Round aborted ===");
                    System.out.println("Current score — " + seats[1].getName() + "=" + pScore[1] + "  " + seats[2].getName() + "=" + pScore[2]);
                    System.out.println("Session results so far: " + seats[1].getName() + " wins=" + p1Wins + ", " + seats[2].getName() + " wins=" + p2Wins + ", ties=" + ties + "\n");
                    return;
                }

                DotsMove m = RulesDots.parse(line);
                if (m == null) { System.out.println("Invalid format. Example: H 0 1"); continue; }

                int closed = b.apply(m, seats[player], player);
                if (closed < 0) { System.out.println("Illegal move (out of bounds or already drawn). Try again."); continue; }

                pScore[player] += closed;
                // Early winner detection: if the lead exceeds remaining boxes, we can declare the winner now.
int totalBoxes = rows * cols;
int claimedBoxes = pScore[1] + pScore[2];
int remainingBoxes = totalBoxes - claimedBoxes;
int diff = Math.abs(pScore[1] - pScore[2]);

if (diff > remainingBoxes) {
    System.out.println(b.render());
    // Decide winner immediately
    if (pScore[1] > pScore[2]) {
        System.out.println("congratulations " + seats[1].getName());
    } else {
        System.out.println("congratulations " + seats[2].getName());
    }
    System.out.println("Final score — " + seats[1].getName() + "=" + pScore[1] + "  " + seats[2].getName() + "=" + pScore[2]);

    // Update session results
    if (pScore[1] > pScore[2]) p1Wins++;
    else p2Wins++;

    System.out.println("Session results — " + seats[1].getName() + " wins=" + p1Wins + ", " + seats[2].getName() + " wins=" + p2Wins + ", ties=" + ties);

    // End-of-round prompt (same as the full-board branch)
    System.out.println("\nWhat next?");
    System.out.println("1) Play again (same settings)");
    System.out.println("2) Change board size");
    System.out.println("3) Back to game menu");
    System.out.println("0) Quit");
    int choice = io.readIntInRange("> ", 0, 3);

    if (choice == 1) {
        // same settings; break to start a fresh round
    } else if (choice == 2) {
        haveBoard = false;
    } else if (choice == 3) {
        return; // back to Main menu
    } else {
        System.out.println("\nSummary results — " + seats[1].getName() + " wins=" + p1Wins + ", " + seats[2].getName() + " wins=" + p2Wins + ", ties=" + ties);
        System.out.println("goodbye");
        System.exit(0);
    }
    break; // break the round loop so the outer loop can continue per choice
}

                if (b.isFull()) {
                    System.out.println(b.render());
                    if (pScore[1] > pScore[2]) { System.out.println("congratulations " + seats[1].getName()); p1Wins++; }
                    else if (pScore[2] > pScore[1]) { System.out.println("congratulations " + seats[2].getName()); p2Wins++; }
                    else { System.out.println("tie"); ties++; }

                    System.out.println("Final score — " + seats[1].getName() + "=" + pScore[1] + "  " + seats[2].getName() + "=" + pScore[2]);
                    System.out.println("Session results — " + seats[1].getName() + " wins=" + p1Wins + ", " + seats[2].getName() + " wins=" + p2Wins + ", ties=" + ties);

                    // End-of-round prompt
                    System.out.println("\nWhat next?");
                    System.out.println("1) Play again (same settings)");
                    System.out.println("2) Change board size");
                    System.out.println("3) Back to game menu");
                    System.out.println("0) Quit");
                    int choice = io.readIntInRange("> ", 0, 3);

                    if (choice == 1) { /* same settings */ }
                    else if (choice == 2) { haveBoard = false; }
                    else if (choice == 3) { return; } // back to Main menu
                    else { // 0
                        System.out.println("\nSummary results — " + seats[1].getName() + " wins=" + p1Wins + ", " + seats[2].getName() + " wins=" + p2Wins + ", ties=" + ties);
                        System.out.println("goodbye");
                        System.exit(0);
                    }
                    break; // break round loop; outer loop continues as per haveBoard
                }

                if (closed == 0) player = 3 - player; // switch turns only if no box closed
            }
        }
    }
}
