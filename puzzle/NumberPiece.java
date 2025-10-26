package puzzle;

import game.Piece;
import game.Player;

/**
 * Numeric tile piece used by the sliding puzzle.
 * Value 0 represents the empty slot.
 */
public final class NumberPiece extends Piece {
    private final int value;

    public NumberPiece(int value) {
        super((Player) null);
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public boolean isEmptyPiece() {
        return value == 0;
    }

    @Override
    public String symbol() {
        return value == 0 ? " " : Integer.toString(value);
    }
}
