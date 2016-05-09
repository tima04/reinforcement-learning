package ampi;

import algorta.domains.tetris.agent.AgentLinear;
import algorta.rl.DomainKnown;
import algorta.scratch.util.Compute;
import algorta.util.EvaluateLinearAgent;
import ampi.Util.ActionType;
import fr.inria.optimization.cmaes.CMAEvolutionStrategy;
import fr.inria.optimization.cmaes.CMASolution;
import fr.inria.optimization.cmaes.fitness.IObjectiveFunction;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Pair;

import java.util.*;

public class Dpi {

	int maxSim, rolloutSetSize, nRollout;
	String featureSetClassification, reportName;
	double gamma, TOLERANCE = 10e-6; //discount factor
	List<Double> betaReg = null, betaCl = null;
	GameTetris game = null;
	public Map<Object, String> policy = null;
	public List<Boolean> policyChanges = null; // kth element is true(false) if in the kth iteration
												// policy of some(none) state in the rolloutset changes.
	public ActionType actionType, actionTypeCl = null;
	public List<Object> rolloutSet = null;
	Random random;
	public DomainKnown domainKnown;

	int samplingFactor = 3; //Size of the rollout set before subsampling is samplingFactor * rolloutSetSize.

	int height;
	int width;

	public Dpi(GameTetris game, String featureSetClassification, int maxSim,
			   int rolloutSetSize, int nRollout, double gamma, List<Double> betaCl,
			   ActionType actionTypeCla, String reportName, int height, int width) {
		// beta is stateAction features, and theta is state features
		this.game = game;
		this.featureSetClassification = featureSetClassification;
		this.maxSim = maxSim;
		this.rolloutSetSize = rolloutSetSize;
		this.nRollout = nRollout;
		this.gamma = gamma;
		this.betaReg = new ArrayList<>(Collections.nCopies(betaCl.size(), 0.));
		this.betaCl = betaCl;
		this.policy = new HashMap<>();
		this.policyChanges = new ArrayList<>();
		this.rolloutSet = new ArrayList<Object>();
		this.actionType = actionTypeCla;
		this.actionTypeCl = actionTypeCla;
		this.reportName = reportName;
		this.height = height;
		this.width = width;
//		this.domainKnown = domain;
		this.random = new Random();
		System.out.println("****************************");
		System.out.println("performance in 100 rounds: ");
		double[] betaVector = new double[betaCl.size()];
		for (int i = 0; i < betaVector.length; i++)
			betaVector[i] = betaCl.get(i);
		EvaluateLinearAgent.gamesTetris(100, 1, featureSetClassification, betaCl, "", height, width);
		System.out.println("****************************");
	}


	public void iterate() {
		for (int k = 0; k < this.maxSim; k++) {
			long t0 = System.currentTimeMillis();
			if(k==0){//Take first rollout set from good policy.
				this.rolloutSet = RolloutUtil.getRolloutSetTetris(this.game, this.rolloutSetSize * samplingFactor, Arrays.asList(new Double[]{ -13.08,-19.77,-9.22,-10.49,6.60,-12.63,-24.04,-1.61,0.}), this.featureSetClassification, actionType);
			}else {
				this.rolloutSet = RolloutUtil.getRolloutSetTetris(this.game, this.rolloutSetSize * samplingFactor, this.betaCl, this.featureSetClassification, actionType);
			}
			System.out.println(String.format("sampling rollout set took: %s seconds", (System.currentTimeMillis() - t0) / (1000.0)));

			//Subsampling with uniform Height
			this.rolloutSet = Util.getSubSampleWithUniformHeightNewTetris(rolloutSet, this.rolloutSetSize);

//			List<List<Double>> xsCl = new ArrayList<>();
//			List<Double> ysCl = new ArrayList<>();
			List<List<Double>> xsReg = new ArrayList<>();
			List<Double> ysReg = new ArrayList<>();
			long t1 = System.currentTimeMillis();
			ObjectiveFunction of = new ObjectiveFunction();
			for (int i = 0; i < this.rolloutSet.size(); i++) {
				Object state = this.rolloutSet.get(i);
				Pair<List<List<Double>>, List<Double>> trainingSet = getFeatureEstimatedQ(state, nRollout, betaReg, betaCl, actionTypeCl);
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
			System.out.println("performance in 100 rounds: ");
			double[] betaVector = new double[betaCl.size()];
			for (int i = 0; i < betaVector.length; i++)
				betaVector[i] = betaCl.get(i);
			EvaluateLinearAgent.gamesTetris(100, 1, featureSetClassification, betaCl, "", height, width);
			System.out.println(String.format("This iteration took: %s seconds", (System.currentTimeMillis() - t0) / (1000.0)));
			System.out.println("****************************");
		}
	}


	private Pair<List<List<Double>>, List<Double>> getFeatureEstimatedQ (Object state, int nrollout, List<Double> betaReg,
																   List<Double> betaCl, ActionType actionType) {
		List<Pair<String,List<Double>>> actions = this.game.getStateActionFeatureValues(featureSetClassification, state, actionType);
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
						gamma,  featureSetClassification, featureSetClassification, actionType);
			}
			utils.add(qEstimate/averageOver);
		}
//		if(utils.size() > 0)
//			normalizeUtils(utils);
		return new Pair<>(features, utils);
	}

	private void normalizeUtils(List<Double> utils) {
		double max = Compute.max(utils);
		if(max > 0)
			for (int i = 0; i < utils.size(); i++)
				utils.set(i, utils.get(i)/max);
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
			// create a CMA-ES and set some initial values
			CMAEvolutionStrategy cma = new CMAEvolutionStrategy();
//			cma.readProperties(); // read options, see file CMAEvolutionStrategy.properties
			cma.setDimension(game.getFeatureNames(featureSetClassification).size()); // overwrite some loaded properties
			cma.setInitialX(betaVector); // in each dimension, also setTypicalX can be used
			cma.setInitialStandardDeviation(30); // also a mandatory setting
			cma.options.stopFitness = 1e-14;       // optional setting

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
			System.out.println("loss " + leastFitness);
		}
		Double[] doubleArray = ArrayUtils.toObject(x);
		AgentLinear agentLinear = new AgentLinear.Builder(1).thieryScherrerDT10().build();
		System.out.println("loss of good weight vector " + fitfun.valueOf(agentLinear.weights()));
		double[] betaArray = new double[betaCl.size()];
		for (int i = 0; i < betaArray.length; i++)
			betaArray[i] = betaCl.get(i);
		System.out.println("loss of last beta vector " + fitfun.valueOf(betaArray));
		return Arrays.asList(doubleArray);
	}


	class ObjectiveFunction implements IObjectiveFunction{

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
				value = Util.dotproduct(Arrays.asList(doubleArray), actions.get(i));
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