package models;

import domains.Action;
import domains.Features;
import domains.State;
import domains.Task;
import domains.tetris.TetrisAction;
import org.apache.commons.math3.util.Pair;
import policy.PickAction;
import util.UtilAmpi;
import util.DistinctCounter;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


public class CustomPolicy implements Policy {

	PickAction pick;

	Random random;

	//usage filter out gameover states
	Task task;



	public CustomPolicy(PickAction pick, Task task, Random random) {
		this.pick = pick;
		this.task = task;
		this.random = random;
	}

	@Override
	public Action pickAction(State s) {

		List<Pair<Action, Features>> actionFeaturesList = s.getActionFeaturesList();

		//filter out gameover actions
		List<Pair<Action, Features>> actions = actionFeaturesList.stream().filter(p -> !task.taskEnds(p.getSecond())).collect(Collectors.toList());
		if(actions.isEmpty())
			return actionFeaturesList.get(0).getFirst();

		int[] actionIndices = pick.pick(s, actions);

		return actions.get(actionIndices[random.nextInt(actionIndices.length)]).getFirst();
	}

	@Override
	public double isGreedyAction(State s, Action a) {
		List<Pair<Action, Features>> actionFeaturesList = s.getActionFeaturesList();
		int[] actionIndices = pick.pick(s, actionFeaturesList);
		for (int actionIndex : actionIndices)
			if(actionFeaturesList.get(actionIndex).getFirst().equals(a))
				return (double)1/(double)actionIndices.length;

		return 0;
	}

	@Override
	public PickAction getPick() {
		return pick;
	}

}
