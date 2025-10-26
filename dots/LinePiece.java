package dots;

import game.Piece;
import game.Player;

/** Piece representing a drawn line along an edge. */
public final class LinePiece extends Piece {
    public enum Orientation { HORIZONTAL, VERTICAL }

    private final Orientation orientation;
    private final boolean strongGlyph;

    public LinePiece(Player owner, Orientation orientation, boolean strongGlyph) {
        super(owner);
        this.orientation = orientation;
        this.strongGlyph = strongGlyph;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    @Override
    public String symbol() {
        switch (orientation) {
            case HORIZONTAL: return strongGlyph ? "═══" : "───";
            case VERTICAL: return strongGlyph ? "║" : "│";
            default: return " ";
        }
    }
}
