package models;

import domains.Action;
import domains.State;

import java.util.HashMap;


public class FixedPolicy implements Policy {

	protected HashMap<State, Action> policy;

	public FixedPolicy(HashMap policy) {
		this.policy = policy;
	}

	@Override
	public Action pickAction(State s) {
		return policy.get(s);
	}

	@Override
	public double isGreedyAction(State s, Action a) {
		return policy.get(s).equals(a)?1:0;
	}

}
