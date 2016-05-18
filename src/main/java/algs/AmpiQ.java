package algs;

import domains.tetris.EvaluateLinearAgent;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.util.Pair;
import util.RolloutUtil;
import util.UtilAmpi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;


public class AmpiQ {

	public static void main(String[] arg){


		String featureSet = "lagoudakisthierry";
		Game game = new Game(1);
		List<Double> initialWeights = new ArrayList<>();
		for (String name: game.getFeatureNames(featureSet)){
			initialWeights.add(-0.);
		};


		int numIt = 30;
		double gamma = 0.9;
		int sampleSize = 50000;
		int nrollout = 10;
		setOutput("ampiq_"+featureSet+"_"+sampleSize+"_"+arg[0]);
		UtilAmpi.ActionType  actionType = UtilAmpi.ActionType.ANY;
		if(arg[0].equals("dom"))
			actionType = UtilAmpi.ActionType.DOM;
		else if (arg[0].equals("cum"))
			actionType = UtilAmpi.ActionType.CUMDOM;

		AmpiQ ampiq = new AmpiQ(game, featureSet, numIt, gamma, initialWeights, sampleSize, nrollout, actionType);
		ampiq.iterate();
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
//			System.setOut(new PrintStream(new File("src/main/resources/tetris/scores/ampiq/"+fileName)));
			System.setOut(new PrintStream(new File("scores/ampiq/"+fileName)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	final String paretoFeatureSet = "bcts";
	final double[] paretoWeights = new double[]{-13.08, -19.77, -9.22, -10.49, 6.60, -12.63, -24.04, -1.61};


	int maxSim, rolloutSetSize, nRollout, M; // M is from equation 4
	double gamma; //discount factor
	List<Double> beta = null;
	Game game = null;
	String featureSet;
	int numFeatures;
	public Map<Object, String> policy = null;
	public List<Boolean> policyChanges = null; // kth element is true(false) if in the kth iteration
												// policy of some(none) state in the rolloutset changes.
	public List<Object> rolloutSet = null;

	private UtilAmpi.ActionType rolloutActionType;
	Random random;


	public AmpiQ(Game game, String featureSet, int maxSim, double gamma, List<Double> beta, int rolloutSetSize, int nRollout, UtilAmpi.ActionType rolloutActionType) {
		this.game = game;
		this.featureSet = featureSet;
		this.numFeatures = game.getFeatureNames(featureSet).size();
		this.maxSim = maxSim;
		this.rolloutSetSize = rolloutSetSize;
		this.nRollout = nRollout;
		this.gamma = gamma;
		this.beta = beta;
		this.policy = new HashMap<>();
		this.policyChanges = new ArrayList<>();
		this.rolloutSet = new ArrayList<Object>();
		this.rolloutActionType = rolloutActionType;

		this.random = new Random();

		// for logging: TODO: remove this
		System.out.println("****************************");
		System.out.println("performance in 100 rounds: ");
		double[] betaVector = new double[beta.size()];
		for (int i = 0; i < betaVector.length; i++) {
			betaVector[i] = beta.get(i);
		}
		EvaluateLinearAgent.gamesTetris(100, random, featureSet, beta, rolloutActionType,  paretoFeatureSet, paretoWeights, true);
		System.out.println("****************************");
	}

	//update beta at each step
	public void iterate() {
		for (int k = 0; k <= this.maxSim; k++) {
			long t0 = System.currentTimeMillis();

			this.rolloutSet = game.getRandomStates(rolloutSetSize, beta, featureSet, 1, rolloutActionType);

			double[] ys = new double[this.rolloutSet.size()];
			double[][] xs = new double[this.rolloutSet.size()][this.numFeatures];

			for (int i = 0; i < this.rolloutSet.size(); i++) {
				Object state = this.rolloutSet.get(i);
//				List<String> actions =  game.getActions(state, this.rolloutActionType);
				String action = "0_0";
//				if(!actions.isEmpty())
//					 action = UtilAmpi.randomChoice(actions);

				Pair<String,Double> actionPair = RolloutUtil.getBestActionTetris(state, beta, game, featureSet, rolloutActionType, random);
				action = actionPair.getFirst();
				if(action.equals(""))
					action = "0_0";

				List<Double> x = this.game.getFeatureValues(featureSet, state, action);
				Pair<Object, String> sa = new Pair<>(state, action);
				double y = RolloutUtil.doRolloutTetris(sa, this.nRollout, game, beta, beta, gamma, featureSet, featureSet, rolloutActionType, random);
				xs[i] = ArrayUtils.toPrimitive(x.toArray(new Double[x.size()]));
				ys[i] = y;
			}

			List<Double> newbeta;
			try {
				newbeta = UtilAmpi.regress(ys, xs);
			}
			catch(SingularMatrixException e) {
				System.out.println("singular matrix :-(");
				newbeta = this.beta;
			}

			//evalRolloutSet();
			this.beta = newbeta;
			System.out.println("beta: " + beta);

			// log
			System.out.println("****************************");
			int round = 100;
			System.out.println(String.format("performance in %s rounds: ", round));
			double[] betaVector = new double[beta.size()];
			for (int i = 0; i < betaVector.length; i++) {
				betaVector[i] = beta.get(i);
			}
			EvaluateLinearAgent.gamesTetris(round, random, featureSet, beta, rolloutActionType, paretoFeatureSet,  paretoWeights, true);
			System.out.println(String.format("This iteration took: %s seconds", (System.currentTimeMillis() - t0)/(1000.0)));
			System.out.println("****************************");
		}
	}
}
