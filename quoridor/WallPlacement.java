package quoridor;

/**
 * Represents a wall placement using a top-left anchor (row, col) and orientation.
 * Rows/cols refer to the upper-left intersection of the wall's 2x1 rectangle.
 */
public final class WallPlacement {
    private final int row;
    private final int col;
    private final WallOrientation orientation;

    public WallPlacement(int row, int col, WallOrientation orientation, int limit) {
        if (row < 0 || row >= limit) throw new IllegalArgumentException("row must be in [0," + (limit - 1) + "]");
        if (col < 0 || col >= limit) throw new IllegalArgumentException("col must be in [0," + (limit - 1) + "]");
        this.row = row;
        this.col = col;
        this.orientation = orientation;
    }

    public int row() { return row; }
    public int col() { return col; }
    public WallOrientation orientation() { return orientation; }

    public static WallPlacement fromEndpoints(Position start, Position end, int limit) {
        int rowDiff = Math.abs(start.row() - end.row());
        int colDiff = Math.abs(start.col() - end.col());
        if (rowDiff == 0 && colDiff == 2) {
            int row = Math.min(start.row(), end.row());
            int col = Math.min(start.col(), end.col());
            if (row >= limit) return null;
            return new WallPlacement(row, col, WallOrientation.HORIZONTAL, limit);
        }
        if (colDiff == 0 && rowDiff == 2) {
            int row = Math.min(start.row(), end.row());
            int col = Math.min(start.col(), end.col());
            if (col >= limit) return null;
            return new WallPlacement(row, col, WallOrientation.VERTICAL, limit);
        }
        return null;
    }

    public WallPlacement shift(int dr, int dc, int limit) {
        int newRow = row + dr;
        int newCol = col + dc;
        if (newRow < 0 || newRow >= limit) newRow = row;
        if (newCol < 0 || newCol >= limit) newCol = col;
        return new WallPlacement(newRow, newCol, orientation, limit);
    }

    public WallPlacement rotate(int limit) {
        WallOrientation next = orientation == WallOrientation.HORIZONTAL
                ? WallOrientation.VERTICAL
                : WallOrientation.HORIZONTAL;
        int newRow = Math.min(row, limit - 1);
        int newCol = Math.min(col, limit - 1);
        return new WallPlacement(newRow, newCol, next, limit);
    }

    @Override
    public String toString() {
        return "Wall{" + orientation + " @ (" + row + "," + col + ")}";
    }
}
