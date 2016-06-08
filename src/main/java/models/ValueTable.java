package models;

import domains.State;

import java.util.HashMap;


public class ValueTable implements V {

    private HashMap<State,Double> table;
    private double defaultValue;

    public ValueTable(int defaultValue) {
        table = new HashMap<State, Double>();
        this.defaultValue = defaultValue;
    }

    @Override
    public double getValue(State s) {
        if(table.containsKey(s)) {
            return table.get(s);
        }else{
            return defaultValue;
        }
    }

    @Override
    public void setValue(State s, double value) {
        table.put(s, value);
    }
}
