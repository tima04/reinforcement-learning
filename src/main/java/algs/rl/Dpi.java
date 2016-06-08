package algs.rl;


import algs.Game;
import domains.FeatureSet;
import domains.tetris.EvaluateLinearAgent;
import domains.tetris.TetrisFeatureSet;
import domains.tetris.TetrisState;
import domains.tetris.TetrisTaskLines;
import fr.inria.optimization.cmaes.CMAEvolutionStrategy;
import fr.inria.optimization.cmaes.CMASolution;
import fr.inria.optimization.cmaes.fitness.IObjectiveFunction;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Pair;
import util.RolloutUtil;
import util.UtilAmpi;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Dpi {

	public static void main(String[] arg){

		Random random = new Random();
//		Random random = new Random(1); // fixed seed
		FeatureSet featureSet = new TetrisFeatureSet("thierry");
		Game game = new Game(random, new TetrisTaskLines(0.9));
		List<Double> initialWeights = new ArrayList<>();
		for (String name: game.getFeatureNames(featureSet))
			initialWeights.add(-0.);

		int numIt = 5;
		double gamma = 0.9;
		int sampleSize = 5000;
		int nrollout = 10;
		setOutput("dpi_"+featureSet.name()+"_"+sampleSize+"_"+arg[0]);
		UtilAmpi.ActionType  actionType = UtilAmpi.ActionType.ANY;
		if(arg[0].equals("dom"))
			actionType = UtilAmpi.ActionType.DOM;
		else if (arg[0].equals("cum"))
			actionType = UtilAmpi.ActionType.CUMDOM;

		Dpi dpi = new Dpi(game, featureSet, numIt, sampleSize, nrollout, gamma, initialWeights, actionType, random);
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

	int samplingFactor = 3; //Size of the rollout set before subsampling is samplingFactor * rolloutSetSize.

	final FeatureSet paretoFeatureSet = new TetrisFeatureSet("bcts");
	//	final double[] paretoWeights = new double[]{-13.08, -19.77, -9.22, -10.49, 6.60, -12.63, -24.04, -1.61};
	final static double[] paretoWeights = new double[]{-5, -6, -2, -3, 1, -4, -7, -1}; //It should have the same effect since the direction and order are the same.


	public Dpi(Game game, FeatureSet featureSetClassification, int maxSim,
			   int rolloutSetSize, int nRollout, double gamma, List<Double> betaCl,
			   UtilAmpi.ActionType actionTypeCla, Random random) {
		// beta is stateAction features, and theta is state features
		System.out.println("TetrisBoard:" + TetrisState.height+"x"+TetrisState.width);
		this.game = game;
		this.featureSetClassification = featureSetClassification;
		this.maxSim = maxSim;
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
		EvaluateLinearAgent.gamesTetris(100, random, featureSetClassification, betaCl, actionType, paretoFeatureSet, paretoWeights, true);
		System.out.println("****************************");
	}


	public List<Double> iterate() {
		for (int k = 0; k < this.maxSim; k++) {
			long t0 = System.currentTimeMillis();
//			if(k==0){//Take first rollout set from good policy.
//				this.rolloutSet = RolloutUtil.getRolloutSetTetris(this.game, this.rolloutSetSize * samplingFactor, Arrays.asList(new Double[]{ -13.08,-19.77,-9.22,-10.49,6.60,-12.63,-24.04,-1.61,0.}), new TetrisFeatureSet("thierry"), actionType, paretoFeatureSet, paretoWeights, random);
//			}else {
				this.rolloutSet = RolloutUtil.getRolloutSetTetris(this.game, this.rolloutSetSize * samplingFactor, this.betaCl, this.featureSetClassification, actionType, paretoFeatureSet, paretoWeights, random);
//			}

			this.rolloutSet = UtilAmpi.getSubSampleWithUniformHeightNewTetris(rolloutSet, this.rolloutSetSize);

			System.out.println(String.format("sampling rollout set took: %s seconds", (System.currentTimeMillis() - t0) / (1000.0)));

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

			List<Double> newbetaCl;


				newbetaCl = minimizeUsingCMAES(of);


			this.betaCl = newbetaCl;
			System.out.println("betaCL: " + this.betaCl);

			System.out.println(String.format("minimizing fitness function took: %s seconds", (System.currentTimeMillis() - t2) / (1000.0)));

			System.out.println("****************************");
			System.out.println("performance in 100 rounds: ");
			double[] betaVector = new double[betaCl.size()];
			for (int i = 0; i < betaVector.length; i++)
				betaVector[i] = betaCl.get(i);
			EvaluateLinearAgent.gamesTetris(100, random, featureSetClassification, betaCl, actionType, paretoFeatureSet, paretoWeights, true);
			System.out.println(String.format("This iteration took: %s seconds", (System.currentTimeMillis() - t0) / (1000.0)));
			System.out.println("****************************");
		}
		return betaCl;
	}


	private Pair<List<List<Double>>, List<Double>> getFeatureEstimatedQ (Object state, int nrollout, List<Double> betaReg,
																		 List<Double> betaCl, UtilAmpi.ActionType actionType) {
		List<Pair<String,List<Double>>> actions = this.game.getStateActionFeatureValues(featureSetClassification, state, actionType, paretoFeatureSet, paretoWeights);
		List<List<Double>> features = new ArrayList<>();
		List<Double> utils = new ArrayList<>();

//		((TetrisState)state).print();
		for (Pair<String,List<Double>> action : actions) {
				Pair<Object, String> stateAction = new Pair<>(state, action.getFirst());
				features.add(action.getSecond());
//			Pair<Object, Double> stateReward = this.game.getNewStateAndReward(state, action.getFirst());
				double qEstimate = 0;
				int averageOver = 1;
				for (int i = 0; i < averageOver; i++) {
					qEstimate = qEstimate + RolloutUtil.doRolloutTetris(stateAction, nrollout, game, betaReg, betaCl,
							gamma, featureSetClassification, featureSetClassification, actionType, random, paretoFeatureSet, paretoWeights);
				}
				utils.add(qEstimate / averageOver);
		}
		return new Pair<>(features, utils);
	}



	private List<Double> minimizeUsingCMAES(ObjectiveFunction fitfun){
		fitfun.printMaxQEstimate();
		double leastFitness = Double.MAX_VALUE;
		double[] x = new double[game.getFeatureNames(featureSetClassification).size()];

		for (int iter = 0; iter < 5; iter++) {
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
			for (Pair<List<List<Double>>, List<Double>> choice : trainingSet) {
					int bestAction = getBestActionIndex(choice.getFirst(), x);
					double maxQEstimate = Collections.max(choice.getSecond());
					value += maxQEstimate - choice.getSecond().get(bestAction);
			}
			return value;
		}

		private int getBestActionIndex(List<List<Double>> actions, double[] x){
			double highestValue = -Double.MAX_VALUE;
			int idx = -1;
			for (int i = 0; i < actions.size(); i++) {
				double value = 0;
				Double[] doubleArray = ArrayUtils.toObject(x);
				value = UtilAmpi.dotproduct(Arrays.asList(doubleArray), actions.get(i));
				if(value > highestValue){
					highestValue = value;
					idx = i;
				}
			}
			return idx;
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