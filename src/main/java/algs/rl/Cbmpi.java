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
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.util.Pair;
import util.Compute;
import util.RolloutUtil;
import util.UtilAmpi;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Cbmpi {

	static boolean firstRolloutBcts = false;
	static boolean subsampingUniformHeight = false;

	public static void main(String[] arg){

		Random random = new Random();
		long seed = random.nextInt();
//		long seed = -391127942;
		random = new Random(seed);
		FeatureSet featureSetPolicy = new TetrisFeatureSet("thierry");
		FeatureSet featureSetValue = new TetrisFeatureSet("thierryrbf");
		Game game = new Game(random, new TetrisTaskLines(1));
		List<Double> initialWeights = new ArrayList<>();
		for (String name: game.getFeatureNames(featureSetPolicy))
			initialWeights.add(-0.);


		int numIt = 20;
		double gamma = 1;
		int sampleSize = Integer.parseInt(arg[2]);
		int nrollout = Integer.parseInt(arg[1]);
		TetrisParameters.getInstance().setSize(10,10);

		setOutput("cbmpi_"+featureSetValue.name()+"_"+featureSetPolicy.name()+"_"+sampleSize+"_"+arg[0]);
		System.out.println("seed:" + seed);

		UtilAmpi.ActionType  actionType = UtilAmpi.ActionType.ANY;
		if(arg[0].equals("dom"))
			actionType = UtilAmpi.ActionType.DOM;
		else if (arg[0].equals("cum"))
			actionType = UtilAmpi.ActionType.CUMDOM;

		Cbmpi cbmpi = new Cbmpi(game, featureSetValue, featureSetPolicy, numIt, sampleSize, nrollout, gamma, initialWeights, actionType, random);
		cbmpi.iterate();
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
//			System.setOut(new PrintStream(new File("src/main/resources/tetris/scores/cbmpi/"+fileName)));
			System.setOut(new PrintStream(new File("scores/cbmpi/"+fileName)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	int maxSim, rolloutSetSize, nRollout;

	FeatureSet featureSetClassification;
	FeatureSet featureSetValue;

	double gamma; //discount factor
	List<Double> betaReg = null, betaCl = null;
	Game game = null;
	public UtilAmpi.ActionType actionType = null;
	public List<Object> rolloutSet = null;
	Random random;
	Task task = new TetrisTaskLines(1);

	final FeatureSet paretoFeatureSet = new TetrisFeatureSet("bcts");
	//	final double[] paretoWeights = new double[]{-13.08, -19.77, -9.22, -10.49, 6.60, -12.63, -24.04, -1.61};
	final static double[] paretoWeights = new double[]{-5, -6, -2, -3, 1, -4, -7, -1}; //It should have the same effect since the direction and order are the same.


	public Cbmpi(Game game, FeatureSet featureSetValue, FeatureSet featureSetPolicy, int maxSim,
				 int rolloutSetSize, int nRollout, double gamma, List<Double> betaCl,
				 UtilAmpi.ActionType actionTypeCla, Random random) {
		// beta is stateAction features, and theta is state features
		System.out.println("TetrisBoard:" + TetrisState.height+"x"+TetrisState.width);
		System.out.println("First Rollout BCTS: " + firstRolloutBcts);
		System.out.println("Subsampling Uniform Height :" + subsampingUniformHeight);
		System.out.println("Sample size:" + rolloutSetSize);
		System.out.println("Rollout length:" + nRollout);
		System.out.println("Feature set policy: " +featureSetPolicy.name());
		System.out.println("Feature set value: " +featureSetValue.name());
		System.out.println("");
		this.game = game;
		this.featureSetClassification = featureSetPolicy;
		this.featureSetValue = featureSetValue;
		this.maxSim = maxSim;
		this.rolloutSetSize = rolloutSetSize;
		this.nRollout = nRollout;
		this.gamma = gamma;
		this.betaReg = new ArrayList<>(Collections.nCopies(featureSetValue.featureNames().size(), 0.));
		this.betaCl = betaCl;
		this.rolloutSet = new ArrayList<Object>();
		this.actionType = actionTypeCla;
		this.random = random;
		System.out.println("****************************");
		System.out.println("performance in 100 rounds: ");
		double[] betaVector = new double[betaCl.size()];
		for (int i = 0; i < betaVector.length; i++)
			betaVector[i] = betaCl.get(i);
		EvaluateTetrisAgent.gamesTetris(100, random, featureSetPolicy, betaCl, actionType, paretoFeatureSet, paretoWeights, true);
		System.out.println("****************************");
	}


	public List<Double> iterate() {
		List<Double> paretoWeightsList = new ArrayList<>();
		for (double paretoWeight : paretoWeights)
			paretoWeightsList.add(paretoWeight);

		for (int k = 0; k < this.maxSim; k++) {
			long t0 = System.currentTimeMillis();
//			if(k==0 && firstRolloutBcts){//Take first rollout set from good policy.
//				this.rolloutSet = RolloutUtil.getRolloutSetTetris(this.game, this.rolloutSetSize, paretoWeightsList, new TetrisFeatureSet("thierry"), actionType, paretoFeatureSet, paretoWeights, random);
//			this.rolloutSet = RolloutUtil.getRolloutSetTetrisGabillon("src/main/resources/tetris/rawGames/sample_gabillon/record_du10++cat.txt", random, this.rolloutSetSize);
//				this.rolloutSet = RolloutUtil.getRolloutSetTetrisGabillon("gabillon_sample/record_du10++cat.txt", random, this.rolloutSetSize);
//			}else {
				this.rolloutSet = RolloutUtil.getRolloutSetTetris(this.game, this.rolloutSetSize, this.betaCl, this.featureSetClassification, actionType, paretoFeatureSet, paretoWeights, random);
//			}


			System.out.println(String.format("sampling rollout set took: %s seconds", (System.currentTimeMillis() - t0) / (1000.0)));

			long t1 = System.currentTimeMillis();
			ObjectiveFunction of = new ObjectiveFunction();
			for (int i = 0; i < this.rolloutSet.size(); i++) {
				Object state = this.rolloutSet.get(i);
				Pair<List<List<Double>>, List<Double>> trainingSet = getFeatureEstimatedQPenalty(state, nRollout, betaReg, betaCl, actionType);

				if(trainingSet.getSecond().size() > 0)
					of.addChoice(trainingSet);

			}
			System.out.println(String.format("creating training set took: %s seconds", (System.currentTimeMillis() - t1) / (1000.0)));

			long t2 = System.currentTimeMillis();


			List<List<Double>> xsReg = new ArrayList<>();
			List<Double> ysReg = new ArrayList<>();
			for (Object state : rolloutSet) {
				List<Pair<Action, List<Double>>> stateActionFeatures = game.getStateActionFeatureValues(featureSetValue, state, paretoFeatureSet, paretoWeights);
				if(stateActionFeatures.size() > 0) {// Size could be 0 if all actions lead to gameover.
					Pair<Action, List<Double>> actionFeatures = UtilAmpi.randomChoice(stateActionFeatures);
					Pair<Object, Action> sa = new Pair<>(state, actionFeatures.getFirst());
					xsReg.add(actionFeatures.getSecond());
					ysReg.add(RolloutUtil.doRolloutTetrisIterative(sa, this.nRollout, betaReg, betaCl, gamma,
							featureSetValue, featureSetClassification, random, task, actionType, paretoFeatureSet, paretoWeights));
//					ysReg.add(RolloutUtil.doRolloutTetrisIterative(sa, this.nRollout, betaReg, betaReg, gamma,
//							featureSetValue, featureSetValue, random, task, actionType, paretoFeatureSet, paretoWeights));
				}
			}



			List<Double> newbetaCl = minimizeUsingCMAES(of);
			this.betaCl = newbetaCl;
			System.out.println("betaCL: " + this.betaCl);


			List<Double> newbetaReg;
			try {
				newbetaReg = UtilAmpi.regress(ysReg, xsReg);
			} catch (SingularMatrixException e) {
				System.out.println("singular matrix in regression :-(");
				newbetaReg = this.betaReg;
			}
			this.betaReg = newbetaReg;
			System.out.println(String.format("minimizing fitness function and regressing  took: %s seconds", (System.currentTimeMillis() - t2) / (1000.0)));


			System.out.println("****************************");
			System.out.println("performance in 100 rounds: ");
			double[] betaVector = new double[betaCl.size()];
			for (int i = 0; i < betaVector.length; i++)
				betaVector[i] = betaCl.get(i);
			EvaluateTetrisAgent.gamesTetris(100, random, featureSetClassification, betaCl, actionType, paretoFeatureSet, paretoWeights, true);
			System.out.println(String.format("This iteration took: %s seconds", (System.currentTimeMillis() - t0) / (1000.0)));
			System.out.println("****************************");
		}
		return betaCl;
	}


	private Pair<List<List<Double>>, List<Double>> getFeatureEstimatedQ (Object state, int nrollout, List<Double> betaReg,
																		 List<Double> betaCl, UtilAmpi.ActionType actionType) {
		List<Pair<Action,List<Double>>> actions = this.game.getStateActionFeatureValues(featureSetClassification, state, actionType, paretoFeatureSet, paretoWeights);

		List<List<Double>> features = new ArrayList<>();
		List<Double> utils = new ArrayList<>();
		double maxQEstimate = Double.NEGATIVE_INFINITY;
//		((TetrisState)state).print();
		for (Pair<Action,List<Double>> action : actions) {
				Pair<Object, Action> stateAction = new Pair<>(state, action.getFirst());
				features.add(action.getSecond());
//			Pair<Object, Double> stateReward = this.game.getNewStateAndReward(state, action.getFirst());
				double qEstimate = 0;
				int averageOver = 1;
				for (int i = 0; i < averageOver; i++) {
					qEstimate = qEstimate + RolloutUtil.doRolloutTetrisIterative(stateAction, nrollout, betaReg, betaCl,
							gamma, featureSetValue, featureSetClassification, random, task, actionType, paretoFeatureSet, paretoWeights);
				}
			if(qEstimate > maxQEstimate)
				maxQEstimate = qEstimate;

			utils.add(qEstimate / averageOver);
		}
		return new Pair<>(features, utils);
	}

	private Pair<List<List<Double>>, List<Double>> getFeatureEstimatedQPenalty(Object state, int nrollout, List<Double> betaReg,
																		 List<Double> betaCl, UtilAmpi.ActionType actionType) {
		List<Pair<Action,List<Double>>> actions = this.game.getStateActionFeatureValues(featureSetClassification, state, UtilAmpi.ActionType.ANY, paretoFeatureSet, paretoWeights);

		List<List<Double>> features = new ArrayList<>();
		List<Double> utils = new ArrayList<>();
		double maxQEstimate = Double.NEGATIVE_INFINITY;
//		((TetrisState)state).print();
		for (Pair<Action,List<Double>> action : actions) {
			Pair<Object, Action> stateAction = new Pair<>(state, action.getFirst());
			features.add(action.getSecond());
//			Pair<Object, Double> stateReward = this.game.getNewStateAndReward(state, action.getFirst());
			double qEstimate = 0;
			int averageOver = 1;
			for (int i = 0; i < averageOver; i++) {
				qEstimate = qEstimate + RolloutUtil.doRolloutTetrisIterative(stateAction, nrollout, betaReg, betaCl,
						gamma, featureSetValue, featureSetClassification, random, task, actionType, paretoFeatureSet, paretoWeights);
			}
			if(qEstimate > maxQEstimate)
				maxQEstimate = qEstimate;

			utils.add(qEstimate / averageOver);
		}
		return new Pair<>(features, utils);
	}


	private List<Double> minimizeUsingCMAES(ObjectiveFunction fitfun){
		fitfun.printMaxQEstimate();
		double leastFitness = Double.MAX_VALUE;
		double[] x = new double[game.getFeatureNames(featureSetClassification).size()];

		for (int iter = 0; iter < 1; iter++) {
			double[] betaVector = new double[betaCl.size()];
			for (int i = 0; i < betaVector.length; i++) {
				betaVector[i] = random.nextGaussian();
//				betaVector[i] = 0;
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
					cma.setInitialStandardDeviation(30); // also a mandatory setting
					cma.options.stopFitness = 1e-14;       // optional setting
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



					if(solution.getFitness() <= leastFitness){
						x = solution.getX();
						leastFitness = solution.getFitness();
					}
			} finally {
				System.setOut(out);
			}
			System.out.println("cmaes optimizer loss " + leastFitness);
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
		}
	}
}