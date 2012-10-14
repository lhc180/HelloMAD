package edu.neu.madcourse.binbo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class BogglePuzzleView extends View {
	private static final String TAG = "Boggle";

	private static final String VIEW_STATE = "viewState";
	private static final int ID = 42;
	private static final String SELX = "selX";
	private static final String SELY = "selY";
	private static final String LIST_SIZE = "sizeList";

	private float width; // width of one tile
	private float height; // height of one tile
	List<Point> selList = new ArrayList<Point>();

	private RectF oval = new RectF();
	private final BoggleGame game;

	public BogglePuzzleView(Context context) {

		super(context);
		this.game = (BoggleGame) context;
		setFocusable(true);
		setFocusableInTouchMode(true);
		
		// ...
		setId(ID);			
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable p = super.onSaveInstanceState();
		Log.d(TAG, "onSaveInstanceState");
		Bundle bundle = new Bundle();
		
		bundle.putInt(LIST_SIZE, selList.size());
		for (int i = 0; i < selList.size(); ++i) {
			Point ptInList = selList.get(i);
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
			selList.add(new Point(x, y));			
		}
		super.onRestoreInstanceState(bundle.getParcelable(VIEW_STATE));
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		width = w / 4f;
		height = h / 4f;
//		getRect(selX, selY, selRect);
		Log.d(TAG, "onSizeChanged: width " + width + ", height " + height);
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
		for (int i = 0; i < 4; i++) {
			canvas.drawLine(0, i * height, getWidth(), i * height, light);
			canvas.drawLine(0, i * height + 1, getWidth(), i * height + 1, hilite);
			canvas.drawLine(i * width, 0, i * width, getHeight(), light);
			canvas.drawLine(i * width + 1, 0, i * width + 1, getHeight(), hilite);
		}

		// Draw the major grid lines
		for (int i = 0; i < 4; i++) {
			canvas.drawLine(0, i * height, getWidth(), i * height, dark);
			canvas.drawLine(0, i * height + 1, getWidth(), i * height + 1, hilite);
			canvas.drawLine(i * width, 0, i * width, getHeight(), dark);
			canvas.drawLine(i * width + 1, 0, i * width + 1, getHeight(), hilite);
		}
		
		if (game.isGameOver()) {
			Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
			text.setColor(getResources().getColor(
					R.color.sudoku_puzzle_foreground));
			text.setStyle(Style.FILL);
			text.setTextSize(50);
			text.setTextAlign(Paint.Align.CENTER);
			canvas.drawText("Game Over", width * 2, height * 2, text);
			return;
		}
		if (game.isGamePaused()) {
			Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
			text.setColor(getResources().getColor(
					R.color.sudoku_puzzle_foreground));
			text.setStyle(Style.FILL);
			text.setTextSize(50);
			text.setTextAlign(Paint.Align.CENTER);
			canvas.drawText("Game Paused", width * 2, height * 2, text);
			return;
		} 

		// Draw the letters...
		// Define color and style for letters
		Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
		foreground.setColor(getResources().getColor(
				R.color.sudoku_puzzle_foreground));
		foreground.setStyle(Style.FILL);
		foreground.setTextSize(height * 0.75f);		
		foreground.setTextScaleX(width / height);
		foreground.setTextAlign(Paint.Align.CENTER);

		// Draw the letter in the center of the tile
		FontMetrics fm = foreground.getFontMetrics();
		// Centering in X: use alignment (and X at midpoint)
		float x = width / 2;
		// Centering in Y: measure ascent/descent first
		float y = height / 2 - (fm.ascent + fm.descent) / 2;
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				canvas.drawText(this.game.getTileString(i, j), i * width + x, j
						* height + y, foreground);
			}
		}

//		if (SudokuPrefs.getHints(getContext())) {
			// Draw the hints...
//		}

		// Draw the selection circles		
		Paint selected = new Paint();	
		selected.setStyle(Paint.Style.STROKE);
		selected.setAntiAlias(true);
		int size = selList.size();
		for (int i = 0; i < size; ++i) {
			Point ptScr = toScreenPoint(selList.get(i));
			if (i == 0) {
				selected.setStrokeWidth(7);
				selected.setColor(Color.RED);
				canvas.drawCircle(ptScr.x, ptScr.y, width / 2 * 0.518f, selected);
			} else if (i == size - 1) {
				selected.setStrokeWidth(7);
				selected.setColor(Color.GREEN);				
				canvas.drawCircle(ptScr.x, ptScr.y, width / 2 * 0.518f, selected);
			} else {
				selected.setStrokeWidth(12);
				selected.setColor(Color.GRAY);
				canvas.drawCircle(ptScr.x, ptScr.y, width / 2 * 0.518f, selected);
			}		
		}
	
		// Draw the arrow for selection order
		Paint arrow = new Paint();	
		arrow.setStyle(Style.FILL);
		arrow.setAntiAlias(true);
		arrow.setColor(Color.BLUE);
		for (int i = 1; i < selList.size(); ++i) {
			Point ptCur  = selList.get(i);
			Point ptPrev = selList.get(i - 1);
			// Draw each arrow if available			
			int dx = ptCur.x - ptPrev.x;
			int dy = ptCur.y - ptPrev.y;
			// Eight conditions for deciding the arrow direction
			if (dx == 0 && dy < 0) { // up
				oval.left   = width * ptPrev.x + width / 4;
				oval.top    = height * ptPrev.y - height / 2;
				oval.right  = width * (ptPrev.x + 1) - width / 4;
				oval.bottom = height * ptPrev.y + height / 8;
				canvas.drawArc(oval, 80, 20, true, arrow);
			} else if (dx == 0 && dy > 0) { // down
				oval.left   = width * ptPrev.x + width / 4;
				oval.top    = height * ptCur.y - height / 8;
				oval.right  = width * (ptPrev.x + 1) - width / 4;
				oval.bottom = height * ptCur.y + height / 2;
				canvas.drawArc(oval, 260, 20, true, arrow);
			} else if (dx < 0 && dy == 0) { // left
				oval.left   = width * ptPrev.x - width / 2;
				oval.top    = height * ptPrev.y + height / 4;
				oval.right  = width * ptPrev.x + width / 8;
				oval.bottom = height * (ptPrev.y + 1) - height / 4;
				canvas.drawArc(oval, -10, 20, true, arrow);
			} else if (dx > 0 && dy == 0) { // right
				oval.left   = width * ptCur.x - width / 8;
				oval.top    = height * ptPrev.y + height / 4;
				oval.right  = width * ptCur.x + width / 2;
				oval.bottom = height * (ptPrev.y + 1) - height / 4;
				canvas.drawArc(oval, 170, 20, true, arrow);
			} else if (dx < 0 && dy < 0) { // up-left
				oval.left   = width * ptPrev.x - width * 7 / 16;
				oval.top    = height * ptPrev.y - height * 7 / 16;
				oval.right  = width * ptPrev.x + width * 3 / 16;
				oval.bottom = height * ptPrev.y + height * 3 / 16;
				canvas.drawArc(oval, 37, 16, true, arrow);
			} else if (dx < 0 && dy > 0) { // down-left
				oval.left   = width * ptPrev.x - width * 7 / 16;
				oval.top    = height * (ptPrev.y + 1) - height * 3 / 16;
				oval.right  = width * ptPrev.x + width * 3 / 16;
				oval.bottom = height * (ptPrev.y + 1) + height * 7 / 16;
				canvas.drawArc(oval, 307, 16, true, arrow);
			} else if (dx > 0 && dy < 0) { // up-right
				oval.left   = width * (ptPrev.x + 1) - width * 3 / 16;
				oval.top    = height * ptPrev.y - height * 7 / 16;
				oval.right  = width * (ptPrev.x + 1) + width * 7 / 16;
				oval.bottom = height * ptPrev.y + height * 3 / 16;
				canvas.drawArc(oval, 127, 16, true, arrow);
			} else if (dx > 0 && dy > 0) { // down-right
				oval.left   = width * ptCur.x - width * 3 / 16;
				oval.top    = height * ptCur.y - height * 3 / 16;
				oval.right  = width * ptCur.x + width * 7 / 16;
				oval.bottom = height * ptCur.y + height * 7 / 16;
				canvas.drawArc(oval, 217, 16, true, arrow);
			}			
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {		
		if (event.getAction() != MotionEvent.ACTION_DOWN)
			return super.onTouchEvent(event);
		
		if (game.isGamePaused() || game.isGameOver()) {
			return true;
		}
		
		game.playClickSound();

		selectNextLetter((int)(event.getX() / width), (int)(event.getY() / height));
		//game.showKeypadOrError(selX, selY);
		game.updateBoggleStringFromSelection(selList);

		return true;
	}
	
	private void selectNextLetter(int x, int y) {
		Point selPt = new Point(Math.min(Math.max(x, 0), 3), 
							 Math.min(Math.max(y, 0), 3));
						
		if (selList.isEmpty()) { // if there is no selection before
			selList.add(selPt);
		} else {
			// invalid position, just return
			if (isInvalidPoint(selPt)) {
				return;
			}
			// select a new one
			if (isNewSelection(selPt)) {
				selList.add(selPt);
			} else if (isSelectedStartPoint(selPt)) {
				selList.clear();			
			} else if (isSelectedEndPoint(selPt)) {
				// should have at least three letters
				if (selList.size() < 3) {
					return;
				}
				// look up the dictionary for the word in the list
				if (game.isWordInDictionary(selList)) {					
					selList.clear();
				} else {
					;
				}
			} else {
				removeSelectionsFrom(selPt);
			}
		}
		
		invalidate();
	}
	
	private boolean isInvalidPoint(Point pt) {
		int size = selList.size();	
		Point endPt = selList.get(size - 1);
		
		for (int i = 0; i < selList.size(); ++i) {
			Point ptInList = selList.get(i);
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
		Iterator<Point> i = selList.iterator();
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
		Point ptStart = selList.get(0);
		if (pt.x == ptStart.x && pt.y == ptStart.y) {
			return true;
		}
		return false;
	}

	private boolean isSelectedEndPoint(Point pt) {
		int size = selList.size();
		Point ptEnd = selList.get(size - 1);
		if (pt.x == ptEnd.x && pt.y == ptEnd.y) {
			return true;
		}
		return false;
	}
	
	private void removeSelectionsFrom(Point pt) {	
		int start = 0;
		for (int i = 0; i < selList.size(); ++i) {
			Point ptInList = selList.get(i);
			if (pt.x == ptInList.x && pt.y == ptInList.y) {
				start = i + 1;
			}
		}
		for (int i = selList.size() - 1; i >= start; --i) {
			selList.remove(i);
		}
		
		Log.d(TAG, "RemoveSelectionsFrom");
	}
	
	private Point toScreenPoint(Point pt) {
		Point ptScr = new Point();
		
		ptScr.x = (int)(pt.x * width + width / 2);
		ptScr.y = (int)(pt.y * height + height / 2);
		
		return ptScr;
	}
	
	public void changePuzzleDirection() {
		for (int i = 0; i < selList.size(); ++i) {
			Point ptInList = selList.get(i);
			int x = ptInList.x;
			ptInList.x = ptInList.y;
			ptInList.y = 3 - x;
		}
	}
}
