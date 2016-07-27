package com.jiahangli.alphagomoku;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.Set;

import gomoku.Action;
import gomoku.ActionListener;
import gomoku.ActionsListener;
import gomoku.Agent;
import gomoku.MinimaxAgent;
import gomoku.State;

public class BoardActivity extends AppCompatActivity {

    public static final int N = State.N;
    public State state = null;

    private static Action toAction(int position) {
        return new Action(position / N, position % N);
    }
    private static int toPosition(Action action) {
        return action.x() * N + action.y();
    }

    private static class AgentRunner implements Runnable {
        private Agent agent = new MinimaxAgent(true);
        private State state = null;
        public void setState(State s) {
            state = s;
        }
        @Override
        public void run() {
            while (true) {
                if (state.isBlacksTurn()) {
                    state.move(agent.getAction(state));
                }
                try { Thread.sleep(200); } catch (InterruptedException e) {}
            }
        }
        public void start() {
            Thread t = new Thread(this);
            t.start();
        }
    }
    private static AgentRunner agent = new AgentRunner();

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private ImageView[] board;
        public ImageAdapter(Context m) {
            mContext = m;
            board = new ImageView[N * N];
            for (int i = 0; i < board.length; i++) {
                board[i] = new ImageView(mContext);
                board[i].setAdjustViewBounds(true);
                board[i].setImageResource(R.drawable.empty);
            }
        }
        @Override public int getCount() { return N * N; }
        @Override public Object getItem(int position) { return null; }
        @Override public long getItemId(int position) { return 0; }
        @Override public View getView(int position, View convertView, ViewGroup parent) {
            return board[position];
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        final ImageAdapter adapter = new ImageAdapter(this);

        state = new State();
        state.onEvaluate(new ActionListener() {
            @Override public void digest(final Action action) {
                BoardActivity.this.runOnUiThread(new Runnable() {
                    @Override public void run() {
                        adapter.board[toPosition(action)].setImageResource(R.drawable.red);
                    }
                });
            }
        });
        state.onHighlight(new ActionsListener() {
            @Override public void digest(final Set<Action> actions) {
                BoardActivity.this.runOnUiThread(new Runnable() {
                    @Override public void run() {
                        for (Action a : actions) {
                            adapter.board[toPosition(a)].setImageResource(R.drawable.yellow);
                        }
                    }
                });
            }
        });
        state.onUnhighlight(new ActionsListener() {
            @Override public void digest(final Set<Action> actions) {
                BoardActivity.this.runOnUiThread(new Runnable() {
                    @Override public void run() {
                        for (Action a : actions) {
                            adapter.board[toPosition(a)].setImageResource(R.drawable.empty);
                        }
                    }
                });
            }
        });
        state.onDetermineMove(new ActionListener() {
            @Override public void digest(final Action action) {
                BoardActivity.this.runOnUiThread(new Runnable() {
                    @Override public void run() { // asuming AI always blue
                        adapter.board[toPosition(action)].setImageResource(R.drawable.blue);
                    }
                });
            }
        });
        agent.setState(state);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Action action = toAction(position);
                if (state.isTurn(false) && state.canMove(action)) {
                    state.move(action);
                    ImageAdapter adapter = (ImageAdapter) parent.getAdapter();
                    adapter.board[position].setImageResource(R.drawable.green);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        agent.start();
    }
}

