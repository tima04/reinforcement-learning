package policy;


import domains.Action;
import domains.Features;
import domains.State;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class StackedPick implements PickAction{

    List<PickAction> picks;

    public StackedPick(List<PickAction> picks){
        this.picks = picks;
    }

    public List<PickAction> getPicks(){
        return picks;
    }

    @Override
    public int[] pick(State state, List<Pair<Action, Features>> actions) {
        List<Pair<Action, Features>> filteredActions = actions;
        int[] actionIndices = new int[]{};
        int[] finalIndices = new int[actions.size()];
        int[] oldActionIndices = new int[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            oldActionIndices[i] = i;
            finalIndices[i] = i;
        }

        for (PickAction pick : picks) {
            actionIndices = pick.pick(state, filteredActions);
            List<Pair<Action, Features>> newFilteredActions = new ArrayList<>();
            for (int actionIdx : actionIndices) {
                newFilteredActions.add(filteredActions.get(actionIdx));
            }
            filteredActions = newFilteredActions;
            finalIndices = new int[actionIndices.length];
            for (int i = 0; i < actionIndices.length; i++)
                finalIndices[i] = oldActionIndices[actionIndices[i]];

            oldActionIndices = finalIndices;
        }
        return finalIndices;
    }

    public void print(List<String> featureNames) {
        for (PickAction pickAction : picks) {
            if(pickAction instanceof SingleCue){
                System.out.println("single cue:");
                System.out.println(featureNames.get(((SingleCue) pickAction).getFeatureIdx())+" (" + ((SingleCue) pickAction).getDirection()+")");
            }else{
                System.out.println("tally:");
                int[] featureIdx = ((Tally)pickAction).getFeaturesIdx();
                int[] signs = ((Tally)pickAction).getDirections();
                for (int i = 0; i < featureIdx.length; i++) {
                    System.out.println(featureNames.get(featureIdx[i])+" (" + signs[i]+")");
                }
            }
        }
    }
}
