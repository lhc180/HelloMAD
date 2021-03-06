package edu.neu.madcourse.binbo.rocketrush;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Message;
import android.os.Vibrator;
import edu.neu.madcourse.binbo.rocketrush.gameobjects.Alient;
import edu.neu.madcourse.binbo.rocketrush.gameobjects.Asteroid;
import edu.neu.madcourse.binbo.rocketrush.gameobjects.BackgroundFar;
import edu.neu.madcourse.binbo.rocketrush.gameobjects.BackgroundNear;
import edu.neu.madcourse.binbo.rocketrush.gameobjects.Barrier;
import edu.neu.madcourse.binbo.rocketrush.gameobjects.Bird;
import edu.neu.madcourse.binbo.rocketrush.gameobjects.Curtain;
import edu.neu.madcourse.binbo.rocketrush.gameobjects.Curtain.OnCurtainEventListener;
import edu.neu.madcourse.binbo.rocketrush.gameobjects.Field;
import edu.neu.madcourse.binbo.rocketrush.gameobjects.Level;
import edu.neu.madcourse.binbo.rocketrush.gameobjects.LifeBar;
import edu.neu.madcourse.binbo.rocketrush.gameobjects.LifeBar.OnLifeChangedListener;
import edu.neu.madcourse.binbo.rocketrush.gameobjects.Odometer;
import edu.neu.madcourse.binbo.rocketrush.gameobjects.Odometer.OnOdometerUpdateListener;
import edu.neu.madcourse.binbo.rocketrush.gameobjects.Rocket;
import edu.neu.madcourse.binbo.rocketrush.gameobjects.SpeedBar;
import edu.neu.madcourse.binbo.rocketrush.gameobjects.Thunder;
import edu.neu.madcourse.binbo.rocketrush.gameobjects.TimeBonus;
import edu.neu.madcourse.binbo.rocketrush.gameobjects.TimeBonus.OnGotTimeBonusListener;
import edu.neu.madcourse.binbo.rocketrush.gameobjects.Timer;
import edu.neu.madcourse.binbo.rocketrush.gameobjects.Timer.OnTimeUpdateListener;
import edu.neu.madcourse.binbo.rocketrush.gameobjects.TrickyAlient;


public class RushScene extends GameScene implements OnOdometerUpdateListener, 
										 			OnLifeChangedListener,
										 			OnTimeUpdateListener,
										 			OnGotTimeBonusListener,
										 			OnCurtainEventListener {	

	private Rocket   mRocket   = null;
	private LifeBar  mLifeBar  = null;
	private SpeedBar mSpeedBar = null;
	private BackgroundFar  mBackgroundFar  = null;
	private BackgroundNear mBackgroundNear = null;
	private Level 	 mLevel    = null;	
	private Odometer mOdometer = null;
	private Timer 	 mTimer    = null;
	private Curtain  mCurtain  = null;
	private int mCurLevel = 1;
	private int mCurLoop  = 1;
	private Random  mRandom  = new Random();
	private Context mContext = null;
	
	public RushScene(Context context) {
		super(context.getResources());
		mContext = context;
	}	
	
	@Override
	public void reset() {
		mCurLevel = 1;
		mCurLoop  = 1; 	
		mProbBird    = 90;
		mProbAster   = 190;
		mProbAlient  = 135;
		mProbThunder = 250;
		release();
		load();
	}

	public List<GameObject> load() {
		// create game objects
		if (mBackgroundFar == null) {
			mBackgroundFar = new BackgroundFar(mContext.getResources());			
			mObjects.add(mBackgroundFar);
		}
		if (mBackgroundNear == null) {
			mBackgroundNear = new BackgroundNear(mContext.getResources());			
			mObjects.add(mBackgroundNear);
		}
		if (mSpeedBar == null) {
			mSpeedBar = new SpeedBar(mRes);
			mObjects.add(mSpeedBar);
		}
		if (mRocket == null) {
			mRocket = new Rocket(mRes);
			mRocket.setOnCollideListener(this);
			mObjects.add(mRocket);
		}
		if (mLevel == null) {
			mLevel = new Level(mRes);
			mObjects.add(mLevel);
		}
		if (mOdometer == null) {
			mOdometer = new Odometer(mRes);
			mOdometer.setOdometerUpdateListener(this);
			mObjects.add(mOdometer);
		}
		if (mLifeBar == null) {
			mLifeBar = new LifeBar(mRes);
			mLifeBar.setOnLifeChangedListener(this);
			mObjects.add(mLifeBar);
		}
		if (mTimer == null) {
			mTimer = new Timer(mRes);
			mTimer.setOnTimeUpdateListener(this);
			mObjects.add(mTimer);
		}
		if (mCurtain == null) {
			mCurtain = new Curtain(mRes);
			mCurtain.setCurtainEventListener(this);
			mObjects.add(mCurtain);
		}
		if (mWidth > 0 || mHeight > 0) {
			for (GameObject obj : mObjects) {
				obj.onSizeChanged(mWidth, mHeight);
			}
		}
		// order by Z
		orderByZ(mObjects);
		
		return mObjects;
	}
	
	public void release() {
		super.release();
		mBackgroundFar  = null;
		mBackgroundNear = null;
		mSpeedBar = null;
		mRocket   = null;
		mLevel    = null;
		mOdometer = null;
		mLifeBar  = null;
		mTimer    = null;
		mCurtain  = null;
	}
	
	public void openInteraction() {
		if (mRocket != null) {
			mRocket.setMovable(true);
			mRocket.setOnCollideListener(this);
		}
		if (mOdometer != null) {
			mOdometer.setOdometerUpdateListener(this);
			mOdometer.setEnable(true);
		}
		if (mLifeBar != null) {
			mLifeBar.setOnLifeChangedListener(this);
			mLifeBar.setEnable(true);
		}
		if (mSpeedBar != null) {
			mSpeedBar.setEnable(true);
		}
		if (mTimer != null) {
			mTimer.setOnTimeUpdateListener(this);
			mTimer.setEnable(true);
		}
	}
	
	public void closeInteraction() {
		if (mRocket != null) {
			mRocket.setMovable(false);
			mRocket.setCollidable(false);
			mRocket.setOnCollideListener(null);
		}
		if (mOdometer != null) {
			mOdometer.setOdometerUpdateListener(null);
			mOdometer.setEnable(false);
		}
		if (mLifeBar != null) {
			mLifeBar.setOnLifeChangedListener(null);
			mLifeBar.setEnable(false);
		}
		if (mSpeedBar != null) {
			mSpeedBar.setEnable(false);
		}
		if (mTimer != null) {
			mTimer.setOnTimeUpdateListener(null);
			mTimer.setEnable(false);
		}
	}
	
	@Override
	public void doDraw(Canvas c) {
		for (GameObject obj : mObjects) {
			obj.doDraw(c);
		}
	}

	@Override
	protected void onSizeChanged(int width, int height) {		
		super.onSizeChanged(width, height);		
	}
	
	@Override
	public void updateBarriers() {
		// surface has not been created
		if (mWidth == 0 || mHeight == 0) { return; }
		// remove invisible barriers
		List<GameObject> invisibles = null;
		for (GameObject b : mBarriers) {
			float x = b.getX(), y = b.getY();
			if (x < -(mWidth >> 2) || x > (mWidth + (mWidth >> 2)) || y > mHeight) {
				if (invisibles == null) {
					invisibles = new ArrayList<GameObject>();
				}
				invisibles.add(b);
				b.release();
			}
		}
		if (invisibles != null) {
			mBarriers.removeAll(invisibles);
			mObjects.removeAll(invisibles);
		}
		// create barriers based on the current game progress
		if (mCurLevel == 1) {
			createBird(mProbBird);
		} else if (mCurLevel == 2) {
			createBird(mProbBird);
			createThunder(mProbThunder);
		} else if (mCurLevel == 3) {
			createBird(mProbBird << 2);
			createThunder(mProbThunder << 1);
			createAsteroid(mProbAster);
		} else if (mCurLevel == 4) {
			createThunder(mProbThunder << 2);
			createAsteroid(mProbAster << 1);
			createAlient(mProbAlient << 1);
		} else if (mCurLevel == 5) {	
			createThunder(mProbThunder << 1);
			createAsteroid(mProbAster);
			createAlient(mProbAlient << 1);
		} else if (mCurLevel == 6) {
			createThunder(mProbThunder);
			createAsteroid(mProbAster);
			createAlient(mProbAlient);
		}		
	}
	
	// probabilities for creating barriers
	private int mProbBird    = 95;
	private int	mProbAster   = 195;
	private int mProbAlient  = 145;
	private int mProbThunder = 255;
	
	private void createBird(int probability) {		
		// get the acceleration time 
		int accTime = mRocket.getAccTime();
		// generate flying red chicken
		if (mRandom.nextInt(probability) == 1) {
			boolean right = mRandom.nextBoolean();
			Bird b = new Bird(mRes, right);			
			b.setX(right ? -b.getWidth() : mWidth);
			b.setY(mRandom.nextInt(mHeight - (mHeight >> 1) - (accTime > 0 ? (mHeight >> 2) : 0)));
			b.initSpeeds(
				(right ? mRandom.nextInt(3) + 5 : -5 - mRandom.nextInt(3)) * mLevel.mSpeedScaleX,   
				3f,
				accTime
			);
			b.onSizeChanged(mWidth, mHeight);
			b.setOnCollideListener(this);
			mBarriers.add(b);
			mObjects.add(b);
			// order by Z
			orderByZ(mObjects);
		}			
	}
	
	private void createAsteroid(int probability) {
		// get the acceleration time 
		int accTime = mRocket.getAccTime();		
		// generate asteroid
		int type = mRandom.nextInt(probability);
		if (type == 1) {
			Asteroid ast = new Asteroid(mRes);
			ast.setX(mRandom.nextInt((int)(mWidth - ast.getWidth() + 1)));
			ast.setY(0 - ast.getHeight());
			ast.initSpeeds(0, (mRandom.nextInt(3) + 2) * mLevel.mSpeedScaleY, accTime);
			ast.onSizeChanged(mWidth, mHeight);
			ast.setOnCollideListener(this);
			mBarriers.add(ast);
			mObjects.add(ast);
			// order by Z
			orderByZ(mObjects);
		} else if (type == 2) {
			Asteroid ast = new Asteroid(mRes);
			boolean right = mRandom.nextBoolean();
			ast.setX(right ? -ast.getWidth() : mWidth);
			ast.setY(mRandom.nextInt(mHeight >> 3) - (accTime > 0 ? (mHeight >> 3) : 0));
			ast.initSpeeds(
				(right ? mRandom.nextInt(3) + 3 : -3 - mRandom.nextInt(3)) * mLevel.mSpeedScaleX,   
				(mRandom.nextInt(3) + 2) * mLevel.mSpeedScaleY,
				accTime
			);
			ast.onSizeChanged(mWidth, mHeight);
			ast.setOnCollideListener(this);
			mBarriers.add(ast);
			mObjects.add(ast);
			// order by Z
			orderByZ(mObjects);
		}		
	}
	
	private void createAlient(int probability) {
		// get the acceleration time 
		int accTime = mRocket.getAccTime();
		// generate alient
		int type = mRandom.nextInt(probability);
//		if (type == 1) {
//			Alient ali = new Alient(mRes);
//			ali.setX(mRandom.nextInt((int)(mWidth - ali.getWidth() + 1)));
//			ali.setY(0 - ali.getHeight());
//			ali.initSpeeds(0, (mRandom.nextInt(4) + 3) * mLevel.mSpeedScaleY, accTime);
//			ali.onSizeChanged(mWidth, mHeight);
//			ali.setOnCollideListener(this);
//			mBarriers.add(ali);
//			mObjects.add(ali);
//			// order by Z
//			orderByZ(mObjects);
//		} else if (type == 2) {
//			Alient ali = new Alient(mRes);
//			boolean right = mRandom.nextBoolean();
//			ali.setX(right ? -ali.getWidth() : mWidth + ali.getWidth());
//			ali.setY(mRandom.nextInt(mHeight >> 3));
//			ali.initSpeeds(
//				(right ? mRandom.nextInt(3) + 3 : -3 - mRandom.nextInt(3)) * mLevel.mSpeedScaleX,   
//				(mRandom.nextInt(4) + 3) * mLevel.mSpeedScaleY,
//				accTime
//			);
//			ali.onSizeChanged(mWidth, mHeight);
//			ali.setOnCollideListener(this);
//			mBarriers.add(ali);
//			mObjects.add(ali); 
//			// order by Z
//			orderByZ(mObjects);
//		}
		if (type == 3) {
			int aliType = mRandom.nextInt(2);
			Alient ali = new TrickyAlient(mRes, aliType);
			if (aliType == 0) {				
				boolean right = mRandom.nextBoolean();
				ali.setX(right ? -ali.getWidth() : mWidth);
				ali.setY(mRandom.nextInt(mHeight >> 5) - (accTime > 0 ? (mHeight >> 3) : 0));
				ali.initSpeeds(
					(right ? mRandom.nextInt(6) + 7 : -7 - mRandom.nextInt(6)),   
					mRandom.nextInt(4) + 2,
					accTime
				);
			} else if (aliType == 1) {
				float offset = ali.getWidth();				
				ali.setX(offset + mRandom.nextInt((int) (mWidth - offset - offset - offset)));
				ali.setY(-ali.getHeight());
				ali.initSpeeds(
					6,   
					(mRandom.nextInt(5) + 2),
					accTime
				);
			}
			ali.onSizeChanged(mWidth, mHeight);
			ali.setOnCollideListener(this);
			mBarriers.add(ali);
			mObjects.add(ali);
			// order by Z
			orderByZ(mObjects);
		}		
	}

	private void createThunder(int probability) {
		// generate flying red chicken
		if (mRandom.nextInt(probability) == 1) {
			Thunder t = new Thunder(mRes);			
			t.setX(mRandom.nextInt((int) (mWidth - t.getWidth())));
			t.setY(-t.getHeight());
			t.initSpeeds(0, 3f, 0);
			t.onSizeChanged(mWidth, mHeight);
			t.setOnCollideListener(this);
			mBarriers.add(t);
			mObjects.add(t);
			// order by Z
			orderByZ(mObjects);
		}			
	}
	
	// probabilities for creating reward
	protected int mProbReward = 1250;
	// flag to indicate whether to generate the time bonus
	protected boolean mGenerateTimeBonus = false;
	
	public void updateReward() {
		if (mRandom.nextInt(mProbReward) == 0) {
			Field f = new Field(mRes);			
			f.setX(mRandom.nextInt((int) (mWidth - f.getWidth())));
			f.setY(-f.getHeight());			
			f.onSizeChanged(mWidth, mHeight);
			f.setOnCollideListener(this);
			mObjects.add(f);
			// order by Z
			orderByZ(mObjects);
		}
		
		if (mGenerateTimeBonus) {
			TimeBonus tb = new TimeBonus(mRes);
			tb.setX(mRandom.nextInt((int) (mWidth - tb.getWidth())));
			tb.setY(-tb.getHeight());	
			tb.onSizeChanged(mWidth, mHeight);
			tb.setOnCollideListener(this);
			tb.setOnGotTimeBonusListener(this);
			mObjects.add(tb);
			// order by Z
			orderByZ(mObjects);
			mGenerateTimeBonus = false;
		}
	}
	
	public int onLevelUp() {
		// level up and update barrier probabilities
		mLevel.levelUp();		
		mCurLevel = mLevel.getValue() % 7;
		if (mCurLevel == 0) { 
			// the difficulty increases about 30% after each loop
			// algorithm: 
			// for speed: ...
			// for complexity: 1 / Math.pow(1.1, 6) * 1.363 / 1.1 � 1 / 1.3
			mLevel.mSpeedScaleX *= 0.875;
			mLevel.mSpeedScaleY *= 0.875;
			mProbBird    *= 1.28;
			mProbAster   *= 1.28;
			mProbAlient  *= 1.28;
			mProbThunder *= 1.2;
			++mCurLoop;			
		}
		mCurLevel = mCurLoop > 1 ? mCurLevel + 1 : mCurLevel;
		
		mProbBird    /= mLevel.mComplexityScale;
		mProbAster   /= mLevel.mComplexityScale;
		mProbAlient  /= mLevel.mComplexityScale;
		mProbThunder /= (mLevel.mComplexityScale - 0.05);
		
		// update the background according to the current level
		if (mCurLevel == 1) {
			mCurtain.close();
		} else if (mCurLevel == 3 || mCurLevel == 5) {
			mBackgroundFar.switchToNext();
			mBackgroundNear.switchToNext();
		}
				
		// create a timebonus in the next loop
		mGenerateTimeBonus = true;
		
		return mCurLevel;
	}
	
	@Override
	public void onCollide(GameObject obj, List<GameObject> collideWith) {
		int kind = obj.getKind();						
		// trigger collide effects for all barriers
		float centerX = obj.getX() + obj.getWidth() * 0.5f;
		float centerY = obj.getY() + obj.getHeight() * 0.5f;
		for (GameObject object : collideWith) {		
			try {				
				Barrier b = (Barrier) object;				
				if (kind == GameObject.ROCKET) {
					if (Prefs.getHints(mContext)) {
						Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
						vibrator.vibrate(30);
					}
					mLifeBar.lifeChange(-0.334f);	
				}
				b.triggerCollideEffect(kind, centerX, centerY);
			} catch (ClassCastException e) {
				; // do nothing, just continue
			} finally {				
				Message msg = new Message();
				msg.what = object.getKind();
				GameEvent e = new SceneEvent(SceneEvent.SCENE_COLLIDE, msg);
				mEventHandler.handleGameEvent(e);
			}
		}
		orderByZ(mObjects);
	}

	public void onReachTarget(int odometer) {
		mLifeBar.lifeChange(0.01f);
	}

	public void onReachMilestone(int odometer) {
		Message msg = new Message();
		msg.what = odometer;
		GameEvent e = new SceneEvent(SceneEvent.SCENE_MILESTONE, msg);
		mEventHandler.handleGameEvent(e);
	}

	public void onLifeChanged(float life) {
		if (life == 0) { // compare a float, not good, modify later if necessary
			GameEvent e = new StateEvent(StateEvent.STATE_OVER, StateEvent.NO_LIFE);
			e.mExtra = Integer.valueOf(mOdometer.getDistance());
			mEventHandler.handleGameEvent(e);
		}
	}

	public void onTimeUpdate(int curTime) {
		if (curTime == 0) {
			GameEvent e = new StateEvent(StateEvent.STATE_OVER, StateEvent.NO_TIME);
			e.mExtra = Integer.valueOf(mOdometer.getDistance());
			mEventHandler.handleGameEvent(e);
		}
	}

	public void onGotTimeBonus(int bonus) {
		mTimer.addBonusTime(bonus);
	}

	public void onCurtainClosed() {				
		mCurtain.setDelay(1000);
		mCurtain.open();
		// switch background
		mBackgroundFar.switchToFirst();
		mBackgroundNear.switchToFirst();
	}

	public void onCurtainOpened() {
		openInteraction();
	}

	public void onCurtainPreClosing() {
		closeInteraction();
	}

	public void onCurtainPreOpening() {
		// disable user interaction
		
	}
}
