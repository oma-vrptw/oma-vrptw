package com.softtechdesign.ga;

/**
 * GAString models chromosomes as strings of characters
 * @author Jeff Smith jeff@SoftTechDesign.com
 */
public abstract class GAString extends GA
{
    /** you can store floating point numbers in strings. For example a chromosome might
     * look like "1234" and if chromDecPts = 2, then the number is 12.34 
     */
    protected int chromDecPts;

    /** legal gene (char) values. For example, if the possGeneValues = "ABCD" then a gene
     *  could have the value "A", "B", "C", or "D" 
     */
    protected String possGeneValues;

    /**
     * returns the given chromosome casted as a ChromChars
     * @param index
     * @return ChromChars
     */
    protected ChromChars getChromosome(int index)
    {
        return ((ChromChars) this.chromosomes[index]);
    }

    /**
     * Initialize the GAString
     * @param chromosomeDim
     * @param populationDim
     * @param crossoverProb
     * @param randomSelectionChance
     * @param maxGenerations
     * @param numPrelimRuns
     * @param maxPrelimGenerations
     * @param mutationProb
     * @param chromDecPts
     * @param possGeneValues
     * @param crossoverType
     * @param computeStatistics
     * @throws GAException
     */
    public GAString(int chromosomeDim,
                    int populationDim,
                    double crossoverProb,
                    int randomSelectionChance,
                    int maxGenerations,
                    int numPrelimRuns,
                    int maxPrelimGenerations,
                    double mutationProb,
                    int chromDecPts,
                    String possGeneValues,
                    int crossoverType,
                    boolean computeStatistics)
                    throws GAException
    {
        super(chromosomeDim,
              populationDim,
              crossoverProb,
              randomSelectionChance,
              maxGenerations,
              numPrelimRuns,
              maxPrelimGenerations,
              mutationProb,
              crossoverType,
              computeStatistics);

        if (possGeneValues.length() < 2)
            throw new GAException("There must be at least 2 possible gene values");

        this.chromDecPts = chromDecPts;
        this.possGeneValues = possGeneValues;

        //create the chromosomes for this population
        for (int i = 0; i < populationDim; i++)
        {
            this.chromosomes[i] = new ChromChars(chromosomeDim);
            this.chromNextGen[i] = new ChromChars(chromosomeDim);
            this.prelimChrom[i] = new ChromChars(chromosomeDim);
        }

        initPopulation();
    }

    /**
     * Convert a chromosome string to it's double equivalent
     * for example, if sChromosome = "01234" and iNumDecPts = 4, then return 0.1234
     * @param sChromosome
     * @param iNumDecPts
     * @return double
     */
    protected double chromStrToFloat(String sChromosome, int iNumDecPts)
    {
        String sFloat;
        int iLen;

        if (iNumDecPts == 0)
            return (binaryStrToInt(sChromosome));
        else
        {
            iLen = sChromosome.length() - iNumDecPts;
            sFloat = sChromosome.substring(0, iLen) + "." +
                     sChromosome.substring(iLen, iLen + iNumDecPts);
            return (Double.parseDouble(sFloat));
        }
    }

    /**
     * convert a chromosome string to a double
     * @param sChromosome
     * @return double
     */
    protected double getChromValAsDouble(String sChromosome)
    {
        return (chromStrToFloat(sChromosome, this.chromDecPts));
    }

    /**
     * Randomly pick and return a possible gene value
     * @return char
     */
    protected char getRandomGeneFromPossGenes()
    {
        int iRandomIndex = getRandom(this.possGeneValues.length());
        return (this.possGeneValues.charAt(iRandomIndex));
    }

    /**
     * randomly swap two genes in the chromosome identified by the given index (iChromIndex)
     * @param iChromIndex
     */
    protected void doRandomMutation(int iChromIndex)
    {
        int iGene1, iGene2;
        char cTemp;

        iGene1 = getRandom(chromosomeDim);
        iGene2 = getRandom(chromosomeDim);

        cTemp = ((ChromChars) this.chromosomes[iChromIndex]).genes[iGene1];
        ((ChromChars) this.chromosomes[iChromIndex]).genes[iGene1] =
                                        ((ChromChars) this.chromosomes[iChromIndex]).genes[iGene2];
        ((ChromChars) this.chromosomes[iChromIndex]).genes[iGene2] = cTemp;
    }

    /**
     * create random chromosomes from the given gene space.
     */
    protected void initPopulation()
    {
        for (int i = 0; i < populationDim; i++)
        {
            for (int iGene = 0; iGene < chromosomeDim; iGene++)
                 ((ChromChars) this.chromosomes[i]).genes[iGene] = getRandomGeneFromPossGenes();
            this.chromosomes[i].fitness = getFitness(i);
        }
    }

    /**
     * Genetically recombine the given chromosomes using a one point crossover technique.
     * <pre>
     * For example, if we have:
     *   chromosome A = "ABCD"
     *   chromosome B = "EFGH"
     * and we randomly choose the crossover point of 1, the new genes will be:
     *   new chromosome A = "A" + "FGH" = "AFGH"
     *   new chromosome B = "E" + "BCD" = "EBCD"
     * </pre>
     * @param Chrom1
     * @param Chrom2
     */
    protected void doOnePtCrossover(Chromosome Chrom1, Chromosome Chrom2)
    {
        String sNewChrom1, sNewChrom2;
        int iCrossoverPoint;
        String sChrom1, sChrom2;

        iCrossoverPoint = getRandom(chromosomeDim - 2);
        sChrom1 = Chrom1.getGenesAsStr();
        sChrom2 = Chrom2.getGenesAsStr();

        // CREATE OFFSPRING ONE
        sNewChrom1 =
            sChrom1.substring(0, iCrossoverPoint)
                + sChrom2.substring(iCrossoverPoint, chromosomeDim);

        // CREATE OFFSPRING TWO
        sNewChrom2 =
            sChrom2.substring(0, iCrossoverPoint)
                + sChrom1.substring(iCrossoverPoint, chromosomeDim);

        ((ChromChars) Chrom1).setGenesFromStr(sNewChrom1);
        ((ChromChars) Chrom2).setGenesFromStr(sNewChrom2);
    }

    /**
     * Genetically recombine the given chromosomes using a two point crossover technique which
     * combines two chromosomes at two random genes (loci), creating two new chromosomes.
     * <pre>
     * For example, if we have:
     *   chromosome A = "ABCDE"
     *   chromosome B = "FGHIJ"
     * and we randomly choose the crossover points of 1 and 3, the new genes will be:
     *   new chromosome A = "A" + "GH" + "DE" = "AGHDE"
     *   new chromosome B = "F" + "BC" + "IJ" = "FBCIJ"
     * </pre>
     * @param Chrom1
     * @param Chrom2
     */
    protected void doTwoPtCrossover(Chromosome Chrom1, Chromosome Chrom2)
    {
        String sNewChrom1, sNewChrom2;
        int iCrossoverPoint1, iCrossoverPoint2;
        String sChrom1, sChrom2;

        iCrossoverPoint1 = 1 + getRandom(chromosomeDim - 2);
        iCrossoverPoint2 = iCrossoverPoint1 + 1 + getRandom(chromosomeDim - iCrossoverPoint1 - 1);

        if (iCrossoverPoint2 == (iCrossoverPoint1 + 1))
            doOnePtCrossover(Chrom1, Chrom2);
        else
        {
            sChrom1 = Chrom1.getGenesAsStr();
            sChrom2 = Chrom2.getGenesAsStr();

            // CREATE OFFSPRING ONE
            sNewChrom1 = sChrom1.substring(0, iCrossoverPoint1) +
                         sChrom2.substring(iCrossoverPoint1, iCrossoverPoint2) +
                         sChrom1.substring(iCrossoverPoint2, chromosomeDim);

            // CREATE OFFSPRING TWO
            sNewChrom2 = sChrom2.substring(0, iCrossoverPoint1) +
                         sChrom1.substring(iCrossoverPoint1, iCrossoverPoint2) +
                         sChrom2.substring(iCrossoverPoint2, chromosomeDim);

            ((ChromChars) Chrom1).setGenesFromStr(sNewChrom1);
            ((ChromChars) Chrom2).setGenesFromStr(sNewChrom2);
        }
    }

    /**
     * Genetically recombine the given chromosomes using a uniform crossover technique. This
     * technique randomly swaps genes from one chromosome to another.
     * <pre>
     * For example, if we have:
     *   chromosome A = "ABCD"
     *   chromosome B = "EFGH"
     * our uniform (random) crossover might result in something like:
     *   new chromosome A = "EBCD"
     *   new chromosome B = "AFGH"
     * if only the first gene in the chromosome was swapped.
     * </pre>
     * @param Chrom1
     * @param Chrom2
     */
    protected void doUniformCrossover(Chromosome Chrom1, Chromosome Chrom2)
    {
        int iGeneToSwap;
        char cGene;

        StringBuffer sbChrom1 = new StringBuffer(Chrom1.getGenesAsStr());
        StringBuffer sbChrom2 = new StringBuffer(Chrom2.getGenesAsStr());

        for (int i = 0; i < chromosomeDim; i++)
        {
            if (getRandom(100) > 50)
            {
                iGeneToSwap = getRandom(chromosomeDim);
                cGene = sbChrom1.charAt(iGeneToSwap);

                sbChrom1.setCharAt(iGeneToSwap, sbChrom2.charAt(iGeneToSwap));
                sbChrom2.setCharAt(iGeneToSwap, cGene);
            }
        }

        ((ChromChars) Chrom1).setGenesFromStr(sbChrom1.toString());
        ((ChromChars) Chrom2).setGenesFromStr(sbChrom2.toString());
    }
}