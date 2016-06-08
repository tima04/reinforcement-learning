package domains.tictactoe;



import domains.Action;
import domains.Domain;
import domains.Features;
import domains.State;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TicTacToe implements Domain {

    protected Set<State> setOfStates = null;  // lazy initialization

    @Override
    public HashSet<State> states() {
        if (setOfStates == null)
            generateSetOfStates();
        HashSet<State> out = new HashSet<State>(setOfStates.size());
        out.addAll(setOfStates);
        return out;
    }

    protected void generateSetOfStates() {
        Set<State> processed = new HashSet<State>();
        List<State> willProcess = new ArrayList<State>();
        willProcess.add(new TicTacToeState());
        while (!willProcess.isEmpty()) {
            State current = willProcess.remove(0);
            processed.add(current);
//            System.out.println(processed.size()+" "+willProcess.size());
            for (State s : successors(current))
                if (!processed.contains(s) && !willProcess.contains(s))
                    willProcess.add(s);
        }
        setOfStates = processed;
    }


    public Set<State> successors(State s) {
        Set<State> successors = new HashSet<State>();
        ArrayList<State> sprimes;
        ArrayList<Double> tprobs;
        List<Pair<Action, Features>> prims = s.getActionFeaturesList();
        // Generate successors using getEffect()
        for (int i = 0; i < prims.size(); i++) {
            Action a = prims.get(i).getFirst();
            sprimes = new ArrayList<State>();
            tprobs = new ArrayList<Double>();
            s.getEffect(a, sprimes, tprobs);
            successors.addAll(sprimes);
        }
        return successors;
    }


}
