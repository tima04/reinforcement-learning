package util;

import com.joptimizer.functions.ConvexMultivariateRealFunction;
import com.joptimizer.optimizers.JOptimizer;
import com.joptimizer.optimizers.OptimizationRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simonalgorta on 03/06/16.
 */
public class QpSolveUtil {

    public static List<Double> solve(ConvexMultivariateRealFunction objectiveFunction, List<ConvexMultivariateRealFunction> inequalities) {

        OptimizationRequest or = new OptimizationRequest();
        or.setF0(objectiveFunction);
//        or.setInitialPoint(initialPoint);

        ConvexMultivariateRealFunction[] ineqArray = new ConvexMultivariateRealFunction[inequalities.size()];

        for (int i = 0; i < inequalities.size(); i++)
            ineqArray[i] =inequalities.get(i);

        or.setFi(ineqArray); //if you want x>0 and y>0

        //optimization
        JOptimizer opt = new JOptimizer();
        opt.setOptimizationRequest(or);
        List<Double> rewardFunction = new ArrayList<>();
        try {
            opt.optimize();
            double[] sol = opt.getOptimizationResponse().getSolution();
            for (int j = 0; j < sol.length; j++)
                rewardFunction.add(sol[j]);

            return rewardFunction;


        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
