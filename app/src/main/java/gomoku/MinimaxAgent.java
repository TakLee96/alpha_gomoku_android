package gomoku;

import java.util.Comparator;
import java.util.HashSet;
// import java.util.HashMap;
import java.util.Arrays;
// import java.util.AbstractMap;
import java.util.Set;

/** Advanced MinimaxAgent
 * @author TakLee96 */
public class MinimaxAgent extends Agent {
    /***********************
     *** CLASS CONSTANTS ***
     ***********************/
    // maximum evaluation score
    protected static final double infinity = 1E10;
    // discount rate
    protected static final double gamma = 0.99;
    // maximum search depth
    protected static final int maxDepth = 12;
    // maximum big search depth
    protected static final int maxBigDepth = 5;
    // separate big depth and normal depth
    protected static final int bigDepthThreshold = 5;
    // branching factor
    protected static final int branch = 12;

    /**********************
     *** HELPER CLASSES ***
     **********************/
    protected static class Node {
        public Action a;
        public double v;
        public Node(Action a, double v) {
            this.a = a;
            this.v = v;
        }
        @Override
        public boolean equals(Object other) {
            if (other == null) return false;
            Node o = (Node) other;
            return a.equals(o.a);
        }
        @Override
        public int hashCode() {
            return a.hashCode();
        }
        @Override
        public String toString() {
            return "Node(" + a.toString() + ", " + v + ")";
        }
    }

    protected static Comparator<Node> blackComparator = new Comparator<Node>(){
        @Override public int compare(Node a, Node b) { return (int) (b.v - a.v); }
    };
    protected static Comparator<Node> whiteComparator = new Comparator<Node>(){
        @Override public int compare(Node a, Node b) { return (int) (a.v - b.v); }
    };

    /***************************
     *** INSTANCE ATTRIBUTES ***
     ***************************/
    // a cache for storing responses and evaluation scores
    // protected AbstractMap<Set<Move>, Node> memo;

    /*******************
     *** CONSTRUCTOR ***
     *******************/
    public MinimaxAgent(boolean isBlack) {
        super(isBlack);
        // memo = new HashMap<Set<Move>, Node>();
    }

    /********************
     *** CORE UTILITY ***
     ********************/
    private double evaluate(State s) {
        if (s.isBlacksTurn())
            return Weights.blackEval.mul(s.extractFeatures());
        return Weights.whiteEval.mul(s.extractFeatures());
    }

    // ANALYSIS
    // static int numInstantEval  = 0;
    // static int numDepthEval    = 0;
    // static int totalEvalDepth  = 0;
    // static int numCacheHit     = 0;
    // static int numCacheMiss    = 0;
    // static int totalBranchSize = 0;
    // static int numRecursion    = 0;
    // static void initAnalysis() {
    //     numInstantEval  = 0;
    //     numDepthEval    = 0;
    //     totalEvalDepth  = 0;
    //     numCacheHit     = 0;
    //     numCacheMiss    = 0;
    //     totalBranchSize = 0;
    //     numRecursion    = 0;       
    // }

    protected Node maxvalue(State s, double alpha, double beta, int depth, int bigDepth, Set<Action> actions) {
        Action maxaction = null; double maxvalue = -infinity, val = 0;
        Rewinder rewinder = null;
        for (Action a : actions) {
            if (depth == 1)
                s.evaluate(a);
            rewinder = s.move(a);
            val = gamma * value(s, alpha, beta, depth, bigDepth).v;
            s.rewind(rewinder);
            if (val > maxvalue) {
                maxvalue = val;
                maxaction = a;
            }
            if (val > beta) {
                return new Node(maxaction, maxvalue);
            }
            alpha = Math.max(alpha, val);
        }
        if (maxaction == null) throw new RuntimeException("everybody is too small");
        return new Node(maxaction, maxvalue);
    }
    protected Node minvalue(State s, double alpha, double beta, int depth, int bigDepth, Set<Action> actions) {
        Action minaction = null; double minvalue = infinity, val = 0;
        Rewinder rewinder = null;
        for (Action a : actions) {
            if (depth == 1) s.evaluate(a);
            rewinder = s.move(a);
            val = gamma * value(s, alpha, beta, depth, bigDepth).v;
            s.rewind(rewinder);
            if (val < minvalue) {
                minvalue = val;
                minaction = a;
            }
            if (val < alpha) {
                return new Node(minaction, minvalue);
            }
            beta = Math.min(beta, val);
        }
        if (minaction == null) throw new RuntimeException("everybody is too large");
        return new Node(minaction, minvalue);
    }
    protected Node value(State s, double alpha, double beta, int depth, int bigDepth) {
        if (s.ended())
            // ANALYSIS: instant eval
            // numInstantEval += 1;
        if (s.win(true))
            return new Node(null, infinity);
        if (s.win(false))
            return new Node(null, -infinity);
        if (s.ended())
            return new Node(null, 0.0);
        if (depth == maxDepth || bigDepth == maxBigDepth) {
            // ANALYSIS: max depth eval
            // numDepthEval += 1;
            // totalEvalDepth += depth;
            return new Node(null, evaluate(s));
        }

        // ANALYSIS: num recursion
        // numRecursion += 1;

        // Set<Move> prev = s.previousMoves(depth);
        // Node memoized = memo.get(prev);
        // if (memoized != null) {
            // ANALYSIS: cache hit
            // numCacheHit += 1;
            // return memoized;
        // } else {
            // ANALYSIS: cache miss
            // numCacheMiss += 1;
        // }

        Set<Action> actions = getActions(s);
        // ANALYSIS: branch size
        // totalBranchSize += actions.size();
        if (actions.size() == 0)
            return new Node(s.randomAction(), (s.isBlacksTurn()) ? -infinity : infinity);
        if (actions.size() > bigDepthThreshold)
            bigDepth += 1;

        if (depth == 0) {
            s.highlight(actions);
            if (actions.size() == 1)
                for (Action a : actions)
                    return new Node(a, 0);
        }

        depth += 1;
        Node node = null; boolean who = s.isBlacksTurn();
        if (who) node = maxvalue(s, alpha, beta, depth, bigDepth, actions);
        else     node = minvalue(s, alpha, beta, depth, bigDepth, actions);
        // memo.put(prev, node);
        return node;
    }

    private int four(Counter features, boolean isBlack) {
        if (isBlack)
            return (features.getInt("-oooox") + features.getInt("four-o"));
        return (features.getInt("-xxxxo") + features.getInt("four-x"));
    }
    private int straightFour(Counter features, boolean isBlack) {
        if (isBlack)
            return features.getInt("-oooo-");
        return features.getInt("-xxxx-");
    }
    private int three(Counter features, boolean isBlack) {
        if (isBlack)
            return (features.getInt("-o-oo-") +
                    features.getInt("-oo-o-") +
                    features.getInt("-ooo-"));
        return (features.getInt("-x-xx-") +
                features.getInt("-xx-x-") +
                features.getInt("-xxx-"));
    }

    private Set<Action> movesExtendFour(State s, Counter features) {
        Set<Action> result = new HashSet<Action>(1, 2);
        String win = (s.isBlacksTurn()) ? "win-o" : "win-x";
        for (Action a : s.getLegalActions()) {
            if (Extractor.diffFeatures(s, a).getInt(win) > 0) {
                result.add(a);
                return result;
            }
        }
        throw new RuntimeException("my four is missing?");
    }
    private Set<Action> movesCounterFour(State s, Counter features) {
        Set<Action> result = new HashSet<Action>(1, 2);
        boolean w = s.isBlacksTurn();
        for (Action a : s.getLegalActions()) {
            if (-four(Extractor.diffFeatures(s, a), !w) >= four(features, !w)) {
                result.add(a);
                return result;
            }
        }
        return result;
    }
    private Set<Action> movesExtendThree(State s, Counter features) {
        Set<Action> result = new HashSet<Action>(1, 2);
        boolean w = s.isBlacksTurn();
        for (Action a : s.getLegalActions()) {
            if (straightFour(Extractor.diffFeatures(s, a), w) > 0) {
                result.add(a);
                return result;
            }
        }
        if (three(features, !w) > 0)
            return movesCounterThree(s, features);
        return movesBestGrowth(s, features);
    }
    private Set<Action> movesCounterThree(State s, Counter features) {
        Set<Action> result = new HashSet<Action>(3, 2);
        boolean w = s.isBlacksTurn(); Counter diff = null;
        for (Action a : s.getLegalActions()) {
            diff = Extractor.diffFeatures(s, a);
            if (-three(diff, !w) >= three(features, !w) ||
                four(diff, w) > 0)
                result.add(a);
        }
        return result;
    }

    private Set<Action> movesBestGrowth(State s, Counter features) {
        Action[] actions = s.getLegalActions();
        Set<Action> result = new HashSet<Action>();
        boolean w = s.isBlacksTurn();

        // nodes that looks contributive
        Node[] nodes = new Node[actions.length];
        for (int i = 0; i < actions.length; i++)
            nodes[i] = new Node(actions[i], heuristic(s, actions[i]));
        Arrays.sort(nodes, (w) ? blackComparator : whiteComparator);
        for (int i = 0; i < branch/3 && i < nodes.length; i++)
            result.add(nodes[i].a);

        // nodes that looks good to me
        nodes = new Node[actions.length]; Rewinder r = null;
        for (int i = 0; i < actions.length; i++) {
            r = s.move(actions[i]);
            nodes[i] = new Node(actions[i], evaluate(s));
            s.rewind(r);
        }
        Arrays.sort(nodes, (w) ? blackComparator : whiteComparator);
        for (int i = 0; i < branch/3 && i < nodes.length; i++)
            result.add(nodes[i].a);

        // nodes that looks good to opponent
        nodes = new Node[actions.length];
        s.makeDangerousNullMove();
        for (int i = 0; i < actions.length; i++) {
            r = s.move(actions[i]);
            nodes[i] = new Node(actions[i], evaluate(s));
            s.rewind(r);
        }
        s.rewindDangerousNullMove();
        Arrays.sort(nodes, (!w) ? blackComparator : whiteComparator);
        for (int i = 0; i < branch/3 && i < nodes.length; i++)
            result.add(nodes[i].a);

        return result;
    }
    private int heuristic(State s, Action a) {
        Counter diff = Extractor.diffFeatures(s, a);
        int score = 0;
        for (String key : diff.keySet())
            score += Extractor.sign(key) * diff.getInt(key);
        return score;
    }

    protected Set<Action> getActions(State s) {
        Counter f = s.extractFeatures();
        boolean w = s.isBlacksTurn();
        if (four(f, w) > 0 || straightFour(f, w) > 0)
            return movesExtendFour(s, f);
        if (straightFour(f, !w) > 0)
            return new HashSet<Action>(1);
        if (four(f, !w) > 0)
            return movesCounterFour(s, f);
        if (three(f, w) > 0)
            return movesExtendThree(s, f);
        if (three(f, !w) > 0)
            return movesCounterThree(s, f);
        return movesBestGrowth(s, f);
    }

    @Override
    public Action getAction(State s) {
        // initAnalysis();
        if (!s.isTurn(isBlack))
            throw new RuntimeException("not my turn");
        Node retval = null;
        if (!s.started()) {
            retval = new Node(s.start, 0);
        } else {
            // memo.clear();
            retval = value(s, -infinity, infinity, 0, 0);
        }
        s.unhighlight();
        s.determineMove(retval.a);
        return retval.a;
    }
}
