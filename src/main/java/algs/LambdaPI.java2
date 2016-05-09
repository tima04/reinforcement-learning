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
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.util.Pair;

import java.util.*;

public class LambdaPI {

	final static String paretoFeatureSet = "bcts";
	final static double[] paretoWeights = new double[]{-13.08, -19.77, -9.22, -10.49, 6.60, -12.63, -24.04, -1.61};


	int maxSim, sampleSize, numFeatures;
	String featureSet, reportName;
	double gamma, lambda, TOLERANCE = 10e-6; //discount factor
	List<Double> beta = null;
	GameTetris game = null;

	public ActionType actionType;
	public List<Object> rolloutSet = null;
	Random random;
	public DomainKnown domainKnown;


	int height;
	int width;

	public LambdaPI(GameTetris game, String featureSet, int maxSim,
					double gamma, double lambda, List<Double> beta, int sampleSize,
					ActionType actionType, String reportName, int height, int width) {

		this.game = game;
		this.maxSim = maxSim;
		this.gamma = gamma;
		this.beta = beta;
		this.rolloutSet = new ArrayList<Object>();
		this.actionType = actionType;
		this.reportName = reportName;
		this.height = height;
		this.width = width;
		this.lambda = lambda;
		this.random = new Random();
		this.sampleSize= sampleSize;
		this.featureSet = featureSet;
		this.numFeatures = game.getFeatureNames(featureSet).size();

		System.out.println("****************************");
		System.out.println("performance in 100 rounds: ");
		double[] betaVector = new double[beta.size()];
		for (int i = 0; i < betaVector.length; i++)
			betaVector[i] = beta.get(i);
		EvaluateLinearAgent.gamesTetris(100, 1, featureSet, beta, "", height, width);
		System.out.println("****************************");
	}


	public void iterate() {
		for (int i = 0; i <= maxSim; i++) {
			long t0 = System.currentTimeMillis();

			List<Object> states = game.getRandomTrajectory(sampleSize, beta, featureSet, actionType); //sample trajectories
			double td_sum = 0;
			double[] ys = new double[numFeatures];
			double[][] xs = new double[numFeatures][numFeatures];
			double sampleWeight = 1;
			for (int j = states.size()-1; j > 0; j--) {//loop backwards

				Object stateBefore = states.get(j-1);
				Object state = states.get(j);

				List<Double> stateBeforeFeatureValues = game.getFeatureValues(featureSet, stateBefore);
				double stateBeforeEstimatedValue = Util.dotproduct(stateBeforeFeatureValues, beta);

				List<Double> stateFeatureValues = game.getFeatureValues(featureSet, state);
				double stateEstimatedValue = Util.dotproduct(stateFeatureValues, beta);

				if(game.isGameover(stateBefore))
					stateBeforeEstimatedValue = 0;
				// If the state before is the last state of its trajectory,
				// then the previous value is 0.

				td_sum = calculateDt(state, stateEstimatedValue, stateBeforeEstimatedValue) + lambda * td_sum;

				//if state is gameover, a new trajectory begins and we restart td_sum.
				if(game.isGameover(state)) {
					td_sum = 0;
//					stateEstimatedValue = stateBeforeEstimatedValue;
				}


				for (int k = 0; k < numFeatures; k++) {
					for (int l = 0; l < numFeatures; l++) {
						xs[k][l] = sampleWeight * (stateFeatureValues.get(k) * stateFeatureValues.get(l)) + xs[k][l];
					}
					ys[k] =  ys[k] + stateFeatureValues.get(k) * (stateEstimatedValue + td_sum);
				}
			}


			List<Double> newbeta;
			try {
				newbeta = Util.regress(ys, xs);
			} catch(SingularMatrixException e) {
				System.out.println("singular matrix :-(");
				newbeta = this.beta;
			}

			this.beta = newbeta;
			System.out.println("beta: " + beta);

			// log
			int round = 20;
			System.out.println("****************************");
			System.out.println(String.format("performance in %s rounds: ", round));
			EvaluateLinearAgent.gamesTetris(round, 1, featureSet, beta, "", height, width, actionType, paretoFeatureSet, paretoWeights);
			System.out.println(String.format("This iteration took: %s seconds", (System.currentTimeMillis() - t0)/(1000.0)));
			System.out.println("****************************");
		}
	}

	private double calculateDt(Object state, double stateEstimatedValue, double stateBeforeEstimatedValue) {
		return game.getReward(state) + stateEstimatedValue - stateBeforeEstimatedValue;
	}

}