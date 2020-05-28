
package com.eliux.catchthepig;

import com.eliux.catchthepig.single.GameBoardView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

/**
 * Copyleft, for educational purposes only
 * @author EliuX, 2013
 * 
 */
public class GameActivity extends Activity{
	/** Resources **/
	SharedPreferences settings;
	GameBoardView mGView;
	LinearLayout layout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);  
		layout = new LinearLayout(this);
		if(savedInstanceState==null){ 
			mGView = new GameBoardView(this, null); 
		}else{
			//mGView = savedInstanceState.getParcelable("game_view");    
			mGView = (GameBoardView) getLastNonConfigurationInstance(); 
		}
		layout.addView(mGView);
		setContentView(layout);  
		settings = PreferenceManager.getDefaultSharedPreferences(this); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) { 
		getMenuInflater().inflate(R.menu.game, menu); 
		return true;
	} 


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.new_game) {
			mGView.loadGame(); 
		} else if (item.getItemId() == R.id.action_settings) {
			Intent intent = new Intent(this, MyPreferences.class);
			startActivity(intent);
		} 
		return super.onOptionsItemSelected(item);
	}  
	
	@Override
	protected void onRestart() { 
		if(layout!=null){			//If came back to the front
			layout.addView(mGView);
		}
		layout.invalidate();
		super.onRestart(); 
	}
	
	/*@Override
	protected void onSaveInstanceState(Bundle b) {
		b.putParcelable("game_view", mGView); 
		layout.removeView(mGView); 
		super.onSaveInstanceState(b);
	} */ 
	
	@Override
	public Object onRetainNonConfigurationInstance() { 
		layout.removeView(mGView); 
		return mGView;
	} 
}
 
