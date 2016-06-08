package models;


import domains.Action;
import domains.State;

public interface Policy {

	 Action pickAction(State s);

	 boolean isGreedyAction(State s, Action a);

}
