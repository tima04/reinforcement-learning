package models;


import domains.Action;
import domains.State;

public interface Q {

	 double getValue(State s, Action a);

     void setValue(State s, Action a, double value);

}
