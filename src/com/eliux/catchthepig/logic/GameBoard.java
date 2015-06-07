package com.eliux.catchthepig.logic;

import java.util.ArrayList;
import java.util.List;


public class GameBoard implements Cloneable {
	// Characters
	final public static int BOARD_EMPTY = 0x0;
	final public static int PLAYER_PIGGY = -1;
	final public static int PLAYER_BIRD = 1;
	// State
	final public static int STATE_PLAY_PIGGIE = PLAYER_PIGGY;
	final public static int STATE_WIN_PIG = -2;
	final public static int STATE_PLAY_BIRDS = PLAYER_BIRD;
	final public static int STATE_WIN_BIRDS = 2;
	final public static int STATE_NONE = BOARD_EMPTY;
	// Properties
	protected int[][] mAIData; // AI Board
	int state;
	int size = 0;
	// Positiones
	Position[] pbirds;
	Position ppig;

	public GameBoard(int viewSize) {
		size = viewSize;
		int half_size = size / 2;
		// Current board
		mAIData = new int[size][size]; // Board for AI thinking
		// Their positions
		pbirds = new Position[half_size];
		// Lets reset the board and IA board
		for (int i = 0, index = 1; i < half_size; i++, index += 2) {
			mAIData[index][0] = PLAYER_BIRD;
			pbirds[i] = new Position(index, 0);
		}
		mAIData[half_size][size - 1] = PLAYER_PIGGY;
		ppig = new Position(half_size, size - 1);
		state = STATE_NONE;
	}

	public int[][] getmAIData() {
		return mAIData;
	}

	public void setmAIData(int[][] mAIData) {
		this.mAIData = mAIData;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public Position[] getPbirds() {
		return pbirds;
	}

	public void setPbirds(Position[] pbirds) {
		this.pbirds = pbirds;
	}

	public Position getPpig() {
		return ppig;
	}

	public void setPpig(Position ppig) {
		this.ppig = ppig;
	}

	public int[][] getModel() {
		return mAIData;
	}

	public void setModel(int[][] model) {
		this.mAIData = model;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	/**
	 * Check the board's Irank
	 * 
	 * @return Irank value;
	 * @throws InterruptedException
	 */
	public int checkBoard() {
		int capable_birds = 0; 				// How many birds can stop the piggie
		int most_delayed_bird = size - 2; 	// Start always with the opposite
											// value
		for (Position posBird : pbirds) { 	// Let search the most advanced bird
			if (posBird.y < most_delayed_bird)
				most_delayed_bird = posBird.y;
			if (posBird.y < ppig.y) { 		// If I am in front to the piggie I can stop him
				capable_birds++;
			}
		}
		
		int most_one_line = 0; 				// Most quantity of birds in one line
		int top_index = pbirds.length; 		// Not count the last, it's no necessary
		for (int i = 0; i < top_index; i++) {
			int quantity = 0;
			for (int j = i + 1; j < top_index; j++) {
				if (pbirds[i].y == pbirds[j].y) {
					quantity++;
				}
			}
			if (quantity > most_one_line)
				most_one_line = quantity;
		}

		// Lets check if the game is over
		if (capable_birds < 2) { // Less of two are imposible to stop the pig
			return GameAI.MIN_IRANK_LIMIT;
		}

		if (isPigCatched()) { 		// If piggie can not move
			return GameAI.MAX_IRANK_LIMIT;
		}

		if (most_one_line > 3) { 	// It should meant thant game is over
			most_one_line = 6; 		// Increass it
		}

		return capable_birds * 100 + most_one_line * 120 - howMuchLeft() * 10;
	}

	/**
	 * I want to know if the piggie can not moves
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public boolean isPigCatched() {
		ArrayList<GameMove> count_of_moves = getAvailablesMoves(PLAYER_PIGGY);
		return count_of_moves.size() <= 1; // If he has no more moves
	}

	/**
	 * How many moves will birds may play in the future
	 * 
	 * @return
	 */
	public int howMuchLeft() {
		int count = 0;
		try {
			List<GameMove> ma = getAvailablesMoves(PLAYER_BIRD); // How many  moves left to the birds
			for (GameMove gameMove : ma) {
				if (gameMove.from.y < gameMove.to.y) { // If its moving forward
					count++;
				}
			}
		} catch (Exception e) {
		}
		return count;
	}

	/**
	 * Get the list of the availables moves 4 a player
	 * 
	 * @param player
	 *            ID of the player. Ej.: BOARD_BIRD
	 * @return A list of moves. List<GameMove>
	 * @throws InterruptedException
	 */
	public ArrayList<GameMove> getAvailablesMoves(int player) {
		ArrayList<GameMove> moves = new ArrayList<GameMove>();
		Position[] positions = (player == PLAYER_BIRD) ? pbirds
				: new Position[] { ppig };
		for (Position from : positions) { // What are the positions of this
											// player's characters
			ArrayList<Position> toList = getValidPositions(player, from);
			// Where he can move each character?
			for (Position to : toList) {
				moves.add(new GameMove(player, from, to)); // Add any posible  player's move
			}
		}
		return moves;
	}

	/**
	 * Validates if some move is valid or not
	 * 
	 * @param move
	 *            Instance of GameMove that we are trying to validate
	 * @return TRUE | FALSE If the Game's logic approves it or not
	 */
	public boolean validateGameMove(GameMove move) {
		if (!isPositionValid(move.to)) {
			return false;
		}

		if (move.player == PLAYER_BIRD) { // Birds moves in cross +
			if (!((move.from.y == move.to.y - 1)
					&& (move.from.x == move.to.x - 1) || (move.from.y == move.to.y - 1)
					&& (move.from.x == move.to.x + 1))) {

				return false;
			}
		}
		if (move.player == PLAYER_PIGGY) { // The pigs moves like X
			if (!((move.from.y == move.to.y - 1)
					&& (move.from.x == move.to.x - 1)
					|| (move.from.y == move.to.y + 1)
					&& (move.from.x == move.to.x - 1) || ((move.from.y == move.to.y - 1)
					&& (move.from.x == move.to.x + 1) || (move.from.y == move.to.y + 1)
					&& (move.from.x == move.to.x + 1)))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Validates of some position can be taken
	 * 
	 * @param pos
	 *            Position where you want to go
	 * @return TRUE|FALSE if somebody can move to this point
	 */
	public boolean isPositionValid(Position pos) {
		if (pos.x >= size || pos.y >= size || pos.y < 0 || pos.x < 0) { // Inside the limits
			return false;
		}

		if (mAIData[pos.x][pos.y] != BOARD_EMPTY) { // If it's occupied
			return false;
		}

		if ((pos.x + pos.y) % 2 == 0) { // Like damas
			return false;
		}

		return true;
	}

	/**
	 * Gets all the position a character in a given position may go
	 * 
	 * @param player
	 *            ID of the player
	 * @param pos
	 *            Position
	 * @return List of Positions(ArrayList<Position>) where the user may go
	 */
	public ArrayList<Position> getValidPositions(int player, Position pos) {
		ArrayList<Position> result = new ArrayList<Position>();
		Position wanted_position;
		if (player != PLAYER_BIRD) // Birds only go forward
		{
			wanted_position = new Position(pos.x - player, pos.y - player); // Up left
			if (isPositionValid(wanted_position))
				result.add(wanted_position);
			wanted_position = new Position(pos.x + player, pos.y - player); // Up right
			if (isPositionValid(wanted_position))
				result.add(wanted_position);
		}
		wanted_position = new Position(pos.x - player, pos.y + player); // Down left - Forward
		if (isPositionValid(wanted_position))
			result.add(wanted_position);
		wanted_position = new Position(pos.x + player, pos.y + player); // Down right
		if (isPositionValid(wanted_position))
			result.add(wanted_position);
		return result;
	}

	/**
	 * Try to move some character in the board
	 * 
	 * @param move
	 *            Movement
	 * @return TRUE | FALSE The character can do this
	 */
	public boolean makeMove(GameMove move) {
		if (validateGameMove(move)) {
			mAIData[move.to.x][move.to.y] = mAIData[move.from.x][move.from.y]; // I change this
			mAIData[move.from.x][move.from.y] = BOARD_EMPTY;
			if (move.player == PLAYER_BIRD) {
				for (int i = 0; i < pbirds.length; i++) {
					if (pbirds[i].x == move.from.x
							&& pbirds[i].y == move.from.y) {
						pbirds[i] = move.to;
						break;
					}
				}
			} else {
				ppig = move.to;
			}
			return true;
		}
		return false;
	}

	/**
	 * Clones the board
	 * 
	 * @return a copy of {@link #GameBoard(int)}
	 * @throws CloneNotSupportedException
	 *             If it couldnt be copied
	 */
	public GameBoard getCopy() throws CloneNotSupportedException {
		GameBoard newGB = (GameBoard) this.clone();
		// Clone the matrix
		newGB.mAIData = new int[size][size];

		newGB.pbirds = new Position[pbirds.length];
		for (int i = 0; i < pbirds.length; i++) {
			newGB.pbirds[i] = pbirds[i].getCopy(); // Clone each one;
			newGB.mAIData[pbirds[i].x][pbirds[i].y] = GameBoard.PLAYER_BIRD; // Set  the matrix
		}
		newGB.ppig = (Position) this.ppig.getCopy();
		newGB.mAIData[ppig.x][ppig.y] = GameBoard.PLAYER_PIGGY; // Set the pig in the matrix
		return newGB;
	}

}
