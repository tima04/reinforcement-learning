package util;

public class LinearDecisionRule {



    static double[] defaultWeights = new double[]{
            -13.08,
            -19.77,
            -9.22,
            -10.49,
            6.60,
            -12.63,
            -24.04,
            -1.61
    };


    //objects with values1 dominates object with values2
    public static boolean exhibitsDominance(double[] weights, double[] values1, double[] values2){
        double[] weightedDelta = new double[weights.length];
        double[] weightSigns = new double[weights.length];
        for (int i = 0; i < weights.length; i++) {
            weightSigns[i] = weights[i]/Math.abs(weights[i]);
        }
        for (int i = 0; i < weights.length; i++) {
            double delta = values1[i] - values2[i];
            weightedDelta[i] = weightSigns[i] * delta;
        }
        int[] orderedIndices = Compute.orderedIndices(weightedDelta);
        return weightedDelta[orderedIndices[0]] > 0 &&
                weightedDelta[orderedIndices[weightedDelta.length-1]] >= 0;

    }

    //objects with values1 dominates object with values2
    public static boolean exhibitsDominance(double[] values1, double[] values2){
        double[] weightedDelta = new double[values1.length];
        for (int i = 0; i < values1.length; i++) {
            double delta = values1[i] - values2[i];
            weightedDelta[i] = delta;
        }
        int[] orderedIndices = Compute.orderedIndices(weightedDelta);
        return weightedDelta[orderedIndices[0]] > 0 &&
                weightedDelta[orderedIndices[weightedDelta.length-1]] >= 0;

    }

    //objects with values1 dominates object with values2
    public static boolean exhibitsDominanceEquivalence(double[] weights, double[] values1, double[] values2){
        double[] weightedDelta = new double[weights.length];
        for (int i = 0; i < weights.length; i++) {
            double delta = values1[i] - values2[i];
            weightedDelta[i] = weights[i] * delta;
        }
        int[] orderedIndices = Compute.orderedIndices(weightedDelta);
        return weightedDelta[orderedIndices[0]] >= 0 &&
                weightedDelta[orderedIndices[weightedDelta.length-1]] >= 0;

    }

    //objects with values1 cumulatively dominates object with values2
    //weights don't need to be ordered
    public static boolean exhibitsCumulativeDominance(double[] weights, double[] values1, double[] values2){
        double[] weightedDelta = new double[weights.length];
        double[] unsignedWeights = new double[weights.length];
        double[] weightSigns = new double[weights.length];
        for (int i = 0; i < weights.length; i++) {
            unsignedWeights[i] = Math.abs(weights[i]);
            weightSigns[i] = weights[i]/Math.abs(weights[i]);
        }
        int[] orderedIndices = Compute.orderedIndices(unsignedWeights);
        for (int i = 0; i < weightSigns.length-1; i++) {
            double delta = 0;
            for (int j = 0; j <= i; j++) {
                delta += (values1[orderedIndices[j]] - values2[orderedIndices[j]])*weightSigns[orderedIndices[j]];
            }
            weightedDelta[i] = delta;
        }
        //last term:
        double delta = 0;
        for (int j = 0; j < weightSigns.length; j++) {
            delta += (values1[orderedIndices[j]] - values2[orderedIndices[j]])*weightSigns[orderedIndices[j]];
        }
        weightedDelta[weightSigns.length-1] = delta;
        int[] orderedIndicesWeightedDelta = Compute.orderedIndices(weightedDelta);
        return weightedDelta[orderedIndicesWeightedDelta[0]] > 0 &&
                weightedDelta[orderedIndicesWeightedDelta[weightedDelta.length-1]] >= 0;
    }

    //objects with values1 cumulatively dominates or is equivalent to object with values2
    //weights don't need to be ordered before
    public static boolean exhibitsCumulativeDominanceEquivalence(double[] weights, double[] values1, double[] values2){
        double[] weightedDelta = new double[weights.length];
        double[] unsignedWeights = new double[weights.length];
        double[] weightSigns = new double[weights.length];
        for (int i = 0; i < weights.length; i++) {
            unsignedWeights[i] = Math.abs(weights[i]);
            weightSigns[i] = weights[i]/Math.abs(weights[i]);
        }
        int[] orderedIndices = Compute.orderedIndices(unsignedWeights);
        for (int i = 0; i < weights.length-1; i++) {
            double delta = 0;
            for (int j = 0; j <= i; j++) {
                delta += (values1[orderedIndices[j]] - values2[orderedIndices[j]])*weightSigns[orderedIndices[j]];
            }
            weightedDelta[i] = (unsignedWeights[orderedIndices[i]]-unsignedWeights[orderedIndices[i+1]])*delta;
        }
        //last term:
        double delta = 0;
        for (int j = 0; j < weights.length; j++) {
            delta += (values1[orderedIndices[j]] - values2[orderedIndices[j]])*weightSigns[orderedIndices[j]];
        }
        weightedDelta[weights.length-1] = (unsignedWeights[orderedIndices[weights.length-1]])*delta;
        int[] orderedIndicesWeightedDelta = Compute.orderedIndices(weightedDelta);
        return weightedDelta[orderedIndicesWeightedDelta[0]] >= 0 &&
                weightedDelta[orderedIndicesWeightedDelta[weightedDelta.length-1]] >= 0;
    }


    public static boolean exhibitsNoncompensatoriness(double[] weights, double[] values1, double[] values2){
        boolean noncompensatoryPossible = false;
        double decision = 0;
        double[] unsignedWeights = new double[weights.length];
        double[] weightSigns = new double[weights.length];
        for (int i = 0; i < weights.length; i++) {
            unsignedWeights[i] = Math.abs(weights[i]);
            weightSigns[i] = weights[i]/Math.abs(weights[i]);
        }
        int[] orderedIndices = Compute.orderedIndices(unsignedWeights);
        double lastDecision = unsignedWeights[orderedIndices[0]]*(values1[orderedIndices[0]] - values2[orderedIndices[0]])*weightSigns[orderedIndices[0]];
        for (int i = 0; i < weights.length; i++) {
            decision += unsignedWeights[orderedIndices[i]]*(values1[orderedIndices[i]] - values2[orderedIndices[i]])*weightSigns[orderedIndices[i]];
            if(decision > 0){
                noncompensatoryPossible = true;
            }
            if(lastDecision*decision < 0){
                return false;
            }
            lastDecision = decision;
        }
        return noncompensatoryPossible;
    }

    public static boolean exhibitsNoncompensatorinessEquivalence(double[] weights, double[] values1, double[] values2){
        boolean noncompensatoryPossible = true;
        double decision = 0;
        double[] unsignedWeights = new double[weights.length];
        double[] weightSigns = new double[weights.length];
        for (int i = 0; i < weights.length; i++) {
            unsignedWeights[i] = Math.abs(weights[i]);
            weightSigns[i] = weights[i]/Math.abs(weights[i]);
        }
        int[] orderedIndices = Compute.orderedIndices(unsignedWeights);
        double lastDecision = unsignedWeights[orderedIndices[0]]*(values1[orderedIndices[0]] - values2[orderedIndices[0]])*weightSigns[orderedIndices[0]];
        for (int i = 0; i < weights.length; i++) {
            decision += unsignedWeights[orderedIndices[i]]*(values1[orderedIndices[i]] - values2[orderedIndices[i]])*weightSigns[orderedIndices[i]];
            if(lastDecision*decision < 0){
                return false;
            }
            lastDecision = decision;
        }
        return noncompensatoryPossible;
    }


    public static boolean exhibitsApproximateDominance(double[] weights, double[] values1, double[] values2,
                                                       int betterIn, int worseIn) {
        if(exhibitsDominance(weights, values1, values2)){
            return true;
        }
        double[] weightedDelta = new double[weights.length];
        double[] weightSigns = new double[weights.length];
        for (int i = 0; i < weights.length; i++) {
            weightSigns[i] = weights[i]/Math.abs(weights[i]);
        }
        for (int i = 0; i < weights.length; i++) {
            double delta = values1[i] - values2[i];
            weightedDelta[i] = weightSigns[i] * delta;
        }
        int[] orderedIndices = Compute.orderedIndices(weightedDelta);
        for (int i = 0; i < betterIn; i++) {
            if(weightedDelta[orderedIndices[i]] <= 0){
                return false;
            }
        }
        if(weightedDelta[orderedIndices[weightedDelta.length -1 - worseIn]] < 0){
            return false;
        }
        return true;
    }



    public static boolean[] paretoDominanceSet(double[] weights, double[][] objects){
        boolean[] pareto = new boolean[objects.length];
        for (int i = 0; i < pareto.length; i++) {
            pareto[i] = true;
        }
        for (int i = 0; i < objects.length; i++) {
            for (int j = 0; j < objects.length; j++) {
                if(pareto[i] && pareto[j]){
                    double[] featuresValuesI = objects[i];
                    double[] featuresValuesJ = objects[j];
                    if(exhibitsDominance(weights, featuresValuesI, featuresValuesJ)){
                        pareto[j] = false;
                    }else if(exhibitsDominance(weights, featuresValuesJ, featuresValuesI)){
                        pareto[i] = false;
                    }
                }
            }
        }
        return pareto;
    }

    public static boolean[] paretoCumDominanceSet(double[] weights, double[][] objects){
        boolean[] pareto = new boolean[objects.length];
        for (int i = 0; i < pareto.length; i++) {
            pareto[i] = true;
        }
        for (int i = 0; i < objects.length; i++) {
            for (int j = 0; j < objects.length; j++) {
                if(pareto[i] && pareto[j]){
                    double[] featuresValuesI = objects[i];
                    double[] featuresValuesJ = objects[j];
                    if(exhibitsCumulativeDominance(weights, featuresValuesI, featuresValuesJ)){
                        pareto[j] = false;
                    }else if(exhibitsCumulativeDominance(weights, featuresValuesJ, featuresValuesI)){
                        pareto[i] = false;
                    }
                }
            }
        }
        return pareto;
    }

    //the objects in the set are the ones that are noncompensatorily chosen over the others.
    public static boolean[] paretoNoncompensatorySet(double[] weights, double[][] objects){
        boolean[] pareto = new boolean[objects.length];
        for (int i = 0; i < pareto.length; i++) {
            pareto[i] = true;
        }
        for (int i = 0; i < objects.length; i++) {
            for (int j = 0; j < objects.length; j++) {
                if(pareto[i] && pareto[j]){
                    double[] featuresValuesI = objects[i];
                    double[] featuresValuesJ = objects[j];
                    if(exhibitsNoncompensatoriness(weights, featuresValuesI, featuresValuesJ)){
                        pareto[j] = false;
                    }else if(exhibitsNoncompensatoriness(weights, featuresValuesJ, featuresValuesI)){
                        pareto[i] = false;
                    }
                }
            }
        }
        return pareto;
    }


}
