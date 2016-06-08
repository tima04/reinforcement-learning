package algs.rl;

import domains.Action;
import domains.Domain;
import domains.State;
import domains.Task;
import domains.tictactoe.TicTacToe;
import domains.tictactoe.TicTacToeTask;
import models.*;
import util.Pick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ValueIteration {


    public static void main(String[] arg){
        TicTacToe ticTacToe = new TicTacToe();
        List<State> states = ticTacToe.states().stream().collect(Collectors.<State>toList());
        System.out.println(states.size());
        ValueIteration valueIteration = new ValueIteration(states, ticTacToe, new TicTacToeTask(0.9), new Random(1));
        valueIteration.computeOptimalV();
        valueIteration.computeQFactors();
    }

    // Parameters
    protected double convThreshold = 0.000001; // possible problem
    protected double gamma;
    protected V valueState;
    protected Q qFactors;
    protected Domain domain;
    protected Task task;
    List<State> states;
    Random random;

    public ValueIteration(List<State> states, Domain dom, Task task, Random random) {
        this.states = states;
        this.gamma = task.gamma();
        valueState = new ValueTable(0);
        qFactors = new QTable(0);
        domain = dom;
        this.task = task;
        this.random = random;
    }

    public V computeOptimalV(){
        int iterationCt = 0;
        double delta;
        do {
            delta = performOneIteration();
            iterationCt++;
        } while (delta > convThreshold);
        return valueState;
    }

    public Q computeQFactors(){
        computeOptimalV();
        fillQTable();
        return qFactors;
    }

    public Policy computeOptimalPolicy(){
        HashMap<State, Action> policy  = new HashMap();
        for (int i = 0; i < states.size(); i++) {
            State s = states.get(i);
            Action bestAction = findBestAction(s);
            policy.put(s, bestAction);
        }
        return new FixedPolicy(policy);
    }
    public Policy createRandomPolicy(){
        HashMap<State, Action> policy  = new HashMap();
        for (int i = 0; i < states.size(); i++) {
            State s = states.get(i);
            List<Action> actions = s.getActions();
            int randomIdx = Pick.indexRandom(actions.size(), random);
            policy.put(s, actions.get(randomIdx));
        }
        return new FixedPolicy(policy);
    }


    private void fillQTable() {
        for (State state : states) {
            List<Action> primitives = state.getActions();
            for (Action pri : primitives) {
                List<State> sprimes = new ArrayList<State>();
                List<Double> probs = new ArrayList();
                state.getEffect(pri, sprimes, probs);
                double qValue = 0;
                for (int j = 0; j < probs.size(); j++) {
                    qValue += probs.get(j)*(task.getReward(state, pri, sprimes.get(j)) + gamma*(stateValue(sprimes.get(j))));
                }
                qFactors.setValue(state, pri, qValue);
            }
        }

    }

    private Action findBestAction(State s) {
        valueState.getValue(s);
        Action bestAction = null;
        double highestActionValue = 0 - Double.MAX_VALUE;
        List<Action> primitives = s.getActions();
        for (int i = 0; i < primitives.size(); i++) {
            ArrayList<State> sPrimes = new ArrayList<State>();
            List<Double> probs = new ArrayList<Double>();
            s.getEffect(primitives.get(i), sPrimes, probs);
            double actionValue = 0;
            for (int j = 0; j < sPrimes.size() ; j++) {
                State sPrime = sPrimes.get(j);
                double prob = probs.get(j);
                actionValue += prob*valueState.getValue(sPrime);
            }
            if(highestActionValue < actionValue){
                highestActionValue = actionValue;
                bestAction = primitives.get(i);
            }
        }

        return bestAction;
    }

    public double performOneIteration() {
        double delta = 0;
        for (int i = 0; i < states.size(); i++) {
            State s = states.get(i);
            delta += updateStateValue(s);
        }
        return delta;
    }


    protected double updateStateValue(State s) {
        // Find possible actions
        List<Action> actions = s.getActions();
        //log.debug("Actions from state " + s + Show.show(actions));

        // For each action, find its consequences: sPrimes, probs, expRewards
        double highestValue = 0 - Double.MAX_VALUE;

        for (int j = 0; j < actions.size(); j++) {
            Action a = actions.get(j);
            // Find out action consequences: sPrimes, probs, expRewards
            ArrayList<State> sPrimes = new ArrayList<State>();
            List<Double> probs = new ArrayList<Double>();
            s.getEffect(a, sPrimes, probs);

            // Debug info
            //log.debug("### Value iteration step");
            //log.debug("    State: " + s);
            //log.debug("    Action: " + a);
            //log.debug("    sPrimes: " + Show.showItems(sPrimes.iterator(), " "));
            //log.debug("    tProbs: " + probs.toString());
            double actionValue = 0;
            // Compute one-step, full-backup value
            for (int i = 0; i < sPrimes.size() ; i++) {
                State sPrime = sPrimes.get(i);
                double prob = probs.get(i);
                actionValue += prob * (task.getReward(s,a,sPrime) + gamma * valueState.getValue(sPrime));
            }

            if (actionValue > highestValue)
                highestValue = actionValue;
        }
        // Set value of s to the maximum expected value
        double diff = Math.abs(valueState.getValue(s) - highestValue);
        valueState.setValue(s, highestValue);

        return diff;
    }

    public double stateValue(State state){
        return valueState.getValue(state);
    }
}
