package models;


import domains.State;

public interface V {

     double getValue(State s);

     void setValue(State s, double value);

}
