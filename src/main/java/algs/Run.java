package ampi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;

import algorta.domains.mountaincar.MountainCar;
import algorta.domains.tetris.Tetris;
import algorta.domains.tetris.TetrisState;

public class Test {
	//args:
	// 	method: one of ampiq, cbmpi or cbmpi2
	//  regression Features, classification features, nrolloutSetSize, nrollout
	static int maxSim = 30;
	static double gamma = 0.9; //discount factor

	public static void main(String[] args) {
		Date yourDate = new Date();
		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-hh:mm");
		String date = DATE_FORMAT.format(yourDate);
		//redirecting output stream
		String file = args[0];
		for (int i = 1; i < args.length; i++)
			file = file + "_" + args[i];
		file += "_" + date + "_"+System.currentTimeMillis()+ ".log";
		//file = file + System.currentTimeMillis();
		try {
			System.out.println(file);
			System.setOut(new PrintStream(new File("scores/"+file)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		long t0 = System.currentTimeMillis();
		assert args.length == 11;
		String method = args[0], regFeature=args[1], classFeature=args[2], nrolloutSetSize=args[3], nrollout = args[4],
		uniformHeightSample = args[5], expReplay = args[6], paretoSample = args[7], paretoRollout = args[8], height = args[9], width = args[10];
		
		if (method.equals("ampiqtetris"))
			ampiq_tetris(regFeature, Integer.parseInt(nrolloutSetSize), Integer.parseInt(nrollout),
					Integer.parseInt(uniformHeightSample), Double.parseDouble(expReplay),
					paretoSample, paretoRollout, Integer.parseInt(height), Integer.parseInt(width));
		else if(method.equals("ampiqtetrisnew"))
			ampiq_tetris_newimpl(regFeature, Integer.parseInt(nrolloutSetSize), Integer.parseInt(nrollout),
					Integer.parseInt(uniformHeightSample), Double.parseDouble(expReplay),
					paretoSample, paretoRollout, Integer.parseInt(height), Integer.parseInt(width));
		else if(method.equals("cbmpitetris"))
			cbmpi_tetris(regFeature, classFeature, Integer.parseInt(nrolloutSetSize), Integer.parseInt(nrollout),
					Integer.parseInt(uniformHeightSample), Double.parseDouble(expReplay),
					paretoSample, paretoRollout, Integer.parseInt(height), Integer.parseInt(width), file+".txt");
		else if(method.equals("dpitetris"))
			dpi_tetris(regFeature, classFeature, Integer.parseInt(nrolloutSetSize), Integer.parseInt(nrollout),
					Integer.parseInt(uniformHeightSample), Double.parseDouble(expReplay),
					paretoSample, paretoRollout, Integer.parseInt(height), Integer.parseInt(width), file+".txt");
		else if (method.equals("ampiqmc"))
			ampiq_mc(regFeature, Integer.parseInt(nrolloutSetSize), Integer.parseInt(nrollout),
					Integer.parseInt(uniformHeightSample), Double.parseDouble(expReplay),
					paretoSample, paretoRollout, Integer.parseInt(height), Integer.parseInt(width));
		else if (method.equals("cbmpimc"))
			cbmpi_mc(regFeature, classFeature, Integer.parseInt(nrolloutSetSize), Integer.parseInt(nrollout),
					Integer.parseInt(uniformHeightSample), Double.parseDouble(expReplay),
					paretoSample, paretoRollout, Integer.parseInt(height), Integer.parseInt(width), file+".txt");
		else if (method.equals("lambdapitetris"))
			lambdapi_tetris(regFeature, classFeature, Integer.parseInt(nrolloutSetSize), Integer.parseInt(nrollout),
					Integer.parseInt(uniformHeightSample), Double.parseDouble(expReplay),
					paretoSample, paretoRollout, Integer.parseInt(height), Integer.parseInt(width), file+".txt");
		else
			return;
		System.out.println("\n Total time: " + ((System.currentTimeMillis() - t0)/1000 + " seconds"));
	}
	

	public static void ampiq_tetris(String featureSet, int rolloutSetSize, int nrollout, int uniformHeightSample, double expReplay, String paretoSample, String paretoRollout, int height, int width) {
		Tetris domain = new Tetris(1, height, width);
		Game game = new Game(domain);
		List<Double> initialBetas = new ArrayList<>();
		for (String s : game.getFeatureNames(featureSet)) {
			initialBetas.add(-1.);
		}
		Util.ActionType paretoSampleActionType = Util.ActionType.ANY;
		if(paretoSample.equals("dom")){
			paretoSampleActionType = Util.ActionType.DOM;
		}else if(paretoSample.equals("cum")){
			paretoSampleActionType = Util.ActionType.CUMDOM;
		}else if(paretoSample.equals("singlecue")){
			paretoSampleActionType = Util.ActionType.SINGLE_CUE;
		}

		Util.ActionType rolloutSampleActionType = Util.ActionType.ANY;
		if(paretoSample.equals("dom")){
			rolloutSampleActionType = Util.ActionType.DOM;
		}else if(paretoSample.equals("cum")){
			rolloutSampleActionType = Util.ActionType.CUMDOM;
		}else if(paretoSample.equals("singlecue")){
			rolloutSampleActionType = Util.ActionType.SINGLE_CUE;
		}
		AmpiQ ampiq = new AmpiQ(game, featureSet, maxSim, rolloutSetSize, nrollout, 1, gamma, initialBetas, paretoSampleActionType, rolloutSampleActionType, height, width, domain);
		ampiq.iterate();
	}

	public static void ampiq_tetris_newimpl(String featureSet, int rolloutSetSize, int nrollout, int uniformHeightSample, double expReplay, String paretoSample, String paretoRollout, int height, int width) {
//		Tetris domain = new Tetris(1, height, width);
		GameTetris game = new GameTetris();

		List<Double> initialBetas = new ArrayList<>();
		for (String s : game.getFeatureNames(featureSet)) {
			initialBetas.add(-0.);
		}
		Util.ActionType paretoSampleActionType = Util.ActionType.ANY;
		if(paretoSample.equals("dom")){
			paretoSampleActionType = Util.ActionType.DOM;
		}else if(paretoSample.equals("cum")){
			paretoSampleActionType = Util.ActionType.CUMDOM;
		}else if(paretoSample.equals("singlecue")){
			paretoSampleActionType = Util.ActionType.SINGLE_CUE;
		}

		Util.ActionType rolloutSampleActionType = Util.ActionType.ANY;
		if(paretoSample.equals("dom")){
			rolloutSampleActionType = Util.ActionType.DOM;
		}else if(paretoSample.equals("cum")){
			rolloutSampleActionType = Util.ActionType.CUMDOM;
		}else if(paretoSample.equals("singlecue")){
			rolloutSampleActionType = Util.ActionType.SINGLE_CUE;
		}
		AmpiQnewimpl ampiq = new AmpiQnewimpl(game, featureSet, maxSim, rolloutSetSize, nrollout, 1, gamma, initialBetas, paretoSampleActionType, rolloutSampleActionType, height, width);
		ampiq.iterate();
	}

	public static void cbmpi_tetris(String featureSetReg, String featureSetCla, int rolloutSetSize, int nrollout, int uniformHeightSample, double expReplay, String paretoSample, String claActionTypeStr, int height, int width, String reportName) {
		Random random = new Random();
		GameTetris game = new GameTetris();
		List<Double> initialBetasReg = new ArrayList<>();
		for (String s : game.getFeatureNames(featureSetReg)) {
			initialBetasReg.add(0.);
		}
//		initialBetasReg = Arrays.asList(new Double[]{ -13.08,-19.77,-9.22,-10.49,6.60,-12.63,-24.04,-1.61,0.});
		List<Double> initialBetasCla = new ArrayList<>();
		for (String s : game.getFeatureNames(featureSetCla)) {
			initialBetasCla.add(random.nextGaussian());
//			initialBetasCla.add(-1.);
		}
		Util.ActionType paretoSampleActionType = Util.ActionType.ANY;
		if(paretoSample.equals("dom")){
			paretoSampleActionType = Util.ActionType.DOM;
		}else if(paretoSample.equals("cum")){
			paretoSampleActionType = Util.ActionType.CUMDOM;
		}else if(paretoSample.equals("singlecue")){
			paretoSampleActionType = Util.ActionType.SINGLE_CUE;
		}

		Util.ActionType claActionType = Util.ActionType.ANY;
		if(claActionTypeStr.equals("dom")){
			claActionType = Util.ActionType.DOM;
		}else if(claActionTypeStr.equals("cum")){
			claActionType = Util.ActionType.CUMDOM;
		}else if(claActionTypeStr.equals("singlecue")){
			claActionType = Util.ActionType.SINGLE_CUE;
		}

		algorta.domains.tetris2.TetrisState.height = height;
		algorta.domains.tetris2.TetrisState.width = width;
		TetrisState.height = height;
		TetrisState.width = width;
		Cbmpi2 cbmpi = new Cbmpi2(game, featureSetReg, featureSetCla, maxSim, rolloutSetSize, nrollout, gamma, initialBetasReg, initialBetasCla, paretoSampleActionType, claActionType, "", height, width);
		cbmpi.iterate();
	}

	public static void dpi_tetris(String featureSetReg, String featureSetCla, int rolloutSetSize, int nrollout, int uniformHeightSample, double expReplay, String paretoSample, String claActionTypeStr, int height, int width, String reportName) {
		Random random = new Random();
		GameTetris game = new GameTetris();
		List<Double> initialBetasReg = new ArrayList<>();
		for (String s : game.getFeatureNames(featureSetReg)) {
			initialBetasReg.add(0.);
		}
//		initialBetasReg = Arrays.asList(new Double[]{ -13.08,-19.77,-9.22,-10.49,6.60,-12.63,-24.04,-1.61,0.});
		List<Double> initialBetasCla = new ArrayList<>();
		for (String s : game.getFeatureNames(featureSetCla)) {
			initialBetasCla.add(random.nextGaussian());
//			initialBetasCla.add(-1.);
		}
		Util.ActionType paretoSampleActionType = Util.ActionType.ANY;
		if(paretoSample.equals("dom")){
			paretoSampleActionType = Util.ActionType.DOM;
		}else if(paretoSample.equals("cum")){
			paretoSampleActionType = Util.ActionType.CUMDOM;
		}else if(paretoSample.equals("singlecue")){
			paretoSampleActionType = Util.ActionType.SINGLE_CUE;
		}

		Util.ActionType claActionType = Util.ActionType.ANY;
		if(claActionTypeStr.equals("dom")){
			claActionType = Util.ActionType.DOM;
		}else if(claActionTypeStr.equals("cum")){
			claActionType = Util.ActionType.CUMDOM;
		}else if(claActionTypeStr.equals("singlecue")){
			claActionType = Util.ActionType.SINGLE_CUE;
		}

		algorta.domains.tetris2.TetrisState.height = height;
		algorta.domains.tetris2.TetrisState.width = width;
		TetrisState.height = height;
		TetrisState.width = width;
		Dpi dpi = new Dpi(game, featureSetCla, maxSim, rolloutSetSize, nrollout, gamma, initialBetasCla, claActionType, "", height, width);
		dpi.iterate();
	}

	public static void lambdapi_tetris(String featureSetReg, String featureSetCla, int rolloutSetSize, int nrollout, int uniformHeightSample, double expReplay, String paretoSample, String claActionTypeStr, int height, int width, String reportName) {
		Random random = new Random();
		GameTetris game = new GameTetris();

		List<Double> initialBetasCla = new ArrayList<>();
		for (String s : game.getFeatureNames(featureSetCla)) {
			initialBetasCla.add(0.);
		}
		initialBetasCla.set(1, -10.);
		initialBetasCla.set(2, -1.);
		Util.ActionType paretoSampleActionType = Util.ActionType.ANY;
		if(paretoSample.equals("dom")){
			paretoSampleActionType = Util.ActionType.DOM;
		}else if(paretoSample.equals("cum")){
			paretoSampleActionType = Util.ActionType.CUMDOM;
		}else if(paretoSample.equals("singlecue")){
			paretoSampleActionType = Util.ActionType.SINGLE_CUE;
		}

		Util.ActionType claActionType = Util.ActionType.ANY;
		if(claActionTypeStr.equals("dom")){
			claActionType = Util.ActionType.DOM;
		}else if(claActionTypeStr.equals("cum")){
			claActionType = Util.ActionType.CUMDOM;
		}else if(claActionTypeStr.equals("singlecue")){
			claActionType = Util.ActionType.SINGLE_CUE;
		}

		algorta.domains.tetris2.TetrisState.height = height;
		algorta.domains.tetris2.TetrisState.width = width;
		TetrisState.height = height;
		TetrisState.width = width;
		LambdaPI lpi = new LambdaPI(game, featureSetCla, maxSim, gamma, 0.9, initialBetasCla, rolloutSetSize, claActionType, "", height, width);
		lpi.iterate();
	}

	public static void ampiq_mc(String featureSet, int rolloutSetSize, int nrollout, int uniformHeightSample, double expReplay, String paretoSample, String paretoRollout, int height, int width) {
		MountainCar domain = new MountainCar();
		Game game = new Game(domain);
		List<Double> initialBetas = new ArrayList<>();
		double[] betas = new double[]{1,-1,1.5,0,0,1.5,-1,-1,1.5};
		int i = 0;
		for (String s : game.getFeatureNames(featureSet)) {
//			initialBetas.add(0.);
			initialBetas.add(betas[i]);
			i++;
		}
//		initialBetas = Arrays.asList(betas);
		Util.ActionType paretoSampleActionType = Util.ActionType.ANY;
		if(paretoSample.equals("dom")){
			paretoSampleActionType = Util.ActionType.DOM;
		}else if(paretoSample.equals("cum")){
			paretoSampleActionType = Util.ActionType.CUMDOM;
		}else if(paretoSample.equals("singlecue")){
			paretoSampleActionType = Util.ActionType.SINGLE_CUE;
		}

		Util.ActionType rolloutSampleActionType = Util.ActionType.ANY;
		if(paretoSample.equals("dom")){
			rolloutSampleActionType = Util.ActionType.DOM;
		}else if(paretoSample.equals("cum")){
			rolloutSampleActionType = Util.ActionType.CUMDOM;
		}else if(paretoSample.equals("singlecue")){
			rolloutSampleActionType = Util.ActionType.SINGLE_CUE;
		}
		AmpiQ ampiq = new AmpiQ(game, featureSet, maxSim, rolloutSetSize, nrollout, 1, gamma, initialBetas, paretoSampleActionType, rolloutSampleActionType, height, width, domain);
		ampiq.iterate();
	}


	public static void cbmpi_mc(String featureSetReg, String featureSetCla, int rolloutSetSize, int nrollout, int uniformHeightSample, double expReplay, String paretoSample, String claActionTypeStr, int height, int width, String reportName) {
		MountainCar domain = new MountainCar();
		Random random = new Random();
		Game game = new Game(domain);
		List<Double> initialBetasReg = new ArrayList<>();
		for (String s : game.getFeatureNames(featureSetReg)) {
			initialBetasReg.add(0.);
		}
		List<Double> initialBetasCla = new ArrayList<>();
		for (String s : game.getFeatureNames(featureSetCla)) {
			initialBetasCla.add(random.nextGaussian());
		}
		Util.ActionType paretoSampleActionType = Util.ActionType.ANY;
		if(paretoSample.equals("dom")){
			paretoSampleActionType = Util.ActionType.DOM;
		}else if(paretoSample.equals("cum")){
			paretoSampleActionType = Util.ActionType.CUMDOM;
		}else if(paretoSample.equals("singlecue")){
			paretoSampleActionType = Util.ActionType.SINGLE_CUE;
		}

		Util.ActionType claActionType = Util.ActionType.ANY;
		if(claActionTypeStr.equals("dom")){
			claActionType = Util.ActionType.DOM;
		}else if(claActionTypeStr.equals("cum")){
			claActionType = Util.ActionType.CUMDOM;
		}else if(claActionTypeStr.equals("singlecue")){
			claActionType = Util.ActionType.SINGLE_CUE;
		}

//		Cbmpi2 cbmpi = new Cbmpi2(game, featureSetReg, featureSetCla, maxSim, rolloutSetSize, nrollout, gamma, initialBetasReg, initialBetasCla, paretoSampleActionType, claActionType, reportName, height, width, domain);
//		cbmpi.iterate();
	}
}
