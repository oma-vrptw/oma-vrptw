package com.mdvrp;

import com.TabuSearch.MovesType;

public class Parameters {
	private MovesType movesType;
	private String inputFileName;
	private String outputFileName;
	private double precision;
	private int iterations;
	private int startClient;
	private int randomSeed;
	private int tabuTenure;
	private boolean variableTenure;
	private String currDir;
	private boolean graphics;
	
	public Parameters() {
		currDir 			= System.getProperty("user.dir");
		outputFileName    	= currDir + "/output/solutions.csv";
		movesType         	= MovesType.SWAP;
		precision         	= 1E-2;
		iterations        	= 1000;
		startClient       	= -1;
		tabuTenure        	= -1;
		randomSeed		  	= -1;
		variableTenure    	= false;
		graphics          	= false;
	}
	
	public void updateParameters(String[] args) throws Exception
	{
		if(args.length % 2 == 0){
			for(int i = 0; i < args.length; i += 2){
				switch (args[i]) {
					case "-mT":
						movesType = MovesType.SWAP;
						break;
					case "-if":
						inputFileName = args[i+1];
						break;
					case "-of":
						outputFileName = args[i+1];
						break;
					case "-p":
						precision = Double.parseDouble(args[i+1]);
						break;
					case "-it":
						iterations = Integer.parseInt(args[i+1]);
						break;
					case "-sc":
						startClient = Integer.parseInt(args[i+1]);
						break;
					case "-rs":
						randomSeed = Integer.parseInt(args[i+1]);
						break;
					case "-t":
						tabuTenure = Integer.parseInt(args[i+1]);
						break;
					case "-vt":
						if(args[i+1].equalsIgnoreCase("true")){
							setVariableTenure(true);
						}else if(args[i+1].equalsIgnoreCase("false")){
							setVariableTenure(false);
						}else {
							System.out.println("Variable tenure argument must be true of false. Set to default false!");
							throw new Exception();
						}
						break;
					case "-g":
						if(args[i+1].equalsIgnoreCase("on")){
							setGraphics(true);
						}else if(args[i+1].equalsIgnoreCase("off")){
							setGraphics(false);
						}else {
							System.out.println("Graphics argument must be on of off. Set to default off!");
						}
						break;
					default: {
						System.out.println("Unknown type of argument: " + args[i]);
						throw new Exception();
					}
				}
			}
		}else {
			System.out.println("Parameters are not in correct format");
			throw new Exception();
		}
	}
	
	public String toString(){
		StringBuffer print = new StringBuffer();
		print.append("\n" + "--- Parameters: -------------------------------------");
		print.append("\n" + "| Moves Type= " + movesType);
		print.append("\n" + "| Input File Name= " + inputFileName);
		print.append("\n" + "| Output File Name= " + outputFileName);
		print.append("\n" + "| Precision: " + precision);
		print.append("\n" + "| Iterations: " + iterations);
		print.append("\n" + "| Start Client: " + startClient);
		print.append("\n" + "| Random Seed: " + randomSeed);
		print.append("\n" + "| Tabu Tenure: " + tabuTenure);
		print.append("\n" + "| Variable Tenure: " + variableTenure);
		print.append("\n" + "------------------------------------------------------");
		return print.toString();	
	}

	/**
	 * @return the movesType
	 */
	public MovesType getMovesType() {
		return movesType;
	}

	/**
	 * @param movesType the movesType to set
	 */
	public void setMovesType(MovesType movesType) {
		this.movesType = movesType;
	}

	/**
	 * @return the inputFileName
	 */
	public String getInputFileName() {
		return inputFileName;
	}

	/**
	 * @param inputFileName the inputFileName to set
	 */
	public void setInputFileName(String inputFileName) {
		this.inputFileName = inputFileName;
	}

	/**
	 * @return the outputFileName
	 */
	public String getOutputFileName() {
		return outputFileName;
	}

	/**
	 * @param outputFileName the outputFileName to set
	 */
	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	/**
	 * @return the iterations
	 */
	public int getIterations() {
		return iterations;
	}

	/**
	 * @param iterations the iterations to set
	 */
	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	/**
	 * @return the startClient
	 */
	public int getStartClient() {
		return startClient;
	}

	/**
	 * @return the randomSeed
	 */
	public int getRandomSeed() {
		return randomSeed;
	}

	/**
	 * @param randomSeed the randomSeed to set
	 */
	public void setRandomSeed(int randomSeed) {
		this.randomSeed = randomSeed;
	}

	/**
	 * @param startClient the startClient to set
	 */
	public void setStartClient(int startClient) {
		this.startClient = startClient;
	}

	/**
	 * @return the tabuTenure
	 */
	public int getTabuTenure() {
		return tabuTenure;
	}

	/**
	 * @param tabuTenure the tabuTenure to set
	 */
	public void setTabuTenure(int tabuTenure) {
		this.tabuTenure = tabuTenure;
	}

	/**
	 * @return the variableTenure
	 */
	public boolean isVariableTenure() {
		return variableTenure;
	}

	/**
	 * @param variableTenure the variableTenure to set
	 */
	public void setVariableTenure(boolean variableTenure) {
		this.variableTenure = variableTenure;
	}

	public double getPrecision() {
		return precision;
	}

	public String getCurrDir() {
		return currDir;
	}

	public void setCurrDir(String currDir) {
		this.currDir = currDir;
	}
	
	/**
	 * @return the graphics
	 */
	public boolean isGraphics() {
		return graphics;
	}

	/**
	 * @param graphics the graphics to set
	 */
	public void setGraphics(boolean graphics) {
		this.graphics = graphics;
	}
}
