package com.eliux.catchthepig.logic;


public class GameAIMiniMax extends GameAI {

	public GameAIMiniMax(GameBoard board, GameMove play) {
		super(board, play); 
	}
 

	@Override
	public GameMove getBestMove(AINode tree, int depth) throws InterruptedException {
		GameMove move = null;
		if (tree.nodes.size() > 0)
			if (tree.played.player == GameBoard.PLAYER_PIGGY) {	//Piggi played, now bird maximize
				int max = MIN_IRANK_LIMIT;
				for (AINode node : tree.nodes) {
					int value = miniMax(node, MIN_IRANK_LIMIT, MAX_IRANK_LIMIT, depth);	
					if (max < value) {		//Looking for the biggest Irank
						max = value;
						move = node.played;	//This gonna be my move
					}
				}
			} else {		//Birds played, now pig minimize
				int min = GameAI.MAX_IRANK_LIMIT;
				for (AINode node : tree.nodes) {
					int value = miniMax(node, MIN_IRANK_LIMIT, MAX_IRANK_LIMIT, depth);
					if (min > value) {		//Looking for the lowest Irank
						min = value;
						move = node.played;	//This gonna be my move
					}
				}
			}
		return move;
	}
	
	/**
	 * Executes the algoritm of Minimax returning an IRank value
	 * @param tree	AINode instance
	 * @param alpha	node alpha for minimax
	 * @param beta	node beta for minimax
	 * @param depth	how much to dig?
	 * @return IRank value(int)	
	 * @throws InterruptedException 
	 */
	private int miniMax(AINode tree, int alpha, int beta, int depth) throws InterruptedException{
		if(depth==0){
			return tree.board.checkBoard();
		}
		//If we have no more nodes, this is a branch
		if(tree.nodes.size()==0){
			return tree.played.irank;
		}
		//If the birds played
		if(tree.played.player==GameBoard.PLAYER_BIRD){
			for (AINode node : tree.nodes) {
				if (alpha >= beta) { // The alpha-beta condition
					break;
				} else {
					int value = miniMax(node,alpha,beta,depth-1);	
					if(beta>value)		
					{	//Choose the beta way
						beta = value;
						tree = node;
					}
				}
			}
			tree.played.irank = beta;
			return beta;
		}else{
			for (AINode node : tree.nodes) {
				if (alpha >= beta) { // The alpha-beta condition
					break;
				} else {
					int value = miniMax(node,alpha,beta,depth-1);	//Lets calculate miniMax of childs 
					if(alpha<value)
					{	//Choose the alpha way
						alpha = value;
						tree = node;
					}
				}
			}

			tree.played.irank = alpha;
			return alpha;
		} 
	}

}
