package com.eliux.catchthepig;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {
	//Activities handlers
	static final public int SETTINGS_ACTIVITY = 0;
	static final public int GAME_ACTIVITY = 1;
	//Sounds'ids
	static final protected int MAIN_SONG = 1;
	static final protected int MAIN_BTN_CLICKED = 2;
	// Intents of other windows
	Intent iGame, iPreferences; 
	//Buttons
	Button btnSingleGame, btnPreferences, btnExit;	
	//Sounds
	MediaPlayer[] mMainSound = new MediaPlayer[2]; 	//Having supposed it may be more than one song
	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);  
		mMainSound[MAIN_SONG] = MediaPlayer.create(MainActivity.this, R.raw.main_song); 
		mMainSound[MAIN_SONG].setAudioStreamType(AudioManager.STREAM_MUSIC);
		mMainSound[MAIN_SONG].setLooping(true);   
		setContentView(R.layout.activity_main);
		btnSingleGame = (Button) findViewById(R.id.BtnSingleGame);
		btnPreferences = (Button) findViewById(R.id.BtnPreferences); 
		btnExit = (Button) findViewById(R.id.btnExit); 
		btnSingleGame.setOnClickListener(this);
		btnPreferences.setOnClickListener(this);
		btnExit.setOnClickListener(this);   
	} 
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) { 
		super.onWindowFocusChanged(hasFocus);
		if(hasFocus){
			if(!mMainSound[MAIN_SONG].isPlaying())
			{  
				mMainSound[MAIN_SONG].start();
			}
		}else{ 
			mMainSound[MAIN_SONG].pause();
		}
	}  
	
	@Override
	protected void onSaveInstanceState(Bundle outState) { 
		super.onSaveInstanceState(outState); 
		for (MediaPlayer media : mMainSound) {	//Release all the Media
			if(media!=null){
				media.pause(); 
			}
		}
	}
  
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		showConfiguration();
		return super.onCreateOptionsMenu(menu);
	} 

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.BtnSingleGame:
			if (iGame == null) {
				iGame = new Intent();
				iGame.setClass(MainActivity.this, GameActivity.class);
			}
			startActivityForResult(iGame, GAME_ACTIVITY);
			break;
		case R.id.BtnPreferences:
			showConfiguration();	//Shows the configuration's option
			break;
		case R.id.btnExit:  
			finish();				//	Closes the app!
			break;
		default:
		} 
	}
	
	/**
	 * Shows the configuration's panel
	 */
	void showConfiguration(){
		if (iPreferences == null) {
			iPreferences = new Intent();
			iPreferences.setClass(MainActivity.this, MyPreferences.class);
		}
		startActivityForResult(iPreferences, SETTINGS_ACTIVITY);
	} 
	 
}
