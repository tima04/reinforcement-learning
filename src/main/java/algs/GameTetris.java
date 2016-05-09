package ampi;

import algorta.domains.tetris2.Tetris;
import algorta.domains.tetris2.TetrisFeatures;
import algorta.domains.tetris2.TetrisState;
import algorta.factory.Agents;
import algorta.rl.DomainKnown;
import algorta.rl.Primitive;
import algorta.rl.State;
import algorta.rl.Task;
import algorta.rle.AgentPrimitive;
import algorta.rle.Experience;
import algorta.rle.Interaction;
import algorta.rle.Updatable;
import algorta.scratch.Feature;
import algorta.scratch.FeatureExtractor;
import algorta.scratch.FeatureSet;
import algorta.util.Compute;
import algorta.util.LinearDecisionRule;
import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GameTetris {
	Tetris tetris;
	Random random;

	final String paretoFeatureSet = "bcts";
	final double[] paretoWeights = new double[]{-13.08, -19.77, -9.22, -10.49, 6.60, -12.63, -24.04, -1.61};

	public GameTetris(){
		tetris = new Tetris();
		random = new Random(1);
	}

	public List<String> getFeatureSets() {
		return TetrisFeatures.getFeatureSetsNames();
	}

	public List<Double> getFeatureValues(String featureSet, Object state) {
		TetrisState tState = (TetrisState) state;
		return tState.features.getFeatureValues(featureSet);
	}


	public List<Double> getFeatureValues(String featureSet, Object state, String action) {
		TetrisState tState = (TetrisState) state;
		List<Pair<String,List<Double>>> actionFeatures = tState.getActionFeatures(featureSet);
		for (Pair<String, List<Double>> actionFeature : actionFeatures)
			if( actionFeature.getFirst().equals(action))
				return actionFeature.getSecond();

		return new ArrayList<>();
	}

	public List<Double> getFeatureValuesIncludeGameOver(String featureSet, Object state, String action) {
		TetrisState tState = (TetrisState) state;
		List<Pair<String,List<Double>>> actionFeatures = tState.getActionFeaturesIncludingGameover(featureSet);
		for (Pair<String, List<Double>> actionFeature : actionFeatures)
			if(actionFeature.getFirst().equals(action))
				return actionFeature.getSecond();

		return new ArrayList<>();
	}

	public List<Pair<String, List<Double>>> getStateActionFeatureValues(String featureSet, Object state) {
		return getStateActionFeatureValues(featureSet, state, Util.ActionType.ANY);
	}

	public List<Pair<String, List<Double>>> getStateActionFeatureValues(String featureSet, Object state, Util.ActionType actionType) {
		TetrisState tState = (TetrisState) state;
		List<Pair<String, List<Double>>> actionFeatures = tState.getActionFeatures(featureSet);
		List<Pair<String, List<Double>>> actionFeaturesPareto = tState.getActionFeatures(paretoFeatureSet);
		List<Pair<String, List<Double>>> actionFeaturesReturn = new ArrayList<>();
		if(actionFeaturesPareto.size() > 0) {
			boolean[] is_pareto = Util.paretoList(actionFeaturesPareto, actionType, paretoWeights);
			for (int i = 0; i < actionFeatures.size(); i++) {
				if (is_pareto[i])
					actionFeaturesReturn.add(actionFeatures.get(i));
			}
		}

		return actionFeaturesReturn;
	}

	public List<Pair<String, List<Double>>> getStateActionFeatureValuesIncludingGameover(String featureSet, Object state, Util.ActionType actionType) {
		TetrisState tState = (TetrisState) state;
		List<Pair<String, List<Double>>> actionFeatures = tState.getActionFeaturesIncludingGameover(featureSet);
		List<Pair<String, List<Double>>> actionFeaturesPareto = tState.getActionFeaturesIncludingGameover(paretoFeatureSet);
		boolean[] is_pareto = Util.paretoList(actionFeaturesPareto, actionType, paretoWeights);
		List<Pair<String, List<Double>>> actionFeaturesReturn = new ArrayList<>();
		for (int i = 0; i < actionFeatures.size(); i++) {
			if(is_pareto[i])
				actionFeaturesReturn.add(actionFeatures.get(i));
		}
		return actionFeatures;
	}

	public List<String> getFeatureNames(String featureSetName) {
		return TetrisFeatures.getFeatureSetNames(featureSetName);
	}

	public List<Object> getRandomStates(int n, List<Double> beta, String featureSet, int modulusFactor , Util.ActionType actionType) {
		TetrisState state = (TetrisState) tetris.root();
		List states = new ArrayList<>();
		for (int i = 0; i < n*5; i++) {
			List<Pair<TetrisState.Action, TetrisFeatures>> actions = state.getActionFeaturesIncludingGameover(); //We include actions that end the game immediatly.
			List<Pair<String, List<Double>>> actionFeaturesPareto = state.getActionFeaturesIncludingGameover(paretoFeatureSet);
			boolean[] is_pareto = Util.paretoList(actionFeaturesPareto, actionType, paretoWeights);
			List<Pair<TetrisState.Action, TetrisFeatures>> actionsPareto = new ArrayList<>();

//			if(actions.isEmpty()) {
//				state = (TetrisState) tetris.root();
//				actions = state.getActionFeatures();
//				actionFeaturesPareto = state.getActionFeatures(paretoFeatureSet);
//				is_pareto = Util.paretoList(actionFeaturesPareto, actionType, paretoWeights);
//			}

			for (int j = 0; j < actions.size(); j++) {
				if(is_pareto[j])
					actionsPareto.add(actions.get(j));
			}
			double[] values = new double[actionsPareto.size()];
			for (int j = 0; j < actionsPareto.size(); j++) {
				TetrisFeatures features = actionsPareto.get(j).getSecond();
				List<Double> featureValues = features.getFeatureValues(featureSet);
				values[j] = Util.dotproduct(beta, featureValues);
			}
			int[] maxIndices = Compute.indicesOfMax(values);
			int chosenAction = random.nextInt(maxIndices.length);
			TetrisState.Action action = actionsPareto.get(maxIndices[chosenAction]).getFirst();
			state.nextState(action.col, action.rot);

			if(state.features.gameOver)
				state = (TetrisState) tetris.root();

			if(i % modulusFactor == 0)
				states.add(state.copy());


		}
		return states;
	}

	public List<Object> getRandomTrajectory(int n, List<Double> beta, String featureSet, Util.ActionType actionType) {
		TetrisState state = (TetrisState) tetris.root();
		List states = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			List<Pair<TetrisState.Action, TetrisFeatures>> actions = state.getActionFeaturesIncludingGameover(); //We include actions that end the game immediatly.
			List<Pair<String, List<Double>>> actionFeaturesPareto = state.getActionFeaturesIncludingGameover(paretoFeatureSet);
			boolean[] is_pareto = Util.paretoList(actionFeaturesPareto, actionType, paretoWeights);
			List<Pair<TetrisState.Action, TetrisFeatures>> actionsPareto = new ArrayList<>();

//			if(actions.isEmpty()) {
//				state = (TetrisState) tetris.root();
//				actions = state.getActionFeatures();
//				actionFeaturesPareto = state.getActionFeatures(paretoFeatureSet);
//				is_pareto = Util.paretoList(actionFeaturesPareto, actionType, paretoWeights);
//			}

			for (int j = 0; j < actions.size(); j++) {
				if(is_pareto[j])
					actionsPareto.add(actions.get(j));
			}
			double[] values = new double[actionsPareto.size()];
			for (int j = 0; j < actionsPareto.size(); j++) {
				TetrisFeatures features = actionsPareto.get(j).getSecond();
				List<Double> featureValues = features.getFeatureValues(featureSet);
//				if (features.gameOver) {//Here exclude the game over states
//					values[j] = -Double.MAX_VALUE;
//				} else {
					values[j] = Util.dotproduct(beta, featureValues);
//				}
			}
			int[] maxIndices = Compute.indicesOfMax(values);
			int chosenAction = random.nextInt(maxIndices.length);
			TetrisState.Action action = actionsPareto.get(maxIndices[chosenAction]).getFirst();
			state.nextState(action.col, action.rot);

			states.add(state.copy());

			if(state.features.gameOver)
				state = (TetrisState) tetris.root();
		}
		return states;
	}

	public List<Object> getRandomStates(int n) {
		return null;//unused

	}

	public Pair<Object, Double> getNewStateAndReward(Object state, String action) {
		TetrisState ts = ((TetrisState)state).copy();
		String[] actionStr = action.split("_");
		int col = Integer.parseInt(actionStr[0]);
		int rot = Integer.parseInt(actionStr[1]);
		ts.nextState(col, rot);
		return new Pair(ts, (double) ts.features.linesCleared.size());
	}

	public List<String> getActions(Object state, Util.ActionType actionType) {
		List<Pair<String, List<Double>>> actionFeatures = getStateActionFeatureValues("bcts", state, actionType);
		List<String> actions = new ArrayList<>();
		for (Pair<String, List<Double>> actionFeaturesPair : actionFeatures)
			actions.add(actionFeaturesPair.getFirst());

		return actions;
	}

	public List<String> getActionsIncludingGameover(Object state, Util.ActionType actionType) {
		List<Pair<String, List<Double>>> actionFeatures = getStateActionFeatureValuesIncludingGameover("bcts", state, actionType);
		List<String> actions = new ArrayList<>();
		for (Pair<String, List<Double>> actionFeaturesPair : actionFeatures)
			actions.add(actionFeaturesPair.getFirst());

		return actions;
	}

	private Primitive getPrimitive(State state, String action){
		return null;//unused
	}

	public double getReward(Object state) {
		TetrisState tState = (TetrisState) state;
		return tState.features.linesCleared.size();
	}

	public boolean isGameover(Object stateBefore) {
		TetrisState tState = (TetrisState) stateBefore;
		return tState.features.gameOver;
	}
}
