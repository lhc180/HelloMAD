package edu.neu.madcourse.binbo.rocketrush;

import java.util.List;
import java.util.Random;


public class GameEngine {
	// singleton game engine 
    protected static GameEngine sEngine = null;
    // engine speed
    public static final int ENGINE_SPEED = 30;
	// game states, only one state is available at the exactly time
	protected int mState = STATE_STOP;
	public static final int STATE_STOP    = 0;
	public static final int STATE_PREPAIR = 1;
    public static final int STATE_PLAY    = 2;
    public static final int STATE_PAUSE   = 3;
    public static final int STATE_WIN     = 4;
    public static final int STATE_LOSE    = 5;    
    // flag to specify whether the initialization is done
    protected boolean mInitialized = false;
    // generate game elements according to the scene configuration
    private Random mRandom = new Random();

    protected GameEngine() {}
    
    public static GameEngine getInstance() {
    	if (sEngine == null) {
    		sEngine = new GameEngine();
    	}
    	return sEngine;    	
    }
    
    public void initialize() {
    	if (mInitialized) {
    		return;
    	}
    	
    	// do something here
    	
    	// update the flag to avoid re-initialize
    	mInitialized = true;
    }

    public void updateGameScene(GameScene scene) {
    	// do the real job here
    	List<GameObject> objects = scene.getGameObjects();
    	for (int i = 0; i < objects.size(); ++i) {
    		objects.get(i).update();
    	}
    }
}