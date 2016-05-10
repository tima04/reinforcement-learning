package domains.tetris; 

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

public class TetrisFeatures {

	public final int height; //board height
	public final int width; //board width
	public final int[] colHeights;
	public final Set<Pair<Integer, Integer>> holesCords;
	public final int colTransition;
	public final int rowTransition;
	public final Set<Integer> rowsWithHoles;
	public final int landingHeight;
	public final int cumWells;
	public final int erodedCells;
	public final int colDiff;
	public final int pileHeight; //max col height;
	public final boolean gameOver;

	TetrisFeatures(int height, int width, int[] colHeights,
				   Set<Pair<Integer, Integer>> holesCords, boolean gameOver) {
		this.height = height;
		this.width = width;
		this.colHeights = colHeights;
		this.holesCords = holesCords;
		this.colTransition = getColTransition();
		this.rowTransition = getRowTransition(); 
		//this.rowsWithHoles = getRowsWithHoles();
		this.rowsWithHoles = holesCords
			.stream()
			.map(p -> p.getFirst())
			.collect(Collectors.toSet());
		this.landingHeight = 0;
		this.cumWells = 0;
		this.erodedCells = 0;
		this.colDiff = 0;
		this.pileHeight = Arrays.stream(colHeights).max().getAsInt();
		this.gameOver = gameOver;
	}	

	// not using it.
	Set<Integer> getRowsWithHoles() {
		Set<Integer> rslt = new HashSet<>();
		for (Pair<Integer, Integer> p : holesCords)
			rslt.add(p.getFirst());
		return rslt;
	}

	
	int getRowTransition() {
		//each consecutive sequence of holes (from left to right) contributes 2,
		// f h h f, will contribute same as f h f, (f is filled and h is hole).

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

		int N = colHeights.length;
		for (int i = 0; i < N; i++)
			rslt += Math.abs(colHeights[i] - colHeights[i+1]);
		//take contribution by walls
		rslt += 2*this.height - colHeights[0] - colHeights[N];

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

