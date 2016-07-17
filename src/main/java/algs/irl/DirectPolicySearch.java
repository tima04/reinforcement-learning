package algs.irl;

import domains.Action;
import domains.Features;
import domains.tetris.*;
import fr.inria.optimization.cmaes.CMAEvolutionStrategy;
import fr.inria.optimization.cmaes.CMASolution;
import fr.inria.optimization.cmaes.fitness.IObjectiveFunction;
import models.CustomPolicy;
import models.Policy;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Pair;
import policy.StackedPick;
import policy.LinearPick;
import policy.PickAction;
import util.Compute;
import util.UtilAmpi;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;


public class DirectPolicySearch {

    TetrisFeatureSet featureSet = new TetrisFeatureSet("lagoudakisthierrybertsekas");
    Random random;
    PickAction filter;

    /**
     * This model first applies the given filter and then looks for the linear rule that best fits.
     * @param random
     * @param featureSet
     * @param filter
     */
    DirectPolicySearch(Random random, TetrisFeatureSet featureSet, PickAction filter){
        this.random = random;
        this.featureSet = featureSet;
        this.filter = filter;

    }

    public Policy fitPolicy(List<List<Pair<TetrisState, TetrisAction>>> trajectories){
        ObjectiveFunction ofun = new ObjectiveFunction();

        for (List<Pair<TetrisState, TetrisAction>> trajectory : trajectories) {
            for (Pair<TetrisState, TetrisAction> stateActionPair : trajectory) {
                List<List<Double>> features = new ArrayList<>();
                List<Double> picked = new ArrayList<>();
                List<Pair<TetrisAction, TetrisFeatures>> actionsFeaturesList = stateActionPair.getFirst().getActionsFeaturesList();
                actionsFeaturesList = actionsFeaturesList.stream().filter(p -> !p.getSecond().gameOver).collect(Collectors.toList());
                List<Pair<Action, Features>> actionsFeaturesListGeneric = actionsFeaturesList.stream().map(p -> (Pair<Action, Features>)new Pair(p.getFirst(), p.getSecond())).collect(Collectors.toList());
                int[] filteredActions = filter.pick(stateActionPair.getFirst(), actionsFeaturesListGeneric);

                for (int filteredAction : filteredActions) {
                    Pair<TetrisAction, TetrisFeatures> actionTetrisFeaturesPair = actionsFeaturesList.get(filteredAction);
                    features.add(featureSet.make(actionTetrisFeaturesPair.getSecond()));
                    if(actionTetrisFeaturesPair.getFirst().equals(stateActionPair.getSecond()))
                        picked.add(1.);
                    else
                        picked.add(0.);
                }
                if(picked.size() > 1)
                    ofun.addChoice(new Pair<>(features, picked));
            }
        }

        List<Double> weights = minimizeUsingCMAES(ofun);
        List<String> names = featureSet.featureNames();
        List<PickAction> picksList = new ArrayList<>();
        picksList.add(filter);
        picksList.add(new LinearPick(weights, featureSet, random));
        Policy policy = new CustomPolicy(new StackedPick(picksList), new TetrisTaskLines(1.), random);
        System.out.println("Policy found:");
        for (int i = 0; i < weights.size(); i++) {
            System.out.println(names.get(i) +":"+weights.get(i));
        }
        return policy;
    }

    private List<Double> minimizeUsingCMAES(ObjectiveFunction fitfun){
        fitfun.printMaxQEstimate();
        double leastFitness = Double.MAX_VALUE;
        int maxScore = 0;
        double[] x = new double[featureSet.featureNames().size()];
//		List<Double> dt10 = TetrisWeightVector.make("DT10");
        for (int iter = 0; iter < 1; iter++) {
            double[] betaVector = new double[featureSet.featureNames().size()];

            for (int i = 0; i < betaVector.length; i++) {
                betaVector[i] = random.nextGaussian();
//				betaVector[i] = 0;
//				betaVector[i] = dt10.get(i);
            }

            PrintStream out = System.out;
            System.setOut(new PrintStream(new OutputStream() {
                @Override public void write(int b) throws IOException {}
            }));
            try {

                // create a CMA-ES and set some initial values
                CMAEvolutionStrategy cma = new CMAEvolutionStrategy();
                //			cma.readProperties(); // read options, see file CMAEvolutionStrategy.properties
                cma.setDimension(featureSet.featureNames().size()); // overwrite some loaded properties
                cma.setInitialX(betaVector); // in each dimension, also setTypicalX can be used
                cma.setInitialStandardDeviation(.01); // also a mandatory setting
                cma.options.stopFitness = 1e-14;       // optional setting
                cma.options.verbosity = 0;
                cma.options.writeDisplayToFile = 0;

                double[] fitness = cma.init();  // new double[cma.parameters.getPopulationSize()];
                // initial output to files
                cma.writeToDefaultFilesHeaders(0); // 0 == overwrites old files

                // iteration loop
                while(cma.stopConditions.getNumber() == 0) {

                    // --- core iteration step ---
                    double[][] pop = cma.samplePopulation(); // get a new population of solutions
                    for(int i = 0; i < pop.length; ++i) {    // for each candidate solution i
                        // a simple way to handle constraints that define a convex feasible domain
                        // (like box constraints, i.e. variable boundaries) via "blind re-sampling"
                        // assumes that the feasible domain is convex, the optimum is
                        while (!fitfun.isFeasible(pop[i]))     //   not located on (or very close to) the domain boundary,
                            pop[i] = cma.resampleSingle(i);    //   initialX is feasible and initialStandardDeviations are
                        //   sufficiently small to prevent quasi-infinite looping here
                        // compute fitness/objective value
                        fitness[i] = fitfun.valueOf(pop[i]); // fitfun.valueOf() is to be minimized
                    }
                    cma.updateDistribution(fitness);         // pass fitness array to update search distribution
                    // --- end core iteration step ---

                    // output to files and console
                    //				cma.writeToDefaultFiles();
                    //				int outmod = 150;
                    //				if (cma.getCountIter() % (15*outmod) == 1)
                    //					cma.printlnAnnotation(); // might write file as well
                    //				if (cma.getCountIter() % outmod == 1)
                    //					cma.println();
                }
                // evaluate mean value as it is the best estimator for the optimum
                cma.setFitnessOfMeanX(fitfun.valueOf(cma.getMeanX())); // updates the best ever solution

                // final output
                //		cma.writeToDefaultFiles(1);
                //			cma.println();
                CMASolution solution = cma.getBestSolution();

//					double[] tempx = solution.getX();
//					System.out.println("cma loss :" +solution.getFitness());
//					Double[] doubleArray = ArrayUtils.toObject(tempx);
//					int mean = EvaluateLinearAgent.gamesTetris(100, random, featureSetClassification, Arrays.asList(doubleArray), actionType, paretoFeatureSet, paretoWeights, false);
//					System.out.println("**** performance in 100 rounds for this iteration of cmaes (mean):" + mean + " loss: "+ solution.getFitness());

                if(solution.getFitness() <= leastFitness){
//					if(mean > maxScore){
                    x = solution.getX();
//						maxScore = mean;
                    leastFitness = solution.getFitness();
                }
            } finally {
                System.setOut(out);
            }
            System.out.println("cmaes optimizer least loss :" + leastFitness);
        }
        Double[] doubleArray = ArrayUtils.toObject(x);
        return Arrays.asList(doubleArray);
    }

    class ObjectiveFunction implements IObjectiveFunction {

        List<Pair<List<List<Double>>, List<Double>>> trainingSet;

        public ObjectiveFunction(){
            trainingSet = new ArrayList<>();
        }

        public void addChoice(Pair<List<List<Double>>, List<Double>> instance){
            trainingSet.add(instance);
        }

        @Override
        public double valueOf(double[] x) {
            double value = 0;
            for (Pair<List<List<Double>>, List<Double>> choice : trainingSet){
//					int bestAction = getBestActionIndex_fast(choice.getFirst(), x);
                double maxQEstimate = Collections.max(choice.getSecond());
                int[] bestActions = getBestActionIndex(choice.getFirst(), x);
                double policyQEstimate = 0;
//					double leastValue = Double.POSITIVE_INFINITY;
                for (int i = 0; i < bestActions.length; i++) {
                    policyQEstimate += choice.getSecond().get(bestActions[i]);
//						if(choice.getSecond().get(bestActions[i]) < leastValue)
//							leastValue = choice.getSecond().get(bestActions[i]);
                }
//					policyQEstimate = leastValue;
                policyQEstimate = policyQEstimate/bestActions.length;
                value += (maxQEstimate - policyQEstimate)/trainingSet.size();
//					value += maxQEstimate - choice.getSecond().get(bestAction);
            }
            return value;
        }


        //this method returns the index of the last best value.
        private int getBestActionIndex_fast(List<List<Double>> actions, double[] x){
            double highestValue = Double.NEGATIVE_INFINITY;
            int idx = -1;
            for (int i = 0; i < actions.size(); i++) {
                double value = UtilAmpi.dotproduct(actions.get(i), x);
                if(value > highestValue){
                    idx = i;
                    highestValue = value;
                }
            }
            return idx;
        }

        private int[] getBestActionIndex(List<List<Double>> actions, double[] x){
            double[] values = new double[actions.size()];
            for (int i = 0; i < actions.size(); i++)
                values[i] = UtilAmpi.dotproduct(actions.get(i), x);

            int[] indicesOfMax = Compute.indicesOfMax(values);
            return indicesOfMax;
        }

        @Override
        public boolean isFeasible(double[] x) {
            return true;
        }

        public void printMaxQEstimate() {
            double value = 0;
            for (Pair<List<List<Double>>, List<Double>> choice : trainingSet) {
                double maxQEstimate = Collections.max(choice.getSecond());
                value += maxQEstimate;
            }
            System.out.println(" MaxQEstimate: "+value);
            System.out.println(" Number of Choices: "+trainingSet.size());
        }
    }
}
