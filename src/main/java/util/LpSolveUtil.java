package util;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simonalgorta on 03/06/16.
 */
public class LpSolveUtil {

    public static List<Double> solve(List<Double> objectiveFunction, List<List<Double>> constraints, List sign, List<Double> rightside) {
        List<Double> solution = new ArrayList<>();
        try {
            // Create a problem with fe.size() variables and 1 constraints
            LpSolve solver = LpSolve.makeLp(constraints.get(0).size(), constraints.get(0).size());

            StringBuilder of = new StringBuilder();

            for (int j = 0; j < constraints.size(); j++) {
                StringBuilder constraint = new StringBuilder();
                for (int i = 0; i < constraints.get(j).size(); i++) {
                    constraint.append(constraints.get(j).get(i)+" ");
                }
//                System.out.println(constraint.toString());
                solver.strAddConstraint(constraint.toString(), (int)sign.get(j), rightside.get(j));
            }

            for (int i = 0; i < objectiveFunction.size(); i++) {
                of.append(objectiveFunction.get(i));
            }
//            System.out.println(of.toString());

            // set objective function
            solver.strSetObjFn(of.toString());
//            solver.strSetObjFn("2 3 -2 3");
            // solve the problem

            solver.solve();

            // print solution
//            System.out.println("Value of objective function: " + solver.getObjective());
            double[] var = solver.getPtrVariables();
            for (int i = 0; i < var.length; i++) {
//                System.out.println("Value of var[" + i + "] = " + var[i]);
                solution.add(var[i]);
            }

            // delete the problem and free memory
            solver.deleteLp();
        }
        catch (LpSolveException e) {
            e.printStackTrace();
        }
        return solution;
    }
}
