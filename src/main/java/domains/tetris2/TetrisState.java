package algorta.domains.tetris2;

import algorta.domains.tetris.helpers.TetrisFeatureExtractor;
import algorta.rl.Primitive;
import algorta.rle.AbstractAction;
import algorta.rle.AbstractState;
import algorta.scratch.Feature;
import algorta.scratch.util.Compute;
import ampi.Util;
import org.apache.commons.math3.util.Pair;

import java.util.*;

import static algorta.domains.tetris2.Tetromino.pieces;

public class TetrisState extends AbstractState{

    boolean[][] board;
    public static int height = 16, width = 10, matHeight = height + 4;
	public TetrisFeatures features;
	Tetromino piece;
	Tetris tetris;
	String stringKey;


	public static void main(String[] args) {
		TetrisState tetris = new TetrisState(null);
        System.out.println("Num Placements "+tetris.getBoards().size());
		List<Pair<Action, TetrisFeatures>> actionFeatures = tetris.getActionFeatures();
		System.out.println(actionFeatures.size());
		int i = 0;
        for (Pair<boolean[][], TetrisFeatures> board: tetris.getBoards()) {
			tetris.printBoard(board.getFirst());
			TetrisFeatures features = board.getSecond();
			TetrisState state = tetris.copy();
			state.nextState(actionFeatures.get(i).getFirst());
			System.out.println(state.getString());
			state.printFeatures(features);
			i++;
		}
	}

	TetrisState(Tetris tetris, boolean[][] board, TetrisFeatures features, Tetromino piece){
		super(tetris);
		this.board = board;
		this.features = features;
		this.piece = piece;
	}

	public TetrisState(Tetris tetris) {
		super(tetris);
		int m = matHeight, n = width;
		board = new boolean[m][n];
		this.tetris = tetris;
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				board[i][j] = false;

		int[] colHeights = new int[n];
		for (int i = 0; i < n; i++)
			colHeights[i] = 0;

		int[] wellDepth = new int[n];
		for (int i = 0; i < n; i++)
			wellDepth[i] = 0;

        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++) {
//				if (i > 2 && i < 6 && j > 4)
//					board[i][j] = true;
//				if (j  > 7 && i < 6)
//					board[i][j] = true;
//				if (j != 6 && j != 7 && j != 8 && i == 1)
//					board[i][j] = true;
			}
//
//		board[1][9] = false;
//		board[0][9] = false;
//		board[1][8] = false;
//        colHeights[0]=0;
//        colHeights[1]=0;
//        colHeights[2]=0;
//        colHeights[3]=0;
//        colHeights[4]=0;
//        colHeights[5]=6;
//        colHeights[6]=6;
//        colHeights[7]=6;
//        colHeights[8]=6;
//        colHeights[9]=6;
//
//		wellDepth[6]=1;
//		wellDepth[3]=1;
//
//		wellDepth[9]=1;
//		int numHoles = 1;
//		int holesdepth = 1;
		ArrayList<Integer> rowsWithHoles = new ArrayList<>();//rowsWithHoles.add(0);
		ArrayList<Pair<Integer,Integer>> colsWithHoles = new ArrayList<>();//colsWithHoles.add(3);
		List<List<Integer>> wellDetail = new ArrayList<List<Integer>>();
		for (int i = 0; i < width; i++)
			wellDetail.add(new ArrayList<Integer>());

		int numHoles = 0;
		int numHolesChange = 0;
		int holesdepth = 0;

		features = new TetrisFeatures(colHeights, wellDepth, numHoles, numHolesChange, 0, 0, 0, holesdepth, wellDetail,
										new ArrayList<>(), 0, width, height*2, rowsWithHoles, colsWithHoles, 0, false);
		Random rand = new Random();
//		piece = pieces.get(rand.nextInt(pieces.size()));
		piece = Tetromino.S;
	}

	 void printBoard(boolean[][] board) {
		boolean[][] pieceMatrix = piece.getRotatedPiece(0);
		int pnrow = pieceMatrix.length;
		int pncol = pieceMatrix[0].length;
		String rslt = "";

		for (int i = pnrow - 1; i >= 0; i--) {
			for (int j = 0; j < pncol; j++)
				rslt += pieceMatrix[i][j] ? " 1 " : " 0 ";

			rslt += "\n";
		}


		int nrow = board.length;
		int ncol = board[0].length;
		rslt += "\n";
		for (int i = nrow-1; i >= 0; i--) {
			for (int j = 0; j < ncol; j++)
				rslt += board[i][j] ? " 1": " 0";
			rslt += "\n";
			if(i == height)
				rslt += "\n";
		}
		System.out.println(rslt);
	}

	public void print(){
		this.printBoard(board);
	}

	public void printFeatures(TetrisFeatures features) {
		System.out.println("numHoles: "+features.numHoles);
		System.out.println("holesDepth: "+features.holesDepth);
		System.out.println("numColTransitions: "+features.numColTransitions);
		System.out.println("numRowTransitions: "+features.numRowTransitions);
		System.out.println("Average Height Column: "+features.averageHeightColumn);
		System.out.println("Height Difference: "+features.heightDifference);
		System.out.println("landingHeight: "+features.landingHeight);
		System.out.print("colHeights: ");
		for (int i = 0; i < features.colHeights.length; i++) {
			System.out.print(features.colHeights[i]+" ");
		}
		System.out.println("");
		System.out.print("cumWells: " + features.cumulativeWells);
		System.out.println("");
		System.out.print("wellDepth: ");
		for (int i = 0; i < features.wellDepth.length; i++) {
			System.out.print(features.wellDepth[i]+" ");
		}
		System.out.println("");
		System.out.print("wellDetail: \n");
		for (int i = 0; i < features.wellDetail.size(); i++) {
			System.out.print("	col "+ i + " : ");
			for (Integer integer : features.wellDetail.get(i))
				System.out.print(integer+" ");
			System.out.println();
		}
		System.out.print("rowsWithHoles: ");
		for (int i = 0; i < features.rowsWithHoles.size(); i++) {
			System.out.print(features.rowsWithHoles.get(i)+" ");
		}
		System.out.println("");

		System.out.print("hole cordinates (row, column): ");
		for (int i = 0; i < features.coordinateHoles.size(); i++) {
			System.out.print(features.coordinateHoles.get(i)+" ");
		}
		System.out.println("");
		System.out.print("numLinesCleared: "+features.linesCleared+" ");
		System.out.println("");
		System.out.print("game over: "+features.gameOver+" ");
		System.out.println("");
	}

   public List<Pair<Action, TetrisFeatures>> getActionFeatures() {
	   List<Pair<Action, TetrisFeatures>> rslt = new ArrayList<>();
	   for (int rot = 0; rot < piece.getNumRotations(); rot++) {
		   	for (int col = 0; col <= width - piece.getRotatedPiece(rot)[0].length; col++) {
			   int elevation = getRightElevation(col, rot);
				TetrisFeatures newFeatures = getTetrisFeatures(col, elevation, rot);
				if(!newFeatures.gameOver)
					rslt.add(new Pair(new Action(col, rot), newFeatures));
			}
	   }
	   return rslt;
   }

	public List<Pair<Action, TetrisFeatures>> getActionFeaturesIncludingGameover() {
		List<Pair<Action, TetrisFeatures>> rslt = new ArrayList<>();
		for (int rot = 0; rot < piece.getNumRotations(); rot++) {
			for (int col = 0; col <= width - piece.getRotatedPiece(rot)[0].length; col++) {
				int elevation = getRightElevation(col, rot);
				if(features.colHeights[col] + elevation <= height) {
					TetrisFeatures newFeatures = getTetrisFeatures(col, elevation, rot);
					rslt.add(new Pair(new Action(col, rot), newFeatures));
				}
			}
		}
		return rslt;
	}

	//Returns the actionfeatures as a matrix of doubles
	public double[][] getDoubleMatrix(List<Pair<Action, TetrisFeatures>> actionFeatures, String featureSet) {
		double[][] objects = new double[actionFeatures.size()][features.getFeatureValues(featureSet).size()];
		for (int i = 0; i < actionFeatures.size(); i++) {
			List<Double> valuesList = actionFeatures.get(i).getSecond().getFeatureValues("bcts");
			for (int j = 0; j < valuesList.size(); j++) {
				objects[i][j] = valuesList.get(j);
			}
		}
		return objects;
	}
	public List<Pair<String, List<Double>>> getActionFeatures(String featureSet) {
		List<Pair<String, List<Double>>> rslt = new ArrayList<>();
		if(features.gameOver)
			return rslt;
		for (int rot = 0; rot < piece.getNumRotations(); rot++) {
			for (int col = 0; col <= width - piece.getRotatedPiece(rot)[0].length; col++) {
				int elevation = getRightElevation(col, rot);
				TetrisFeatures newFeatures = getTetrisFeatures(col, elevation, rot);
				Action a = new Action(col, rot);
				if(!newFeatures.gameOver)
					rslt.add(new Pair(a.name(), newFeatures.getFeatureValues(featureSet)));
			}
		}
		return rslt;
	}

	public List<Pair<String, List<Double>>> getActionFeaturesIncludingGameover(String featureSet) {
		List<Pair<String, List<Double>>> rslt = new ArrayList<>();
		if(features.gameOver)
			return rslt;
		for (int rot = 0; rot < piece.getNumRotations(); rot++) {
			for (int col = 0; col <= width - piece.getRotatedPiece(rot)[0].length; col++) {
				int elevation = getRightElevation(col, rot);
				if(features.colHeights[col] + elevation <= height) { //This is for the case when the piece lands COMPLETELY outside of the board.
					TetrisFeatures newFeatures = getTetrisFeatures(col, elevation, rot);
					Action a = new Action(col, rot);
					rslt.add(new Pair(a.name(), newFeatures.getFeatureValues(featureSet)));
				}
			}
		}
		return rslt;
	}

	List<Pair<boolean[][], TetrisFeatures>> getBoards() {
		List<Pair<boolean[][], TetrisFeatures>> rslt = new ArrayList<>();
		for (int rot = 0; rot < piece.getNumRotations(); rot++) {
			for (int col = 0; col <= width - piece.getRotatedPiece(rot)[0].length; col++) {
				int elevation = getRightElevation(col, rot);
				TetrisFeatures newFeatures = getTetrisFeatures(col, elevation, rot);
				rslt.add(new Pair(makeBoard(col, rot, elevation), newFeatures));
			}
		}
		return rslt;
	}

	private TetrisFeatures getTetrisFeatures(int col, int elevation, int rot) {
		int[] newColHeights = getNewColHeights(col, elevation, piece.getRotatedPieceHeights(rot));
		boolean gameOver = false;
		boolean[][] pieceMatrix = piece.getRotatedPiece(rot);
		int[] pieceHoles = piece.getRotatedPieceHoles(rot);
		int[] pieceHeights = piece.getRotatedPieceHeights(rot);

		if(Compute.max(newColHeights) > height){
			gameOver = true;
		}

//		int[] newWellDepth = getNewWellDepth(col, elevation, pieceMatrix);
		List<List<Integer>> newWellDetail = getNewWellDetail(col, elevation, pieceMatrix);
		Pair<List<Integer>, Integer> clearedRows = numClearedRows(col, pieceMatrix, newColHeights);
		Pair<Integer, List<Pair<Integer,Integer>>> newHoles = getNewHoles(col, pieceHoles, newColHeights, pieceHeights, (ArrayList<Pair<Integer,Integer>>) features.coordinateHoles);
		List<Integer> newRowsWithHoles = getNewRowsWithHoles(col, pieceHoles, newColHeights, pieceHeights, (ArrayList<Integer>) features.rowsWithHoles);
		int newColTransitions = getNewColTransitions(col, pieceHoles, newColHeights, pieceHeights);
		int newRowTransitions = getNewRowTransitions(col, elevation, pieceMatrix, newColHeights);
		double landingHeight = landingHeight(col, pieceMatrix, newColHeights);
		int newHolesDepth = getNewHolesDepth(col, newHoles.getSecond(), pieceHeights, pieceHoles);

		//At this point features are ready before clearing full rows.

		int numHoles = newHoles.getFirst();
		List<Pair<Integer,Integer>> holesCoordinates = newHoles.getSecond();

		//If rows are cleared or it is a gameover we use the featureExtractor to get the feature values. Maybe change later to make faster and prettier.
		if (clearedRows.getFirst().size() > 0 || gameOver) { //Gameover condition here is to replicate old version of tetris.
			int[][] intBoard = new int[height][width];
			boolean[][] boolBoard = makeBoard(col, rot, elevation);

			if(!gameOver) {
				for (int i = 0; i < clearedRows.getFirst().size(); i++) {//Clear Lines
					int row = clearedRows.getFirst().get(i) - i;
					for (int r = row; r < height - 1; r++) {
						for (int c = 0; c < width; c++) {
							boolBoard[r][c] = boolBoard[r + 1][c];
						}
					}
					for (int c = 0; c < width; c++)
						boolBoard[height - 1][c] = false;
				}
			}

//			printBoard(boolBoard);
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					intBoard[height - i - 1][j] = boolBoard[i][j] ? 1 : 0;
				}
			}
			algorta.domains.tetris.TetrisState statePrime = new algorta.domains.tetris.TetrisState(null, intBoard, algorta.domains.tetris.TetrisState.I());
			TetrisFeatureExtractor tfe = new TetrisFeatureExtractor(statePrime);
			tfe.calculateFeatures(statePrime);
			newColHeights = Util.ArrayDoubletoInt(tfe.featureValues(new TetrisFeatureExtractor.TetrisFeature[]{
					TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_ZERO,
					TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_ONE,
					TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_TWO,
					TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_THREE,
					TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_FOUR,
					TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_FIVE,
					TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_SIX,
					TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_SEVEN,
					TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_EIGHT,
					TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_NINE}));

//			newWellDepth = tfe.wellDepth;
			newWellDetail = tfe.wellDetail;
			numHoles = (int) tfe.featureValue(TetrisFeatureExtractor.TetrisFeature.HOLES);
			holesCoordinates = tfe.holesCoordinates;
			newHolesDepth = (int) tfe.featureValue(TetrisFeatureExtractor.TetrisFeature.HOLES_DEPTH);
			newColTransitions = (int) tfe.featureValue(TetrisFeatureExtractor.TetrisFeature.COLUMN_TRANSITIONS);
			newRowTransitions = (int) tfe.featureValue(TetrisFeatureExtractor.TetrisFeature.ROW_TRANSITIONS);
			newRowsWithHoles = tfe.rowsWithHolesIndices;
		}


		return new TetrisFeatures(newColHeights, new int[0],
				numHoles, numHoles - features.numHoles, features.pileHeight,
				features.averageHeightColumn, features.heightDifference,
				newHolesDepth, newWellDetail,
				clearedRows.getFirst(), clearedRows.getSecond(),
				newColTransitions, newRowTransitions, newRowsWithHoles,
				holesCoordinates, landingHeight, gameOver);
	}


	private int[] getNewWellDepth(int col, int elevation, boolean[][] pieceMatrix) {
		int[] newWellDepth = features.wellDepth.clone();
		int nrow = pieceMatrix.length;
		int ncol = pieceMatrix[0].length;

		//Left and Right border outside the piece:
		for (int i = 0; i < nrow; i++) {
			//0
			boolean minusOne = col-1 >= 0? board[features.colHeights[col] + i + elevation][col - 1] :true;
			boolean minusTwo = col-2 >= 0?board[features.colHeights[col] + i + elevation][col - 2]:true;
			if((board[features.colHeights[col] + i + elevation][col] || pieceMatrix[i][0]) &&
					!minusOne && minusTwo){
				newWellDepth[col-1] += 1;
			}
			//ncol
			boolean plusOne = col + ncol < width? board[features.colHeights[col] + i + elevation][col + ncol]:true;
			boolean plusTwo = col + ncol + 1 < width? board[features.colHeights[col] + i + elevation][col + ncol + 1]:true;
			if((board[features.colHeights[col] + i + elevation][col + ncol -1] || pieceMatrix[i][ncol-1]) &&
					!plusOne && plusTwo){
				newWellDepth[col + ncol] += 1;
			}
		}
		//Inside the piece:
		for (int i = 0; i < nrow; i++) {
			for (int j = 0; j < ncol - 2; j++) {
				boolean plusOne = col + j + 1 < width? board[features.colHeights[col] + i + elevation][col + j + 1]:true;
				boolean plusTwo = col + j + 2 < width? board[features.colHeights[col] + i + elevation][col + j + 2]:true;
				if((board[features.colHeights[col] + i + elevation][col + j] || pieceMatrix[i][j]) &&
						!plusOne && plusTwo){
					newWellDepth[col + j + 1] += 1;
				}
			}
		}
		return newWellDepth;
	}


	private List<List<Integer>> getNewWellDetail(int col, int elevation, boolean[][] pieceMatrix) {
//		int[] newWellDepth = features.wellDepth.clone();
		List<List<Integer>> wellDetail = new ArrayList<>();
		for (int i = 0; i < width; i++)//copying well details
			wellDetail.add(new ArrayList<>(features.wellDetail.get(i)));

		int nrow = pieceMatrix.length;
		int ncol = pieceMatrix[0].length;

		//Left and Right border outside the piece:
		for (int i = 0; i < nrow; i++) {
			//0
			boolean minusOne = col-1 >= 0? board[features.colHeights[col] + i + elevation][col - 1] :true;
			boolean minusTwo = col-2 >= 0?board[features.colHeights[col] + i + elevation][col - 2]:true;
			if((board[features.colHeights[col] + i + elevation][col] || pieceMatrix[i][0]) &&
					!minusOne && minusTwo){
//				newWellDepth[col-1] += 1;
				if(!wellDetail.get(col-1).contains(features.colHeights[col] + i + elevation))
					wellDetail.get(col-1).add(features.colHeights[col] + i + elevation);
			}else if(col-1 >=0 && pieceMatrix[i][0] && minusOne && minusTwo){//removing wells?
				if(wellDetail.get(col-1).contains(features.colHeights[col] + i + elevation))
					wellDetail.get(col-1).remove(new Integer(features.colHeights[col] + i + elevation));
			}
			//ncol
			boolean plusOne = col + ncol < width? board[features.colHeights[col] + i + elevation][col + ncol]:true;
			boolean plusTwo = col + ncol + 1 < width? board[features.colHeights[col] + i + elevation][col + ncol + 1]:true;
			if((board[features.colHeights[col] + i + elevation][col + ncol -1] || pieceMatrix[i][ncol-1]) &&
					!plusOne && plusTwo){
//				newWellDepth[col + ncol] += 1;
				if(!wellDetail.get(col + ncol).contains(features.colHeights[col] + i + elevation))
					wellDetail.get(col + ncol).add(features.colHeights[col] + i + elevation);
			}else if(col + ncol < width && pieceMatrix[i][ncol-1] && plusOne && plusTwo){//removing wells?
				if(wellDetail.get(col + ncol).contains(features.colHeights[col] + i + elevation))
					wellDetail.get(col + ncol).remove(new Integer(features.colHeights[col] + i + elevation));
			}
		}
		//Inside the piece:
		for (int i = 0; i < nrow; i++) {
			for (int j = -1; j < ncol; j++) {
				boolean plusOne = col + j + 1 < width? board[features.colHeights[col] + i + elevation][col + j + 1] :true;
				boolean plusTwo = col + j + 2 < width? board[features.colHeights[col] + i + elevation][col + j + 2] :true;
				boolean n = col + j >= 0? board[features.colHeights[col] + i + elevation][col + j] : true;
				plusOne = j+1 < ncol? plusOne || pieceMatrix[i][j+1] : plusOne;
				plusTwo = j+2 < ncol? plusTwo || pieceMatrix[i][j+2] : plusTwo;
				n = j >= 0? n || pieceMatrix[i][j]: n;
				if(n && !plusOne && plusTwo){
//					newWellDepth[col + j + 1] += 1;
					if(!wellDetail.get(col + j + 1).contains(features.colHeights[col] + i + elevation))
						wellDetail.get(col + j + 1).add(features.colHeights[col] + i + elevation);
				}else if(col + j + 1 < width && n && plusOne && plusTwo){//removing wells?
					if(wellDetail.get(col + j + 1).contains(features.colHeights[col] + i + elevation))
						wellDetail.get(col + j + 1).remove(new Integer(features.colHeights[col] + i + elevation));
				}
			}
		}
		return wellDetail;
	}


	private int getRightElevation(int col, int rot) {
		int elevation = 0;
		while (features.colHeights[col] + elevation < height && !fits(col, rot, elevation)){
			elevation++;
		}
		elevation--;
		while (features.colHeights[col] + elevation >= 0 && fits(col, rot, elevation)){
			elevation--;
		}
		elevation++;
		return elevation;
	}

	//This method returns the index of the rows that are cleared and the number of bricks of the piece.
	private Pair<List<Integer>, Integer> numClearedRows(int col, boolean[][] rotatedPiece, int[] newHeights) {
		List<Integer> fullRows = new ArrayList<>();
		int pieceHeight = rotatedPiece.length;
		int pieceWidth = rotatedPiece[0].length;
		int totalBrickPieces = 0;
		int maxNewHeight = maxNewHeight(col, rotatedPiece, newHeights);
		int initRow = maxNewHeight - pieceHeight;
		int finalRow = maxNewHeight;
		for (int r = initRow; r < finalRow; r++) {
			boolean fullRow = true;
			int brickPieces = 0;
			for (int c = 0; c < width; c++) {
				if (!board[r][c]) {
					if (c >= col && c - col < pieceWidth){//Piece Territory
						if (!rotatedPiece[r - initRow][c - col]) {
							fullRow = false;
							break;
						}else{
							brickPieces++;
						}
					}else{
						fullRow = false;
						break;
					}
				}
			}
			if(fullRow) {
				fullRows.add(r);
				totalBrickPieces += brickPieces;
			}
		}
		return new Pair(fullRows,totalBrickPieces);
	}

	private int getNewHolesDepth(int col, List<Pair<Integer,Integer>> newColCoordinates, int[] pieceHeights, int[] pieceHoles) {
		int holesDepth = 0;
		for (int i = col; i < col + pieceHoles.length; i++) {
			if(columnHasHoles(newColCoordinates, i))
				holesDepth += pieceHeights[i - col] - pieceHoles[i - col];
		}
		return holesDepth + features.holesDepth;
	}

	private boolean columnHasHoles(List<Pair<Integer,Integer>> colCoordinates, int column){
		for (int i = 0; i < colCoordinates.size(); i++)
			if(colCoordinates.get(i).getSecond() == column)
				return true;

		return false;
	}

	private int getNewRowTransitions(int col, int elevation, boolean[][] pieceMatrix, int[] newHeights) {
		int newRowTransitions = 0;
		int nrow = pieceMatrix.length;
		int ncol = pieceMatrix[0].length;

		int maxNewHeight = maxNewHeight(col, pieceMatrix, newHeights);

		int initRow = maxNewHeight - nrow;
		int finalRow = maxNewHeight;

		int oldChunkTransitions = 0;
		for (int i = initRow; i < finalRow; i++) {//i is relative to the board
			boolean leftPadding = col - 1 >= 0 ? board[i][col - 1] : true;
			boolean rightPadding = col + ncol < width ? board[i][col + ncol] : true;

			if (leftPadding != board[i][col]) oldChunkTransitions++;
			if (rightPadding != board[i][col + ncol -1]) oldChunkTransitions++;

			for (int j = 0; j < ncol - 1; j++)
				if (board[i][col + j] != board[i][col + j + 1])
					oldChunkTransitions++;

			if (leftPadding != (board[i][col] || pieceMatrix[i - initRow][0]))
				newRowTransitions++;

			if (rightPadding != (board[i][col + ncol -1] || pieceMatrix[i - initRow][ncol-1]))
				newRowTransitions++;


			for (int j = 0; j < ncol - 1; j++)
				if ((board[i][col + j] || pieceMatrix[i - initRow][j]) !=
						(board[i][col + j + 1] || pieceMatrix[i - initRow][j + 1]))
					newRowTransitions++;
		}

		return newRowTransitions - oldChunkTransitions + features.numRowTransitions;
	}

	//this method returns the maximum height where the piece was placed
	private int maxNewHeight(int col, boolean[][] rotatedPiece, int[] newHeights){
		int maxNewHeight = 0;
		int pieceWidth = rotatedPiece[0].length;
		for (int i = 0; i < pieceWidth; i++)
			if(newHeights[i+col] > maxNewHeight)
				maxNewHeight = newHeights[i+col];

		return maxNewHeight;
	}

	//
	private double landingHeight(int col, boolean[][] rotatedPiece, int[] newHeights){
		int maxNewHeight = maxNewHeight(col, rotatedPiece, newHeights);
		int pieceHeight = rotatedPiece.length;
		return ((double)maxNewHeight - (double)pieceHeight/2);
	}

	private int getNewColTransitions(int col, int[] pieceHoles, int[] newHeights, int[] pieceHeights) {
		int newColTransitions = 0;
		for (int i = col; i < col + pieceHoles.length; i++) {
			newColTransitions +=  (newHeights[i] - features.colHeights[i] - pieceHeights[i - col] + pieceHoles[i - col] > 0)?2:0;
		}
		return newColTransitions + features.numColTransitions;
	}

	//this method returns the number of holes and the coordinates of the holes
	private Pair<Integer, List<Pair<Integer,Integer>>> getNewHoles(int col, int[] pieceHoles, int[] newHeights, int[] pieceHeights, ArrayList<Pair<Integer,Integer>> rowCoordinates) {
		int newHoles = 0;
		ArrayList<Pair<Integer,Integer>> newHolesIndex = (ArrayList<Pair<Integer,Integer>>)(rowCoordinates).clone();
		for (int i = col; i < col + pieceHoles.length; i++) {
			int newHoleDelta = newHeights[i] - features.colHeights[i] - pieceHeights[i - col] + pieceHoles[i - col];
			newHoles += newHoleDelta;
			if(newHoleDelta > 0)
				for (int row = features.colHeights[i]; row < (newHeights[i] - pieceHeights[i - col]+ pieceHoles[i - col]); row++)
				 	newHolesIndex.add(new Pair(row , i));
		}
		return new Pair(newHoles + features.numHoles, newHolesIndex);
	}

	//this method returns the rows with holes
	private List<Integer> getNewRowsWithHoles(int col, int[] pieceHoles, int[] newHeights, int[] pieceHeights, ArrayList<Integer> rowsWithHoles) {
		ArrayList<Integer> newHolesIndex = (ArrayList)(rowsWithHoles).clone();
		for (int i = col; i < col + pieceHoles.length; i++) {
			int holeLowLimit = features.colHeights[i];
			int holeUpperLimit = newHeights[i]- pieceHeights[i - col] + pieceHoles[i - col];
			for (int j = holeLowLimit; j < holeUpperLimit; j++) {
				if(!newHolesIndex.contains(j))
					newHolesIndex.add(j);
			}
		}
		return newHolesIndex;
	}

	/**
	 * Method calculates the new column heights.
	 * The changes only happen in the columns where the piece is placed. These columns: (col, col + pieceWidth)
	 * The height changes in this way:
	 * colHeight[thisColumn] = elevation  + colHeight[col] + pieceHeight[thisColumn]
	 * thisColumn ranges from 'col' to 'col + pieceWidth'.
	 * @param col
	 * @param elevation
	 * @param pieceHeights
     * @return
     */
	private int[] getNewColHeights(int col, int elevation, int[] pieceHeights) {
		int[] newColHeights = features.colHeights.clone();
		for (int i = col; i < col + pieceHeights.length; i++) {
			newColHeights[i] = elevation + features.colHeights[col] + pieceHeights[i - col];
		}
		return newColHeights;
	}

	boolean fits(int col, int rotation, int elevation) {
		boolean[][] pieceMatrix = piece.getRotatedPiece(rotation);
		int[] pieceHoles = piece.getRotatedPieceHoles(rotation);
		for (int i = 0; i < pieceHoles.length; i++)
			if(features.colHeights[col] + elevation < features.colHeights[col+i] - pieceHoles[i])
				return false;

		int nrow = pieceMatrix.length;
		int ncol = pieceMatrix[0].length;
		for (int i = 0; i < nrow; i++) 
			for (int j = 0; j < ncol; j++) {
                if (col + j >= width)
                    return false;
				try {
					if (pieceMatrix[i][j] && board[features.colHeights[col] + i + elevation][col + j])
						return false;
				}catch(ArrayIndexOutOfBoundsException exception){//GameOver, but the piece fits.
					return true;
				}
            }
		return true;	
	}

	boolean[][] makeBoard(int col, int rotation, int elevation) {
		boolean[][] rslt = new boolean[matHeight][width];
		int m = board.length;
		int n = board[0].length;
		for (int i = 0; i < m; i++) 
			for (int j = 0; j < n; j++) 
				rslt[i][j] = board[i][j];

		boolean[][] pieceMatrix = piece.getRotatedPiece(rotation);
		int nrow = pieceMatrix.length;
		int ncol = pieceMatrix[0].length;
		for (int i = 0; i < nrow; i++) 
			for (int j = 0; j < ncol; j++) 
				rslt[features.colHeights[col] + i + elevation][col+j] = pieceMatrix[i][j] || rslt[features.colHeights[col] + i + elevation][col+j];

		return rslt;
	}

	@Override
	public List<Primitive> getPrimitives() {
		return null;
	}

	public TetrisState copy(){
		boolean[][] board = new boolean[matHeight][width];
		for (int i = 0; i < matHeight; i++) {
			for (int j = 0; j < width; j++) {
				board[i][j] = this.board[i][j];
			}
		}
		return new TetrisState(tetris, board, features.copy(), piece.copy());
	}

	@Override
	public boolean isGameOver() {
		return features.gameOver;
	}

	public void nextState(Action a){
		nextState(a.col, a.rot);
	}

	public void nextState(int col, int rot){
		nextState(col, rot, null);
	}
	//This method changes the state for the new one. If the old state is needed, copy should be called first.
	public void nextState(int col, int rot, Random random) {
		int elevation = getRightElevation(col, rot);
		boolean[][] pieceMatrix = piece.getRotatedPiece(rot);

		//calculate new features:
		TetrisFeatures newFeatures = getTetrisFeatures(col, elevation, rot);

		//make piece part of board:
		int nrow = pieceMatrix.length;
		int ncol = pieceMatrix[0].length;
		for (int i = 0; i < nrow; i++)
			for (int j = 0; j < ncol; j++)
				board[features.colHeights[col] + i + elevation][col + j] = pieceMatrix[i][j] || board[features.colHeights[col] + i + elevation][col + j];


		if(!newFeatures.gameOver) {
			for (int i = 0; i < newFeatures.linesCleared.size(); i++) {//Clear Lines
				int row = newFeatures.linesCleared.get(i) - i;
				for (int r = row; r < height - 1; r++) {
					for (int c = 0; c < width; c++) {
						board[r][c] = board[r + 1][c];
					}
				}
				for (int c = 0; c < width; c++)
					board[height - 1][c] = false;
			}
		}
		features = newFeatures;
		if(random == null)
		 	random = new Random();
		piece = pieces.get(random.nextInt(pieces.size()));
	}


	public class Action extends AbstractAction implements Primitive{
		public final int col;
		public final int rot;
		public Action(int col, int rot){
			super(col+"_"+rot, col*10+rot);
			this.col = col;
			this.rot = rot;
		}
	}

	public String getString(){
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(piece.name());
		stringBuilder.append("|");
		for (int row = 0; row < features.pileHeight; row++) {
			for (int column = 0; column < width; column++) {
				if(board[row][column]) {
					stringBuilder.append(column);
					stringBuilder.append(":");
				}
			}
			stringBuilder.append("|");
		}
		stringKey = stringBuilder.toString();
		return stringKey;
	}

	public TetrisState parseState(String stateStr) {
		boolean[][] board = new boolean[TetrisState.matHeight][TetrisState.width];
		String[] rows = stateStr.split("\\|");
		Tetromino piece = Tetromino.getPiece(rows[0]);
		int rowNum = TetrisState.height;
		for (int i = 1; i < rows.length; i++) {
			String row = rows[i];
			String[] cols = row.split(":");
			for (String col : cols) {
				if (!col.equals("")) {
					board[i-1][Integer.parseInt(col)] = true;
				}
			}
		}
		return new TetrisState((Tetris)domain, board, getTetrisFeatures(board),piece);
	}

	private TetrisFeatures getTetrisFeatures(boolean[][] board) {
		int[][] intBoard = new int[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				intBoard[height - i - 1][j] = board[i][j] ? 1 : 0;
			}
		}
		TetrisState.height = height;
		TetrisState.width = width;
		algorta.domains.tetris.TetrisState statePrime = new algorta.domains.tetris.TetrisState(null, intBoard, algorta.domains.tetris.TetrisState.I());
		TetrisFeatureExtractor tfe = new TetrisFeatureExtractor(statePrime);
		tfe.calculateFeatures(statePrime);
		int[] newColHeights = Util.ArrayDoubletoInt(tfe.featureValues(new TetrisFeatureExtractor.TetrisFeature[]{
				TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_ZERO,
				TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_ONE,
				TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_TWO,
				TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_THREE,
				TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_FOUR,
				TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_FIVE,
				TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_SIX,
				TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_SEVEN,
				TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_EIGHT,
				TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_NINE}));

		int[] newWellDepth = tfe.wellDepth;
		List<List<Integer>> newWellDetail = tfe.wellDetail;
		int numHoles = (int) tfe.featureValue(TetrisFeatureExtractor.TetrisFeature.HOLES);
		List<Pair<Integer,Integer>> colsWithHoles = tfe.holesCoordinates;
		int newHolesDepth = (int) tfe.featureValue(TetrisFeatureExtractor.TetrisFeature.HOLES_DEPTH);
		int newColTransitions = (int) tfe.featureValue(TetrisFeatureExtractor.TetrisFeature.COLUMN_TRANSITIONS);
		int newRowTransitions = (int) tfe.featureValue(TetrisFeatureExtractor.TetrisFeature.ROW_TRANSITIONS);
		List<Integer> newRowsWithHoles = tfe.rowsWithHolesIndices;
		Pair<List<Integer>, Integer> clearedRows = new Pair(new ArrayList<>(), 0);
		return new TetrisFeatures(newColHeights, newWellDepth,
				numHoles, 0, 0, 0, 0, newHolesDepth, newWellDetail,
				clearedRows.getFirst(), clearedRows.getSecond(),
				newColTransitions, newRowTransitions, newRowsWithHoles,
				colsWithHoles, 0, false);
	}


}
