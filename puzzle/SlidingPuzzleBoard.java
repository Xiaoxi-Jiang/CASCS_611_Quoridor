package puzzle;

import game.Board;
import game.Tile;
import java.util.*;

/**
 * Concrete board for N-by-M sliding puzzle. Guarantees solvable shuffles.
 * Supports moving by tile number: the chosen tile must be adjacent to the empty.
 * 
 * Author: Jigar K
 * Date: 2025-09-29
 */
public final class SlidingPuzzleBoard extends Board {
    public static final int MAX_CELLS = 10_000;
    private Tile[][] spaces;
    private int emptyR, emptyC;

    public SlidingPuzzleBoard(int rows, int cols) {
        super(rows, cols);
        if ((long)rows * (long)cols > MAX_CELLS) {
            throw new IllegalArgumentException("rows*cols must be <= " + MAX_CELLS);
        }
        reset();
    }

    /** Fill in goal state, then perform solvable shuffle. */
    @Override public void reset() {
        spaces = new Tile[rows][cols];
        int k = 1;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int val = (r == rows - 1 && c == cols - 1) ? 0 : k++;
                spaces[r][c] = new Tile(new NumberPiece(val));
            }
        }
        emptyR = rows - 1; emptyC = cols - 1;
        shuffleSolvable(rows * cols * 10); // random walk to keep solvable
    }

    private void shuffleSolvable(int steps) {
        Random rnd = new Random();
        // Perform a random walk moving the empty tile; this preserves solvability.
        for (int i = 0; i < steps; i++) {
            List<int[]> moves = legalMovesFrom(emptyR, emptyC);
            int[] pick = moves.get(rnd.nextInt(moves.size()));
            swap(emptyR, emptyC, pick[0], pick[1]);
            emptyR = pick[0]; emptyC = pick[1];
        }
    }

    private List<int[]> legalMovesFrom(int r, int c) {
        List<int[]> list = new ArrayList<>();
        if (r > 0) list.add(new int[]{r-1, c});
        if (r < rows-1) list.add(new int[]{r+1, c});
        if (c > 0) list.add(new int[]{r, c-1});
        if (c < cols-1) list.add(new int[]{r, c+1});
        return list;
    }

    private void swap(int r1, int c1, int r2, int c2) {
        NumberPiece p1 = (NumberPiece) spaces[r1][c1].getPiece();
        NumberPiece p2 = (NumberPiece) spaces[r2][c2].getPiece();
        spaces[r1][c1].setPiece(p2);
        spaces[r2][c2].setPiece(p1);
    }

    /** Move by direction (legacy support). Returns true if a swap occurred. */
    public boolean move(String dir) {
        dir = dir.toLowerCase();
        int nr = emptyR, nc = emptyC;
        switch (dir) {
            case "w": nr = emptyR + 1; nc = emptyC; break; // moving empty up pulls tile down
            case "s": nr = emptyR - 1; nc = emptyC; break;
            case "a": nr = emptyR; nc = emptyC + 1; break;
            case "d": nr = emptyR; nc = emptyC - 1; break;
            default: return false;
        }
        if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) return false;
        swap(emptyR, emptyC, nr, nc);
        emptyR = nr; emptyC = nc;
        return true;
    }

    /**
     * Move by tile number. The tile must be adjacent (Manhattan distance 1) to the empty.
     * @param value tile number to slide into the empty space (1..rows*cols-1)
     * @return true if the move happened; false if not adjacent / out of range.
     */
    public boolean moveNumber(int value) {
        if (value <= 0 || value >= rows * cols) return false;
        // Find the tile
        int tr = -1, tc = -1;
        outer:
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                NumberPiece piece = (NumberPiece) spaces[r][c].getPiece();
                if (piece.getValue() == value) { tr = r; tc = c; break outer; }
            }
        }
        if (tr == -1) return false;
        int manhattan = Math.abs(tr - emptyR) + Math.abs(tc - emptyC);
        if (manhattan != 1) return false; // not adjacent
        swap(tr, tc, emptyR, emptyC);
        emptyR = tr; emptyC = tc;
        return true;
    }

    public boolean isSolved() {
        int k = 1;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int expect = (r == rows - 1 && c == cols - 1) ? 0 : k++;
                NumberPiece piece = (NumberPiece) spaces[r][c].getPiece();
                if (piece.getValue() != expect) return false;
            }
        }
        return true;
    }

    @Override public String render() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r <= rows; r++) {
            sb.append("+");
            for (int c = 0; c < cols; c++) sb.append("----+");
            sb.append("\n");
            if (r == rows) break;
            sb.append("|");
            for (int c = 0; c < cols; c++) {
                sb.append(String.format("%-4s|", spaces[r][c].render()));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
