package game;

/**
 * Simple immutable player model with a display name.
 
 * Author: Xiaoxi J
 * Date: 2025-10-01
 */
public final class Player {
    private final String name;
    public Player(String name) { this.name = name == null ? "Player" : name; }
    public String getName() { return name; }
}
