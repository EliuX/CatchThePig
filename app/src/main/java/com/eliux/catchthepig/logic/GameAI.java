package com.eliux.catchthepig.logic;


import java.util.ArrayList;




abstract public class GameAI {
	static public int MAX_IRANK_LIMIT = Integer.MAX_VALUE; 
	static public int MIN_IRANK_LIMIT = Integer.MIN_VALUE;
	static public int MAX_IRANK = 2000;			//No rank shall excel this. Ej.: 2000
	protected AINode aitree; 					// Tree for calculation of the AI 

	public GameAI(GameBoard board, GameMove play) { 
		aitree = new AINode(board, play); 
	} 
	
	public GameMove movements(int depth) throws CloneNotSupportedException, InterruptedException{
		init(aitree,depth);
		return getBestMove(aitree, depth);
	}
	
	/**
	 * Builds the AI tree (Recursive)
	 * @param tree	Before noe
	 * @param depth number of the iteration
	 * @throws CloneNotSupportedException 
	 * @throws InterruptedException 
	 */
	protected void init(AINode tree, int depth) throws CloneNotSupportedException{ 
		if(depth==0){
			tree.played.irank = tree.board.checkBoard();
			return;
		}	
		
		//What the other player can play?
		ArrayList<GameMove> am = tree.board.getAvailablesMoves(-tree.played.player);
		if(am.size()>0){
			for (GameMove gameMove : am) {	//See the future for each game's move
				//How the board will look with this active play
				GameBoard newBoard = tree.board.getCopy();	
				//newBoard.mAIData  = tree.board.mAIData.clone();
				//newBoard.state = tree.board.state;
				newBoard.makeMove(gameMove);	//Lets create this play
				AINode aitree = new AINode(newBoard,gameMove);
				init(aitree, depth-1);		//Starts the child node
				tree.nodes.add(aitree); 
			}
		}else{
			tree.played.irank = tree.board.checkBoard();
			return;
		}
	}
	
	/**
	 * Decides with would be the best move for the player is about to play?
	 * @param tree	Actual AI tree
	 * @param depth How much to dig in?
	 * @return What to move?
	 * @throws InterruptedException 
	 */
	abstract public GameMove getBestMove(AINode tree, int depth) throws InterruptedException;  
}
