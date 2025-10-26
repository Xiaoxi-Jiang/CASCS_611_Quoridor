package dots;

/** Helper for parsing/validating text moves. 
 * 
 * Author: Jigar K
 * Date: 2025-10-03
*/
public final class RulesDots {
    /** Parse a token like "H 1 2" or "V 0 3". Returns null if invalid. */
    public static DotsMove parse(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        String[] parts = s.split("\\s+");
        if (parts.length != 3) return null;
        String t = parts[0].toLowerCase();
        boolean horizontal;
        if (t.equals("h")) horizontal = true;
        else if (t.equals("v")) horizontal = false;
        else return null;
        try {
            int r = Integer.parseInt(parts[1]);
            int c = Integer.parseInt(parts[2]);
            return new DotsMove(horizontal, r, c);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
