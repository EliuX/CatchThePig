package com.eliux.catchthepig.logic;



public class GameMove{
	static final public int PLAYER_NONE = 0;
	static final public int PLAYER_PIG = 1;
	static final public int PLAYER_BIRD = 2;
	public int player = PLAYER_NONE;
	public int irank = 0;
	public Position from;
	public Position to;
	public boolean isHuman = false;
	public GameMove(int player, Position from, Position to) {
		super();
		this.player = player;
		this.from = from;
		this.to = to;
		irank = 0;
	}

	public int getPlayer() {
		return player;
	}

	public void setPlayer(int player) {
		this.player = player;
	}

	public int getIrank() {
		return irank;
	}

	public void setIrank(int irank) {
		this.irank = irank;
	}

	public Position getFrom() {
		return from;
	}

	public void setFrom(Position from) {
		this.from = from;
	}

	public Position getTo() {
		return to;
	}

	public void setTo(Position to) {
		this.to = to;
	} 

}
