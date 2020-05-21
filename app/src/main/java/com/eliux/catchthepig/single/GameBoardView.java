package com.eliux.catchthepig.single;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.eliux.catchthepig.R;
import com.eliux.catchthepig.logic.GameBoard;
import com.eliux.catchthepig.logic.GameMove;
import com.eliux.catchthepig.logic.Position;

public class GameBoardView extends View{ 
	/** Preferences **/
	SharedPreferences settings; 
	/** Resources and handlers **/
	static public final int BOARD_COLOR = 0xFFFFFFFF;  
	Resources res; // Resources handler
	GameController ctrlGame;
	GameCharacter caracter_selected;	//If some caracter is moving which is it? 
	/** Game Characters **/
	// Where are the birds?
	protected Bird[] mBirds; // Several Birds
	protected Piggie mPiggy; // VS 1 piggie
	protected Egg mEgg;		 //A tresor image	
	/** Images **/
	Drawable bgdrawable; // The background
	Drawable[] mDrawPiggie = new Drawable[GameCharacter.ANIM_COUNT];
	Drawable[] mDrawBirds = new Drawable[GameCharacter.ANIM_COUNT];  
	Drawable[] mDrawEgg = new Drawable[GameCharacter.ANIM_COUNT];  
    /** Sounds **/
	MediaPlayer[] mSoundPig = new MediaPlayer[GameCharacter.ANIM_COUNT];
	MediaPlayer[] mSoundBirds = new MediaPlayer[GameCharacter.ANIM_COUNT];
	MediaPlayer mStartSound;
	/** Layers **/
	Paint layerLines; 		// Layer for lines 
	Paint layerFloor; 
	public int BOARD_SIZE; 	// The board is squared
	public int HALF_BOARD_SIZE; // I use this several times
	/** Graphic configurations **/ 
	final protected int LINE_WIDTH = 2; // Grosor de las lineas
	protected int sizeXY; // Size of each square of the board
	protected int sizeBoard; // Size in pixels of each size of the squared board
	protected int mOffetX; // Margenes
	protected int mOffetY;
	
	public GameBoardView(Context context, AttributeSet attrs) {
		super(context, attrs);    
		setSaveEnabled(true);
		// Loading starts
		setFocusableInTouchMode(true); 
		res = getResources();
		bgdrawable = res.getDrawable(R.drawable.bg_board);
		// Drawables
		mDrawPiggie[GameCharacter.STATUS_NORMAL] = res
				.getDrawable(R.drawable.normal_piggie);
		mDrawPiggie[GameCharacter.STATUS_ON_MOVE] = res
				.getDrawable(R.drawable.smily_piggie); 
		mDrawPiggie[GameCharacter.STATUS_ON_ANIMATION] = res
				.getDrawable(R.drawable.closed_piggies);
		mDrawPiggie[GameCharacter.STATUS_WIN] = mDrawPiggie[GameCharacter.STATUS_ON_MOVE];
		
		mDrawBirds[GameCharacter.STATUS_NORMAL] = res
				.getDrawable(R.drawable.normal_bird);
		mDrawBirds[GameCharacter.STATUS_ON_MOVE] = res
				.getDrawable(R.drawable.angry_bird);
		mDrawBirds[GameCharacter.STATUS_ON_ANIMATION] = res
				.getDrawable(R.drawable.closed_bird);  
		mDrawBirds[GameCharacter.STATUS_WIN] = mDrawBirds[GameCharacter.STATUS_ON_MOVE];
		
		mDrawEgg[GameCharacter.STATUS_NORMAL] = res
				.getDrawable(R.drawable.tresor); 

		//Sounds
		mSoundBirds[GameCharacter.STATUS_NORMAL] = MediaPlayer.create(context,R.raw.bird_normal);	//Birds
		mSoundBirds[GameCharacter.STATUS_ON_MOVE] = MediaPlayer.create(context,R.raw.bird_moves);
		mSoundBirds[GameCharacter.STATUS_WIN] = MediaPlayer.create(context,R.raw.bird_victory); 
		
		mSoundPig[GameCharacter.STATUS_NORMAL] = MediaPlayer.create(context,R.raw.pig_normal);	//Birds
		mSoundPig[GameCharacter.STATUS_ON_MOVE] = MediaPlayer.create(context,R.raw.pig_moves);
		mSoundPig[GameCharacter.STATUS_WIN] = MediaPlayer.create(context,R.raw.pig_victory);

		mStartSound = MediaPlayer.create(context,R.raw.level_start);
		mStartSound.setAudioStreamType(AudioManager.STREAM_MUSIC);
		
		// Set the rectangle(wich will handle slots) upon the piggies size 
		layerFloor = new Paint();
		layerFloor.setColor(BOARD_COLOR);
		layerFloor.setAlpha(128);

		layerLines = new Paint(); // The divitions
		layerLines.setColor(BOARD_COLOR);
		layerLines.setStrokeWidth(LINE_WIDTH);
		layerLines.setStyle(Style.STROKE); 
		// Drawing starts
		setBackgroundDrawable(bgdrawable); // Background set 
		loadGame();
	} 
	
	/**
	 * Repaints the control
	 */
	public void refresh(){ 
		invalidate();
	}
	

	/**
	 * Does all the steps necessaries for (re)starting the level
	 */
	public void loadGame(){  
		requestFocus(); 
		if(settings==null){
			settings = PreferenceManager.getDefaultSharedPreferences(getContext());
		}
		//Variables
		ctrlGame = null;
		// Size upon the Piggie for example
		if(sizeXY==0){ 
			int w = mDrawPiggie[GameCharacter.STATUS_NORMAL].getIntrinsicWidth();
			int h = mDrawPiggie[GameCharacter.STATUS_NORMAL].getIntrinsicHeight();
			sizeXY = w < h ? w : h; 			// Like its rectangular choose the shortest
		}
		// Set the size of the KeyBoard
		changeBoardSize(8);
		// Initialize the Game 
		ctrlGame = new GameController(this); 	// My controller 
		if(allowSounds()){
			mStartSound.start(); 
		}
		refresh();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.save();
		for (int i = 0, k = 0; i <= BOARD_SIZE; i++, k += sizeXY) {
			canvas.drawLine(mOffetX, mOffetY + k, mOffetX + sizeBoard - 1,
					mOffetY + k, layerLines);
			canvas.drawLine(mOffetX + k, mOffetY, mOffetX + k, mOffetY
					+ sizeBoard - 1, layerLines);
		}
		paintPlayer(canvas); 	// Paint the birds and the Piggie
		canvas.restore();
	}
	
	/**
	 * When the status has changed
	 */
	public void onChangeStatus(){ 
		switch(ctrlGame.getGame_state()){ 
			case GameBoard.STATE_WIN_BIRDS:
				mSoundBirds[GameCharacter.STATUS_WIN].start();
			case GameBoard.STATE_WIN_PIG:  
				mSoundPig[GameCharacter.STATUS_WIN].start();
				break; 
			default:	
		}
	}

	/**
	 * Change the size of the board
	 * 
	 * @param newsize
	 *            Number of squares per size. Ej.: 8
	 */
	public void changeBoardSize(int newsize) {
		BOARD_SIZE = newsize;
		HALF_BOARD_SIZE = (int) Math.abs(BOARD_SIZE / 2);
		sizeBoard = sizeXY * BOARD_SIZE;
		mBirds = new Bird[HALF_BOARD_SIZE];
	}

	protected void createBird(int index, int x, int y) {
		mBirds[index] = new Bird(x, y);
	}

	protected void createPiggie(int x, int y) {
		mPiggy = new Piggie(x, y);
	}
	
	protected void createEgg(int x, int y){
		mEgg = new Egg(x, y);
	}

	/**
	 * Paints the players upon their positions
	 * 
	 * @param canvas
	 *            Instance of the canvas. Generally used in
	 *            {@link #onDraw(Canvas)}
	 */
	public void paintPlayer(Canvas canvas) {
		mEgg.onPaint(canvas);
		for (Bird b : mBirds) {
			b.onPaint(canvas);
		}
		mPiggy.onPaint(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled())
			return false; 
		if (event.getAction()==MotionEvent.ACTION_DOWN) { 
			int x = (int) event.getX(); // Real pixels
			int y = (int) event.getY();
			x = (int) Math.abs((x - mOffetX) / sizeXY);
			y = (y - mOffetY) / sizeXY; 
			caracter_selected = ctrlGame.getCharacterAt(x, y);
		}
		if(caracter_selected!=null){ 
			return caracter_selected.onTouchEvent(event); // Let the object handle it self;
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		int sx = w / BOARD_SIZE;
		int sy = h / BOARD_SIZE;
		sizeXY = sx < sy ? sx : sy; // Like its rectangular choose the shortest
		sizeBoard = sizeXY * BOARD_SIZE;
		mOffetX = (w - BOARD_SIZE * sizeXY) / 2;
		mOffetY = (h - BOARD_SIZE * sizeXY) / 2; 
	}
 
	
	/**
	 * Move some character of the board
	 * @param move
	 */
	public void makeMove(GameMove move) {
		GameCharacter gc;
		if (move.player == GameBoard.PLAYER_PIGGY) {
			gc = mPiggy;	//The Pig
		} else { // If its no the Pig..
			gc = mBirds[ctrlGame.getIndex(move.from.x, move.from.y)]; //is its sister (A Bird)
		}
		gc.moveTo(move); 	// Moves it in the map;
		refresh();
	}


	
	/**
	 * How hard the Computer AI will play against you
	 * @return int Count of steps the Computer can predict (1-7)
	 */
	public int getAiLevel(){
		return Integer.valueOf(settings.getString("level", "3"));
	}
	
	/**
	 * Human player may move any side of the board?
	 * @return true | false If you moves any side of the board the PC will try to moves the other
	 */
	public boolean anyOnCanPlay(){
		return settings.getBoolean("anyone_plays", false); 
	}

	/**
	 * Let play music and sound effects?
	 * @return
	 */
	public boolean allowSounds(){
		return settings.getBoolean("allow_sounds", true);
	}	
	
	/**
	 * Measure of each size of the square
	 * @return
	 */
	public int getBoardSize() { 
		return BOARD_SIZE;
	} 
	
	/**
	 * Class ment to manage some Logic and Graphic for every character on the
	 * GameBoardView
	 * Implements {@link OnGestureListener} interface
	 * @author EliuX
	 * 
	 */
	public class GameCharacter implements OnGestureListener, Runnable{ 
		// Constants for animations
		final static int STATUS_NORMAL = 0; 	// Its the estandard
		final static int STATUS_ON_MOVE = 1;
		final static int STATUS_WIN = 2; 		// When it has lost 
		final static int STATUS_ON_ANIMATION = 3; 	// Its the estandard
		final static int ANIM_COUNT = 4;
		// Which is the active painting to show
		int active_draw = STATUS_NORMAL;
		// Resources for animations
		protected Drawable[] draws;
		int status = STATUS_NORMAL;  
		// Coordinates
		Position active_pos = new Position(-1, -1); 
		// Temporary coordinates
		Position moving_pos = new Position(-1, -1);  
		// Current position
		Rect currentPosition; 

		public GameCharacter(int x, int y) { 
			Position from = new Position(active_pos.x, active_pos.y);
			Position to = new Position(x, y);
			GameMove move = new GameMove(this.getCharacterId(), from, to);
			moveTo(move);
		}

		public GameCharacter() {}

		/**
		 * Whats the picture to show now?
		 * 
		 * @param newdraw
		 *            Index of the Picture to paint
		 */
		public void changeActiveDraw(int newdraw) {
			active_draw = newdraw; 
		}

		/**
		 * Draw the caracter in a determinated position
		 * 
		 * @param canvas
		 *            Instance of Canvas
		 */
		public void onPaint(Canvas canvas) {
			if (active_pos.x < 0 || active_pos.y < 0)
				return;
			repaintPosition();
			if (this.status == STATUS_ON_MOVE) { // If its on move, lets paint the posible places
				paintValidPositions(canvas);
			}
			if (draws[active_draw] != null) {
				draws[active_draw].draw(canvas); // Paint this character
			}
		}
		
		/**
		 * Paints where the player should go
		 * @param canvas Instance of Canvas
		 */
		protected void paintValidPositions(Canvas canvas){ 
			ArrayList<Position> list = ctrlGame.board.getValidPositions(getCharacterId(),active_pos);
			int xtop = -1, ytop = -1;
			for (Position position : list) {	//Paint each position 
				xtop = (int) (mOffetX + position.x * sizeXY);
				ytop = (int) (mOffetY + position.y * sizeXY); 
				canvas.drawRect(xtop, ytop, xtop + sizeXY, ytop + sizeXY, layerFloor); 
			}
		}

		/**
		 * Move this character to the given position. Its donde by humans only
		 * 
		 * @param x
		 *            X position
		 * @param y
		 *            Y position
		 * @return true | false TRUE if the position was right and it moved
		 */
		public boolean moveTo(GameMove move) {  
			if (ctrlGame == null || ctrlGame.makeMove(move))	//If the controller let me
			{
				active_pos.x = move.to.x;
				active_pos.y = move.to.y;
				this.playSound(STATUS_NORMAL);
				return true; 
			}
			return false;
		}

		/**
		 * Reset where i am going to be drawn
		 */
		protected void repaintPosition() {
			int xtop, ytop;
			if (this.status == STATUS_ON_MOVE) {  
				xtop = (int) moving_pos.x-sizeXY/2;
				ytop = (int) moving_pos.y-sizeXY/2;
			} else {
				xtop = mOffetX + active_pos.x * sizeXY;
				ytop = mOffetY + active_pos.y * sizeXY;
			}
			currentPosition = new Rect(xtop, ytop, xtop + sizeXY - 1, ytop
					+ sizeXY - 1);
			draws[active_draw].setBounds(currentPosition); // Where am I?
		}
		 
		/**
		 * Can I move to this position
		 * @param x Position X axis
		 * @param y Position Y axis
		 * @return true | false TRUE if the position is valid
		 */
		public boolean isValidPosition(int x, int y){
			Position from = new Position(active_pos.x, active_pos.y);
			Position to = new Position(x, y);
			GameMove move = new GameMove(this.getCharacterId(), from, to); 
			return ctrlGame.board.validateGameMove(move);
		}
		 
		/**
		 * Am I a player, can I interact?
		 */
		@Override
		public boolean isHumanPlayer() {
				int state = ctrlGame.getGame_state();
				return ((anyOnCanPlay() || (ctrlGame.getHuman_player_id() == this.getCharacterId()) || (state == GameBoard.STATE_NONE))
						&& (state != GameBoard.STATE_WIN_PIG && state != GameBoard.STATE_WIN_BIRDS));	
		}

		@Override
		public boolean onTouchEvent(MotionEvent ev) { 
			this.status = STATUS_NORMAL; 
			if (!isHumanPlayer())		//FOR HUMANS PLAYED CHARACTERS ONLY!	
				return false;
			int action = ev.getAction();
			switch (action) {
				case MotionEvent.ACTION_DOWN: // If its selected
					changeActiveDraw(STATUS_ON_MOVE); 
					this.playSound(STATUS_ON_MOVE);
					break;
				case MotionEvent.ACTION_MOVE: 
					this.status = STATUS_ON_MOVE; 
					moving_pos.x = (int) ev.getX();
					moving_pos.y = (int) ev.getY();
					break;
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					caracter_selected = null;
					run();							//Keep running animations
					changeActiveDraw(STATUS_NORMAL);
					this.status = STATUS_NORMAL;
					moving_pos.x = (int) ev.getX();
					moving_pos.y = (int) ev.getY();
					int wantedX = (int) Math.abs((moving_pos.x- mOffetX) / sizeXY);	//Where would you like yo move?
					int wantedY = (int) ((moving_pos.y - mOffetY) / sizeXY); 

					Position from = new Position(active_pos.x, active_pos.y);
					Position to = new Position(wantedX, wantedY);
					GameMove move = new GameMove(this.getCharacterId(), from, to);
					move.isHuman = true;
					this.moveTo(move); 		//Try to do the move
					break;
				default:
			}
			refresh();
			return true;
		}

		@Override
		public void run() {}

		/**
		 * Plays a sound acording to the animation
		 * @param anim
		 */
		public void playSound(int anim){ 
			MediaPlayer[] mSounds = getSounds();
			if(mSounds!=null && mSounds[anim]!=null && allowSounds())
				mSounds[anim].start();
		} 	 
		
		/**
		 * Obtains an Id like a GameBoard item
		 * @return	an integer like PLAYER_BIRD
		 */
		public int getCharacterId(){
			return GameBoard.BOARD_EMPTY;	//Nobody
		}


		/**
		 * Get the MediaPlayer array used for animations animation 
		 * @return An array of MediaPlayer(MediaPlayer[])
		 */
		public MediaPlayer[] getSounds(){
			return null;					//Plays nothing
		}
	}

	public class Piggie extends GameCharacter {
		{
			this.draws = mDrawPiggie;
		}

		public Piggie(int x, int y) {
			super(x, y); 
		}

		public Piggie() { }

		@Override
		public int getCharacterId() { 
			return GameBoard.PLAYER_PIGGY;
		}
 

		@Override
		public void run() {
			 if(status == STATUS_NORMAL){						//If now its normal
				 status = active_draw = STATUS_ON_ANIMATION;  	//Animate it
				 GameBoardView.this.postDelayed(this, 200);	
			 }else if(status == STATUS_ON_ANIMATION){			//If its animating
				 status = active_draw = STATUS_NORMAL;
				 GameBoardView.this.postDelayed(this, 1000);
			 }
			 refresh();
		}

		@Override
		public MediaPlayer[] getSounds() { 
			return mSoundPig;
		}
	}

	public class Bird extends GameCharacter {
		{
			this.draws = mDrawBirds;
		}

		public Bird(int x, int y) {
			super(x, y);
		}

		public Bird() {
			super();
		}

		@Override
		public int getCharacterId() { 
			return GameBoard.PLAYER_BIRD;
		} 

		@Override
		public MediaPlayer[] getSounds() { 
			return mSoundBirds;
		}
		
	}
	
	public class Egg extends GameCharacter{ 
		{
			this.draws = mDrawEgg;
		}
		
		public Egg(int x, int y) {
			super(x, y);
		}  
	}

	/**
	 * For the players that should move
	 */
	public interface OnGestureListener {
		/**
		 * What to do when this object is moved?
		 * 
		 * @param event
		 *            Instance of MotionEvent of the View
		 * @return TRUE | FALSE @see {@link OnTouchListener}
		 */
		public boolean onTouchEvent(MotionEvent event);

		/**
		 * Am i a human player, should this be moved?
		 * 
		 * @return
		 */
		public boolean isHumanPlayer();
	}
}
