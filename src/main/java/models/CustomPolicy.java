package models;

import domains.Action;
import domains.Features;
import domains.State;
import domains.Task;
import org.apache.commons.math3.util.Pair;
import policy.PickAction;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


public class CustomPolicy implements Policy {

	PickAction pick;

	//usage filter out gameover states
	Task task;

	public CustomPolicy(PickAction pick, Task task) {
		this.pick = pick;
		this.task = task;
	}

	@Override
	public Action pickAction(State s) {

		List<Pair<Action, Features>> actionFeaturesList = s.getActionFeaturesList();

		//filter out gameover actions
		List<Pair<Action, Features>> actions = actionFeaturesList.stream().filter(p -> !task.taskEnds(p.getSecond())).collect(Collectors.toList());

		return actionFeaturesList.get(pick.pick(s, actionFeaturesList)).getFirst();
	}

	@Override
	public boolean isGreedyAction(State s, Action a) {
		return pickAction(s).equals(a);
	}

}
