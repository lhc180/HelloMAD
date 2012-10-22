package edu.neu.madcourse.binbo;

import android.content.Context;

public class BogglePuzzle {			
	protected char[] mPuzzle = null;
	protected int mSize = 4;
	protected IBoggleGame mGame = null;
	
	private final int FREQUENCY[] = {
		326395, 73910, 169177, 129257, 437303, 46188, 98557, 104164, 
		355476, 6416, 33829, 215219, 119469, 285562, 280921, 127706, 
		6784, 280998, 326647, 262874, 146434, 37786, 27811, 11837, 74044, 17531
	};
	private final int SUM_OF_CHARS = 4002295;	
	
	public BogglePuzzle(IBoggleGame game, int size) {
		assert(game != null);		
		mGame = game;
		makePuzzle(size);
	}
	
	public BogglePuzzle(IBoggleGame game, char[] puzzle) {
		mGame = game;
		mPuzzle = puzzle;
		mSize = (int)Math.sqrt(puzzle.length);
	}
	
	public void makePuzzle(int size) {	
		mSize = size;
		
		StringBuffer sf = new StringBuffer();		
		int count = mSize * mSize;		
		char letters[] = new char[count];			
		
		for (int i = 0; i < count; ++i) {
			letters[i] = ' ';
		}
		for (int i = 0; i < count; ++i) {
			char letter = makeLetter();
			letters[i] = letter;
			while (isTooMuchRepeated(letters, letter)) {
				letter = makeLetter(); // try to make another letter
				letters[i] = letter;
			}
			sf.append(String.valueOf(letter));
		}

		mPuzzle = sf.toString().toCharArray();
	}
	
	public char[] getPuzzle() {
		return mPuzzle;
	}
	
	public void rotatePuzzle() {
		char[] puzzle = new char[mSize * mSize];  	
		
		int k = 0;
		for (int i = 0; i < mSize; ++i) {
			for (int j = mSize - 1; j >= 0; --j) {
				puzzle[mSize * j + i] = mPuzzle[k++];
			}
		}
		
		mPuzzle = puzzle;		
	}
	
	public int getPuzzleSize() {
		return mSize;
	}
	
	public String getTileString(int x, int y) {
		char c = mPuzzle[mSize * y + x];
		return String.valueOf(c);
	}

	protected boolean isTooMuchRepeated(char[] letters, char letter) {
		int count = 0;
		
		for (int i = 0; i < letters.length; ++i) {
			if (letters[i] == letter) {
				++count;
			}
		}
		if (count > 3) {
			return true;
		} else if (count == 3) {
			int occurs[] = new int[26];
			for (int i = 0; i < 26; ++i) {
				occurs[i] = 0;
			}
			// get the occurrence of each letter
			for (int i = 0; i < letters.length; ++i) {
				int k = (int)letters[i] - 65;
				if (k >= 0) {
					occurs[k]++;
				}				 
			}		
			// check whether some other letter already occurred 3 times,
			// if so, the repeated letters are too much.
			for (int i = 0; i < 26; ++i) {
				if (occurs[i] > 2) {
					if ((char)(i + 65) == letter) // skip the current letter 
						continue;
					return true; // more than one letter occurred 3 times
				}
			}			
		}
		
		return false;
	}
	
	/** Make every letter */
	protected char makeLetter() {
		char letter = 'e';
		int l = 0, r = 0;
		int t = (int)(Math.random() * SUM_OF_CHARS);	
		
		int i;
		for (i = 0; i < 26; ++i) {
			r += FREQUENCY[i];
			if (l < t && t < r) {
				letter = (char)(65 + i);
				break;
			}
			l += FREQUENCY[i];
		}		
		assert(i < 26);
		
		return letter;
	}
}