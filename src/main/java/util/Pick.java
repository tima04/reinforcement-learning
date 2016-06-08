package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Helping out when picking one among a set of candidates  
 *
 */
public class Pick {

	static final Double	DELTA = Double.MIN_VALUE;

	/** Static class. */
	private Pick() { }

	public static int indexRandom(int numChoices, Random random) {
		return random.nextInt(numChoices);
	}

    /** @param probs probs[i] contains probability of selecting index i; sums to 1. */
	public static int indexwrtProbs(double[] probs, Random random) {
		double randomDouble = random.nextDouble();
		double cum = 0;
		for (int i = 0; i < probs.length; i++) {
			cum += probs[i];
			if (randomDouble <= cum)
				return i;
		}
		return probs.length-1;
	}

	/** @param probs probs[i] contains probability of selecting index i; sums to 1. */
	public static int indexwrtProbs(List<Double> probs, Random random) {
		double randomDouble = random.nextDouble();
		double cum = 0;
		for (int i = 0; i < probs.size(); i++) {
			cum += probs.get(i);
			if (randomDouble <= cum)
				return i;
		}
		return probs.size()-1;
	}

	/** @return index of (a randomly-selected) maximum value in input list. */
	public static int indexMax(List<Double> list, Random random){
		List<Integer> eligibles = indicesMax(list);
		return eligibles.get(indexRandom(eligibles.size(), random));
	}

	/** @return indices of all maximum values in input list. */
	public static List<Integer> indicesMax(List<Double> list){
		List<Integer> indicesMax = new ArrayList<Integer>(list.size());
		double max = Compute.max(list);
		for (int i = 0; i < list.size(); i++)
			if (list.get(i) + DELTA >= max)
				indicesMax.add(i);
		return indicesMax;
	}
}
