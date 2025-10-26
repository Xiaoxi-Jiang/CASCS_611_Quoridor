package game;

/**
 * Abstract board surface for console games (rows x cols).
 * Encapsulates dimensions; concrete boards implement reset() and render().
 *
 * Author: Xiaoxi J
 * Date: 2025-09-30
 */

public abstract class Board {
    protected final int rows;
    protected final int cols;

    protected Board(int rows, int cols) {
        if (rows <= 0 || cols <= 0) throw new IllegalArgumentException("rows/cols must be > 0");
        this.rows = rows; this.cols = cols;
    }

    public final int getRows() { return rows; }
    public final int getCols() { return cols; }

    /** Reset the board to a fresh game state. */
    public abstract void reset();

    /** Render a textual representation of the board. */
    public abstract String render();
}
