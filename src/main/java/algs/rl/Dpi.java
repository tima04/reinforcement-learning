package algs.rl;


import algs.Game;
import domains.Action;
import domains.FeatureSet;
import domains.Task;
import domains.tetris.*;
import fr.inria.optimization.cmaes.CMAEvolutionStrategy;
import fr.inria.optimization.cmaes.CMASolution;
import fr.inria.optimization.cmaes.fitness.IObjectiveFunction;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Pair;
import util.Compute;
import util.RolloutUtil;
import util.UtilAmpi;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Dpi {

	static boolean firstRolloutBcts = true;
	static boolean subsampingUniformHeight = false;


	public static void main(String[] arg){

		Random random = new Random();
		long seed = random.nextInt();
//		long seed = 1;

		random = new Random(seed);
		FeatureSet featureSet = new TetrisFeatureSet("thierry");
		Game game = new Game(random, new TetrisTaskLines(1));
		List<Double> initialWeights = new ArrayList<>();
		for (String name: game.getFeatureNames(featureSet))
			initialWeights.add(random.nextGaussian());


//		initialWeights = TetrisWeightVector.make("DT10"); best.
		int numIt = 15;
		double gamma = 1;
		int sampleSize = 20000;
		int nrollout = 5;
		TetrisParameters.getInstance().setSize(10,10);

		setOutput("dpi_"+featureSet.name()+"_"+sampleSize+"_"+arg[0]);
		System.out.println("seed:" + seed);

		UtilAmpi.ActionType  actionType = UtilAmpi.ActionType.ANY;
		if(arg[0].equals("dom"))
			actionType = UtilAmpi.ActionType.DOM;
		else if (arg[0].equals("cum"))
			actionType = UtilAmpi.ActionType.CUMDOM;

		Dpi dpi = new Dpi(game, featureSet, numIt, sampleSize, nrollout, gamma, 10, initialWeights, actionType, random);
		dpi.iterate();
	}

	private static void setOutput(String fileName) {
		Date yourDate = new Date();
		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-hh:mm");
		String date = DATE_FORMAT.format(yourDate);
		//redirecting output stream
		fileName += "_" + date + "_"+System.currentTimeMillis()+ ".log";
		//file = file + System.currentTimeMillis();
		try {
			System.out.println(fileName);
//			System.setOut(new PrintStream(new File("src/main/resources/tetris/scores/dpi/"+fileName)));
			System.setOut(new PrintStream(new File("scores/dpi/"+fileName)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	int maxSim, rolloutSetSize, nRollout;
	FeatureSet featureSetClassification;
	double gamma; //discount factor
	List<Double> betaReg = null, betaCl = null;
	Game game = null;
	public UtilAmpi.ActionType actionType = null;
	public List<Object> rolloutSet = null;
	Random random;
	int rounds = 100;
	int samplingFactor = 7; //Size of the rollout set before subsampling is samplingFactor * rolloutSetSize.
	Task task = new TetrisTaskLines(1);
	int cmaesIterations;

	final FeatureSet paretoFeatureSet = new TetrisFeatureSet("bcts");
	//	final double[] paretoWeights = new double[]{-13.08, -19.77, -9.22, -10.49, 6.60, -12.63, -24.04, -1.61};
	final static double[] paretoWeights = new double[]{-5, -6, -2, -3, 1, -4, -7, -1}; //It should have the same effect since the direction and order are the same.


	public Dpi(Game game, FeatureSet featureSetClassification, int maxSim,
			   int rolloutSetSize, int nRollout, double gamma, int cmaesIterations, List<Double> betaCl,
			   UtilAmpi.ActionType actionTypeCla, Random random) {
		// beta is stateAction features, and theta is state features
		System.out.println("TetrisBoard:" + TetrisState.height+"x"+TetrisState.width);
		System.out.println("First Rollout BCTS: " + firstRolloutBcts);
		System.out.println("Subsampling Uniform Height :" + subsampingUniformHeight);
		System.out.println("Sample size:" + rolloutSetSize);
		System.out.println("Rollout length:" + nRollout);
		System.out.println("Feature set: " +featureSetClassification.name());
		System.out.println("gamma: " + gamma);
		System.out.println("Action type: " + actionTypeCla);
		System.out.println("fixing seed for rollouts");
//		System.out.println("picking cmaes run based on score");
		this.game = game;
		this.featureSetClassification = featureSetClassification;
		this.maxSim = maxSim;
		this.cmaesIterations = cmaesIterations;
		this.rolloutSetSize = rolloutSetSize;
		this.nRollout = nRollout;
		this.gamma = gamma;
		this.betaReg = new ArrayList<>(Collections.nCopies(betaCl.size(), 0.));
		this.betaCl = betaCl;
		this.rolloutSet = new ArrayList<Object>();
		this.actionType = actionTypeCla;
		this.random = random;
		System.out.println("****************************");
		System.out.println("performance in 100 rounds: ");
		double[] betaVector = new double[betaCl.size()];
		for (int i = 0; i < betaVector.length; i++)
			betaVector[i] = betaCl.get(i);
		EvaluateTetrisAgent.gamesTetris(100, random, featureSetClassification, betaCl, actionType, paretoFeatureSet, paretoWeights, true);
		System.out.println("****************************");
	}


	public List<Double> iterate() {
		for (int k = 0; k < this.maxSim; k++) {
			long t0 = System.currentTimeMillis();
			if(!subsampingUniformHeight)
				samplingFactor = 1;

//			if(k==0 && firstRolloutBcts){//Take first rollout set from good policy.
//				this.rolloutSet = RolloutUtil.getRolloutSetTetris(this.game, this.rolloutSetSize * samplingFactor, Arrays.asList(new Double[]{ -13.08,-19.77,-9.22,-10.49,6.60,-12.63,-24.04,-1.61,0.}), new TetrisFeatureSet("thierry"), actionType, paretoFeatureSet, paretoWeights, random);
//				this.rolloutSet = RolloutUtil.getRolloutSetTetrisGabillon("src/main/resources/tetris/rawGames/sample_gabillon/record_du10++cat.txt", random);
				this.rolloutSet = RolloutUtil.getRolloutSetTetrisGabillon("gabillon_sample/record_du10++cat.txt", random, this.rolloutSetSize);
//			}else {
//				this.rolloutSet = RolloutUtil.getRolloutSetTetris(this.game, this.rolloutSetSize * samplingFactor, this.betaCl, this.featureSetClassification, actionType, paretoFeatureSet, paretoWeights, random);
//			}

			if(subsampingUniformHeight)
				this.rolloutSet = UtilAmpi.getSubSampleWithUniformHeightNewTetris(rolloutSet, this.rolloutSetSize);

			System.out.println(String.format("sampling rollout set (%s samples) took: %s seconds", rolloutSet.size(), (System.currentTimeMillis() - t0) / (1000.0)));

			long t1 = System.currentTimeMillis();
			ObjectiveFunction of = new ObjectiveFunction();
			for (int i = 0; i < this.rolloutSet.size(); i++) {
				Object state = this.rolloutSet.get(i);
				Pair<List<List<Double>>, List<Double>> trainingSet = getFeatureEstimatedQ(state, nRollout, betaReg, betaCl, actionType);

				if(trainingSet.getSecond().size() > 0)
					of.addChoice(trainingSet);

			}
			System.out.println(String.format("creating training set took: %s seconds", (System.currentTimeMillis() - t1) / (1000.0)));

			long t2 = System.currentTimeMillis();

			List<Double> newbetaCl = minimizeUsingCMAES(of);
			this.betaCl = newbetaCl;
			System.out.println("betaCL: " + this.betaCl);

			System.out.println(String.format("minimizing fitness function took: %s seconds", (System.currentTimeMillis() - t2) / (1000.0)));

			System.out.println("****************************");
			System.out.println("performance in " +rounds+ " rounds: ");
			double[] betaVector = new double[betaCl.size()];
			for (int i = 0; i < betaVector.length; i++)
				betaVector[i] = betaCl.get(i);
			EvaluateTetrisAgent.gamesTetris(rounds, random, featureSetClassification, betaCl, actionType, paretoFeatureSet, paretoWeights, true);
			System.out.println(String.format("This iteration took: %s seconds", (System.currentTimeMillis() - t0) / (1000.0)));
			System.out.println("****************************");
		}
		return betaCl;
	}


	private Pair<List<List<Double>>, List<Double>> getFeatureEstimatedQ (Object state, int nrollout, List<Double> betaReg,
																		 List<Double> betaCl, UtilAmpi.ActionType actionType) {
//		List<Pair<String,List<Double>>> actions = this.game.getStateActionFeatureValues(featureSetClassification, state, actionType, paretoFeatureSet, paretoWeights);
		List<Pair<Action,List<Double>>> actions = this.game.getStateActionFeatureValues(featureSetClassification, state, actionType, paretoFeatureSet, paretoWeights);

		List<List<Double>> features = new ArrayList<>();
		List<Double> utils = new ArrayList<>();

//		((TetrisState)state).print();
		long rolloutSeed = random.nextInt();
		double maxQEstimate = Double.NEGATIVE_INFINITY;
		double minQEstimate = Double.POSITIVE_INFINITY;
		for (Pair<Action,List<Double>> action : actions) {
				Pair<Object, Action> stateAction = new Pair<>(state, action.getFirst());
					features.add(action.getSecond());
//			Pair<Object, Double> stateReward = this.game.getNewStateAndReward(state, action.getFirst());
				double qEstimate = 0;
				int averageOver = 1;
				for (int i = 0; i < averageOver; i++) {
					Random rolloutRandom = new Random(rolloutSeed+i);
					qEstimate = qEstimate + RolloutUtil.doRolloutTetrisIterative(stateAction, nrollout, betaReg, betaCl,
							gamma, featureSetClassification, featureSetClassification, rolloutRandom, task, actionType, paretoFeatureSet, paretoWeights);
				}
				if(qEstimate > maxQEstimate)
					maxQEstimate = qEstimate;

				if(qEstimate < minQEstimate)
					minQEstimate = qEstimate;

				utils.add(qEstimate / averageOver);
		}
//		List<Double> normalizedUtils = new ArrayList<>();
//		if(!utils.isEmpty()) {
//			double min = Collections.min(utils);
//			double maxUtil = Collections.max(utils);
//			double normalizingFactor = maxUtil + Math.abs(min);
//			if(normalizingFactor != 0) {
//				for (Double util : utils) {
//					double normalizedUtil = (util + Math.abs(min)) / (normalizingFactor);
//					assert normalizedUtil <= 1 && normalizedUtil >= 0;
//					normalizedUtils.add(normalizedUtil);
//				}
//			}
//		}
//		return new Pair<>(features, normalizedUtils);
		if(maxQEstimate == minQEstimate)
			utils = new ArrayList<>();
		return new Pair<>(features, utils);
	}



	private List<Double> minimizeUsingCMAES(ObjectiveFunction fitfun){
		fitfun.printMaxQEstimate();
		double leastFitness = Double.MAX_VALUE;
		int maxScore = 0;
		double[] x = new double[game.getFeatureNames(featureSetClassification).size()];
//		List<Double> dt10 = TetrisWeightVector.make("DT10");
		for (int iter = 0; iter < cmaesIterations; iter++) {
			double[] betaVector = new double[betaCl.size()];

			for (int i = 0; i < betaVector.length; i++) {
				betaVector[i] = random.nextGaussian();
//				betaVector[i] = 0;
//				betaVector[i] = dt10.get(i);
			}

			PrintStream out = System.out;
			System.setOut(new PrintStream(new OutputStream() {
				@Override public void write(int b) throws IOException {}
			}));
			try {

					// create a CMA-ES and set some initial values
					CMAEvolutionStrategy cma = new CMAEvolutionStrategy();
		//			cma.readProperties(); // read options, see file CMAEvolutionStrategy.properties
					cma.setDimension(game.getFeatureNames(featureSetClassification).size()); // overwrite some loaded properties
					cma.setInitialX(betaVector); // in each dimension, also setTypicalX can be used
					cma.setInitialStandardDeviation(.5); // also a mandatory setting
					cma.options.stopFitness = 1e-14;       // optional setting
					cma.options.stopMaxIter = 15 * betaVector.length;
					cma.options.verbosity = 0;
					cma.options.writeDisplayToFile = 0;

					double[] fitness = cma.init();  // new double[cma.parameters.getPopulationSize()];
					// initial output to files
					cma.writeToDefaultFilesHeaders(0); // 0 == overwrites old files

					// iteration loop
					while(cma.stopConditions.getNumber() == 0) {

						// --- core iteration step ---
						double[][] pop = cma.samplePopulation(); // get a new population of solutions
						for(int i = 0; i < pop.length; ++i) {    // for each candidate solution i
							// a simple way to handle constraints that define a convex feasible domain
							// (like box constraints, i.e. variable boundaries) via "blind re-sampling"
							// assumes that the feasible domain is convex, the optimum is
							while (!fitfun.isFeasible(pop[i]))     //   not located on (or very close to) the domain boundary,
								pop[i] = cma.resampleSingle(i);    //   initialX is feasible and initialStandardDeviations are
							//   sufficiently small to prevent quasi-infinite looping here
							// compute fitness/objective value
							fitness[i] = fitfun.valueOf(pop[i]); // fitfun.valueOf() is to be minimized
						}
						cma.updateDistribution(fitness);         // pass fitness array to update search distribution
						// --- end core iteration step ---

						// output to files and console
		//				cma.writeToDefaultFiles();
		//				int outmod = 150;
		//				if (cma.getCountIter() % (15*outmod) == 1)
		//					cma.printlnAnnotation(); // might write file as well
		//				if (cma.getCountIter() % outmod == 1)
		//					cma.println();
					}
					// evaluate mean value as it is the best estimator for the optimum
					cma.setFitnessOfMeanX(fitfun.valueOf(cma.getMeanX())); // updates the best ever solution

					// final output
			//		cma.writeToDefaultFiles(1);
		//			cma.println();
					CMASolution solution = cma.getBestSolution();

//					double[] tempx = solution.getX();
//					System.out.println("cma loss :" +solution.getFitness());
//					Double[] doubleArray = ArrayUtils.toObject(tempx);
//					int mean = EvaluateLinearAgent.gamesTetris(100, random, featureSetClassification, Arrays.asList(doubleArray), actionType, paretoFeatureSet, paretoWeights, false);
//					System.out.println("**** performance in 100 rounds for this iteration of cmaes (mean):" + mean + " loss: "+ solution.getFitness());

					if(solution.getFitness() <= leastFitness){
//					if(mean > maxScore){
						x = solution.getX();
//						maxScore = mean;
						leastFitness = solution.getFitness();
					}
			} finally {
				System.setOut(out);
			}
			System.out.println("cmaes optimizer least loss :" + leastFitness);
		}
		Double[] doubleArray = ArrayUtils.toObject(x);
		double[] betaArray = new double[betaCl.size()];
		for (int i = 0; i < betaArray.length; i++)
			betaArray[i] = betaCl.get(i);
		System.out.println("cmaes optimizer loss of last beta vector " + fitfun.valueOf(betaArray));
		return Arrays.asList(doubleArray);
	}


	class ObjectiveFunction implements IObjectiveFunction {

		List<Pair<List<List<Double>>, List<Double>>> trainingSet;

		public ObjectiveFunction(){
			trainingSet = new ArrayList<>();
		}

		public void addChoice(Pair<List<List<Double>>, List<Double>> instance){
			trainingSet.add(instance);
		}

		@Override
		public double valueOf(double[] x) {
			double value = 0;
			for (Pair<List<List<Double>>, List<Double>> choice : trainingSet){
//					int bestAction = getBestActionIndex_fast(choice.getFirst(), x);
					double maxQEstimate = Collections.max(choice.getSecond());
					int[] bestActions = getBestActionIndex(choice.getFirst(), x);
					double policyQEstimate = 0;
//					double leastValue = Double.POSITIVE_INFINITY;
					for (int i = 0; i < bestActions.length; i++) {
						policyQEstimate += choice.getSecond().get(bestActions[i]);
//						if(choice.getSecond().get(bestActions[i]) < leastValue)
//							leastValue = choice.getSecond().get(bestActions[i]);
					}
//					policyQEstimate = leastValue;
					policyQEstimate = policyQEstimate/bestActions.length;
					value += (maxQEstimate - policyQEstimate)/trainingSet.size();
//					value += maxQEstimate - choice.getSecond().get(bestAction);
			}
			return value;
		}


		//this method returns the index of the last best value.
		private int getBestActionIndex_fast(List<List<Double>> actions, double[] x){
			double highestValue = Double.NEGATIVE_INFINITY;
			int idx = -1;
			for (int i = 0; i < actions.size(); i++) {
				double value = UtilAmpi.dotproduct(actions.get(i), x);
				if(value > highestValue){
					idx = i;
					highestValue = value;
				}
			}
			return idx;
		}

		private int[] getBestActionIndex(List<List<Double>> actions, double[] x){
			double[] values = new double[actions.size()];
			for (int i = 0; i < actions.size(); i++)
				values[i] = UtilAmpi.dotproduct(actions.get(i), x);

			int[] indicesOfMax = Compute.indicesOfMax(values);
			return indicesOfMax;
		}

		@Override
		public boolean isFeasible(double[] x) {
			return true;
		}

		public void printMaxQEstimate() {
			double value = 0;
			for (Pair<List<List<Double>>, List<Double>> choice : trainingSet) {
				double maxQEstimate = Collections.max(choice.getSecond());
				value += maxQEstimate;
			}
			System.out.println(" MaxQEstimate: "+value);
			System.out.println(" Number of Choices: "+trainingSet.size());
		}
	}

	public void setRounds(int rounds){
		this.rounds = rounds;
	}

	public void setTask(Task task){
		this.task = task;
	}
}