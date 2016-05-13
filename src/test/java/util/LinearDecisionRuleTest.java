package util;

import junit.framework.TestCase;

public class LinearDecisionRuleTest extends TestCase {

    //DOMINANCE
    public void testExhibitsDominanceTrueOneValue(){
        double[] weights = new double[]{10,9,8,27,56,5,4,3};
        double[] values1 = new double[]{10,9,8,8,6,5,4,3};
        double[] values2 = new double[]{10,9,8,7,6,5,4,3};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsDominance(weights, values1, values2);
        assertTrue(exhibitsDominance);
    }

    public void testExhibitsDominanceTrueOneValueNegative(){
        double[] weights = new double[]{10,9,8,27,56,5,4,3};
        double[] values1 = new double[]{10,9,8,-8,6,5,4,3};
        double[] values2 = new double[]{10,9,8,-9,6,5,4,3};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsDominance(weights, values1, values2);
        assertTrue(exhibitsDominance);
    }

    public void testExhibitsDominanceTrueAllButOne(){
        double[] weights = new double[]{10,9,8,27,56,5,4,3};
        double[] values1 = new double[]{10,9,8,8,6,5,4,1};
        double[] values2 = new double[]{1,1,1,1,1,1,1,2};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsDominance(weights, values1, values2);
        assertFalse(exhibitsDominance);
    }

    public void testExhibitsDominanceTrueAllButOneNegative(){
        double[] weights = new double[]{10,9,8,27,56,5,4,3};
        double[] values1 = new double[]{10,9,8,8,6,5,4,-20};
        double[] values2 = new double[]{1,1,1,1,1,1,1,-10};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsDominance(weights, values1, values2);
        assertFalse(exhibitsDominance);
    }


    public void testExhibitsDominanceNegative(){
        double[] weights = new double[]{10,9,8,27,56,5,4,3};
        double[] values1 = new double[]{10,9,8,8,6,5,4,3};
        double[] values2 = new double[]{10,9,8,7,6,7,4,3};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsDominance(weights, values1, values2);
        assertFalse(exhibitsDominance);
    }

    public void testExhibitsDominanceNegativeEqualValues(){
        double[] weights = new double[]{10,9,8,27,56,5,4,3};
        double[] values1 = new double[]{10,9,8,8,6,5,4,3};
        double[] values2 = new double[]{10,9,8,8,6,5,4,3};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsDominance(weights, values1, values2);
        assertFalse(exhibitsDominance);
    }

    //DOMINANCE EQUIVALENT
    public void testExhibitsDominanceEquivalentTrueOneValue(){
        double[] weights = new double[]{10,9,8,27,56,5,4,3};
        double[] values1 = new double[]{10,9,8,8,6,5,4,3};
        double[] values2 = new double[]{10,9,8,7,6,5,4,3};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsDominanceEquivalence(weights, values1, values2);
        assertTrue(exhibitsDominance);
    }

    public void testExhibitsDominanceEquivalentTrueOneValueNegative(){
        double[] weights = new double[]{10,9,8,27,56,5,4,3};
        double[] values1 = new double[]{10,9,8,-8,6,5,4,3};
        double[] values2 = new double[]{10,9,8,-9,6,5,4,3};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsDominanceEquivalence(weights, values1, values2);
        assertTrue(exhibitsDominance);
    }

    public void testExhibitsDominanceEquivalentTrueAllButOne(){
        double[] weights = new double[]{10,9,8,27,56,5,4,3};
        double[] values1 = new double[]{10,9,8,8,6,5,4,1};
        double[] values2 = new double[]{1,1,1,1,1,1,1,2};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsDominanceEquivalence(weights, values1, values2);
        assertFalse(exhibitsDominance);
    }

    public void testExhibitsDominanceEquivalentTrueAllButOneNegative(){
        double[] weights = new double[]{10,9,8,27,56,5,4,3};
        double[] values1 = new double[]{10,9,8,8,6,5,4,-20};
        double[] values2 = new double[]{1,1,1,1,1,1,1,-10};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsDominanceEquivalence(weights, values1, values2);
        assertFalse(exhibitsDominance);
    }


    public void testExhibitsDominanceEquivalentNegative(){
        double[] weights = new double[]{10,9,8,27,56,5,4,3};
        double[] values1 = new double[]{10,9,8,8,6,5,4,3};
        double[] values2 = new double[]{10,9,8,7,6,7,4,3};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsDominanceEquivalence(weights, values1, values2);
        assertFalse(exhibitsDominance);
    }

    public void testExhibitsDominanceEquivalentNegativeEqualValues(){
        double[] weights = new double[]{10,9,8,27,56,5,4,3};
        double[] values1 = new double[]{10,9,8,8,6,5,4,3};
        double[] values2 = new double[]{10,9,8,8,6,5,4,3};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsDominanceEquivalence(weights, values1, values2);
        assertTrue(exhibitsDominance);
    }

    //CUMULATIVE DOMINANCE

    public void testExhibitsCumulativeDominanceWeightsTrue(){
        double[] weights = new double[]{0.5,1,0.25};
        double[] values1 = new double[]{2,1,1};
        double[] values2 = new double[]{1,1,1};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsCumulativeDominance(weights, values1, values2);
        assertTrue(exhibitsDominance);
    }

    public void testExhibitsCumulativeDominanceWeightsTrueNegativeValues(){
        double[] weights = new double[]{0.5,1,0.25};
        double[] values1 = new double[]{-2,1,1};
        double[] values2 = new double[]{-3,1,1};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsCumulativeDominance(weights, values1, values2);
        assertTrue(exhibitsDominance);
    }

    public void testExhibitsCumulativeDominanceValuesTrue(){
        double[] weights = new double[]{1,1,1};
        double[] values1 = new double[]{2,5,1};
        double[] values2 = new double[]{1,1,1};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsCumulativeDominance(weights, values1, values2);
        assertTrue(exhibitsDominance);
    }

    public void testExhibitsCumulativeDominanceNegative(){
        double[] weights = new double[]{0.5,1,0.6};
        double[] values1 = new double[]{2,1,1};
        double[] values2 = new double[]{1,1,2};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsCumulativeDominance(weights, values1, values2);
        assertFalse(exhibitsDominance);
    }

    public void testExhibitsCumulativeDominanceNegativeEqualValues(){
        double[] weights = new double[]{0.5,1,0.6};
        double[] values1 = new double[]{1,1,2};
        double[] values2 = new double[]{1,1,2};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsCumulativeDominance(weights, values1, values2);
        assertFalse(exhibitsDominance);
    }

    public void testExhibitsCumulativeDominanceTrueOneValue(){
        double[] weights = new double[]{10,9,8,27,56,5,4,3};
        double[] values1 = new double[]{10,9,8,8,6,5,4,3};
        double[] values2 = new double[]{10,9,8,7,6,5,4,3};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsCumulativeDominance(weights, values1, values2);
        assertTrue(exhibitsDominance);
    }

    public void testExhibitsCumulativeDominanceTrueOneValueNegative(){
        double[] weights = new double[]{10,9,8,27,56,5,4,3};
        double[] values1 = new double[]{10,9,8,-8,6,5,4,3};
        double[] values2 = new double[]{10,9,8,-9,6,5,4,3};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsCumulativeDominance(weights, values1, values2);
        assertTrue(exhibitsDominance);
    }
    //CUMULATIVE DOMINANCE EQUIVALENT

    public void testExhibitsCumulativeDominanceEquivalentWeightsTrue(){
        double[] weights = new double[]{0.5,1,0.25};
        double[] values1 = new double[]{2,1,1};
        double[] values2 = new double[]{1,1,1};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsCumulativeDominanceEquivalence(weights, values1, values2);
        assertTrue(exhibitsDominance);
    }

    public void testExhibitsCumulativeDominanceEquivalentWeightsTrueNegativeValues(){
        double[] weights = new double[]{0.5,1,0.25};
        double[] values1 = new double[]{-2,1,1};
        double[] values2 = new double[]{-3,1,1};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsCumulativeDominanceEquivalence(weights, values1, values2);
        assertTrue(exhibitsDominance);
    }

    public void testExhibitsCumulativeDominanceEquivalentValuesTrue(){
        double[] weights = new double[]{1,1,1};
        double[] values1 = new double[]{2,5,1};
        double[] values2 = new double[]{1,1,1};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsCumulativeDominanceEquivalence(weights, values1, values2);
        assertTrue(exhibitsDominance);
    }

    public void testExhibitsCumulativeDominanceEquivalentNegative(){
        double[] weights = new double[]{0.5,1,0.6};
        double[] values1 = new double[]{2,1,1};
        double[] values2 = new double[]{1,1,2};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsCumulativeDominanceEquivalence(weights, values1, values2);
        assertFalse(exhibitsDominance);
    }

    public void testExhibitsCumulativeDominanceEquivalentNegativeEqualValues(){
        double[] weights = new double[]{0.5,1,0.6};
        double[] values1 = new double[]{1,1,2};
        double[] values2 = new double[]{1,1,2};
        boolean exhibitsDominance = LinearDecisionRule.exhibitsCumulativeDominanceEquivalence(weights, values1, values2);
        assertTrue(exhibitsDominance);
    }

    //NONCOMPENSATORINESS

    public void testExhibitsNoncompensatorinessTrue(){
        double[] weights = new double[]{10,5,2};
        double[] values1 = new double[]{10,8,8};
        double[] values2 = new double[]{9,9,8};
        boolean exhibitsNoncompensatoriness = LinearDecisionRule.exhibitsNoncompensatoriness(weights, values1, values2);
        assertTrue(exhibitsNoncompensatoriness);
    }

    public void testExhibitsNoncompensatorinessFalse(){
        double[] weights = new double[]{10,5,2,2,3};
        double[] values1 = new double[]{10,8,8,5,3};
        double[] values2 = new double[]{9,9,8,7,4};
        boolean exhibitsNoncompensatoriness = LinearDecisionRule.exhibitsNoncompensatoriness(weights, values1, values2);
        assertFalse(exhibitsNoncompensatoriness);
    }

    public void testExhibitsNoncompensatorinessTrueFalseValues(){
        double[] weights = new double[]{10,5,2,3};
        double[] values1 = new double[]{10,-9,8,-10};
        double[] values2 = new double[]{9,-8,8,-9};
        boolean exhibitsNoncompensatoriness = LinearDecisionRule.exhibitsNoncompensatoriness(weights, values1, values2);
        assertTrue(exhibitsNoncompensatoriness);
    }

    public void testExhibitsNoncompensatorinessNegative(){
        double[] weights = new double[]{10,5,2,2,3};
        double[] values1 = new double[]{10,8,8,-5,3};
        double[] values2 = new double[]{9,9,8,-7,20};
        boolean exhibitsNoncompensatoriness = LinearDecisionRule.exhibitsNoncompensatoriness(weights, values1, values2);
        assertFalse(exhibitsNoncompensatoriness);
    }

    public void testExhibitsNoncompensatorinessFalse2(){
        double[] weights = new double[]{10,5,2,2,3};
        double[] values1 = new double[]{10,15,15,-5,3};
        double[] values2 = new double[]{12,9,8,-7,20};
        boolean exhibitsNoncompensatoriness = LinearDecisionRule.exhibitsNoncompensatoriness(weights, values1, values2);
        assertFalse(exhibitsNoncompensatoriness);
    }

    public void testExhibitsNoncompensatorinessNegative2(){
        double[] weights = new double[]{10,5,2,1,3};
        double[] values1 = new double[]{10,2,8,-5,3};
        double[] values2 = new double[]{5,13,8,-7,20};
        boolean exhibitsNoncompensatoriness = LinearDecisionRule.exhibitsNoncompensatoriness(weights, values1, values2);
        assertFalse(exhibitsNoncompensatoriness);
    }

    public void testExhibitsNoncompensatorinessEquals(){
        double[] weights = new double[]{10,5,2,1,3};
        double[] values1 = new double[]{10,2,8,-5,3};
        double[] values2 = new double[]{10,2,8,-5,3};
        boolean exhibitsNoncompensatoriness = LinearDecisionRule.exhibitsNoncompensatoriness(weights, values1, values2);
        assertFalse(exhibitsNoncompensatoriness);
    }

    public void testExhibitsNoncompensatoriness(){
        double[] weights = new double[]{-486.96,
                -243.24,
                -155.52,
                -626.42,
                195.2,
                -287.86,
                -387.64,
                -37.760000000000005};
        double[] values1 =new double[]{0.10810810810810811,
        1.5,
        3.125,
        0.3448275862068966,
        0.59375,
        0.5909090909090909,
        0.25,
        0.8125};
        double[] values2 = new double[]{0.13513513513513514,
                1.6666666666666667,
                3.0,
                0.3448275862068966,
                0.5,
                0.5909090909090909,
                0.3125,
                0.875,};
        boolean exhibitsNoncompensatoriness = LinearDecisionRule.exhibitsNoncompensatoriness(weights, values1, values2);
        assertTrue(exhibitsNoncompensatoriness);
    }

    //NONCOMPENSATORINESS EQUIVALENCE

    public void testExhibitsNoncompensatorinessEquivalenceTrue(){
        double[] weights = new double[]{10,5,2};
        double[] values1 = new double[]{10,8,8};
        double[] values2 = new double[]{9,9,8};
        boolean exhibitsNoncompensatoriness = LinearDecisionRule.exhibitsNoncompensatorinessEquivalence(weights, values1, values2);
        assertTrue(exhibitsNoncompensatoriness);
    }

    public void testExhibitsNoncompensatorinessEquivalenceFalse(){
        double[] weights = new double[]{10,5,2,2,3};
        double[] values1 = new double[]{10,8,8,5,3};
        double[] values2 = new double[]{9,9,8,7,4};
        boolean exhibitsNoncompensatoriness = LinearDecisionRule.exhibitsNoncompensatorinessEquivalence(weights, values1, values2);
        assertFalse(exhibitsNoncompensatoriness);
    }

    public void testExhibitsNoncompensatorinessEquivalenceTrueFalseValues(){
        double[] weights = new double[]{10,5,2,3};
        double[] values1 = new double[]{10,-9,8,-10};
        double[] values2 = new double[]{9,-8,8,-9};
        boolean exhibitsNoncompensatoriness = LinearDecisionRule.exhibitsNoncompensatorinessEquivalence(weights, values1, values2);
        assertTrue(exhibitsNoncompensatoriness);
    }

    public void testExhibitsNoncompensatorinessEquivalenceNegative(){
        double[] weights = new double[]{10,5,2,2,3};
        double[] values1 = new double[]{10,8,8,-5,3};
        double[] values2 = new double[]{9,9,8,-7,20};
        boolean exhibitsNoncompensatoriness = LinearDecisionRule.exhibitsNoncompensatorinessEquivalence(weights, values1, values2);
        assertFalse(exhibitsNoncompensatoriness);
    }

    public void testExhibitsNoncompensatorinessEquivalenceFalse2(){
        double[] weights = new double[]{10,5,2,2,3};
        double[] values1 = new double[]{10,15,15,-5,3};
        double[] values2 = new double[]{12,9,8,-7,20};
        boolean exhibitsNoncompensatoriness = LinearDecisionRule.exhibitsNoncompensatorinessEquivalence(weights, values1, values2);
        assertFalse(exhibitsNoncompensatoriness);
    }

    public void testExhibitsNoncompensatorinessEquivalenceNegative2(){
        double[] weights = new double[]{10,5,2,1,3};
        double[] values1 = new double[]{10,2,8,-5,3};
        double[] values2 = new double[]{5,13,8,-7,20};
        boolean exhibitsNoncompensatoriness = LinearDecisionRule.exhibitsNoncompensatorinessEquivalence(weights, values1, values2);
        assertFalse(exhibitsNoncompensatoriness);
    }

    public void testExhibitsNoncompensatorinessEquivalenceEquals(){
        double[] weights = new double[]{10,5,2,1,3};
        double[] values1 = new double[]{10,2,8,-5,3};
        double[] values2 = new double[]{10,2,8,-5,3};
        boolean exhibitsNoncompensatoriness = LinearDecisionRule.exhibitsNoncompensatorinessEquivalence(weights, values1, values2);
        assertTrue(exhibitsNoncompensatoriness);
    }

    //Approximate Dominance Test
    public void testExhibitsApproximateDominance1(){
        double[] weights = new double[]{1,1,1,1,1,1};
        double[] values1 = new double[]{10,3,8,-5,3,3};
        double[] values2 = new double[]{9,2,7,-6,15,3};
        boolean eAD = LinearDecisionRule.exhibitsApproximateDominance(weights, values1, values2, 4, 1);
        assertTrue(eAD);
    }

    public void testExhibitsApproximateDominance2(){
        double[] weights = new double[]{1,1,1,1,1,1};
        double[] values1 = new double[]{10,3,8,-5,3,3};
        double[] values2 = new double[]{9,2,7,-6,15,15};
        boolean eAD = LinearDecisionRule.exhibitsApproximateDominance(weights, values1, values2, 4, 1);
        assertFalse(eAD);
    }

    public void testExhibitsApproximateDominance3(){
        double[] weights = new double[]{1,1,1,1,1,1,1,1};
        double[] values1 = new double[]{10,3,8,-5,3,3,4,3};
        double[] values2 = new double[]{9,2,7,-6,15,15,2,3};
        boolean eAD = LinearDecisionRule.exhibitsApproximateDominance(weights, values1, values2, 5, 2);
        assertTrue(eAD);
    }

    public void testExhibitsApproximateDominance4(){
        double[] weights = new double[]{1,1,1,1,1,1,1,1};
        double[] values1 = new double[]{10,3,8,-5,3,3,4,3};
        double[] values2 = new double[]{9,2,8,-6,15,15,2,3};
        boolean eAD = LinearDecisionRule.exhibitsApproximateDominance(weights, values1, values2, 5, 2);
        assertFalse(eAD);
    }
}
