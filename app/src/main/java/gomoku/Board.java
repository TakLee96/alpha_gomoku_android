package gomoku;

/** Board representation of game
 * @author TakLee96 */
public class Board {
    private boolean[] board = new boolean[2 * State.N * State.N];
    public boolean isEmpty(Action a) { return isEmpty(a.x(), a.y()); }
    public boolean isEmpty(int x, int y) {
        int base = 2 * x * State.N + 2 * y;
        return !board[base];
    }
    public boolean isBlack(Action a) { return isBlack(a.x(), a.y()); }
    public boolean isBlack(int x, int y) {
        int base = 2 * (x * State.N + y);
        return board[base] && board[base+1];
    }
    public boolean isWhite(Action a) { return isWhite(a.x(), a.y()); }
    public boolean isWhite(int x, int y) {
        int base = 2 * (x * State.N + y);
        return board[base] && !board[base+1];
    }
    public boolean is(boolean who, Action a) { return is(who, a.x(), a.y()); }
    public boolean is(boolean who, int x, int y) {
        int base = 2 * (x * State.N + y);
        return board[base] && board[base+1] == who;
    }
    public void put(boolean who, Action a) { put(who, a.x(), a.y()); }
    public void put(boolean who, int x, int y) {
        int base = 2 * (x * State.N + y);
        board[base]   = true;
        board[base+1] = who;
    }
    public void clean(Action a) { clean(a.x(), a.y()); }
    public void clean(int x, int y) {
        int base = 2 * (x * State.N + y);
        board[base] = false;
    }
}
