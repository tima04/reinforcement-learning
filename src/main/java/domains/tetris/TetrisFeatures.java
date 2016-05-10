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
	public final int landingHeight;
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
	// TetrisFeatures(int height, int width, int[] colHeights,
	// 			   Set<Pair<Integer, Integer>> holesCords, boolean gameOver,
	// 			   int nClearedLines, int nBrickCleared) {
	TetrisFeatures(Builder builder) {
		this.height = builder.height;
		this.width = builder.width;
		this.colHeights = builder.colHeights;
		this.holesCords = builder.holesCords;
		this.gameOver = builder.gameOver;
		this.nClearedLines = builder.nClearedLines;
		this.nBrickCleared = builder.nBrickCleared;
		//this.rowsWithHoles = getRowsWithHoles();
		this.nRowsWithHoles = holesCords
			.stream()
			.map(p -> p.getFirst())
			.collect(Collectors.toSet()).size();
		this.landingHeight = 0;
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
		private Integer height = null;
		private Integer width = null;
		private Set<Pair<Integer, Integer>> holesCords = null;
		private int[] colHeights = null;
		private Boolean gameOver = null;
		private Integer nClearedLines = null;
		private Integer nBrickCleared = null;

		public Builder(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public Builder holesCords(Set<Pair<Integer, Integer>> val) {
			this.holesCords = val;
			return this;
		}


		public Builder height(int val) { this.height = val; return this;}

		public Builder width(int val) { this.width= val; return this;}

		public Builder colHeights(int[] val) { this.colHeights = val; return this;}

		public Builder nClearedLines(int val) { this.nClearedLines = val; return this;}

		public Builder nBrickCleared(int val) { this.nBrickCleared = val; return this;}

		public Builder gameOver (boolean val) { this.gameOver = val; return this;}

		public TetrisFeatures build() {
			if (holesCords == null || nClearedLines == null ||
				nBrickCleared == null || gameOver == null)
				throw new RuntimeException("please supply all arguments!");
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
		ToIntBiFunction<Integer, Integer> max = (x, y) -> x > y ? x : y;	
		ToIntBiFunction<Integer, Integer> min = (x, y) -> x < y ? x : y;	
		for (int i = 0; i < this.width; i++) {
			int maxmin;
			if (i == 0)
				maxmin = max.applyAsInt(colHeights[1], colHeights[i]);
			else if (i == this.width-1)
				maxmin = max.applyAsInt(colHeights[i-1], colHeights[i]);
			else
				maxmin = max.applyAsInt(min.applyAsInt(colHeights[i-1], colHeights[i+1]),
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
		int N = colHeights.length;
		for (int i = 0; i < N; i++)
			rslt += Math.abs(colHeights[i] - colHeights[i+1]);
		return rslt;
	}

	int getRowTransition() {
		//each consecutive sequence of holes (from left to right) contributes 2,
		//"f h h f", will contribute same as "f h f", (f is filled and h is hole).

		// moving from left to right change in colHeight by amount delta contributes,
		// delta, its also assumed that left and right wall are full cols, so for
		// example if height is 4 and width is 3 and colHeights is {2,3,2}, then
		// contribution will be |4-2| + |2-3| + |3-2| + |2-3| + |2-4| = 6.
		int rslt = 0;

		//for each hole if right cell is not a hole increment rslt by 2.
		for (Pair<Integer, Integer> hole : holesCords ) {
			if (! holesCords.contains(new Pair(hole.getFirst(), hole.getSecond()+1)))
								rslt += 2;
		}

		//each delta diff in consecutive colHeights will contribute delta.
		rslt += sumHeightDiff;
		// for (int i = 0; i < N; i++)
		// 	rslt += Math.abs(colHeights[i] - colHeights[i+1]);

		//take contribution by walls
		rslt += 2*this.height - colHeights[0] - colHeights[colHeights.length];

		return rslt;
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
		//for each column if it's height is less then board height increment rslt by 1.
		for (int ch : colHeights)
			if (ch < this.height)
				rslt++;
		return rslt;
	}

}

