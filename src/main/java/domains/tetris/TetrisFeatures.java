package domains.tetris; 

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToIntBiFunction;
import java.util.stream.Collectors;
import java.lang.RuntimeException;

import org.apache.commons.math3.analysis.function.Max;
import org.apache.commons.math3.util.Pair;

import domains.tetris.TetrisFeatures.Builder;

public class TetrisFeatures {

	public final int height; //board height
	public final int width; //board width
	public final int[] colHeights;
	public final int[] colHeightsDiff;
	public final Set<Pair<Integer, Integer>> holesCords;
	public final int colTransition;
	public final int rowTransition;
	public final int nRowsWithHoles;
	public final double landingHeight;
	public final int cumWells;
	public final int pileHeight; //max col height;
	public final boolean gameOver;
	public final int holesDepth;
	public final int nHoles; 
	public final int sumHeightDiff; 
	public final int nErodedCells; 
	public final int nPatternDiversity;
	public final int nClearedLines;
	public final int nBrickCleared;
	public final double averageHeight;

	public final TetrisFeatures oldFeatures;
	public final int pileHeightDelta;
	public final int nHolesDelta;
	public final double averageHeightDelta;
	public final int sumHeightDiffDelta;

	public static void main(String[] args) {
		int[] ch = {1, 1, 2, 3};
		Set<Pair<Integer, Integer>> set = new HashSet<>();
		set.add(new Pair(0,2));
		set.add(new Pair(0,3));
		set.add(new Pair(1,3));
		TetrisFeatures tf = new Builder(4, 4, 0, 0, 0, true).colHeights(ch).holesCords(set).build();
		System.out.println("text:" + tf.holesDepth);
	}
	
	TetrisFeatures(Builder builder) {
		this.height = builder.height;
		this.width = builder.width;
		
		if (builder.board != null) {
			this.colHeights = getColHeights(builder.board);
			this.holesCords = getHolesCords(builder.board);
			}
		else {
			this.colHeights = builder.colHeights;
			this.holesCords = builder.holesCords;
		}
		this.nClearedLines = builder.nClearedLines;
		this.nBrickCleared = builder.nBrickCleared;
		this.nRowsWithHoles = holesCords
			.stream()
			.map(p -> p.getFirst())
			.collect(Collectors.toSet()).size();
		this.landingHeight = builder.landingHeight;
		this.cumWells = getCumWells();
		this.colHeightsDiff = getColDiff();
		this.pileHeight = Arrays.stream(colHeights).max().getAsInt();
		this.nHoles = holesCords.size();
		this.sumHeightDiff = Arrays.stream(colHeightsDiff).sum();
		this.averageHeight = (double)Arrays.stream(colHeights).sum()/(double)width;
		this.colTransition = getColTransition();
		this.rowTransition = getRowTransition(); 
		this.nErodedCells = nClearedLines * nBrickCleared;
		this.holesDepth =  getHolesDepth();
		this.nPatternDiversity = getNpatternDiversity();
		this.gameOver = builder.gameOver;
		this.oldFeatures = builder.oldFeatures;
		if(oldFeatures != null){
			this.pileHeightDelta = pileHeight - oldFeatures.pileHeight;
			this.nHolesDelta = nHoles - oldFeatures.nHoles;
			this.sumHeightDiffDelta = sumHeightDiff - oldFeatures.sumHeightDiff;
			this.averageHeightDelta = averageHeight - oldFeatures.averageHeight;
		}else{
			this.pileHeightDelta = 0;
			this.nHolesDelta = 0;
			this.sumHeightDiffDelta = 0;
			this.averageHeightDelta = 0;
		}

	}

	int getCumWells() {
		//well is an empty cell s.t adjacent left and right cells are filled.
		int rslt = 0;
		//in the interior it is the hole with both left and right cells filled.
		for(Pair<Integer, Integer> hc : holesCords) {
			Pair<Integer, Integer> left = new Pair<>(hc.getFirst(), hc.getSecond()-1);
			Pair<Integer, Integer> right = new Pair<>(hc.getFirst(), hc.getSecond()+1);
			int heightLeft = hc.getSecond() == 0 ? height : colHeights[hc.getSecond()-1];
			int heightRight = hc.getSecond() == width - 1 ? height : colHeights[hc.getSecond()+1];
			if (!(holesCords.contains(left) || holesCords.contains(right)) &&
					(heightLeft > hc.getFirst() && heightRight > hc.getFirst()))
				rslt = rslt + getWellDepth(hc);
			}

		//at top of each column nwells are
		//max(min(colheightLeft, colheightRight), colheightright) - colHeight
		// ToIntBiFunction<Integer, Integer> max = (x, y) -> x > y ? x : y;	
		// ToIntBiFunction<Integer, Integer> min = (x, y) -> x < y ? x : y;	
		for (int i = 0; i < this.width; i++) {
			int heightLeft = i == 0 ? height : colHeights[i-1];
			int heightRight = i == width - 1 ? height : colHeights[i+1];
			int maxmin = Math.max(Math.min(heightLeft, heightRight), colHeights[i]);
			int cum = 0;
			for (int r = colHeights[i]; r <= maxmin-1 ; r++) { //We iterate from the top of the well to the bottom
				boolean holeLeft = i == 0 ? false : holesCords.contains(new Pair<>(r, i-1));
				boolean holeRight = i == width -1 ? false : holesCords.contains(new Pair<>(r, i+1));
				if((!holeLeft && !holeRight) &&
						heightLeft >= r && heightRight >= r) {//This is a well
					cum++;//we accumulate
					rslt += cum;//we sum
				}else{//this is not a well but there was already a well above and it is an empty cell
					cum++;//we only accumulate
				}
			}
		}
		return rslt;
	}

	int getNpatternDiversity() {
		//five possible patterns: 1. lh - rh =1, 2. lh - rh > 1,
		//3. lh - rh = 0, 4. lh - rh = -1, 5. lh -rh < -2.
		//(lh(rh) is left(right) column height)
		//If the pattern is higher than 2, it is ignored.
		Set<Integer> patterns = new HashSet<>();
		for (int i = 0; i < this.width -1; i++)
			if(!patterns.contains(colHeights[i] - colHeights[i+1]) && Math.abs(colHeights[i] - colHeights[i+1]) <= 2)
				patterns.add(colHeights[i] - colHeights[i+1]);

		return patterns.size();
	}

	int getWellDepth(Pair<Integer, Integer> hc) {
		//contribution of each hole is 1 + num of holes directly below it, with no fillers
		// in between. 
		int rslt = 1;
			//for each hole directly below add 1 to the rslt.
			for (int i = hc.getFirst();
				 holesCords.contains(new Pair(i-1, hc.getSecond()));
					 i--)
				rslt++;

		return rslt;
	}

	int getHolesDepth() {
		//for each hole count num of rows above first filled cell, sum it over all holes.
		int rslt = 0;
		for (Pair<Integer, Integer> hole : holesCords) {
			int r = hole.getFirst();
			int c = hole.getSecond();
			if (holesCords.contains(new Pair(r+1, c)))
				continue;
			int i = r+1;
			for (; (! holesCords.contains(new Pair(i, c))) && i < colHeights[c]; i++);
			rslt += (i - r - 1);
		}
		return rslt;
	}

	int[] getColDiff() {
		int[] rslt = new int[colHeights.length-1];
		int N = colHeights.length-1;
		for (int i = 0; i < N; i++)
			rslt[i] = Math.abs(colHeights[i] - colHeights[i + 1]);

		return rslt;
	}

	int getRowTransition() {
		//each consecutive sequence of holes (from left to right) contributes 2(except *) 
		//"f h h f", will contribute same as "f h f", (f is filled and h is hole).
		//* if a hole height is larger then min(colheightleft, colheightright) then
		// that hole contribution is taken into account by change in colheights.

		// moving from left to right change in colHeight by amount delta contributes,
		// delta, its also assumed that left and right wall are full cols, so for
		// example if height is 4 and width is 3 and colHeights is {2,3,2}, then
		// contribution will be |4-2| + |2-3| + |3-2| + |2-3| + |2-4| = 6.
		int rslt = 0;

		//for each hole if right cell is not a hole and hole is not open  
		// then increment slt by 2.
		for (Pair<Integer, Integer> hole : holesCords ) {
			if ((!holesCords.contains(new Pair(hole.getFirst(), hole.getSecond()+1))) &&
				!isOpen(hole))
				rslt += 2;

			if((!holesCords.contains(new Pair(hole.getFirst(), hole.getSecond()+1))) &&
					isBelowFloatingCell(hole))
				rslt -= 2;
		}


		//each delta diff in consecutive colHeights will contribute delta.
		rslt += sumHeightDiff;

		//take contribution by walls
		rslt += 2*this.height - colHeights[0] - colHeights[colHeights.length - 1];

		return rslt;
	}

	private boolean isBelowFloatingCell(Pair<Integer, Integer> hole) {
		//helper function for getRowTransition.
		// A floating piece happens in rare cases. It is a filled cell that is not supported by any filled cell.
		// A hole is below a floating piece if it is higher than its left and right column height.
		int r = hole.getFirst();
		int c = hole.getSecond();

		//get columns of left and right most sibling.
		int lc = c; //column of left-most sibling.
		int rc = c; //column of right-most sibling.
		for(; holesCords.contains(new Pair<Integer, Integer>(r, lc-1));lc--);
		for(; holesCords.contains(new Pair<Integer, Integer>(r, rc+1));rc++);

		if (c == 0 || c == width-1 || lc == 0 || rc == width-1) //holes at boundaries can not be below floating pieces.
			return false;

		if(colHeights[lc -1] -1 < r && colHeights[rc +1] -1 < r)
			return true;

		return false;
	}

	boolean isOpen(Pair<Integer, Integer> hole) {
		//helper function for getRowTransition.
		// hole is open if its left-most sibling* is higher than their left columnheight
		// or its right most siblinb is higher then their right columnheight.
		// *sibling: consecutive holes at the same height. A hole is a sibling of itself
		// if a corresponding row at left(right) is filled then hole is it's own
		// leftmost(rightmost) sibling.

		int r = hole.getFirst();
		int c = hole.getSecond();

//		if (c == 0 || c == width-1) //holes at boundaries can not be open.
//			return false;

		//get columns of left and right most sibling. 
		int lc = c; //column of left-most sibling.
		int rc = c; //column of right-most sibling.
		for(; holesCords.contains(new Pair<Integer, Integer>(r, lc-1));lc--);
		for(; holesCords.contains(new Pair<Integer, Integer>(r, rc+1));rc++);
		
		int lhght = lc > 0 ? colHeights[lc-1] : height;//height of col left to
		//leftmost-sibling, equals board-height if leftmost-sibling is at the border.
		int rhght = rc < width-1 ? colHeights[rc+1] : height;
		return r >= Math.min(lhght, rhght);
	}

	int getColTransition() {
		//each consecutive sequence of empty cells (from top to bottom) contribute 2,
		// the top filled cell of a column if less then board.height contributes 1.
		int rslt = 0;
		//for each hole if top cell is not a hole increment rslt by 2.
		for (Pair<Integer, Integer> hole : holesCords ) {
			if (! holesCords.contains(new Pair(hole.getFirst()+1, hole.getSecond())))
								rslt += 2;
		}
		// for each column if it's height is less then board height increment rslt by 1.
		// since there is no ceiling so this holds for all columns. 
		rslt += width;

		return rslt;
	}

	int[] getColHeights(boolean[][] board) {
		int[] colHeights = new int[width];
		//initialize to 0
		for (int i = 0; i < width ; i++) {
			colHeights[i] = 0;
		}
		for (int r = height - 1; r >=0; r--) //column heights have a max value of height.
			for (int c = 0; c < width; c++)
				if (board[r][c] && colHeights[c] == 0)
					colHeights[c] = r + 1;
		return colHeights;
	}

	Set<Pair<Integer, Integer>> getHolesCords(boolean[][] board) {
		Set<Pair<Integer, Integer>> rslt = new HashSet<>();
		for (int col = 0; col < width; col++)	
			for (int row = 0; row < colHeights[col]; row++)
				if (!board[row][col] && row < height)
					rslt.add(new Pair<Integer, Integer> (row, col));
		return rslt;
	}


	public static class Builder {

		//required parameters, raise error if not set
		private int height; 
		private int width;
		private boolean gameOver;
		private int nClearedLines;
		private int nBrickCleared;
		private double landingHeight;

		//optional either a and b or c must be given.
		private int[] colHeights = null; //a 
		private Set<Pair<Integer, Integer>> holesCords = null; //b
		private boolean[][] board = null; //c

		//optional parameter for calculating change features
		private TetrisFeatures oldFeatures = null;

		public Builder(int height, int width, int nClearedLines, int nBrickCleared, double landingHeight,
					   boolean gameOver) {
			this.height = height;
			this.width = width;
			this.nClearedLines = nClearedLines;
			this.nBrickCleared = nBrickCleared;
			this.gameOver = gameOver;
			this.landingHeight = landingHeight;
		}

		public Builder holesCords(Set<Pair<Integer, Integer>> val) {
			this.holesCords = val;
			return this;
		}

		public Builder oldFeatures(TetrisFeatures val) { this.oldFeatures = val; return this;}

		public Builder colHeights(int[] val) { this.colHeights = val; return this;}

		public Builder board(boolean[][] val) { this.board = val; return this;}

		public TetrisFeatures build() {
			if ((holesCords == null && colHeights == null) && board == null)
				throw new RuntimeException("please either set board or (holesCords and colHeights)!");
			return new TetrisFeatures(this);
		}
	}
}

