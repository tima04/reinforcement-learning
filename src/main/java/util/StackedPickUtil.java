package util;


import domains.FeatureSet;
import domains.tetris.TetrisTaskLines;
import policy.PickAction;
import policy.SingleCue;
import policy.StackedPick;
import policy.Tally;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StackedPickUtil {

    public static policy.StackedPick generateRandomStackedPick(int numLevels, FeatureSet featureSet, Random random){
        List<PickAction> picks = new ArrayList<>();
        for (int i = 0; i < numLevels; i++)
            picks.add(getRandomPick(featureSet, random));

        policy.StackedPick stackedPick = new policy.StackedPick(picks);
        return stackedPick;
    }

    /**
     * Returns a mutated decision rule.
     * @param stackedPick
     * @return
     */
    public static policy.StackedPick mutateStackedPick(policy.StackedPick stackedPick, FeatureSet featureSet, Random random){
        List<PickAction> picks = new ArrayList<>();
        List<PickAction> motherPicks = stackedPick.getPicks();
        int mutatedPickIdx = random.nextInt(motherPicks.size());
        for (int i = 0; i < motherPicks.size(); i++) {
            PickAction motherPick = motherPicks.get(i);
            if(i == mutatedPickIdx) {
                picks.add(getRandomPick(featureSet, random));
            }else{
                picks.add(motherPick);
            }
        }
        policy.StackedPick mutatedStackedPick = new policy.StackedPick(picks);
        return mutatedStackedPick;
    }

    public static PickAction getRandomPick(FeatureSet featureSet, Random random){
        if(random.nextGaussian() > 0){//add single cue
            double signProducer = random.nextGaussian();
            int sign = (int) (signProducer / Math.abs(signProducer));
            int featureIdx = Pick.indexRandom(featureSet.featureNames().size(), random);
            SingleCue singleCue = new SingleCue(sign, featureIdx, featureSet, 0, random, new TetrisTaskLines(1));
            return singleCue;
        }else{//add double cue tally
            double signProducer = random.nextGaussian();
            int sign1 = (int) (signProducer / Math.abs(signProducer));
            int featureIdx1 = Pick.indexRandom(featureSet.featureNames().size(), random);
            signProducer = random.nextGaussian();
            int sign2 = (int) (signProducer / Math.abs(signProducer));
            int featureIdx2 = Pick.indexRandom(featureSet.featureNames().size(), random);
            Tally tally = new Tally(new int[]{sign1, sign2}, new int[]{featureIdx1, featureIdx2}, featureSet, random);
            return tally;
        }
    }

    public static List<StackedPick> generateOffspring(List<StackedPick> bestHeuristics, FeatureSet featureSet, int n, Random random, int numNew) {
        List<StackedPick> offpsring = new ArrayList<>();
        for (StackedPick bestHeuristic : bestHeuristics)
            offpsring.add(bestHeuristic);

        int motherIndex = 0;
        for (int i = offpsring.size(); i < n - numNew; i++) {
            offpsring.add(mutateStackedPick(bestHeuristics.get(motherIndex), featureSet, random));
            motherIndex++;
            if(motherIndex >= bestHeuristics.size())
                motherIndex = 0;
        }
        for (int i = n-numNew; i < n; i++) {
            offpsring.add(generateRandomStackedPick((random.nextInt(3)+1),featureSet, random));
        }

        return offpsring;
    }

}
