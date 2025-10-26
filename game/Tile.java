package game;

/**
 * A board location that can optionally hold a Piece.
 * Tiles are shared across games to keep board infrastructure consistent.
 */
public class Tile {
    private Piece piece;

    public Tile() { }

    public Tile(Piece piece) {
        this.piece = piece;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public void clear() {
        this.piece = null;
    }

    public boolean isEmpty() {
        return piece == null;
    }

    public String render() {
        return isEmpty() ? " " : piece.symbol();
    }
}
