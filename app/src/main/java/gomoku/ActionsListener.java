package gomoku;

import java.util.Set;

/** For UI Update purposes
 * @author TakLee96 */
public interface ActionsListener {

    void digest(Set<Action> actions);

}
