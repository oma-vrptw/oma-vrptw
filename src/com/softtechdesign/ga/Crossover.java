package com.softtechdesign.ga;

/**
 * The valid genetic mating crossover types
 * @author Jeff Smith jeff@SoftTechDesign.com
 */
public interface Crossover
{
    /** one point crossover */
    public static final int ctOnePoint = 0;
    /** two point crossover */
    public static final int ctTwoPoint = 1;
    /** uniform crossover */
    public static final int ctUniform = 2;
    /** roulette crossover (either one point, two point, or uniform) */
    public static final int ctRoulette = 3;
}
