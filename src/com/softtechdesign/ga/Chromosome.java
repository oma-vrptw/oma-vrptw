package com.softtechdesign.ga;

/**
 * Chromosome is the abstract base class for all chromosomes. It defines each chromosome's
 * genes, fitness, fitness rank, and provides simple methods for copying and returning
 * chromosome values as strings.
 *
 * ChromChars, ChromStrings, and ChromFloat both extend Chromosome and model individual candidate
 * solutions. You will probably never need to subclass these classes.
 * @author Jeff Smith jeff@SoftTechDesign.com
 */
public abstract class Chromosome
{
    /** absolute (not relative) fitness value */
    protected double fitness; 
    /** 0 = worst fit, PopDim = best fit */
    protected int fitnessRank; 
    /** Get the genes as a string */
    abstract String getGenesAsStr();
    /** Copy the genes from the given chromosome over this chromosome's genes */
    abstract void copyChromGenes(Chromosome chromosome);
    /** Get the number of genes in common between this chromosome and the given chromosome */
    abstract int getNumGenesInCommon(Chromosome chromosome);
}
