package domains.tictactoe;


import domains.Action;

public class TicTacToeAction implements Action {

	private static final TicTacToeAction[] actions = new TicTacToeAction[]{
		new TicTacToeAction(0),
		new TicTacToeAction(1),
		new TicTacToeAction(2),
		new TicTacToeAction(3),
		new TicTacToeAction(4),
		new TicTacToeAction(5),
		new TicTacToeAction(6),
		new TicTacToeAction(7),
		new TicTacToeAction(8),
		new TicTacToeAction(9)
	};

	private final int position;

	private TicTacToeAction(int position) {
		this.position = position;
	}

	@Override
	public int id() {
		return position;
	}

	@Override
	public String name() {
		return position+"";
	}

	public static TicTacToeAction pos(int position){
		return actions[position];
	}
}
