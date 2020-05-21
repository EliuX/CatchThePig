package com.eliux.catchthepig.logic;


public class Position implements Cloneable{
	public int x;
	public int y;

	public Position(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public Position getCopy() throws CloneNotSupportedException {
		return (Position) this.clone();
	} 

}
