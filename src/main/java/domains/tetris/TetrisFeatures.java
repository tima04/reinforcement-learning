package domains.tetris; 

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToIntBiFunction;
import java.util.stream.Collectors;
import java.lang.RuntimeException;

import org.apache.commons.math3.analysis.function.Max;
import org.apache.commons.math3.util.Pair;

public class TetrisFeatures {

	public final int height; //board height
	public final int width; //board width
	public final int[] colHeights;
	public final Set<Pair<Integer, Integer>> holesCords;
	public final int colTransition;
	public final int rowTransition;
	public final int nRowsWithHoles;
	public final double landingHeight;
	public final int cumWells;
	public final int nColDiff;
	public final int pileHeight; //max col height;
	public final boolean gameOver;
	public final int nWells; 
	public final int holesDepth; 
	public final int nHoles; 
	public final int sumHeightDiff; 
	public final int nErodedCells; 
	public final int nPatternDiversity;
	public final int nClearedLines;
	public final int nBrickCleared;

	public static void main(String[] args) {
		System.out.println("text: foo");
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
		
		this.gameOver = builder.gameOver;
		this.nClearedLines = builder.nClearedLines;
		this.nBrickCleared = builder.nBrickCleared;
		//this.rowsWithHoles = getRowsWithHoles();
		this.nRowsWithHoles = holesCords
			.stream()
			.map(p -> p.getFirst())
			.collect(Collectors.toSet()).size();
		this.landingHeight = builder.landingHeight;
		this.cumWells = 0;
		this.nColDiff = 0;
		this.pileHeight = Arrays.stream(colHeights).max().getAsInt();
		this.nHoles = holesCords.size();
		this.sumHeightDiff = getSumHeightDiff();
		this.colTransition = getColTransition();
		this.rowTransition = getRowTransition(); 
		this.nErodedCells = nClearedLines * nBrickCleared;
		this.holesDepth =  getHolesDepth();
		this.nWells = getNwells(); 
		this.nPatternDiversity = getNpatternDiversity();
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

		public Builder colHeights(int[] val) { this.colHeights = val; return this;}

		public Builder board(boolean[][] val) { this.board = val; return this;}

		public TetrisFeatures build() {
			if ((holesCords == null && colHeights == null) && board == null)
				throw new RuntimeException("please either set board or (holesCords and colHeights)!");
			return new TetrisFeatures(this);
		}
	}

	int getNwells() {
		//well is an empty cell s.t adjacent left and right cells are filled.
		int rslt = 0;
		//in the interior it is the hole with both left and right cells filled.
		for(Pair<Integer, Integer> hc : holesCords) {
			Pair<Integer, Integer> left = new Pair<>(hc.getFirst(), hc.getSecond()-1);
			Pair<Integer, Integer> right = new Pair<>(hc.getFirst(), hc.getSecond()+1);
			if (!(holesCords.contains(left) || holesCords.contains(right)))
				rslt++;
			}

		//at top of each column nwells are
		//max(min(colheightLeft, colheightRight), colheightright) - colHeight
		// ToIntBiFunction<Integer, Integer> max = (x, y) -> x > y ? x : y;	
		// ToIntBiFunction<Integer, Integer> min = (x, y) -> x < y ? x : y;	
		for (int i = 0; i < this.width; i++) {
			int maxmin;
			if (i == 0)
				maxmin = Math.max(colHeights[1], colHeights[i]);
			else if (i == this.width-1)
				maxmin = Math.max(colHeights[i-1], colHeights[i]);
			else
				maxmin = Math.max(Math.min(colHeights[i-1], colHeights[i+1]),
										colHeights[i]);
			rslt += maxmin - colHeights[i];
		} 
		return rslt;
	}

	int getNpatternDiversity() {
		//five possible patterns: 1. lh - rh =1, 2. lh - rh > 1,
		//3. lh - rh = 0, 4. lh - rh = -1, 5. lh -rh < -1.
		//(lh(rh) is left(right) column height)
		Set<Integer> patterns = new HashSet<>();
		Function<Integer, Integer> sgn = x -> x >= 0 ? 1 : -1;
		Function<Integer, Integer> truncate = x -> Math.abs(x) < 2 ? x : sgn.apply(x)*x; 
			
		for (int i = 0; i < this.width -1; i++) 
			patterns.add(truncate.apply(colHeights[i] - colHeights[i+1]));

		return patterns.size();
	}

	int getHolesDepth() {
		//contribution of each hole is 1 + num of holes directly below it, with no fillers
		// in between. 
		int rslt = 0;
		for(Pair<Integer, Integer> hc : holesCords) {
			rslt++;
			//for each hole directly below add 1 to the rslt.
			for (int i = hc.getFirst();
				 holesCords.contains(new Pair<Integer, Integer>(i+1, hc.getSecond()));
					 i++)
				rslt++;
		}
		return rslt;
	}

	int getSumHeightDiff() {
		int rslt = 0;
		int N = colHeights.length-1;
		for (int i = 0; i < N; i++)
			rslt += Math.abs(colHeights[i] - colHeights[i+1]);
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
		}

		//each delta diff in consecutive colHeights will contribute delta.
		rslt += sumHeightDiff;
		// for (int i = 0; i < N; i++)
		// 	rslt += Math.abs(colHeights[i] - colHeights[i+1]);

		//take contribution by walls
		rslt += 2*this.height - colHeights[0] - colHeights[colHeights.length - 1];

		return rslt;
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

		if (c == 0 || c == width-1) //holes at boundaries can not be open.
			return false;

		//get columns of left and right most sibling. 
		int lc = c; //column of left-most sibling.
		int rc = c; //column of right-most sibling.
		for(; holesCords.contains(new Pair<Integer, Integer>(r, lc-1));lc--);
		for(; holesCords.contains(new Pair<Integer, Integer>(r, rc+1));rc++);
		assert lc > 0 && rc < width-1;

		return r >= Math.min(colHeights[lc-1], colHeights[rc + 1]);
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
		//initialize to -1
		for (int i = 0; i < width ; i++) {
			colHeights[i] = 0;
		}
		for (int r = height-1; r >=0; r--)
			for (int c = 0; c < width; c++)
				if (board[r][c] && colHeights[c] == 0)
					colHeights[c] = r + 1;
		return colHeights;
	}

	Set<Pair<Integer, Integer>> getHolesCords(boolean[][] board) {
		Set<Pair<Integer, Integer>> rslt = new HashSet<>();
		for (int col = 0; col < width; col++)	
			for (int row = 0; row < colHeights[col]; row++)
				if (!board[row][col])
					rslt.add(new Pair<Integer, Integer> (row, col));
		return rslt;
	}
}

