package dots;

import game.Move;

/** Move type for Dots & Boxes: H/V r c 
 * 
 * Author: Jigar K
 * Date: 2025-10-03
*/
public final class DotsMove implements Move {
    public final boolean horizontal;
    public final int r, c;
    public DotsMove(boolean horizontal, int r, int c) {
        this.horizontal = horizontal; this.r = r; this.c = c;
    }
}
