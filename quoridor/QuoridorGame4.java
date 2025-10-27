package quoridor;

import game.Game;
import game.Player;

import java.util.*;

/**
 * Console Quoridor (4-player variant). Java 8 compatible.
 */
public final class QuoridorGame4 extends Game {
    private static final String RED = "\u001B[31m";
    private static final String BLUE = "\u001B[34m";
    private static final String GREEN = "\u001B[32m";
    private static final String PINK = "\u001B[35m"; // magenta as pink
    private static final String RESET = "\u001B[0m";
    private static final String INFO = "\u001B[36m";
    private static final String WARN = "\u001B[33m";

    private enum ActionMode { NONE, MOVE, PLACE }

    private static final class PendingMove {
        private Position target;
        public void set(Position target) { this.target = target; }
        public Position target() { return target; }
        public void clear() { target = null; }
        public boolean ready() { return target != null; }
    }

    private static final class PendingWall {
        private WallPlacement placement;
        public void set(WallPlacement placement) { this.placement = placement; }
        public WallPlacement placement() { return placement; }
        public void clear() { placement = null; }
        public boolean ready() { return placement != null; }
    }

    @Override
    public void start() {
        System.out.println("\n=== Quoridor (4 Players) ===");
        System.out.println("Goal: reach your opposite edge. Each has 5 walls.\n");

        // Gather 4 distinct player names
        List<String> names = new ArrayList<String>();
        for (int i = 1; i <= 4; i++) {
            while (true) {
                String name = io.readNonEmpty("Player " + i + " name: ");
                boolean dup = false;
                for (int j = 0; j < names.size(); j++) if (names.get(j).equalsIgnoreCase(name)) { dup = true; break; }
                if (!dup) { names.add(name); break; }
                System.out.println(WARN + "Names must be different. Please retry." + RESET);
            }
        }

        Player p1 = new Player(names.get(0));
        Player p2 = new Player(names.get(1));
        Player p3 = new Player(names.get(2));
        Player p4 = new Player(names.get(3));
        List<Player> players = Arrays.asList(p1, p2, p3, p4);
        registerPlayers(players);

        Map<Player, String> pawnColors = new HashMap<Player, String>();
        pawnColors.put(p1, RED);
        pawnColors.put(p2, BLUE);
        pawnColors.put(p3, GREEN);
        pawnColors.put(p4, PINK);

        QuoridorBoard4 board = new QuoridorBoard4(players, pawnColors);
        registerBoard(board);

        List<Player> turnOrder = getPlayers();
        int currentIdx = 0;
        while (true) {
            Player winner = board.checkWinner();
            if (winner != null) { announceWinner(board, winner, pawnColors); return; }

            Player current = turnOrder.get(currentIdx);
            boolean finishedTurn = handleTurn(board, current, pawnColors);
            if (!finishedTurn) {
                System.out.println("Returning to game menu.");
                return;
            }

            winner = board.checkWinner();
            if (winner != null) { announceWinner(board, winner, pawnColors); return; }

            currentIdx = (currentIdx + 1) % turnOrder.size();
        }
    }

    private boolean handleTurn(QuoridorBoard4 board, Player current, Map<Player, String> pawnColors) {
        ActionMode mode = ActionMode.NONE;
        PendingMove pendingMove = new PendingMove();
        PendingWall pendingWall = new PendingWall();
        List<Position> moveOptions = Collections.emptyList();

        while (true) {
            System.out.println(board.render(pendingWall.placement(), pendingMove.target()));
            System.out.println(statusLine(board, current, pawnColors.get(current)));
            if (mode == ActionMode.MOVE) {
                System.out.println(INFO + "Reachable squares (row col, 1-9): " + formatPositions(moveOptions) + RESET);
                if (!pendingMove.ready()) {
                    System.out.println("Enter coordinates (row col) to choose a target, or type 'change' to pick another action.");
                } else {
                    System.out.println("Type 'enter' to confirm, or re-enter coordinates to modify the choice.");
                }
            } else if (mode == ActionMode.PLACE) {
                System.out.println("Use WASD to shift, 'turn' to rotate, 'enter' to confirm, 'change' to go back.");
                if (pendingWall.ready()) {
                    System.out.println("Current wall preview: " + pendingWall.placement());
                }
            } else {
                System.out.println("Type 'move' to relocate or 'place' to drop a wall (remaining " + board.getWallsRemaining(current) + ").");
            }

            String raw = io.readNonEmpty("Command: ").trim();
            String cmd = raw.toLowerCase(Locale.ROOT);

            if (cmd.equals("quit") || cmd.equals("q")) return false;

            if (cmd.equals("change")) { mode = ActionMode.NONE; pendingMove.clear(); pendingWall.clear(); continue; }

            if (cmd.equals("enter")) {
                if (mode == ActionMode.MOVE && pendingMove.ready()) {
                    if (board.applyMove(current, pendingMove.target())) {
                        System.out.println("Moved to " + humanReadable(pendingMove.target()));
                        return true;
                    }
                    System.out.println(WARN + "Move failed: illegal destination." + RESET);
                } else if (mode == ActionMode.PLACE && pendingWall.ready()) {
                    if (board.applyWall(current, pendingWall.placement())) {
                        System.out.println("Wall placed: " + pendingWall.placement());
                        return true;
                    } else {
                        System.out.println(WARN + "Cannot place wall: conflict or blocked paths." + RESET);
                    }
                } else {
                    System.out.println(WARN + "Nothing to confirm yet." + RESET);
                }
                continue;
            }

            if (mode == ActionMode.NONE) {
                if (cmd.equals("move")) {
                    moveOptions = board.legalMoves(current);
                    if (moveOptions.isEmpty()) { System.out.println(WARN + "No legal moves available." + RESET); continue; }
                    mode = ActionMode.MOVE; pendingMove.clear();
                } else if (cmd.equals("place")) {
                    if (board.getWallsRemaining(current) <= 0) { System.out.println(WARN + "No walls remaining; cannot place." + RESET); continue; }
                    mode = ActionMode.PLACE; pendingWall.set(defaultWallPlacement());
                    System.out.println("Wall preview placed at default location. Adjust with WASD/turn.");
                } else {
                    System.out.println(WARN + "Unknown command. Type 'move' or 'place'." + RESET);
                }
                continue;
            }

            if (mode == ActionMode.MOVE) {
                Position pos = parsePosition(raw);
                if (pos == null) { System.out.println(WARN + "Please enter coordinates like 'row col' (1-9)." + RESET); continue; }
                if (!moveOptions.contains(pos)) { System.out.println(WARN + "Destination not reachable. Try again." + RESET); continue; }
                pendingMove.set(pos); continue;
            }

            if (cmd.equals("turn") && mode == ActionMode.PLACE) {
                if (!pendingWall.ready()) { System.out.println(WARN + "No wall preview to rotate." + RESET); continue; }
                pendingWall.set(pendingWall.placement().rotate(QuoridorBoard4.WALL_RANGE));
                System.out.println("Rotated wall: " + pendingWall.placement());
                continue;
            }
            if (mode == ActionMode.PLACE && (cmd.equals("w") || cmd.equals("a") || cmd.equals("s") || cmd.equals("d"))) {
                if (!pendingWall.ready()) { System.out.println(WARN + "No wall preview to move." + RESET); continue; }
                int dr = 0, dc = 0;
                switch (cmd) {
                    case "w": dr = -1; break;
                    case "s": dr = 1; break;
                    case "a": dc = -1; break;
                    case "d": dc = 1; break;
                    default: break;
                }
                pendingWall.set(pendingWall.placement().shift(dr, dc, QuoridorBoard4.WALL_RANGE));
                System.out.println("Moved wall: " + pendingWall.placement());
                continue;
            }

            System.out.println(WARN + "Unknown command. Valid inputs: wasd/turn/enter/change." + RESET);
        }
    }

    private String statusLine(QuoridorBoard4 board, Player current, String colorToken) {
        return colorToken + current.getName() + RESET +
                " turn | walls left " + board.getWallsRemaining(current);
    }

    private void announceWinner(QuoridorBoard4 board, Player winner, Map<Player, String> colors) {
        System.out.println(board.render(null, null));
        System.out.println(colors.get(winner) + "Congrats " + winner.getName() + "! You win." + RESET);
    }

    private String formatPositions(List<Position> positions) {
        if (positions.isEmpty()) return "none";
        StringBuilder sb = new StringBuilder();
        for (Position p : positions) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("(").append(p.row() + 1).append(",").append(p.col() + 1).append(")");
        }
        return sb.toString();
    }

    private Position parsePosition(String text) {
        String normalized = text.replace(",", " ").trim();
        if (normalized.isEmpty()) return null;
        String[] tokens = normalized.split("\\s+");
        if (tokens.length != 2) return null;
        try {
            int r = Integer.parseInt(tokens[0]);
            int c = Integer.parseInt(tokens[1]);
            if (r < 1 || r > QuoridorBoard4.SIZE || c < 1 || c > QuoridorBoard4.SIZE) return null;
            return new Position(r - 1, c - 1);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String humanReadable(Position pos) { return "(" + (pos.row() + 1) + "," + (pos.col() + 1) + ")"; }

    private WallPlacement defaultWallPlacement() {
        int mid = QuoridorBoard4.WALL_RANGE / 2;
        return new WallPlacement(mid, mid, WallOrientation.HORIZONTAL, QuoridorBoard4.WALL_RANGE);
        }
}

