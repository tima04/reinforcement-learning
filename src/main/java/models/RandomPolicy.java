package models;



import domains.Action;
import domains.State;

import java.util.List;
import java.util.Random;


public class RandomPolicy implements Policy {

	private Random random;

	public RandomPolicy(Random random) {
		  this.random = random;
	}

	public Action pickAction(State s) {
		List<Action> primitives = s.getActions();
		return primitives.get(random.nextInt(primitives.size()));
	}

	@Override
	public boolean isGreedyAction(State s, Action a) {
		return false;
	}

}
