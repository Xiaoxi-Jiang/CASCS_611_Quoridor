package app;

import game.InputValidator;

/**
 * Main launcher offering both games.
 * Author: Jigar K
 * Date: 2025-10-05
 */
public final class Main {
    public static void main(String[] args) {
        InputValidator inputValidator = new InputValidator(System.in, System.out);
        new GameHub(inputValidator).run();
    }
}
