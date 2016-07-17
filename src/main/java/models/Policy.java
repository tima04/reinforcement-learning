package models;


import domains.Action;
import domains.State;
import policy.PickAction;

public interface Policy {

	 Action pickAction(State s);

	 double isGreedyAction(State s, Action a);

	default PickAction getPick(){
		return null;
	}
}
