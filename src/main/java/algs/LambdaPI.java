package algs;


import domains.tetris.EvaluateLinearAgent;
import org.apache.commons.math3.linear.SingularMatrixException;
import util.UtilAmpi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class LambdaPI {


	public static void main(String[] arg){
		List<Double> initialWeights = new ArrayList<>();
		initialWeights.add(0.);
		initialWeights.add(-10.);
		initialWeights.add(-1.);
		for (int i = 0; i < 10; i++) {
			initialWeights.add(0.);
		}
		for (int i = 0; i < 9; i++) {
			initialWeights.add(0.);
		}
		String featureSet = "bertsekas";
		int numIt = 30;
		double gamma = 0.9;
		double lambda = 0.9;
		int sampleSize = 100000;
		setOutput("lpi_"+featureSet+"_"+sampleSize+"_"+lambda+"_"+arg[0]);
		UtilAmpi.ActionType  actionType = UtilAmpi.ActionType.ANY;
		if(arg[0].equals("dom"))
			actionType = UtilAmpi.ActionType.DOM;
		else if (arg[0].equals("cum"))
			actionType = UtilAmpi.ActionType.CUMDOM;

		Random random = new Random();
		LambdaPI lambdaPI = new LambdaPI(new Game(random), "bertsekas", numIt, gamma, lambda, initialWeights, sampleSize, actionType, random);
		lambdaPI.iterate();
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
//			System.setOut(new PrintStream(new File("src/main/resources/tetris/scores/lpi/"+fileName)));
			System.setOut(new PrintStream(new File("scores/lpi/"+fileName)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	final static String paretoFeatureSet = "bcts";
//	final static double[] paretoWeights = new double[]{-13.08, -19.77, -9.22, -10.49, 6.60, -12.63, -24.04, -1.61};
	final static double[] paretoWeights = new double[]{-5, -6, -2, -3, 1, -4, -7, -1}; //It should have the same effect since the direction and order are the same.


	int maxSim, sampleSize, numFeatures;
	String featureSet;
	double gamma, lambda; //discount factor
	List<Double> beta = null;
	Game game = null;

	public UtilAmpi.ActionType actionType;
	Random random;

	public LambdaPI(Game game, String featureSet, int maxSim,
					double gamma, double lambda, List<Double> beta, int sampleSize,
					UtilAmpi.ActionType actionType, Random random) {

		this.game = game;
		this.maxSim = maxSim;
		this.gamma = gamma;
		this.beta = beta;
		this.actionType = actionType;

		this.lambda = lambda;
		this.random = random;
		this.sampleSize= sampleSize;
		this.featureSet = featureSet;
		this.numFeatures = game.getFeatureNames(featureSet).size();

		System.out.println("beta: " + beta);
		System.out.println("****************************");
		System.out.println("performance in 100 rounds: ");
		double[] betaVector = new double[beta.size()];
		for (int i = 0; i < betaVector.length; i++)
			betaVector[i] = beta.get(i);
		EvaluateLinearAgent.gamesTetris(100, random, featureSet, beta, actionType, paretoFeatureSet, paretoWeights, true);
		System.out.println("****************************");
	}


	public void iterate() {
		for (int i = 0; i < maxSim; i++) {
			long t0 = System.currentTimeMillis();

			List<Object> states = game.getSampleTrajectory(sampleSize, beta, featureSet, actionType, paretoFeatureSet, paretoWeights); //sample trajectories
			double td_sum = 0;
			double[] ys = new double[numFeatures];
			double[][] xs = new double[numFeatures][numFeatures];
			double sampleWeight = 1;

			for (int j = states.size()-1; j > 0; j--) {//loop backwards

				Object stateBefore = states.get(j-1);
				Object state = states.get(j);

				List<Double> stateBeforeFeatureValues = game.getFeatureValues(featureSet, stateBefore);
				double stateBeforeEstimatedValue = UtilAmpi.dotproduct(stateBeforeFeatureValues, beta);

				List<Double> stateFeatureValues = game.getFeatureValues(featureSet, state);
				double stateEstimatedValue = UtilAmpi.dotproduct(stateFeatureValues, beta);

				if(game.isGameover(stateBefore))
					stateBeforeEstimatedValue = 0;
				// If the state before is the last state of its trajectory,
//				 then the previous value is 0.

				td_sum = calculateDt(state, stateEstimatedValue, stateBeforeEstimatedValue) + lambda * gamma * td_sum;

				//if state is gameover, a new trajectory begins and we restart td_sum.
				if(game.isGameover(state)) {
					td_sum = 0;
					stateEstimatedValue = stateBeforeEstimatedValue;
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
				newbeta = UtilAmpi.regress(ys, xs);
			} catch(SingularMatrixException e) {
				System.out.println("singular matrix :-(");
				newbeta = this.beta;
			}

			this.beta = newbeta;
			System.out.println("beta: " + beta);

			// log
			int round = 100;
			System.out.println("****************************");
			System.out.println(String.format("performance in %s rounds: ", round));
			EvaluateLinearAgent.gamesTetris(round, random, featureSet, beta, actionType, paretoFeatureSet, paretoWeights, true);
			System.out.println(String.format("This iteration took: %s seconds", (System.currentTimeMillis() - t0)/(1000.0)));
			System.out.println("****************************");
		}
	}

	private double calculateDt(Object state, double stateEstimatedValue, double stateBeforeEstimatedValue) {
		return game.getReward(state) + stateEstimatedValue - stateBeforeEstimatedValue;
	}

}