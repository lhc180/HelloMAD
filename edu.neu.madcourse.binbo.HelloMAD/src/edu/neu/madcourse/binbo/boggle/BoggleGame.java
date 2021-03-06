package edu.neu.madcourse.binbo.boggle;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import edu.neu.madcourse.binbo.R;


public class BoggleGame extends Activity implements IBoggleGame, OnClickListener, OnTouchListener {
	private static final String TAG = "Boggle";	
	private static final String BOGGLE_PUZZLE = "puzzle";
	private static final String BOGGLE_TIME = "time_left";
	private static final String BOGGLE_GAME_PAUSED = "game_paused";
	private static final String BOGGLE_GAME_OVER = "game_over";
	private static final String BOGGLE_GAME_SCORE = "game_score";
	private static final String BOGGLE_GAME_BEST_SCORE = "game_best_score";
	private static final String BOGGLE_WORDS = "words";
	private static final String BOGGLE_WORDS_SIZE = "words_size";
	private static final int DEFAULT_GAME_TIME = 179;
	private static final int ACC_DETECTION_ACCURACY = 13;
	public static final String KEY_COMMAND = "edu.neu.madcourse.binbo.boggle.command";
	public static final int NEW_GAME = 0;
	public static final int CONTINUE = 1;	
	public static final int LETTER_COUNT = 16;	
	
	private NativeDictionary dict = null;
	private BogglePuzzle mPuzzle = null;
	private BogglePuzzleView puzzleView;
	private SensorManager sm = null;
	private final String high_frequency[] = { 
		"a", "e", "i", "l", "n", "o", "r", "s", "t"
	};
	
	private final float GOLDEN_DIVIDE = 0.618f;
	
	private int game_time = DEFAULT_GAME_TIME;
	private boolean paused = false;
	private boolean game_over = false;
	private TextView timeView  = null;	
	private TextView bestView  = null;
	private TextView scoreView = null;
	private Button pauseButton = null;
	private Button shakeButton = null;
	private ListView listView  = null;
	
	private int game_score = 0; 
	private int game_best_score = 0;	
	private List<String> wordsFound = new ArrayList<String>();
	
	private ToneGenerator tonePlayer = null; 
	private int defTextColor = Color.WHITE;
	
	Handler  colorHandler  = new Handler();
	Runnable colorRunnable = new Runnable() {
		public void run() {
			int curColor = timeView.getCurrentTextColor(); 
			
			timeView.setTextColor(
				(curColor == defTextColor ? Color.RED : defTextColor));
			colorHandler.postDelayed(this, 500);
		}
	};
	
	Handler  textHandler  = new Handler();
	Runnable textRunnable = new Runnable() {
	    public void run() {	        
	    	// format and set the time
	    	game_time--;
	    	String minute = "" + game_time / 60;
	    	String second = "" + game_time % 60;
	    	if (second.length() == 1) {
	    		second = "0" + second;
	    	}
	    	
	    	if (game_time <= 20) {
	    		if (game_time == 20) {
	    			colorHandler.postDelayed(colorRunnable, 500);
	    		}
	    		if (tonePlayer != null) {
	    			tonePlayer.startTone(ToneGenerator.TONE_DTMF_5, 100);
	    		}	    		
	    	} 
	    	timeView.setText(minute + ":" + second);	    	
	    	
	    	if (game_time > 0) { 
	    		textHandler.postDelayed(this, 1000);
	    	} else {	    	
	    		timeView.setTextColor(defTextColor);
	    		doGameOver();
	    	}
	    }
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		int command = getIntent().getIntExtra(KEY_COMMAND, NEW_GAME);
		makePuzzle(command);
		
		loadPreferences(command);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN, 
			WindowManager.LayoutParams.FLAG_FULLSCREEN
		);
		setContentView(R.layout.boggle_game);					
        
		// get views and set listeners
		initViews();

		// adjust layouts according to the screen resolution
		DisplayMetrics dm = new DisplayMetrics();  
        Display display = getWindowManager().getDefaultDisplay(); 		
        display.getMetrics(dm);
		if (dm.heightPixels >= dm.widthPixels) {
			adjustPortraitLayout(dm.widthPixels, dm.heightPixels);
		} else if (dm.widthPixels > dm.heightPixels) {
			adjustLandscapeLayout(dm.widthPixels, dm.heightPixels);
		}		
		
		loadDictionaries();
 
 		// If the activity is restarted, do a continue next time
 		getIntent().putExtra(KEY_COMMAND, CONTINUE); 	
 		tonePlayer = new ToneGenerator(AudioManager.STREAM_MUSIC, 70); 
 		
 		// create the boggle music
 		BoggleMusic.create(this, R.raw.boggle_game);
 		if (command == NEW_GAME) {
 			BoggleMusic.reset();
 		}
 		
 		// create accelerometer
 		createSensor();
	}
	
	@Override
	protected void onDestroy() {
		BoggleMusic.stop();
		// TODO Auto-generated method stub
		super.onDestroy();		
	}
	
	private void loadPreferences(int command) {
		if (command == CONTINUE) {
			game_time = getPreferences(MODE_PRIVATE).getInt(BOGGLE_TIME, DEFAULT_GAME_TIME);
			game_score = getPreferences(MODE_PRIVATE).getInt(BOGGLE_GAME_SCORE, 0);
			paused = getPreferences(MODE_PRIVATE).getBoolean(BOGGLE_GAME_PAUSED, false);
			game_over = getPreferences(MODE_PRIVATE).getBoolean(BOGGLE_GAME_OVER, false);
			game_over = (game_time > 0) ? false : true;
			int size = getPreferences(MODE_PRIVATE).getInt(BOGGLE_WORDS_SIZE, 0);
			for (int i = 0; i < size; ++i) {								
				String word = getPreferences(MODE_PRIVATE).getString(BOGGLE_WORDS + i, "");
				wordsFound.add(word);
			}	
		} else if (command == NEW_GAME) {
			game_time = DEFAULT_GAME_TIME;
			game_score = 0;			
			paused = false;
			game_over = false;
			wordsFound.clear();
		}
	
		game_best_score = getPreferences(MODE_PRIVATE).getInt(BOGGLE_GAME_BEST_SCORE, 0);
	}
	
	private void savePreferences() {
		// Save the time left
		getPreferences(MODE_PRIVATE).edit().putInt(BOGGLE_TIME, game_time).commit();
		// Save the game score
		getPreferences(MODE_PRIVATE).edit().putInt(BOGGLE_GAME_SCORE, game_score).commit();
		// Save the game best score
		getPreferences(MODE_PRIVATE).edit().putInt(BOGGLE_GAME_BEST_SCORE, game_best_score).commit();
		// Save the game state
		getPreferences(MODE_PRIVATE).edit().putBoolean(BOGGLE_GAME_PAUSED, paused).commit();
		// Save the words list BOGGLE_WORDS_SIZE
		getPreferences(MODE_PRIVATE).edit().putInt(BOGGLE_WORDS_SIZE, wordsFound.size()).commit();
		for (int i = 0; i < wordsFound.size(); ++i) {
			String word = wordsFound.get(i);
			getPreferences(MODE_PRIVATE).edit().putString(BOGGLE_WORDS + i, word).commit();
		}			
		// Save the current puzzle
		String puzzleToSave = "";
		char[] puzzle = mPuzzle.getPuzzle();
		for (int i = 0; i < puzzle.length; ++i) {
			puzzleToSave += puzzle[i];
		}		
		getPreferences(MODE_PRIVATE).edit().putString(BOGGLE_PUZZLE, puzzleToSave).commit();
	}
	
	private void initViews() {
		// get views
		timeView  = (TextView)findViewById(R.id.textViewTime);
		bestView  = (TextView)findViewById(R.id.textViewBestScore);
		scoreView = (TextView)findViewById(R.id.textViewScore);
		listView  = (ListView)findViewById(R.id.listView);
		shakeButton = (Button)findViewById(R.id.boggle_shake_button); 		
 		pauseButton = (Button)findViewById(R.id.boggle_pause_button);
 		// register events handler
 	 	shakeButton.setOnTouchListener(this);
 		pauseButton.setOnClickListener(this);
 		// get current text color of timeView
 		defTextColor = timeView.getCurrentTextColor();
	}

	private void loadDictionaries() {
		// load dictionaries of high frequency
		dict = new NativeDictionary(getAssets());
        for (int i = 0; i < high_frequency.length; ++i) {        	
        	// use ".mpg" to make sure that android won't consider the files in the apk
        	// as the compressed files, or it won't be available to get a valid native fd.
        	// these files have been compressed to huffman coding.
        	// due to the singleton pattern used in NDK code, if the corresponding wordlist
        	// has been loaded before, it won't be loaded again when the activity is created again.
        	String dictName = "" + high_frequency[i];
        	if (!dict.isLoaded(dictName)) {
        		dict.load("wordlist_" + dictName + ".mpg", dictName, getAssets());
        	}
        } 
	}
	
	private void adjustPortraitLayout(int width, int height) {
		// adjust the layout according to the screen resolution
		LinearLayout main = (LinearLayout)findViewById(R.id.linearLayoutRoot);
		List<Point> selList = null;
		if (puzzleView != null) {
			selList = puzzleView.mSelList;			
			puzzleView = new BogglePuzzleView(this, mPuzzle);
			puzzleView.mSelList = selList;
		} else {
			puzzleView = new BogglePuzzleView(this, mPuzzle);
		}		
		main.addView(puzzleView);				
      
        LayoutParams laParams = null;
        RelativeLayout rl = (RelativeLayout)findViewById(R.id.relativeLayout);
        laParams = rl.getLayoutParams();
        laParams.height = height - width;
        rl.setLayoutParams(laParams);
        
        LinearLayout llLog = (LinearLayout)findViewById(R.id.linearLayoutLog);
        laParams = llLog.getLayoutParams();
        laParams.width = (int)(width * GOLDEN_DIVIDE);
        llLog.setLayoutParams(laParams);
        
        LinearLayout llTime = (LinearLayout)findViewById(R.id.linearLayoutTime);
        laParams = llTime.getLayoutParams();
        laParams.width = (int)(width * (1 - GOLDEN_DIVIDE));
        llTime.setLayoutParams(laParams);
	}
	
	private void adjustLandscapeLayout(int width, int height) {
		// adjust the layout according to the screen resolution
		LinearLayout main = (LinearLayout)findViewById(R.id.linearLayoutRoot);
		List<Point> selList = null;
		if (puzzleView != null) {
			selList = puzzleView.mSelList;
			puzzleView = new BogglePuzzleView(this, mPuzzle);
			puzzleView.mSelList = selList;
		} else {
			puzzleView = new BogglePuzzleView(this, mPuzzle);
		}	
		main.addView(puzzleView);				
      
        LayoutParams laParams = null;
        RelativeLayout rl = (RelativeLayout)findViewById(R.id.relativeLayout);
        laParams = rl.getLayoutParams();
        laParams.width = width - height;
        rl.setLayoutParams(laParams);
        
        LinearLayout llTime = (LinearLayout)findViewById(R.id.linearLayoutTime);
        laParams = llTime.getLayoutParams();
        laParams.height = (int)(height * (1 - GOLDEN_DIVIDE - 0.1));
        llTime.setLayoutParams(laParams);
        
        LinearLayout llButtons = (LinearLayout)findViewById(R.id.linearLayoutButtons);
        laParams = llButtons.getLayoutParams();
        laParams.width = (int)((width - height) * (1 - GOLDEN_DIVIDE));
        llButtons.setLayoutParams(laParams);
        
        LinearLayout llLog = (LinearLayout)findViewById(R.id.linearLayoutLog);
        laParams = llLog.getLayoutParams();
        laParams.height = (int)(height * (GOLDEN_DIVIDE + 0.1));
        llLog.setLayoutParams(laParams);
	}
	
	private void createSensor() {
		// get system sensor manager to deal with sensor issues  
        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);                              
	}

	/* 
     * SensorEventListener implement
     * method1: onSensorChanged 
     * method2: onAccuracyChanged 
     * */  
    final SensorEventListener boggleAccelerometerListener = new SensorEventListener() {  
           
        public void onSensorChanged(SensorEvent sensorEvent){  
            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){  
                Log.i(TAG, "onSensorChanged");  
 
                float X_lateral = sensorEvent.values[0];  
                float Y_longitudinal = sensorEvent.values[1];  
                float Z_vertical = sensorEvent.values[2];                
                //Log.i(TAG,"\n heading " + X_lateral);  
                //Log.i(TAG,"\n pitch " + Y_longitudinal);  
                //Log.i(TAG,"\n roll " + Z_vertical);
                double distance = Math.sqrt(X_lateral * X_lateral + 
          			  					    Y_longitudinal * Y_longitudinal + 
          			  					    Z_vertical * Z_vertical);
                if (distance >= ACC_DETECTION_ACCURACY) {
                	rotatePuzzle();
                }
            }  
        }  
 
        public void onAccuracyChanged(Sensor sensor, int accuracy){  
            Log.i(TAG, "onAccuracyChanged");  
        }  
    };  

	private void updateViews() {
		// update text views
 		scoreView.setText("score: " + game_score);
 		bestView.setText("best: " + game_best_score);
 		String minute = "" + game_time / 60;
    	String second = "" + game_time % 60;
    	if (second.length() == 1) {
    		second = "0" + second;
    	}
    	timeView.setText(minute + ":" + second);
    	// fill list view
        listView.setAdapter(
		 	new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, wordsFound)
		); 
        
        if (game_over) {
        	pauseButton.setEnabled(false);
    		shakeButton.setEnabled(false);
        }
	}
	
	public void updateGameViews() {
		// do nothing
	}	
	
	@Override
	protected void onResume() {
		super.onResume();							
		
		if (game_over) {
			doGameOver();
		} else {
			pauseGame(paused);
		}
		
		updateViews();   
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");		
		savePreferences();		
		pauseGame(true);
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.boggle_shake_button:			
			break;		
		case R.id.boggle_pause_button:
			pauseGame(!paused);			
			break;
		}
	}
	
	public boolean onTouch(View v, MotionEvent event) {  
        if (v.getId() == R.id.boggle_shake_button) { 
        	
            if (event.getAction() == MotionEvent.ACTION_DOWN){  
                Log.d(TAG, "shake button ---> down");  
                if (sm != null) {
                	int sensorType = Sensor.TYPE_ACCELEROMETER;
                	sm.registerListener(
                		boggleAccelerometerListener,
                		sm.getDefaultSensor(sensorType),
                		SensorManager.SENSOR_DELAY_NORMAL
                	);                 	
                	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
                }
            }   
            if (event.getAction() == MotionEvent.ACTION_UP){  
                Log.d(TAG, "shake button ---> up");  
                if (sm != null) {
                	sm.unregisterListener(boggleAccelerometerListener);
                	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);             
                }
            }  
        }  
        return false;  
    }  
	
	private void doGameOver() {
		game_over = true;		
		textHandler.removeCallbacks(textRunnable);
		colorHandler.removeCallbacks(colorRunnable);
		pauseButton.setEnabled(false);
		shakeButton.setEnabled(false);
		BoggleMusic.play(); // play the music whatever
		puzzleView.invalidate();		
	}

	private void pauseGame(boolean paused) {
		this.paused = paused;
		if (paused) {
			pauseButton.setText("Resume");
			shakeButton.setEnabled(false);
			textHandler.removeCallbacks(textRunnable);
			colorHandler.removeCallbacks(colorRunnable);
			BoggleMusic.pause();
		} else {
			pauseButton.setText("Pause");
			shakeButton.setEnabled(true);
			textHandler.postDelayed(textRunnable, 1000);
			if (game_time <= 20) {
				colorHandler.postDelayed(colorRunnable, 500);
			}
			BoggleMusic.play();
		}
		this.puzzleView.invalidate();
	}
	
	public boolean isGamePaused() {
		return paused;
	}
	
	public boolean isGameOver() {
		return game_over;
	}
	
	public void playClickSound() {
		if (tonePlayer != null) {
			tonePlayer.startTone(ToneGenerator.TONE_DTMF_2, 100);
		}
	}
	
	/** Come up with a new puzzle */
	private void makePuzzle(int command) {		
		StringBuffer sf = new StringBuffer();
		
		if (command == NEW_GAME) {
			mPuzzle = new BogglePuzzle(this, 4);			
		} else if (command == CONTINUE) {
			String defaultPuzzle = "ABCDEFGHIJKLMNOP";
			String savedPuzzle = getPreferences(MODE_PRIVATE).getString(BOGGLE_PUZZLE, defaultPuzzle);			
			mPuzzle = new BogglePuzzle(this, savedPuzzle.toCharArray());
		}		
	}
	
	private void rotatePuzzle() {
		mPuzzle.rotatePuzzle();
		puzzleView.rotatePuzzle();		
	}
	
	public boolean lookUpWord(String word) {
		String dictName = "" + word.charAt(0);
		
		if (!dict.isLoaded(dictName)) {
			dict.load("wordlist_" + dictName + ".mpg", dictName, getAssets());			
		} 
		
		if (wordsFound.contains(word)) {
			Toast.makeText(this, "Oops! Repeated!", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		boolean found = dict.lookupWord(word);		
		if (found) {
			wordsFound.add(word);
			listView.setAdapter(
	        	new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, wordsFound)
	        );			
			int bonus = measureBonus(word);
			game_score += bonus;
			if (game_best_score < game_score) {
				game_best_score = game_score;
			}
			scoreView.setText("score: " + game_score);
			bestView.setText("best: " + game_best_score);
			String toastText = "";
			if (bonus <= 1) {
				toastText = "Good! +";
			} else if (bonus > 1 && bonus <= 4) {
				toastText = "Great! +";
			} else {
				toastText = "Excellent! +";
			}
			// special sound
			if (tonePlayer != null) {
    			tonePlayer.startTone(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_NORMAL, 100);
    		}
			Toast.makeText(this, toastText + bonus, Toast.LENGTH_SHORT).show();
		}
		
		return found;
	}
	
	private int measureBonus(String wordFound) {
		int bonus  = 0;
		int length = wordFound.length();
		
		switch (length) {
		case 3: bonus = 1; break;
		case 4: bonus = 2; break;
		case 5: bonus = 4; break;
		case 6: bonus = 6; break;
		default: bonus = 10; break;
		}
		
		return bonus;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
				
		DisplayMetrics dm = new DisplayMetrics();  
        Display display = getWindowManager().getDefaultDisplay(); 		
        display.getMetrics(dm);

		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
			setContentView(R.layout.boggle_game);
			adjustLandscapeLayout(dm.widthPixels, dm.heightPixels);						
		} else {		  
			setContentView(R.layout.boggle_game);
			adjustPortraitLayout(dm.widthPixels, dm.heightPixels);
		}
		
		initViews();
		updateViews();
		puzzleView.invalidate();

		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}
	
}
