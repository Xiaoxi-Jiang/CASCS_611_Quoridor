package quoridor;

import game.Piece;
import game.Player;

/**
 * Pawn piece shown on the Quoridor board with colored symbol.
 */
public final class PawnPiece extends Piece {
    private final String symbol;

    public PawnPiece(Player owner, String symbol) {
        super(owner);
        this.symbol = symbol;
    }

    @Override
    public String symbol() {
        return symbol;
    }
}
