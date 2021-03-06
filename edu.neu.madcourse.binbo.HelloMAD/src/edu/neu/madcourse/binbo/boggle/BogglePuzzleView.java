package edu.neu.madcourse.binbo.boggle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import edu.neu.madcourse.binbo.R;
import edu.neu.madcourse.binbo.persistentboggle.PBPlayerInfo;

public class BogglePuzzleView extends View {
	private static final String TAG = "Boggle";

	private static final String VIEW_STATE = "viewState";
	private static final int ID = 42;
	private static final String SELX = "selX";
	private static final String SELY = "selY";
	private static final String LIST_SIZE = "listSize";

	protected int   mSize;   // could be 4�6
	protected float mWidth;  // width of one tile
	protected float mHeight; // height of one tile	
	protected RectF mOval = new RectF();	
	protected List<Point> mSelList = new ArrayList<Point>();	
	protected IBoggleGame mGame = null;
	protected BogglePuzzle mPuzzle = null;
	private ToneGenerator mToneGen = null;
	
	private PBPlayerInfo mHost = null;

	public BogglePuzzleView(Context context, BogglePuzzle puzzle) {
		super(context);
		
		mGame   = (IBoggleGame)context;
		mSize   = puzzle.getPuzzleSize();		
		mPuzzle = puzzle;		
		
		setFocusable(true);
		setFocusableInTouchMode(true);
		
		// initialize tone generator
		mToneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 70);		
		// ...
		setId(ID);			
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable p = super.onSaveInstanceState();
		Log.d(TAG, "onSaveInstanceState");
		Bundle bundle = new Bundle();
		
		bundle.putInt(LIST_SIZE, mSelList.size());
		for (int i = 0; i < mSelList.size(); ++i) {
			Point ptInList = mSelList.get(i);
			String postfixX = "" + i;
			String postfixY = "" + i;
			bundle.putInt(SELX + postfixX, ptInList.x);
			bundle.putInt(SELY + postfixY, ptInList.y);
		}		
		
		bundle.putParcelable(VIEW_STATE, p);
		return bundle;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Log.d(TAG, "onRestoreInstanceState");
		Bundle bundle = (Bundle)state;
				
		int listSize = bundle.getInt(LIST_SIZE);
		for (int i = 0; i < listSize; ++i) {
			String postfixX = "" + i;
			String postfixY = "" + i;
			int x = bundle.getInt(SELX + postfixX);
			int y = bundle.getInt(SELY + postfixY);
			mSelList.add(new Point(x, y));			
		}
		super.onRestoreInstanceState(bundle.getParcelable(VIEW_STATE));
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mWidth  = w / mSize;
		mHeight = h / mSize;

		Log.d(TAG, "onSizeChanged: width " + mWidth + ", height " + mHeight);
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Draw the background...
		Paint background = new Paint();
		background.setColor(getResources().getColor(
				R.color.sudoku_puzzle_background));
		canvas.drawRect(0, 0, getWidth(), getHeight(), background);

		// Draw the board...

		// Define colors for the grid lines
		Paint dark = new Paint();
		dark.setColor(getResources().getColor(R.color.sudoku_puzzle_dark));

		Paint hilite = new Paint();
		hilite.setColor(getResources().getColor(R.color.sudoku_puzzle_hilite));

		Paint light = new Paint();
		light.setColor(getResources().getColor(R.color.sudoku_puzzle_light));

		// Draw the minor grid lines
		for (int i = 0; i < mSize; i++) {
			canvas.drawLine(0, i * mHeight, getWidth(), i * mHeight, light);
			canvas.drawLine(0, i * mHeight + 1, getWidth(), i * mHeight + 1, hilite);
			canvas.drawLine(i * mWidth, 0, i * mWidth, getHeight(), light);
			canvas.drawLine(i * mWidth + 1, 0, i * mWidth + 1, getHeight(), hilite);
		}

		// Draw the major grid lines
		for (int i = 0; i < mSize; i++) {
			canvas.drawLine(0, i * mHeight, getWidth(), i * mHeight, dark);
			canvas.drawLine(0, i * mHeight + 1, getWidth(), i * mHeight + 1, hilite);
			canvas.drawLine(i * mWidth, 0, i * mWidth, getHeight(), dark);
			canvas.drawLine(i * mWidth + 1, 0, i * mWidth + 1, getHeight(), hilite);
		}
		
		if (mGame.isGameOver()) {
			Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
			text.setColor(getResources().getColor(
					R.color.sudoku_puzzle_foreground));
			text.setStyle(Style.FILL);
			text.setTextSize(50);
			text.setTextAlign(Paint.Align.CENTER);
			canvas.drawText("Game Over", mWidth * 2, mHeight * 2, text);
			return;
		}
		if (mGame.isGamePaused()) {
			Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
			text.setColor(getResources().getColor(
					R.color.sudoku_puzzle_foreground));
			text.setStyle(Style.FILL);
			text.setTextSize(50);
			text.setTextAlign(Paint.Align.CENTER);
			canvas.drawText("Game Paused", mHeight * 2, mHeight * 2, text);
			return;
		} 

		// Draw the letters...
		// Define color and style for letters
		Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
		foreground.setColor(getResources().getColor(
				R.color.sudoku_puzzle_foreground));
		foreground.setStyle(Style.FILL);
		foreground.setTextSize(mHeight * 0.75f);		
		foreground.setTextScaleX(mWidth / mHeight);
		foreground.setTextAlign(Paint.Align.CENTER);

		// Draw the letter in the center of the tile
		FontMetrics fm = foreground.getFontMetrics();
		// Centering in X: use alignment (and X at midpoint)
		float x = mWidth / 2;
		// Centering in Y: measure ascent/descent first
		float y = mHeight / 2 - (fm.ascent + fm.descent) / 2;
		for (int i = 0; i < mSize; i++) {
			for (int j = 0; j < mSize; j++) {
				canvas.drawText(mPuzzle.getTileString(i, j), i * mWidth + x, j
						* mHeight + y, foreground);
			}
		}

		// Draw the selection circles		
		Paint selected = new Paint();	
		selected.setStyle(Paint.Style.STROKE);
		selected.setAntiAlias(true);
		int size = mSelList.size();
		for (int i = 0; i < size; ++i) {
			Point ptScr = toScreenPoint(mSelList.get(i));
			if (i == 0) {
				selected.setStrokeWidth(7);
				selected.setColor(Color.RED);
				canvas.drawCircle(ptScr.x, ptScr.y, mWidth / 2 * 0.518f, selected);
			} else if (i == size - 1) {
				selected.setStrokeWidth(7);
				selected.setColor(Color.GREEN);				
				canvas.drawCircle(ptScr.x, ptScr.y, mWidth / 2 * 0.518f, selected);
			} else {
				selected.setStrokeWidth(12);
				selected.setColor(Color.GRAY);
				canvas.drawCircle(ptScr.x, ptScr.y, mWidth / 2 * 0.518f, selected);
			}		
		}
	
		// Draw the arrow for selection order
		Paint arrow = new Paint();	
		arrow.setStyle(Style.FILL);
		arrow.setAntiAlias(true);
		arrow.setColor(Color.BLUE);
		for (int i = 1; i < mSelList.size(); ++i) {
			Point ptCur  = mSelList.get(i);
			Point ptPrev = mSelList.get(i - 1);
			// Draw each arrow if available			
			int dx = ptCur.x - ptPrev.x;
			int dy = ptCur.y - ptPrev.y;
			// Eight conditions for deciding the arrow direction
			if (dx == 0 && dy < 0) { // up
				mOval.left   = mWidth * ptPrev.x + mWidth / 4;
				mOval.top    = mHeight * ptPrev.y - mHeight / 2;
				mOval.right  = mWidth * (ptPrev.x + 1) - mWidth / 4;
				mOval.bottom = mHeight * ptPrev.y + mHeight / 8;
				canvas.drawArc(mOval, 80, 20, true, arrow);
			} else if (dx == 0 && dy > 0) { // down
				mOval.left   = mWidth * ptPrev.x + mWidth / 4;
				mOval.top    = mHeight * ptCur.y - mHeight / 8;
				mOval.right  = mWidth * (ptPrev.x + 1) - mWidth / 4;
				mOval.bottom = mHeight * ptCur.y + mHeight / 2;
				canvas.drawArc(mOval, 260, 20, true, arrow);
			} else if (dx < 0 && dy == 0) { // left
				mOval.left   = mWidth * ptPrev.x - mWidth / 2;
				mOval.top    = mHeight * ptPrev.y + mHeight / 4;
				mOval.right  = mWidth * ptPrev.x + mWidth / 8;
				mOval.bottom = mHeight * (ptPrev.y + 1) - mHeight / 4;
				canvas.drawArc(mOval, -10, 20, true, arrow);
			} else if (dx > 0 && dy == 0) { // right
				mOval.left   = mWidth * ptCur.x - mWidth / 8;
				mOval.top    = mHeight * ptPrev.y + mHeight / 4;
				mOval.right  = mWidth * ptCur.x + mWidth / 2;
				mOval.bottom = mHeight * (ptPrev.y + 1) - mHeight / 4;
				canvas.drawArc(mOval, 170, 20, true, arrow);
			} else if (dx < 0 && dy < 0) { // up-left
				mOval.left   = mWidth * ptPrev.x - mWidth * 7 / 16;
				mOval.top    = mHeight * ptPrev.y - mHeight * 7 / 16;
				mOval.right  = mWidth * ptPrev.x + mWidth * 3 / 16;
				mOval.bottom = mHeight * ptPrev.y + mHeight * 3 / 16;
				canvas.drawArc(mOval, 37, 16, true, arrow);
			} else if (dx < 0 && dy > 0) { // down-left
				mOval.left   = mWidth * ptPrev.x - mWidth * 7 / 16;
				mOval.top    = mHeight * (ptPrev.y + 1) - mHeight * 3 / 16;
				mOval.right  = mWidth * ptPrev.x + mWidth * 3 / 16;
				mOval.bottom = mHeight * (ptPrev.y + 1) + mHeight * 7 / 16;
				canvas.drawArc(mOval, 307, 16, true, arrow);
			} else if (dx > 0 && dy < 0) { // up-right
				mOval.left   = mWidth * (ptPrev.x + 1) - mWidth * 3 / 16;
				mOval.top    = mHeight * ptPrev.y - mHeight * 7 / 16;
				mOval.right  = mWidth * (ptPrev.x + 1) + mWidth * 7 / 16;
				mOval.bottom = mHeight * ptPrev.y + mHeight * 3 / 16;
				canvas.drawArc(mOval, 127, 16, true, arrow);
			} else if (dx > 0 && dy > 0) { // down-right
				mOval.left   = mWidth * ptCur.x - mWidth * 3 / 16;
				mOval.top    = mHeight * ptCur.y - mHeight * 3 / 16;
				mOval.right  = mWidth * ptCur.x + mWidth * 7 / 16;
				mOval.bottom = mHeight * ptCur.y + mHeight * 7 / 16;
				canvas.drawArc(mOval, 217, 16, true, arrow);
			}			
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {		
		if (event.getAction() != MotionEvent.ACTION_DOWN)
			return super.onTouchEvent(event);
		
		if (mGame.isGamePaused() || mGame.isGameOver()) {
			return true;
		}
		
		if (mToneGen != null) {
			mToneGen.startTone(ToneGenerator.TONE_DTMF_2, 100);
		}

		selectNextLetter((int)(event.getX() / mWidth), (int)(event.getY() / mHeight));				

		return true;
	}
	
	public void bindHost(PBPlayerInfo host) {
		mHost = host;
	}
	
	public void rotatePuzzle() {
		for (int i = 0; i < mSelList.size(); ++i) {
			Point ptInList = mSelList.get(i);
			int x = ptInList.x;
			ptInList.x = ptInList.y;
			ptInList.y = mSize - 1 - x;
		}
		invalidate();
	}
	
	private void selectNextLetter(int x, int y) {
		Point selPt = new Point(Math.min(Math.max(x, 0), mSize - 1), 
							 Math.min(Math.max(y, 0), mSize - 1));
						
		if (mSelList.isEmpty()) { // if there is no selection before
			mSelList.add(selPt);
		} else {
			// invalid position, just return
			if (isInvalidPoint(selPt)) {
				return;
			}
			// select a new one
			if (isNewSelection(selPt)) {
				mSelList.add(selPt);
			} else if (isSelectedStartPoint(selPt)) {
				mSelList.clear();			
			} else if (isSelectedEndPoint(selPt)) {
				// should have at least three letters
				if (mSelList.size() < 3) {
					return;
				}
				// look up the dictionary for the word in the list
				if (mGame.lookUpWord(listToString(mSelList))) {					
					mSelList.clear();
				} else {
					;
				}
			} else {
				removeSelectionsFrom(selPt);
			}
		}
				
		invalidate();
		if (mHost != null) {
			mHost.setSelLetters(listToString(mSelList));
		}
		if (mGame != null) {
			mGame.updateGameViews();
		}
	}
	
	private boolean isInvalidPoint(Point pt) {
		int size = mSelList.size();	
		Point endPt = mSelList.get(size - 1);
		
		for (int i = 0; i < mSelList.size(); ++i) {
			Point ptInList = mSelList.get(i);
			if (pt.x == ptInList.x && pt.y == ptInList.y) {
				return false;
			}
		}
		if ((pt.x < endPt.x - 1) || (pt.x > endPt.x + 1)) {
			return true;
		}
		if ((pt.y < endPt.y - 1) || (pt.y > endPt.y + 1)) {
			return true;
		}		
		return false;
	}
	
	private boolean isNewSelection(Point pt) {
		Iterator<Point> i = mSelList.iterator();
		// loop the list to see whether the point is already selected
		while (i.hasNext()) {
			Point ptInList = i.next();
			if (pt.x == ptInList.x && pt.y == ptInList.y) {
				return false;
			}
		}		
		return true;
	}
	
	private boolean isSelectedStartPoint(Point pt) {
		Point ptStart = mSelList.get(0);
		if (pt.x == ptStart.x && pt.y == ptStart.y) {
			return true;
		}
		return false;
	}

	private boolean isSelectedEndPoint(Point pt) {
		int size = mSelList.size();
		Point ptEnd = mSelList.get(size - 1);
		if (pt.x == ptEnd.x && pt.y == ptEnd.y) {
			return true;
		}
		return false;
	}
	
	private void removeSelectionsFrom(Point pt) {	
		int start = 0;
		for (int i = 0; i < mSelList.size(); ++i) {
			Point ptInList = mSelList.get(i);
			if (pt.x == ptInList.x && pt.y == ptInList.y) {
				start = i + 1;
			}
		}
		for (int i = mSelList.size() - 1; i >= start; --i) {
			mSelList.remove(i);
		}
		
		Log.d(TAG, "RemoveSelectionsFrom");
	}
	
	private Point toScreenPoint(Point pt) {
		Point ptScr = new Point();
		
		ptScr.x = (int)(pt.x * mWidth + mWidth / 2);
		ptScr.y = (int)(pt.y * mHeight + mHeight / 2);
		
		return ptScr;
	}
	
	private String listToString(List<Point> selList) {
		String s = "";		
		for (int i = 0; i < selList.size(); ++i) {
			Point pt = selList.get(i);
			s += mPuzzle.getTileString(pt.x, pt.y);
		}		
		return s.toLowerCase();
	}
		
}
