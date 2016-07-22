package algs.rl;


import algs.Game;
import domains.*;
import domains.tetris.*;
import fr.inria.optimization.cmaes.CMAEvolutionStrategy;
import fr.inria.optimization.cmaes.CMASolution;
import fr.inria.optimization.cmaes.fitness.IObjectiveFunction;
import models.CustomPolicy;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Pair;
import policy.StackedPick;
import util.Compute;
import util.RolloutUtil;
import util.StackedPickUtil;
import util.UtilAmpi;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static util.StackedPickUtil.generateOffspring;
import static util.StackedPickUtil.generateRandomStackedPick;

public class DpiHeuristic {

	static boolean firstRolloutBcts = true;
	static boolean subsampingUniformHeight = false;



	public static void main(String[] arg){

		Random random = new Random();
		long seed = random.nextInt();
//		long seed = 1;

		random = new Random(seed);
		FeatureSet featureSet = new TetrisFeatureSet("lagoudakisthierrybertsekas");
		Game game = new Game(random, new TetrisTaskLines(1));
		List<Double> initialWeights = new ArrayList<>();
		for (String name: game.getFeatureNames(featureSet))
			initialWeights.add(random.nextGaussian());


//		initialWeights = TetrisWeightVector.make("DT10"); best.
		int numIt = 15;
		double gamma = 1;
		int sampleSize = 100;
		int nrollout = 10;
		boolean sampleGabillon = true;
		TetrisParameters.getInstance().setSize(10,10);

		setOutput("dpiHeuristic_"+featureSet.name()+"_"+sampleSize);
		System.out.println("seed:" + seed);

		UtilAmpi.ActionType  actionType = UtilAmpi.ActionType.ANY;

		DpiHeuristic dpi = new DpiHeuristic(game, featureSet, numIt, sampleSize, nrollout, gamma, 10, StackedPickUtil.generateRandomStackedPick(3, featureSet, random), actionType, random, sampleGabillon);
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
	CustomPolicy currentPolicy;
	Game game = null;
	public UtilAmpi.ActionType actionType = null;
	public List<Object> rolloutSet = null;
	Random random;
	int rounds = 100;
	int samplingFactor = 1; //Size of the rollout set before subsampling is samplingFactor * rolloutSetSize.
	Task task = new TetrisTaskLines(1);
	int cmaesIterations;

	final FeatureSet paretoFeatureSet = new TetrisFeatureSet("bcts");
	//	final double[] paretoWeights = new double[]{-13.08, -19.77, -9.22, -10.49, 6.60, -12.63, -24.04, -1.61};
	final static double[] paretoWeights = new double[]{-5, -6, -2, -3, 1, -4, -7, -1}; //It should have the same effect since the direction and order are the same.
	boolean sampleGabillon = false;

	public DpiHeuristic(Game game, FeatureSet featureSetClassification, int maxSim,
						int rolloutSetSize, int nRollout, double gamma, int cmaesIterations, StackedPick initialPick,
						UtilAmpi.ActionType actionTypeCla, Random random, boolean sampleGabillon) {
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
		this.rolloutSet = new ArrayList<Object>();
		this.actionType = actionTypeCla;
		this.random = random;
		this.sampleGabillon = sampleGabillon;
		this.currentPolicy  = new CustomPolicy(initialPick, new TetrisTaskLines(1), random);
		System.out.println("****************************");
		System.out.println("performance in 100 rounds: ");
		EvaluateTetrisAgent.gamesTetris(100, random, currentPolicy, true);
		System.out.println("****************************");
	}


	public void iterate() {
		for (int k = 0; k < this.maxSim; k++) {
			long t0 = System.currentTimeMillis();

//			if(k==0 && firstRolloutBcts){//Take first rollout set from good policy.
				this.rolloutSet = RolloutUtil.getRolloutSetTetris(this.game, this.rolloutSetSize * samplingFactor, Arrays.asList(new Double[]{ -13.08,-19.77,-9.22,-10.49,6.60,-12.63,-24.04,-1.61,0.}), new TetrisFeatureSet("thierry"), actionType, paretoFeatureSet, paretoWeights, random);
//				this.rolloutSet = RolloutUtil.getRolloutSetTetrisGabillon("src/main/resources/tetris/rawGames/sample_gabillon/record_du10++cat.txt", random, rolloutSetSize);
//				this.rolloutSet  = RolloutUtil.getRolloutSetTetrisGabillon("gabillon_sample/record_du10++cat.txt", random, this.rolloutSetSize);

//			}else {
//				this.rolloutSet = RolloutUtil.getRolloutSetTetris(this.game, this.rolloutSetSize * samplingFactor, this.betaCl, this.featureSetClassification, actionType, paretoFeatureSet, paretoWeights, random);
//			}


//			if(subsampingUniformHeight)
//				this.rolloutSet = UtilAmpi.getSubSampleWithUniformHeightNewTetris(rolloutSet, this.rolloutSetSize);

			System.out.println(String.format("sampling rollout set (%s samples) took: %s seconds", rolloutSet.size(), (System.currentTimeMillis() - t0) / (1000.0)));

			long t1 = System.currentTimeMillis();
			ObjectiveFunction of = new ObjectiveFunction();
			for (int i = 0; i < this.rolloutSet.size(); i++) {
				Object state = this.rolloutSet.get(i);
				Pair<State, List<Double>> trainingSet = getFeatureEstimatedQ(state, nRollout);

				if(trainingSet.getSecond().size() > 0)
					of.addChoice(trainingSet);

			}
			System.out.println(String.format("creating training set took: %s seconds", (System.currentTimeMillis() - t1) / (1000.0)));

			long t2 = System.currentTimeMillis();

			StackedPick newStackedPick = searchHeuristic(of);
			this.currentPolicy = new CustomPolicy(newStackedPick, new TetrisTaskLines(1), random);

			System.out.println(String.format("minimizing fitness function took: %s seconds", (System.currentTimeMillis() - t2) / (1000.0)));

			System.out.println("****************************");
			System.out.println("performance in " +rounds+ " rounds: ");
			EvaluateTetrisAgent.gamesTetris(100, random, currentPolicy, true);
			System.out.println(String.format("This iteration took: %s seconds", (System.currentTimeMillis() - t0) / (1000.0)));
			System.out.println("****************************");
		}
	}


	private Pair<State, List<Double>> getFeatureEstimatedQ (Object state, int nrollout) {
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
					qEstimate = qEstimate + RolloutUtil.doRolloutTetrisIterative(stateAction, nrollout, currentPolicy,
							gamma, rolloutRandom, task);
				}
				if(qEstimate > maxQEstimate)
					maxQEstimate = qEstimate;

				if(qEstimate < minQEstimate)
					minQEstimate = qEstimate;

				utils.add(qEstimate / averageOver);
		}
		if(maxQEstimate == minQEstimate)
			utils = new ArrayList<>();
		return new Pair<State, List<Double>>((State)state, utils);
	}



	private StackedPick searchHeuristic(ObjectiveFunction fitfun){
		fitfun.printMaxQEstimate();
		double leastFitness = Double.MAX_VALUE;
		StackedPick bestHeuristic = null;
		int n = 100;
		int p = 5;
		int generations = 5;

		for (int iter = 0; iter < 1; iter++) {
			List<StackedPick> heuristics = new ArrayList<>();
			for (int i = 0; i < n; i++)
				heuristics.add(generateRandomStackedPick((random.nextInt(3)+1), featureSetClassification, random));

			for (int gen = 0; gen < generations; gen++) {
				double[] values = new double[n];

				for (int i = 0; i < n; i++)
					values[i] = fitfun.valueOf(heuristics.get(i));

				int[] orderedIndices = Compute.orderedIndices(values);
				System.out.println("Best value generation "+ values[orderedIndices[orderedIndices.length-1]]);
				List<StackedPick> bestHeuristics = new ArrayList<>();
				for (int i = orderedIndices.length-1; i >= orderedIndices.length - p; i--)
					bestHeuristics.add(heuristics.get(orderedIndices[i]));

				heuristics = generateOffspring(bestHeuristics, featureSetClassification, n, random, 10);
				bestHeuristic = bestHeuristics.get(0);
//				bestHeuristic.print(featureSetClassification.featureNames());
			}
		}
		bestHeuristic.print(featureSetClassification.featureNames());
		return bestHeuristic;
	}


	class ObjectiveFunction{

		List<Pair<State, List<Double>>> trainingSet;

		public ObjectiveFunction(){
			trainingSet = new ArrayList<>();
		}

		public void addChoice(Pair<State, List<Double>> instance){
			trainingSet.add(instance);
		}

		public double valueOf(StackedPick x) {
			double value = 0;
			for (Pair<State, List<Double>> choice : trainingSet){
					double maxQEstimate = Collections.max(choice.getSecond());
					int[] bestActions = getBestActionIndex(choice.getFirst(), x);
					double policyQEstimate = 0;
					for (int i = 0; i < bestActions.length; i++) {
						policyQEstimate += choice.getSecond().get(bestActions[i]);
					}
					policyQEstimate = policyQEstimate/bestActions.length;
					value += (maxQEstimate - policyQEstimate)/trainingSet.size();
			}
			return value;
		}



		private int[] getBestActionIndex(State state, StackedPick x){
			List<Pair<Action, Features>> actionFeaturesList = state.getActionFeaturesList();
			List<Pair<Action, Features>> actions = actionFeaturesList.stream().filter(p -> !((TetrisFeatures)p.getSecond()).gameOver).collect(Collectors.toList());
			return x.pick(state, actions);
		}

		public void printMaxQEstimate() {
			double value = 0;
			for (Pair<State, List<Double>> choice : trainingSet) {
				double maxQEstimate = Collections.max(choice.getSecond());
				value += maxQEstimate;
			}
			System.out.println(" MaxQEstimate: "+(value/trainingSet.size()));
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