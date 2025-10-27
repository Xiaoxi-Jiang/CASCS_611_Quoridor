package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for console games that provides shared IO utilities and
 * keeps track of the active board/players for reuse across titles.
 *
 * Author: Xiaoxi J
 * Date: 2025-09-30
 */

public abstract class Game {
    protected final InputValidator io;
    protected final Renderer renderer;
    private Board board;
    private List<Player> players = Collections.emptyList();

    protected Game() {
        this.io = new InputValidator(System.in, System.out);
        this.renderer = new TextRenderer();
    }

    protected void registerBoard(Board board) {
        this.board = board;
    }

    protected void registerPlayers(List<Player> players) {
        this.players = new ArrayList<>(players);
    }

    public Board getBoard() {
        return board;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    /** Convenience output helpers wired to the Renderer. */
    protected void print(String text) { renderer.show(text); }
    protected void println(String text) { renderer.show(text + "\n"); }

    /** Start the game loop. */
    public abstract void start();
}
