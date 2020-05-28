package com.eliux.catchthepig.single;

import android.app.Dialog;
import android.util.Log;

import com.eliux.catchthepig.R;
import com.eliux.catchthepig.logic.GameAI;
import com.eliux.catchthepig.logic.GameAIMiniMax;
import com.eliux.catchthepig.logic.GameBoard;
import com.eliux.catchthepig.logic.GameMove;
import com.eliux.catchthepig.single.GameBoardView.GameCharacter;

public class GameController{ 
	//Human player's character
	int human_player_id = GameBoard.BOARD_EMPTY;
	//Vars
	static public final int X_OFFSET = 0;
	static public final int Y_OFFSET = 1;   
	/** GameLogic **/ 
	GameBoard board;
	protected int[][] mData; // current   
	//View
	GameBoardView mView;  
	Thinker mthinker;
	 
	
	public GameController(GameBoardView v) {
		super(); 
		mView = v;
		resetGame(); // Initiate the game when created
	}

	/**
	 * Restart game configuration
	 */
	public synchronized void resetGame() {    
		mthinker = new Thinker();
		// Wipe all the Data
		board = new GameBoard(mView.getBoardSize());
		board.setSize(mView.BOARD_SIZE);
		mData = new int[mView.BOARD_SIZE][mView.BOARD_SIZE]; 	// Current board 
		// Lets reset the board and IA board
		for (int index = 0, i = 1; index < mView.HALF_BOARD_SIZE; index++, i += 2) {
			// Set the Birds
			mData[i][0] = (index+1);	//Bird's index 
			// Lets establish where are the birds not asking to the whole board
			mView.createBird(index, i, 0);
		}
		// Set the Piggie position in the board
		mData[mView.HALF_BOARD_SIZE][mView.BOARD_SIZE - 1] = GameBoard.PLAYER_PIGGY;	//Vertical direction
		//Locate the piggie  
		mView.createPiggie(mView.HALF_BOARD_SIZE, mView.BOARD_SIZE - 1); 
		mView.createEgg(mView.HALF_BOARD_SIZE, 0);	//Put the egg in the middle 
	}
	 
	/**
	 * Establishes which is the character
	 * @param character PLAYER_PIGGIE or PLAYER_BIRD
	 */
	public void setHumanPlayerId(int character){ 
		human_player_id = (character == GameBoard.PLAYER_PIGGY) ? GameBoard.PLAYER_PIGGY
				: GameBoard.PLAYER_BIRD; 
	}	
	
	/**
	 * Returns the caracter at a give position of the map else return null
	 * @param x	X position on the board
	 * @param y	Y position on the board
	 * @return	An instance of GameCharacter else null
	 */
	public GameCharacter getCharacterAt(int x, int y){ 
		try{
			if(isPig(x,y)){
				return mView.mPiggy;
			}else if(isBird(x,y)){
				return mView.mBirds[getIndex(x,y)];
			}
		}catch(Exception e){
			Log.e("GameController", "Error obtaining Game Character at the position ("+x+","+y+")");
		}
		return null;
	}
	
	public boolean isPig(int x, int y){
		return mData[x][y]<0;
	}
	
	public boolean isBird(int x, int y){
		return mData[x][y]>0;
	}
	
	
	public int getIndex(int x, int y){
		return Math.abs(mData[x][y])-1;
	}
	
	public int[][] getBoardModel(){
		return mData;
	} 

	
	/**
	 * Move one character from one position of the map to another
	 * @param move Indicates who and how did the move
	 * @return TRUE | FALSE The move was done or not
	 */
	synchronized public boolean makeMove(GameMove move) {
		try {
			if (board.makeMove(move)) {
				mData[move.to.x][move.to.y] = mData[move.from.x][move.from.y]; // I  change this
				mData[move.from.x][move.from.y] = GameBoard.BOARD_EMPTY;
				board.setState(move.player); // Who played this

				if (human_player_id == GameBoard.BOARD_EMPTY) // If this is the first play
				{
					human_player_id = move.player; // The human does the first play
				}

				//If the human played the computer's side
				if (move.isHuman && human_player_id != move.player) {
					human_player_id = move.player;	//Change the humans side
					// Let put AI on move 
					calculateAIMove(move);
				}

				if (human_player_id == move.player) { // If the humans plays
					// Let put AI on move
					calculateAIMove(move);
				}

				// The player id is designed to be also a game_state
				board.setState(-move.player); // Its turn to the other guy
				return true;
			}
		} catch (Exception e) {
			Log.e("GameController", "Error trying to move character from ("
					+ move.from.x + "," + move.from.y + ") to (" + move.to.x
					+ "," + move.to.y + "): " + e.getMessage());
		}
		return false; 
	} 
	
	/**
	 * Let the computer think what to do
	 * @param move Move given by the human
	 */
	synchronized public void calculateAIMove(GameMove move){
		//First I paint
		mView.refresh();
		move.isHuman = false;	//It's done by the computer always
		mthinker.setMove(move);
		mView.post(mthinker);
	}
	
	/**
	 * Ends the current game
	 * @param msg_id which is the status id
	 */
	public void endGame(int msg_id){
		Dialog dialog = new Dialog(mView.getContext());
		if(msg_id == GameBoard.STATE_WIN_BIRDS){   
			dialog.setContentView(R.layout.msg_bird_wins);
		}else if(msg_id == GameBoard.STATE_WIN_PIG){ 
			dialog.setContentView(R.layout.msg_pig_wins); 
		}else{ 
			return;
		} 
		dialog.setTitle(R.string.app_name);	
		board.setState(msg_id);
		dialog.show();   
		mView.onChangeStatus();
	}
	 
	
	/**
	 * Converts a X,Y position into an integer one
	 * @param x X position	
	 * @param y	Y position
	 * @return	An integer indicating the position
	 */
	public int getPosFromXY(int x, int y){
		return (x*mView.BOARD_SIZE)+y;
	}
	
	/**
	 * Get the X,Y position based on an integer on
	 * @param x An integer indicating the position
	 */
	public int getXYFromPos(Integer pos, int offset) {
		if (offset == Y_OFFSET) {
			return (int) pos % mView.BOARD_SIZE;
		} 
		return (int) Math.ceil(pos / mView.BOARD_SIZE);
	}

	public GameBoardView getView() { 
		return mView;
	}

	
	public int getHuman_player_id() {
		return human_player_id;
	}
 

	public int getGame_state() {
		return board.getState();
	}

	public void setGame_state(int game_state) {
		board.setState(game_state);
	}

	public GameBoard getBoard() {
		return board;
	}

	public void setBoard(GameBoard board) {
		this.board = board;
	}
 
   	class Thinker implements Runnable{
		GameMove move; 
		@Override
		public void run() {
			GameAI computer;
			try {
				computer = new GameAIMiniMax(board.getCopy(), move);	//Using the Minimax algorithm
				GameMove computer_move = computer.movements(mView.getAiLevel());
				//Lets moves it in the GameBoardView and he will pass it for the rest
				int rank = 0;
				if(computer_move!=null){
					rank = computer_move.irank;
					mView.makeMove(computer_move);
				}else{	//Computer can no think
					rank = human_player_id == GameBoard.PLAYER_BIRD ? GameAI.MAX_IRANK_LIMIT : GameAI.MIN_IRANK_LIMIT;
				}
				//Lets see if everything is over.
				if(rank>=GameAI.MAX_IRANK_LIMIT){		
					endGame(GameBoard.STATE_WIN_BIRDS);			//Pursuiters won 
				}	
				if(rank<=GameAI.MIN_IRANK_LIMIT){				//Pursuited won, he ran away
					endGame(GameBoard.STATE_WIN_PIG); 
				}
			} catch (CloneNotSupportedException e) {
				Log.e("GameController", "Imposible cloning the board: "+board);
				e.printStackTrace();
			} catch (InterruptedException e) {
				Log.e("GameController", "The AI procesing was interrupted: "+e.getMessage());
				e.printStackTrace();
			}catch (Exception e) {
				Log.e("GameController", "To much procesing: "+e.getMessage());
				e.printStackTrace();
			}		
		}

		public GameMove getMove() {
			return move;
		}

		public void setMove(GameMove move) {
			this.move = move;
		} 
		
		public Thinker(){} 

	} 
}
