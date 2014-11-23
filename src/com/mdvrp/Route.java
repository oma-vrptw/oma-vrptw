package com.mdvrp;

import java.util.ArrayList;
import java.util.List;

public class Route {
	private int index;				  // Number of the route
	private Cost cost;                // cost of the route
	private Vehicle assignedVehicle;  // vehicle assigned to the route
	private Depot depot;              // depot the route starts from
	private List<Customer> customers; //list of customers served in the route
	
	/**
	 * Constructor of the route
	 */
	public Route() {
		cost = new Cost();
		customers = new ArrayList<>();
		
	}
	
	public Route(Route route) {
		
		this.index = new Integer(route.index);
		this.cost = new Cost(route.cost);
		this.assignedVehicle = route.assignedVehicle;
		this.depot = route.depot;
		this.customers = new ArrayList<>();
		for (int i = 0; i < route.customers.size(); ++i) {
			this.customers.add(new Customer(route.getCustomer(i)));
		}
	}
	
	public double getDuration() {
		return cost.serviceTime + cost.waitingTime;
	}
	
	public Customer getCustomer(int index) {
		return this.customers.get(index);
	}
	public void setDepot(Depot depot) {
		this.depot = depot;
	}
	
	public void removeCustomer(int index){
		this.customers.remove(index);
	}
	
	public int getDepotNr(){
		return this.depot.getNumber();
	}
	
	public Depot getDepot(){
		return this.depot;
	}
	
	public int getLastCustomerNr(){
		return getCustomerNr(customers.size() - 1);
	}
	
	public int getFirstCustomerNr(){
		return getCustomerNr(0);
	}
	
	public boolean isEmpty(){
		if(getCustomersLength() > 0)
			return false;
		else return true;
	}
	
	/**
	 * Get the customer index found at certain position in the customer list
	 * @param index
	 * @return
	 */
	public int getCustomerNr(int index){
		return this.customers.get(index).getNumber();
	}
	/**
	 * Prints the route
	 */
	public String printRoute() {
		StringBuffer print = new StringBuffer();
		print.append("Route[" + index + ", " + (getCustomersLength() + 1) + "]=");
		print.append(" " + this.depot.getNumber());
		for (int i = 0; i < this.customers.size(); ++i) {
			print.append(" " + this.customers.get(i).getNumber());
		}
		print.append("\n");
		return print.toString();
	}
	
	
	public String printRouteCost() {
		StringBuffer print = new StringBuffer();
		print.append("\n" + "Route[" + index + "]");
		print.append("\n" + "--------------------------------------------");
		print.append("\n" + "| Capacity=" + cost.load + " ServiceTime=" + cost.serviceTime + " TravelTime=" + cost.travelTime + " WaitingTime=" + cost.waitingTime +" Totaltime=" + cost.total);
		print.append("\n" + cost);
		print.append("\n");
		return print.toString();
	}
	
	
	/**
	 * @param customers list to set
	 */
	public void setCustomers(ArrayList<Customer> customers) {
		this.customers = customers;
	}
	
	/**
	 * Add a new customer to the route
	 * @param customer
	 */
	public void addCustomer(Customer customer) {
		this.customers.add(customer);
	}
	
	/**
	 * Add a new customer to the route on specific position
	 * @param node
	 */
	public void addCustomer(Customer customer, int index) {
		this.customers.add(index, customer);
	}
	
	/**
	 * Set the index to the route
	 * @param index
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @param capacity the capacity to set
	 */
	public void setCapacity(double capacity) {
		this.cost.load = capacity;
	}

	/**
	 * @param cost the cost to set
	 */
	public void setCost(Cost cost) {
		this.cost = cost;
	}

	/**
	 * @param assignedvehicle the assignedvehicle to set
	 */
	public void setAssignedVehicle(Vehicle assignedvehicle) {
		this.assignedVehicle = assignedvehicle;
	}
	
	public Vehicle getAssignedVehicle() {
		return this.assignedVehicle;
	}
	
	public double getDurationAdmited() {
		return assignedVehicle.getDuration();
	}
	
	public double getLoadAdmited() {
		return assignedVehicle.getCapacity();
	}

	/**
	 * @return customers list
	 */
	public List<Customer> getCustomers() {
		return this.customers;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return the number of nodes in the route
	 */
	public int getCustomersLength() {
		return this.customers.size();
	}

	/**
	 * @return the cost
	 */
	public Cost getCost() {
		return this.cost;
	}


	public void initializeTimes() {
		cost.initialize();
		
		
	}

	/**
	 * @return the depotTwViol
	 */
	public double getDepotTwViol() {
		return cost.depotTwViol;
	}

	/**
	 * @param depotTwViol the depotTwViol to set
	 */
	public void setDepotTwViol(double depotTwViol) {
		this.cost.depotTwViol = depotTwViol;
	}

	/**
	 * @return the returnToDepotTime
	 */
	public double getReturnToDepotTime() {
		return cost.returnToDepotTime;
	}

	/**
	 * @param returnToDepotTime the returnToDepotTime to set
	 */
	public void setReturnToDepotTime(double returnToDepotTime) {
		this.cost.returnToDepotTime = returnToDepotTime;
	}

	

	  
}
