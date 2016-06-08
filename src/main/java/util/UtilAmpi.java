package util;


import domains.tetris.TetrisState;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class UtilAmpi {

	public enum ActionType{
		ANY,
		DOM,
		CUMDOM,
		SINGLE_CUE
	}

	public static int[] ArrayDoubletoInt(double[] doubles) {
		int[] newArray = new int[doubles.length];
		for (int i = 0; i < doubles.length; i++)
			newArray[i] = (int) doubles[i];
		return newArray;
	}



public static double dotproduct(List<Double> xs, List<Double> ys) {
	assert xs.size() == ys.size();

	double rslt = 0.;
	for (int i = 0; i < xs.size(); i++)
		rslt += xs.get(i) * ys.get(i);

	return rslt;
}

	public static double dotproduct(List<Double> xs, double[] ys) {
		assert xs.size() == ys.length;

		double rslt = 0.;
		for (int i = 0; i < xs.size(); i++)
			rslt += xs.get(i) * ys[i];

		return rslt;
	}

public static double dotproduct(double[] xs, double[] ys) {
	assert xs.length == ys.length;
	double rslt = 0;
	for (int i = 0; i < xs.length; i++)
		rslt += xs[i] * ys[i];

	return rslt;
}

public static double l2norm(double[] xs) {
	double norm = 0;
	for (int i = 0; i < xs.length; i++)
		norm += xs[i] * xs[i];

	return Math.sqrt(norm);
}

public static double dist(List<Double> xs, List<Double> ys) {
	assert xs.size() == ys.size();
	double rslt = 0.;

	for (int i = 0; i < xs.size(); i++)
		rslt += Math.abs(xs.get(i) - ys.get(i));

	return rslt;
}

public static List<Double> regress(double[] ys, double[][] xs) {
	List<Double> beta = new ArrayList<>();

	RealMatrix mat = MatrixUtils.createRealMatrix(xs);
	DecompositionSolver solver = new SingularValueDecomposition(mat).getSolver();
	double[] beta_ =  solver.solve(MatrixUtils.createRealVector(ys)).toArray();

	for (int i = 0; i < beta_.length; i++) {
		beta.add((Double) beta_[i]);
	}
	return beta;

}

public static List<Double> regress(List<Double> ys, List<List<Double>> xs) {
	double[] arrayY = new double[ys.size()];
	double[][] arrayX = new double[xs.size()][xs.get(0).size()];
	for(int row = 0; row < ys.size(); row++) {
		arrayY[row] = ys.get(row);
		for (int col = 0; col < xs.get(0).size(); col++)
			arrayX[row][col] = xs.get(row).get(col);
	}
	return regress(arrayY, arrayX);
}


public static List<Double> getConvexCombination(List<Double> xs, List<Double> ys, double alpha) {
	assert alpha >= 0 && alpha <= 1;
	assert xs.size() == ys.size();
	List<Double> rslt = new ArrayList<>();
	for (int i = 0; i < xs.size(); i++)
		rslt.add(alpha*xs.get(i) + (1-alpha)*ys.get(i));
	return rslt;
}

	//Returns a sample from the List (value of the map) uniformly distributed according to the Integer (key of the map).
public static <T> List<T> uniformSample(Map<Integer, List<T>> map, int nsample) {
//	map.keySet().stream()
//		.forEach(k -> System.out.println("Before sampling, key: " + k + " elts: " + map.get(k).size()));
	List<T> rslt = new ArrayList<>();
	System.out.println("Subsampling with uniform height.");
	return sampleHelper(map, nsample, rslt);
}

private static <T> List<T> sampleHelper(Map<Integer, List<T>> map, int nsample, List<T> rslt) {
	if (map.size() == 0 || nsample == 0)
		return rslt;

	Integer minKey = Collections.min(map.keySet(), new Comparator<Integer>() {
		@Override
		public int compare(Integer k1, Integer k2) {
			return map.get(k1).size() - map.get(k2).size();
		}
	});
	int k = Math.min(nsample/map.size(), map.get(minKey).size());
	rslt.addAll(new ArrayList<>(map.get(minKey).subList(0, k)));
//	System.out.println("after sampling, key: " + minKey + " nelts: " + k);
	map.remove(minKey);
	return sampleHelper(map, nsample-k, rslt);
}

//required for easy exploration in python
public static Pair<Object, String> makePair(Object state, String action) {
	return new Pair<Object, String>(state, action);
}

public static <T> T randomChoice(List<T> xs, Random random) {
	return xs.get(random.nextInt(xs.size()));
}

public static <T> T randomChoice(List<T> xs) {
	return randomChoice(xs, new Random());
}

	/**
	 * It returns an array of boolean indicating if the objects are in the pareto set.
	 * @param objectFeaturesList
	 * @param actionType
	 * @param weights
	 * @param <T>
     * @return
     */
	public static <T> boolean[] paretoList(List<Pair<T, List<Double>>> objectFeaturesList, ActionType actionType, double[] weights){
		List<Pair<T, List<Double>>> pareto = new ArrayList<>();
		if(objectFeaturesList.isEmpty())
			return new boolean[]{};

		//fill objects
		double[][] objects = new double[objectFeaturesList.size()][objectFeaturesList.get(0).getSecond().size()];

		for (int i = 0; i < objects.length; i++) {
			List<Double> features = objectFeaturesList.get(i).getSecond();
			for (int j = 0; j < features.size(); j++) {
				objects[i][j] = features.get(j);
			}
		}

		boolean[] is_pareto = new boolean[objectFeaturesList.size()];
		Arrays.fill(is_pareto, true);//default is actiontype any, so every object is picked.

		if(actionType.equals(ActionType.DOM))
			is_pareto = LinearDecisionRule.paretoDominanceSet(weights, objects);
		else if(actionType.equals(ActionType.CUMDOM))
			is_pareto = LinearDecisionRule.paretoCumDominanceSet(weights, objects);

		return is_pareto;
	}

	//Works for tetris pile height but can be generalized
	public static <T> List<T> getSubSampleWithUniformHeightNewTetris(List<T> states, int nsample) {
		ArrayList<Integer> heights = states.stream()
				.map(s -> ((TetrisState)s).features.pileHeight)
				.collect(Collectors.toCollection(ArrayList::new));
		Map<Integer, List<T>> map = new HashMap<>();
		for (int i = 0; i < states.size(); i++) {
			int h = heights.get(i);
			List<T> xs = map.getOrDefault(h, new ArrayList<T>());
			xs.add(states.get(i));
			map.put(h, xs);
		}
		return UtilAmpi.uniformSample(map, nsample);
	}



	public static List<int[]> combinations(int n, int k){
		int[] input = new int[n];    // input array
		for (int i = 0; i < n; i++)
			input[i] = i;

		List<int[]> subsets = new ArrayList<>();

		int[] s = new int[k];              // here we keep indices
		// pointing to elements in input array

		if (k <= input.length) {
			// first index sequence: 0, 1, 2, ...
			for (int i = 0; (s[i] = i) < k - 1; i++);
			subsets.add(getSubset(input, s));
			for(;;) {
				int i;
				// find position of item that can be incremented
				for (i = k - 1; i >= 0 && s[i] == input.length - k + i; i--);
				if (i < 0) {
					break;
				} else {
					s[i]++;                    // increment this item
					for (++i; i < k; i++) {    // fill up remaining items
						s[i] = s[i - 1] + 1;
					}
					subsets.add(getSubset(input, s));
				}
			}
		}
		return subsets;
	}

	// generate actual subset by index sequence
	static int[] getSubset(int[] input, int[] subset) {
		int[] result = new int[subset.length];
		for (int i = 0; i < subset.length; i++)
			result[i] = input[subset[i]];
		return result;
	}
}


