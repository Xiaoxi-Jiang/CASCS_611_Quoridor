package dots;

import game.Board;
import game.Player;
import game.Tile;

/**
 * Board for Dots & Boxes with shared Tile/Piece infrastructure.
 */
public final class DotsBoard extends Board {
    private final Tile[][] horizontalEdges; // (rows+1) x cols
    private final Tile[][] verticalEdges;   // rows x (cols+1)
    private final Tile[][] boxes;           // rows x cols

    public DotsBoard(int rows, int cols) {
        super(rows, cols);
        horizontalEdges = new Tile[rows + 1][cols];
        verticalEdges = new Tile[rows][cols + 1];
        boxes = new Tile[rows][cols];
        reset();
    }

    @Override
    public void reset() {
        for (int r = 0; r < rows + 1; r++) {
            for (int c = 0; c < cols; c++) {
                horizontalEdges[r][c] = new Tile();
            }
        }
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols + 1; c++) {
                verticalEdges[r][c] = new Tile();
            }
        }
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                boxes[r][c] = new Tile();
            }
        }
    }

    /**
     * Apply a move; return number of boxes closed by this move (0, 1, or 2). Negative if illegal.
     */
    public int apply(DotsMove move, Player player, int playerIndex) {
        if (move.horizontal) {
            if (move.r < 0 || move.r > rows || move.c < 0 || move.c >= cols) return -1;
            Tile edge = horizontalEdges[move.r][move.c];
            if (!edge.isEmpty()) return -1;
            edge.setPiece(new LinePiece(player, LinePiece.Orientation.HORIZONTAL, playerIndex == 2));
            int closed = 0;
            if (tryClaimBox(move.r - 1, move.c, player, playerIndex)) closed++;
            if (tryClaimBox(move.r, move.c, player, playerIndex)) closed++;
            return closed;
        } else {
            if (move.r < 0 || move.r >= rows || move.c < 0 || move.c > cols) return -1;
            Tile edge = verticalEdges[move.r][move.c];
            if (!edge.isEmpty()) return -1;
            edge.setPiece(new LinePiece(player, LinePiece.Orientation.VERTICAL, playerIndex == 2));
            int closed = 0;
            if (tryClaimBox(move.r, move.c - 1, player, playerIndex)) closed++;
            if (tryClaimBox(move.r, move.c, player, playerIndex)) closed++;
            return closed;
        }
    }

    private boolean tryClaimBox(int boxR, int boxC, Player player, int playerIndex) {
        if (boxR < 0 || boxR >= rows || boxC < 0 || boxC >= cols) return false;
        Tile box = boxes[boxR][boxC];
        if (!box.isEmpty()) return false;
        if (hasHorizontalEdge(boxR, boxC) &&
            hasHorizontalEdge(boxR + 1, boxC) &&
            hasVerticalEdge(boxR, boxC) &&
            hasVerticalEdge(boxR, boxC + 1)) {
            box.setPiece(new BoxClaimPiece(player, playerIndex));
            return true;
        }
        return false;
    }

    private boolean hasHorizontalEdge(int r, int c) {
        return r >= 0 && r <= rows && c >= 0 && c < cols && !horizontalEdges[r][c].isEmpty();
    }

    private boolean hasVerticalEdge(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c <= cols && !verticalEdges[r][c].isEmpty();
    }

    public boolean isFull() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (boxes[r][c].isEmpty()) return false;
            }
        }
        return true;
    }

    @Override
    public String render() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < rows * 2 + 1; r++) {
            if (r % 2 == 0) {
                int rr = r / 2;
                for (int c = 0; c < cols; c++) {
                    sb.append("•");
                    sb.append(horizontalEdges[rr][c].isEmpty() ? "   " : horizontalEdges[rr][c].getPiece().symbol());
                }
                sb.append("•\n");
            } else {
                int br = r / 2;
                for (int c = 0; c < cols + 1; c++) {
                    sb.append(verticalEdges[br][c].isEmpty() ? " " : verticalEdges[br][c].getPiece().symbol());
                    if (c < cols) {
                        sb.append(boxes[br][c].isEmpty() ? "   " : boxes[br][c].getPiece().symbol());
                    }
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
