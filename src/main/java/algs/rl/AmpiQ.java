package algs.rl;

import algs.Game;
import domains.Action;
import domains.FeatureSet;
import domains.Task;
import domains.tetris.*;
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

		Random random = new Random();
		FeatureSet featureSet = new TetrisFeatureSet("thierryrbf");
		Game game = new Game(random, new TetrisTaskLines(0.9));
		List<Double> initialWeights = new ArrayList<>();
		TetrisParameters.getInstance().setSize(10,10);
		for (String name: game.getFeatureNames(featureSet)){
			initialWeights.add(-0.);
		};


		int numIt = 10;
		double gamma = 1;
		int sampleSize = 10000;
		int nrollout = 15;
		setOutput("ampiq_"+featureSet.name()+"_"+sampleSize+"_"+arg[0]);
		UtilAmpi.ActionType  actionType = UtilAmpi.ActionType.ANY;
		if(arg[0].equals("dom"))
			actionType = UtilAmpi.ActionType.DOM;
		else if (arg[0].equals("cum"))
			actionType = UtilAmpi.ActionType.CUMDOM;
		else if (arg[0].equals("cum095"))
			actionType = UtilAmpi.ActionType.APPROX_CUMDOM_095;
		else if (arg[0].equals("dom095"))
			actionType = UtilAmpi.ActionType.APPROX_DOM_095;

		AmpiQ ampiq = new AmpiQ(game, featureSet, numIt, gamma, initialWeights, sampleSize, nrollout, actionType, random);
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

	final FeatureSet paretoFeatureSet = new TetrisFeatureSet("bcts");
//	final double[] paretoWeights = new double[]{-13.08, -19.77, -9.22, -10.49, 6.60, -12.63, -24.04, -1.61};
	final static double[] paretoWeights = new double[]{-5, -6, -2, -3, 1, -4, -7, -1}; //It should have the same effect since the direction and order are the same.


	int maxSim, rolloutSetSize, nRollout, M; // M is from equation 4
	double gamma; //discount factor
	List<Double> beta = null;
	Game game = null;
	FeatureSet featureSet;
	int numFeatures;
	public List<Object> rolloutSet = null;
	Task task = new TetrisTaskLines(0.9);

	private UtilAmpi.ActionType rolloutActionType;
	Random random;


	public AmpiQ(Game game, FeatureSet featureSet, int maxSim, double gamma, List<Double> beta, int rolloutSetSize, int nRollout, UtilAmpi.ActionType rolloutActionType, Random random) {
		System.out.println("TetrisBoard:" + TetrisState.height+"x"+TetrisState.width);
		System.out.println("Sample size:" + rolloutSetSize);
		System.out.println("Rollout length:" + nRollout);
		System.out.println("Action Type:" + rolloutActionType);
		System.out.println("rollout iterative");
		this.game = game;
		this.featureSet = featureSet;
		this.numFeatures = game.getFeatureNames(featureSet).size();
		this.maxSim = maxSim;
		this.rolloutSetSize = rolloutSetSize;
		this.nRollout = nRollout;
		this.gamma = gamma;
		this.beta = beta;
		this.rolloutSet = new ArrayList<Object>();
		this.rolloutActionType = rolloutActionType;

		this.random = random;

		// for logging: TODO: remove this
		System.out.println("****************************");
		System.out.println("performance in 100 rounds: ");
		double[] betaVector = new double[beta.size()];
		for (int i = 0; i < betaVector.length; i++) {
			betaVector[i] = beta.get(i);
		}
		EvaluateTetrisAgent.gamesTetris(1, random, featureSet, beta, rolloutActionType,  paretoFeatureSet, paretoWeights, true);
		System.out.println("****************************");
	}

	//update beta at each step
	public void iterate() {
		for (int k = 0; k <= this.maxSim; k++) {
			long t0 = System.currentTimeMillis();
			System.out.println("next integer: " +random.nextInt());
			this.rolloutSet = RolloutUtil.getRolloutSetTetris(game, rolloutSetSize, beta, featureSet, rolloutActionType, paretoFeatureSet, paretoWeights, random);

			double[] ys = new double[this.rolloutSet.size()];
			double[][] xs = new double[this.rolloutSet.size()][this.numFeatures];

			for (int i = 0; i < this.rolloutSet.size(); i++) {
				Object state = this.rolloutSet.get(i);
				List<Action> actions =  game.getActionsIncludingGameover(state, this.rolloutActionType, featureSet, paretoFeatureSet, paretoWeights);
				Action action = UtilAmpi.randomChoice(actions, random);

				List<Double> x = this.game.getFeatureValues(featureSet, state, action);
				Pair<Object, Action> sa = new Pair<>(state, action);
 				double y = RolloutUtil.doRolloutTetrisIterative(sa, this.nRollout, beta, beta, gamma, featureSet, featureSet, random, task, rolloutActionType, paretoFeatureSet, paretoWeights);
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
			EvaluateTetrisAgent.gamesTetris(round, random, featureSet, beta, rolloutActionType, paretoFeatureSet,  paretoWeights, true);
			System.out.println(String.format("This iteration took: %s seconds", (System.currentTimeMillis() - t0)/(1000.0)));
			System.out.println("****************************");
		}
	}
}
