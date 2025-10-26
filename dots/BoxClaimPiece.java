package dots;

import game.Piece;
import game.Player;

/** Piece representing a completed box claimed by a player. */
public final class BoxClaimPiece extends Piece {
    private final String label;

    public BoxClaimPiece(Player owner, int playerIndex) {
        super(owner);
        this.label = " " + playerIndex + " ";
    }

    @Override
    public String symbol() {
        return label;
    }
}
