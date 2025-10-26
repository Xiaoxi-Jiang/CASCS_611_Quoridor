package game;

/** Minimal scoreboard to show simple integer scores. 
 * Author: Xiaoxi J
 * Date: 2025-10-01
*/
public final class ScoreBoard {
    private int a = 0, b = 0;
    public void addToA(int d) { a += d; }
    public void addToB(int d) { b += d; }
    public int getA() { return a; }
    public int getB() { return b; }
}
