package game;

/**
 * Base class for any game element that can occupy a Tile.
 * Pieces may optionally be associated with a Player (owner).
 */
public abstract class Piece {
    private final Player owner;

    protected Piece(Player owner) {
        this.owner = owner;
    }

    public Player getOwner() {
        return owner;
    }

    public boolean isOwnedBy(Player player) {
        return owner != null && owner == player;
    }

    /** Textual symbol used when rendering the piece on a Tile. */
    public abstract String symbol();
}
