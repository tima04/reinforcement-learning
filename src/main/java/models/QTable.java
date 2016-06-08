package models;

import domains.Action;
import domains.State;

import java.util.HashMap;


public class QTable implements Q {

	private HashMap<State, HashMap<Action, Double>> table;
	private double defaultValue;

	public QTable(double defaultValue) {
		table = new HashMap();
		this.defaultValue = defaultValue;
	}

	public double getValue(State s, Action a) {
		// if in table, return its value; otherwise return default Q value.
		if (!isActionInTable(s, a))
			return defaultValue;
		else
			return table.get(s).get(a).doubleValue();
	}

	public void setValue(State s, Action a, double value) {
		HashMap insideTable = (isStateInTable(s) ? table.get(s) : makeAndGetActionTable(s));
		insideTable.put(a, new Double(value));
	}

	private boolean isStateInTable(State s) {
		return (table.containsKey(s));
	}

	private boolean isActionInTable(State s, Action a) {
		return (isStateInTable(s) && table.get(s).containsKey(a));
	}

	private HashMap<Action, Double> makeAndGetActionTable(State s) {
		HashMap<Action, Double> out = new HashMap();
		table.put(s, out);
		return out;
	}
}

