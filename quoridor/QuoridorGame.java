package quoridor;

import game.Game;
import game.Player;

import java.util.*;

/**
 * Console Quoridor implementation with move/place previews and ANSI-colored players.
 */
public final class QuoridorGame extends Game {
    private static final String RED = "\u001B[31m";
    private static final String BLUE = "\u001B[34m";
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
        System.out.println("\n=== Quoridor ===");
        System.out.println("Goal: be the first to reach the opposite edge.");
        System.out.println("Commands: type 'move' to relocate your pawn, or 'place' to position a wall.");
        System.out.println("Moves require selecting a destination (row col) followed by 'enter'. For walls, use WASD to shift, 'turn' to rotate, 'enter' to confirm, 'change' to re-choose.\n");

        String redName = io.readNonEmpty("Red player name: ");
        String blueName;
        while (true) {
            blueName = io.readNonEmpty("Blue player name: ");
            if (!blueName.equalsIgnoreCase(redName)) break;
            System.out.println(WARN + "Names must be different. Please retry." + RESET);
        }

        Player red = new Player(redName);
        Player blue = new Player(blueName);
        registerPlayers(Arrays.asList(red, blue));

        Map<Player, String> pawnColors = Map.of(
                red, RED,
                blue, BLUE
        );
        QuoridorBoard board = new QuoridorBoard(red, blue, pawnColors);
        registerBoard(board);

        List<Player> turnOrder = getPlayers();
        int currentIdx = 0;
        while (true) {
            Player current = turnOrder.get(currentIdx);
            Player opponent = board.opponent(current);
            if (board.hasPlayerWon(opponent)) {
                announceWinner(board, opponent, pawnColors);
                return;
            }
            boolean finishedTurn = handleTurn(board, current, pawnColors);
            if (!finishedTurn) {
                System.out.println("Returning to game menu.");
                return;
            }
            if (board.hasPlayerWon(current)) {
                announceWinner(board, current, pawnColors);
                return;
            }
            currentIdx = 1 - currentIdx;
        }
    }

    private boolean handleTurn(QuoridorBoard board, Player current, Map<Player, String> pawnColors) {
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

            if (cmd.equals("quit") || cmd.equals("q")) {
                return false;
            }

            if (cmd.equals("change")) {
                mode = ActionMode.NONE;
                pendingMove.clear();
                pendingWall.clear();
                continue;
            }

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
                    if (moveOptions.isEmpty()) {
                        System.out.println(WARN + "No legal moves available." + RESET);
                        continue;
                    }
                    mode = ActionMode.MOVE;
                    pendingMove.clear();
                } else if (cmd.equals("place")) {
                    if (board.getWallsRemaining(current) <= 0) {
                    System.out.println(WARN + "No walls remaining; cannot place." + RESET);
                        continue;
                    }
                    mode = ActionMode.PLACE;
                    pendingWall.set(defaultWallPlacement());
                    System.out.println("Wall preview placed at default location. Adjust with WASD/turn.");
                } else {
                    System.out.println(WARN + "Unknown command. Type 'move' or 'place'." + RESET);
                }
                continue;
            }

            if (mode == ActionMode.MOVE) {
                Position pos = parsePosition(raw);
                if (pos == null) {
                    System.out.println(WARN + "Please enter coordinates like 'row col' (1-9)." + RESET);
                    continue;
                }
                if (!moveOptions.contains(pos)) {
                    System.out.println(WARN + "Destination not reachable. Try again." + RESET);
                    continue;
                }
                pendingMove.set(pos);
                continue;
            }

            // PLACE mode adjustments
            if (cmd.equals("turn") && mode == ActionMode.PLACE) {
                if (!pendingWall.ready()) {
                    System.out.println(WARN + "No wall preview to rotate." + RESET);
                    continue;
                }
                pendingWall.set(pendingWall.placement().rotate(QuoridorBoard.WALL_RANGE));
                System.out.println("Rotated wall: " + pendingWall.placement());
                continue;
            }
            if (mode == ActionMode.PLACE && (cmd.equals("w") || cmd.equals("a") || cmd.equals("s") || cmd.equals("d"))) {
                if (!pendingWall.ready()) {
                    System.out.println(WARN + "No wall preview to move." + RESET);
                    continue;
                }
                int dr = 0, dc = 0;
                switch (cmd) {
                    case "w" -> dr = -1;
                    case "s" -> dr = 1;
                    case "a" -> dc = -1;
                    case "d" -> dc = 1;
                }
                pendingWall.set(pendingWall.placement().shift(dr, dc, QuoridorBoard.WALL_RANGE));
                System.out.println("Moved wall: " + pendingWall.placement());
                continue;
            }

            System.out.println(WARN + "Unknown command. Valid inputs: wasd/turn/enter/change." + RESET);
        }
    }

    private String statusLine(QuoridorBoard board, Player current, String colorToken) {
        Player opponent = board.opponent(current);
        return colorToken + current.getName() + RESET +
                " turn | walls left " + board.getWallsRemaining(current) +
                " | opponent walls " + board.getWallsRemaining(opponent);
    }

    private void announceWinner(QuoridorBoard board, Player winner, Map<Player, String> colors) {
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
            if (r < 1 || r > QuoridorBoard.SIZE || c < 1 || c > QuoridorBoard.SIZE) return null;
            return new Position(r - 1, c - 1);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String humanReadable(Position pos) {
        return "(" + (pos.row() + 1) + "," + (pos.col() + 1) + ")";
    }

    private WallPlacement defaultWallPlacement() {
        int mid = QuoridorBoard.WALL_RANGE / 2;
        return new WallPlacement(mid, mid, WallOrientation.HORIZONTAL, QuoridorBoard.WALL_RANGE);
    }
}
