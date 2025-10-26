package game;

/**
 * Basic text renderer that prints to stdout.
 *
 * Author: Xiaoxi J
 * Date: 2025-10-02
 */

public final class TextRenderer implements Renderer {
    @Override public void show(String text) { System.out.print(text); }
}
