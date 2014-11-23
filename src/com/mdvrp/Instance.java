package com.mdvrp;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * Instance class holds all the information about the problem, customers, depots, vehicles.
 * It offers functions to grab all the data from a file print it formated and all the function
 * needed for the initial solution.
 */
public class Instance {
	private int vehiclesNr;
	private int customersNr;
	private int depotsNr;
	private int daysNr = 1;;
	private ArrayList<Customer> customers 	= new ArrayList<>(); 		// vector of customers;
	private ArrayList<Depot> depots 		= new ArrayList<>();       	// vector of depots;
	private double[][] durations;
	private double[][] capacities;
	private double[][] distances;
	private Route[][] routes;
	private Random random 					= new Random();
	private Parameters parameters;
	
	double maxX = Double.NEGATIVE_INFINITY;
	double maxY = Double.NEGATIVE_INFINITY;
	double minX = Double.POSITIVE_INFINITY;
	double minY = Double.POSITIVE_INFINITY;
	
	public Instance(Parameters parameters) 
	{
		this.setParameters(parameters);
		// set the random seet if passed as parameter
		if(parameters.getRandomSeed() != -1)
			random.setSeed(parameters.getRandomSeed());
	}
	
	/**
	 * Returns the time necessary to travel from node 1 to node 2
	 * @param node1
	 * @param node2
	 * @return
	 */
	public double getTravelTime(int v1, int v2) {
		return this.distances[v1][v2];
	}
	
	/**
	 * Read from file the problem data: D and Q, customers data
	 * and depots data. After the variables are populated
	 * calculates the distances, assign customers to depot
	 * and calculates angles
	 * @param filename
	 */
	public void populateFromHombergFile(String filename) {
		try {
						
			Scanner in = new Scanner(new FileReader(parameters.getCurrDir() + "/input/" + filename));
			depotsNr = 1;
			
			// skip unusefull lines
			in.nextLine(); // skip filename
			in.nextLine(); // skip empty line
			in.nextLine(); // skip vehicle line
			in.nextLine();
			vehiclesNr	= in.nextInt();
			
			// read D and Q
			durations	= new double[depotsNr][daysNr];
			capacities	= new double[depotsNr][daysNr];
			durations[0][0] = Double.MAX_VALUE;
			capacities[0][0] = in.nextInt();
			
			// skip unusefull lines
			in.nextLine();
			in.nextLine();
			in.nextLine();
			in.nextLine();
			in.nextLine();
			
			// read depots data
			Depot depot = new Depot();
			depot.setNumber(in.nextInt());
			depot.setXCoordinate(in.nextDouble());
			depot.setYCoordinate(in.nextDouble());
			in.nextDouble();
			depot.setStartTW(in.nextInt());
			depot.setEndTW(in.nextInt());
			in.nextDouble();
			// this data is used for DrawPanel
			maxX = depot.getXCoordinate() > maxX ? depot.getXCoordinate() : maxX;
			maxY = depot.getYCoordinate() > maxY ? depot.getYCoordinate() : maxY;
			minX = depot.getXCoordinate() < minX ? depot.getXCoordinate() : minX;
			minY = depot.getYCoordinate() < minY ? depot.getYCoordinate() : minY;
			depots.add(depot);
			
			// read customers data
			customersNr = 0;
			while(in.hasNextInt())
			{					
				Customer customer = new Customer();
				customer.setNumber(in.nextInt() - 1);
				customer.setXCoordinate(in.nextDouble());
				customer.setYCoordinate(in.nextDouble());
				customer.setCapacity(in.nextDouble());
				customer.setStartTw(in.nextInt());
				customer.setEndTw(in.nextInt());
				customer.setServiceDuration(in.nextDouble());
				
				// this data is used for DrawPanel
				maxX = customer.getXCoordinate() > maxX ? customer.getXCoordinate() : maxX;
				maxY = customer.getYCoordinate() > maxY ? customer.getYCoordinate() : maxY;
				minX = customer.getXCoordinate() < minX ? customer.getXCoordinate() : minX;
				minY = customer.getYCoordinate() < minY ? customer.getYCoordinate() : minY;
							
				// add customer to customers list
				customers.add(customer);
				customersNr++;
			}// end for customers
			in.close();
			
			depot.setNumber(customersNr);
			
			if(parameters.getTabuTenure() == -1)
				parameters.setTabuTenure((int)(Math.sqrt(getCustomersNr())));
			
			calculateDistances();
			assignCustomersToDepots();			
			calculateAngles();
			sortAssignedCustomers();
		} catch (FileNotFoundException e) {
			// File not found
			System.out.println("File not found!");
			System.exit(-1);
		}
	}
	
	/**
	 * Order for each depot the list containing the assigned customers based on angles
	 */
	public void sortAssignedCustomers() {
		for (int i = 0; i < depotsNr; ++i)
			Quick.sort(depots.get(i).getAssignedcustomers(), i);
	}
	
	/**
	 * Get the depot number found at the passed position
	 * @param index
	 * @return
	 */
	public int getNumberOfDepotAt(int index) {
		return depots.get(index).getNumber();
	}
	
	/**
	 * Get the customer number found at the passed position
	 * @param index
	 * @return
	 */
	public int getNumberOfCustomerAt(int index) {
		return customers.get(index).getNumber();
	}
	
	
	/**
	 *  Assign to each customer the closed depot based on distances
	 */
	public void assignCustomersToDepots() {
		
		for (int i = 0; i < customersNr; ++i){
			double min = Double.MAX_VALUE;
			int depotToAssign = 0;
			for (int j = customersNr; j < customersNr + depotsNr; ++j)
				if (min > distances[i][j]) {
					min = distances[i][j];
					depotToAssign = j - customersNr;
				}
			customers.get(i).setAssignedDepot(depots.get(depotToAssign));
			depots.get(depotToAssign).addAssignedCustomer(customers.get(i));
		}
		
	}
	
	public String printRoutes(Route[][] routes) {
		StringBuffer print = new StringBuffer();
		print.append("------------Routes-----------\n");
		for(int i =0; i < routes.length; ++i) {
			
			for (int j = 0; j < routes[i].length; ++j) {
				print.append((routes[i][j].getCustomersLength()) + " " + routes[i][j].getDepotNr());
				for(int k = 0; k < routes[i][j].getCustomersLength(); ++k) {
					print.append(" " + routes[i][j].getCustomerNr(k));
				}// end for customers
				print.append("\n");
			}// end for vehicles
		}// end for depots
		print.append("------------Routes-----------\n");
		return print.toString();
	}// end method printRoutes
	
	/**
	 * Print all the routes for each depots, day and vehicle
	 */
	public String printRoutes() {
		StringBuffer print = new StringBuffer();
		for (int i = 0; i < depotsNr; ++i){
			print.append("\n" + "Depot: " + i + "\n");
			for (int j = 0; j < vehiclesNr; ++j){
				print.append(routes[i][j].printRoute());
			}
			print.append("\n");
		}
		return print.toString();
	}
	
	public void initializeRoutes() {
		routes = new Route[depotsNr][vehiclesNr];
		// Creation of the routes; each route starts at the depot
		for (int i = 0; i < depotsNr; ++i)
			for (int j = 0; j < vehiclesNr; ++j){
					routes[i][j] = new Route();
					routes[i][j].setIndex(i*(vehiclesNr) + j);
					
					// set the starting depot of the route
					routes[i][j].setDepot(this.depots.get(i));
					
					// set the cost of the route
					Cost cost = new Cost();
					routes[i][j].setCost(cost);
					
					// assign vehicle
					Vehicle vehicle = new Vehicle();
					vehicle.setCapacity(capacities[i][0]);
					vehicle.setDuration(durations[i][0]);
					routes[i][j].setAssignedVehicle(vehicle);
				}
	}
	
	/**
	 * Print for the list of depots their number on a row separated by space
	 * Used for debugging
	 */
	public String printDepotsNumber(ArrayList<Depot> depots) {
		StringBuffer print = new StringBuffer();
		print.append("Depots:");
		for (int i = 0; i < depots.size(); ++i) {
			print.append(" " + depots.get(i).getNumber());
		}
		print.append("\n");
		return print.toString();
	}
	
	/**
	 * Print for the list of customers their number on a row separated by space
	 * Used for debugging
	 */
	public String printCustomersNumber(ArrayList<Customer> customers) {
		StringBuffer print = new StringBuffer();
		print.append("Customers:");
		for (int i = 0; i < customers.size(); ++i) {
			print.append(" " + customers.get(i).getNumber());
		}
		print.append("\n");
		return print.toString();
	}
	
	/**
	 * Calculate the symmetric euclidean matrix of costs
	 */
	public void calculateDistances() {
		distances = new double[customersNr + depotsNr][customersNr + depotsNr];
		for (int i = 0; i  < customersNr + depotsNr - 1; ++i)
			for (int j = i + 1; j < customersNr +  depotsNr; ++j) {
				//case both customers
				if(i < customersNr && j < customersNr){
					distances[i][j] = Math.sqrt(Math.pow(customers.get(i).getXCoordinate() - customers.get(j).getXCoordinate(), 2)
										+ Math.pow(customers.get(i).getYCoordinate() - customers.get(j).getYCoordinate(), 2));
					distances[j][i] = distances[i][j];
					
				// case customer and depot					
				}else if(i < customersNr && j >= customersNr){
					int d = j - customersNr; // depot number in the instance list
					distances[i][j] = Math.sqrt(Math.pow(customers.get(i).getXCoordinate() - depots.get(d).getXCoordinate(), 2)
							+ Math.pow(customers.get(i).getYCoordinate() - depots.get(d).getYCoordinate(), 2));
					distances[j][i] = distances[i][j];
				
				// case both depots
				}else if(i >= customersNr && j >= customersNr){
					int d1 = i - customersNr; // first depot number in the instance list
					int d2 = j - customersNr; // second depot number in the instance list
					distances[i][j] = Math.sqrt(Math.pow(depots.get(d1).getXCoordinate() - depots.get(d2).getXCoordinate(), 2)
							+ Math.pow(depots.get(d1).getYCoordinate() - depots.get(d2).getYCoordinate(), 2));
					distances[j][i] = distances[i][j];
				}
			}		
	}
	
	/**
	 * Calculates the angles between customers and depots
	 */
	public void calculateAngles() {
		
		for (int i = 0; i < customersNr; ++i) {
			double[] angles = new double[depotsNr];
			for (int j = 0; j < depotsNr; ++j) {

				angles[j] = Math.atan2(customers.get(i).getYCoordinate() - depots.get(j).getYCoordinate(), customers.get(i).getXCoordinate() - depots.get(j).getXCoordinate());
				customers.get(i).setAnglesToDepots(angles);
			}
		}
	}
	
	/**
	 * @return distances as a string
	 */
	public String printDistances() {
		StringBuffer print = new StringBuffer();
		for	(int i = 0; i < customersNr + depotsNr; ++i) {
			for	(int j = 0; j < customersNr + depotsNr; ++j)
				print.append(distances[i][j] + " ");
			print.append("\n");
		}
		return print.toString();
	}
	
	
	/**
	 * @return all the customers as string
	 */
	public String printCustomers() {
		StringBuffer print = new StringBuffer();
		for (int i = 0; i < customersNr; ++i) {
			print.append(customers.get(i));
		}
		return print.toString();
	}
	
	/**
	 * @return all the depots as string
	 */
	public String printDepots() {
		StringBuffer print = new StringBuffer();
		for (int i = 0; i < depotsNr; ++i) {
			print.append(depots.get(i) + "\n");
		}
		return print.toString();
	}
	
	/**
	 * @param costs the costs to set
	 */
	public void setCosts(double[][] costs) {
		this.distances = costs;
	}


	/**
	 * @return the parameters
	 */
	public Parameters getParameters() {
		return parameters;
	}


	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}


	/**
	 * @return the vehiclesNr
	 */
	public int getVehiclesNr() {
		return vehiclesNr;
	}


	/**
	 * @param vehiclesNr the vehiclesNr to set
	 */
	public void setVehiclesNr(int vehiclesNr) {
		this.vehiclesNr = vehiclesNr;
	}


	/**
	 * @return the customersNr
	 */
	public int getCustomersNr() {
		return customersNr;
	}


	/**
	 * @param customersNr the customersNr to set
	 */
	public void setCustomersNr(int customersNr) {
		this.customersNr = customersNr;
	}


	/**
	 * @return the depotsNr
	 */
	public int getDepotsNr() {
		return depotsNr;
	}


	/**
	 * @param depotsNr the depotsNr to set
	 */
	public void setDepotsNr(int depotsNr) {
		this.depotsNr = depotsNr;
	}


	/**
	 * @return the daysNr
	 */
	public int getDaysNr() {
		return daysNr;
	}


	/**
	 * @param daysNr the daysNr to set
	 */
	public void setDaysNr(int daysNr) {
		this.daysNr = daysNr;
	}


	public Depot getDepot(int i) {
		return depots.get(i);
	}


	public double getCapacity(int i, int j) {
		return capacities[i][j];
	}


	public double getDuration(int i, int j) {
		return durations[i][j];
	}

	/**
	 * @return the random
	 */
	public Random getRandom() {
		return random;
	}

	/**
	 * @param random the random to set
	 */
	public void setRandom(Random random) {
		this.random = random;
	}
	
	/**
	 * @return the precision
	 */
	public double getPrecision(){
		return parameters.getPrecision();
	}
}
