package quoridor;

import game.Board;
import game.Player;
import game.Tile;

import java.util.*;

/**
 * 4-player Quoridor board (Java 8 compatible).
 * Players start at top/bottom/left/right midpoints; goals are opposite edges.
 */
public final class QuoridorBoard4 extends Board {
    public static final int SIZE = 9;
    public static final int WALL_RANGE = SIZE - 1;
    public static final int WALLS_PER_PLAYER = 5;

    private final Tile[][] nodes = new Tile[SIZE][SIZE];
    private final Player[][] horizontalWalls = new Player[WALL_RANGE][WALL_RANGE];
    private final Player[][] verticalWalls = new Player[WALL_RANGE][WALL_RANGE];
    private final boolean[][] northBlocked = new boolean[SIZE][SIZE];
    private final boolean[][] southBlocked = new boolean[SIZE][SIZE];
    private final boolean[][] eastBlocked = new boolean[SIZE][SIZE];
    private final boolean[][] westBlocked = new boolean[SIZE][SIZE];

    private final Map<Player, Position> pawnPositions = new LinkedHashMap<Player, Position>();
    private final Map<Player, Position> startingPositions = new LinkedHashMap<Player, Position>();
    private final Map<Player, Integer> goalRows = new LinkedHashMap<Player, Integer>();
    private final Map<Player, Integer> goalCols = new LinkedHashMap<Player, Integer>();
    private final Map<Player, Integer> wallsRemaining = new LinkedHashMap<Player, Integer>();
    private final Map<Player, PawnPiece> pawnPieces = new HashMap<Player, PawnPiece>();
    private final List<Player> order;
    private final Map<Player, String> colorTokens;

    private static final String RESET = "\u001B[0m";
    private static final String PREVIEW = "\u001B[33m";

    public QuoridorBoard4(List<Player> players, Map<Player, String> colorTokens) {
        super(SIZE, SIZE);
        if (players == null || players.size() != 4) {
            throw new IllegalArgumentException("QuoridorBoard4 requires exactly 4 players");
        }
        this.order = Collections.unmodifiableList(new ArrayList<Player>(players));
        this.colorTokens = new HashMap<Player, String>(colorTokens);
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                nodes[r][c] = new Tile();
            }
        }

        // Assign pawn pieces with colors
        for (Player p : order) {
            String color = this.colorTokens.get(p);
            pawnPieces.put(p, new PawnPiece(p, (color == null ? "" : color) + "●" + RESET));
        }

        // Start positions and goals (order: top, bottom, left, right)
        Player top = order.get(0);
        Player bottom = order.get(1);
        Player left = order.get(2);
        Player right = order.get(3);

        startingPositions.put(top, new Position(0, SIZE / 2));
        startingPositions.put(bottom, new Position(SIZE - 1, SIZE / 2));
        startingPositions.put(left, new Position(SIZE / 2, 0));
        startingPositions.put(right, new Position(SIZE / 2, SIZE - 1));

        goalRows.put(top, SIZE - 1);
        goalRows.put(bottom, 0);
        goalCols.put(left, SIZE - 1);
        goalCols.put(right, 0);

        reset();
    }

    @Override
    public void reset() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                nodes[r][c].clear();
                northBlocked[r][c] = false;
                southBlocked[r][c] = false;
                eastBlocked[r][c] = false;
                westBlocked[r][c] = false;
            }
        }
        for (int r = 0; r < WALL_RANGE; r++) {
            Arrays.fill(horizontalWalls[r], null);
            Arrays.fill(verticalWalls[r], null);
        }
        pawnPositions.clear();
        for (Player p : order) {
            wallsRemaining.put(p, WALLS_PER_PLAYER);
            placePawn(p, startingPositions.get(p));
        }
        // Block outer borders
        for (int c = 0; c < SIZE; c++) {
            northBlocked[0][c] = true;
            southBlocked[SIZE - 1][c] = true;
        }
        for (int r = 0; r < SIZE; r++) {
            westBlocked[r][0] = true;
            eastBlocked[r][SIZE - 1] = true;
        }
    }

    private void placePawn(Player player, Position pos) {
        pawnPositions.put(player, pos);
        nodes[pos.row()][pos.col()].setPiece(pawnPieces.get(player));
    }

    public Position getPawnPosition(Player player) { return pawnPositions.get(player); }

    public int getWallsRemaining(Player player) { return wallsRemaining.getOrDefault(player, 0); }

    public boolean hasPlayerWon(Player player) {
        Position p = pawnPositions.get(player);
        Integer gr = goalRows.get(player);
        if (gr != null) return p.row() == gr.intValue();
        Integer gc = goalCols.get(player);
        return gc != null && p.col() == gc.intValue();
    }

    public Player checkWinner() {
        for (Player p : order) if (hasPlayerWon(p)) return p;
        return null;
    }

    public List<Position> legalMoves(Player player) {
        Position current = pawnPositions.get(player);
        List<Position> moves = new ArrayList<Position>();
        for (Direction dir : Direction.values()) {
            Position adj = current.translate(dir.dr, dir.dc);
            if (!inBounds(adj) || isBlocked(current, dir)) continue;
            if (isOccupied(adj)) {
                Position jump = adj.translate(dir.dr, dir.dc);
                if (inBounds(jump) && !isBlocked(adj, dir) && !isOccupied(jump)) {
                    moves.add(jump);
                } else {
                    Direction[] sides = dir.perpendiculars();
                    for (int i = 0; i < sides.length; i++) {
                        Direction side = sides[i];
                        Position diag = adj.translate(side.dr, side.dc);
                        if (inBounds(diag) && !isBlocked(adj, side) && !isOccupied(diag)) {
                            moves.add(diag);
                        }
                    }
                }
            } else {
                moves.add(adj);
            }
        }
        Collections.sort(moves, new Comparator<Position>() {
            public int compare(Position a, Position b) {
                int cr = Integer.compare(a.row(), b.row());
                return cr != 0 ? cr : Integer.compare(a.col(), b.col());
            }
        });
        return moves;
    }

    private boolean inBounds(Position pos) {
        return pos.row() >= 0 && pos.row() < SIZE && pos.col() >= 0 && pos.col() < SIZE;
    }

    private boolean isOccupied(Position pos) {
        for (Position value : pawnPositions.values()) {
            if (value.equals(pos)) return true;
        }
        return false;
    }

    private boolean isBlocked(Position pos, Direction dir) {
        switch (dir) {
            case NORTH: return northBlocked[pos.row()][pos.col()];
            case SOUTH: return southBlocked[pos.row()][pos.col()];
            case EAST:  return eastBlocked[pos.row()][pos.col()];
            case WEST:  return westBlocked[pos.row()][pos.col()];
            default: return false;
        }
    }

    public boolean applyMove(Player player, Position target) {
        if (!legalMoves(player).contains(target)) return false;
        Position current = pawnPositions.get(player);
        nodes[current.row()][current.col()].clear();
        pawnPositions.put(player, target);
        nodes[target.row()][target.col()].setPiece(pawnPieces.get(player));
        return true;
    }

    public boolean canPlaceWall(Player player, WallPlacement placement) {
        if (placement == null) return false;
        if (getWallsRemaining(player) <= 0) return false;
        return !conflicts(placement);
    }

    private boolean conflicts(WallPlacement placement) {
        int row = placement.row();
        int col = placement.col();
        if (placement.orientation() == WallOrientation.HORIZONTAL) {
            // Same-orientation overlap: any of the two horizontal segments already blocked
            if (southBlocked[row][col] || southBlocked[row][col + 1]
                    || northBlocked[row + 1][col] || northBlocked[row + 1][col + 1]) {
                return true;
            }
            // Crossing at center with an existing vertical wall
            if (verticalWalls[row][col] != null) return true;
        } else { // VERTICAL
            // Same-orientation overlap: any of the two vertical segments already blocked
            if (eastBlocked[row][col] || eastBlocked[row + 1][col]
                    || westBlocked[row][col + 1] || westBlocked[row + 1][col + 1]) {
                return true;
            }
            // Crossing at center with an existing horizontal wall
            if (horizontalWalls[row][col] != null) return true;
        }
        return false;
    }

    public boolean applyWall(Player player, WallPlacement placement) {
        if (!canPlaceWall(player, placement)) return false;
        setWall(placement, player);
        // Verify all players still have a path
        boolean valid = true;
        for (Player p : order) {
            if (!hasPath(p)) { valid = false; break; }
        }
        if (!valid) {
            setWall(placement, null);
            return false;
        }
        wallsRemaining.put(player, getWallsRemaining(player) - 1);
        return true;
    }

    private void setWall(WallPlacement placement, Player owner) {
        int row = placement.row();
        int col = placement.col();
        boolean present = owner != null;
        if (placement.orientation() == WallOrientation.HORIZONTAL) {
            horizontalWalls[row][col] = owner;
            southBlocked[row][col] = present;
            northBlocked[row + 1][col] = present;
            southBlocked[row][col + 1] = present;
            northBlocked[row + 1][col + 1] = present;
        } else {
            verticalWalls[row][col] = owner;
            eastBlocked[row][col] = present;
            westBlocked[row][col + 1] = present;
            eastBlocked[row + 1][col] = present;
            westBlocked[row + 1][col + 1] = present;
        }
    }

    private boolean hasPath(Player player) {
        Position start = pawnPositions.get(player);
        Integer targetRow = goalRows.get(player);
        Integer targetCol = goalCols.get(player);
        boolean[][] visited = new boolean[SIZE][SIZE];
        Deque<Position> queue = new ArrayDeque<Position>();
        queue.add(start);
        visited[start.row()][start.col()] = true;
        while (!queue.isEmpty()) {
            Position pos = queue.removeFirst();
            if ((targetRow != null && pos.row() == targetRow.intValue()) ||
                (targetCol != null && pos.col() == targetCol.intValue())) {
                return true;
            }
            for (Direction dir : Direction.values()) {
                Position next = pos.translate(dir.dr, dir.dc);
                if (!inBounds(next) || visited[next.row()][next.col()] || isBlocked(pos, dir)) continue;
                visited[next.row()][next.col()] = true;
                queue.add(next);
            }
        }
        return false;
    }

    @Override
    public String render() { return render(null, null); }

    public String render(WallPlacement previewWall, Position previewMove) {
        StringBuilder sb = new StringBuilder();
        sb.append(columnHeader());
        for (int r = 0; r < SIZE; r++) {
            sb.append("    ").append(horizontalLine(r, previewWall)).append("\n");
            sb.append(rowLabel(r)).append(verticalRowContent(r, previewWall, previewMove)).append("\n");
        }
        sb.append("    ").append(horizontalLine(SIZE, previewWall));
        return sb.toString();
    }

    private String horizontalLine(int rowLine, WallPlacement previewWall) {
        StringBuilder line = new StringBuilder();
        line.append("+");
        for (int c = 0; c < SIZE; c++) {
            line.append(horizontalDash(rowLine, c, previewWall));
            line.append("+");
        }
        return line.toString();
    }

    private String horizontalDash(int rowLine, int col, WallPlacement previewWall) {
        if (rowLine == 0 || rowLine == SIZE) return "---";
        boolean blocked = southBlocked[rowLine - 1][col];
        if (!blocked && previewWall != null && previewWall.orientation() == WallOrientation.HORIZONTAL) {
            if (previewWall.row() == rowLine - 1 && (col == previewWall.col() || col == previewWall.col() + 1)) {
                return PREVIEW + "===" + RESET;
            }
        }
        if (!blocked) return "---";
        Player owner = horizontalOwner(rowLine - 1, col);
        String color = owner == null ? "" : colorTokens.get(owner);
        return (color == null ? "" : color) + "===" + RESET;
    }

    private String verticalRowContent(int row, WallPlacement previewWall, Position previewMove) {
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < SIZE; c++) {
            sb.append(verticalBar(row, c, previewWall));
            sb.append(cellContent(row, c, previewMove));
        }
        sb.append(verticalBar(row, SIZE, previewWall));
        return sb.toString();
    }

    private String columnHeader() {
        StringBuilder header = new StringBuilder("    ");
        for (int c = 1; c <= SIZE; c++) header.append(String.format(" %2d ", c));
        header.append("\n");
        return header.toString();
    }

    private String rowLabel(int rowIndex) { return String.format("%2d  ", rowIndex + 1); }

    private String verticalBar(int row, int colLine, WallPlacement previewWall) {
        if (colLine == 0 || colLine == SIZE) return "|";
        boolean blocked = eastBlocked[row][colLine - 1];
        if (!blocked && previewWall != null && previewWall.orientation() == WallOrientation.VERTICAL) {
            boolean covers = (previewWall.row() == row || previewWall.row() + 1 == row) && previewWall.col() == colLine - 1;
            if (covers) return PREVIEW + "║" + RESET;
        }
        if (!blocked) return "|";
        Player owner = verticalOwner(row, colLine - 1);
        String color = owner == null ? "" : colorTokens.get(owner);
        return (color == null ? "" : color) + "║" + RESET;
    }

    private Player horizontalOwner(int row, int col) {
        if (row < 0 || row >= WALL_RANGE) return null;
        if (col < WALL_RANGE && horizontalWalls[row][col] != null) return horizontalWalls[row][col];
        if (col - 1 >= 0 && horizontalWalls[row][col - 1] != null) return horizontalWalls[row][col - 1];
        return null;
    }

    private Player verticalOwner(int row, int col) {
        if (col < 0 || col >= WALL_RANGE) return null;
        if (row < WALL_RANGE && verticalWalls[row][col] != null) return verticalWalls[row][col];
        if (row - 1 >= 0 && verticalWalls[row - 1][col] != null) return verticalWalls[row - 1][col];
        return null;
    }

    private String cellContent(int row, int col, Position previewMove) {
        if (previewMove != null && previewMove.row() == row && previewMove.col() == col) {
            return " " + PREVIEW + "*" + RESET + " ";
        }
        String raw = nodes[row][col].render();
        if (raw.trim().isEmpty()) return "   ";
        if (raw.length() == 1) return " " + raw + " ";
        return " " + raw + " ";
    }

    private enum Direction {
        NORTH(-1, 0), SOUTH(1, 0), EAST(0, 1), WEST(0, -1);
        final int dr; final int dc;
        Direction(int dr, int dc) { this.dr = dr; this.dc = dc; }
        Direction[] perpendiculars() {
            switch (this) {
                case NORTH:
                case SOUTH:
                    return new Direction[]{WEST, EAST};
                case EAST:
                case WEST:
                    return new Direction[]{NORTH, SOUTH};
                default:
                    throw new IllegalStateException("Unexpected direction: " + this);
            }
        }
    }
}

