package com.TabuSearch;

import org.coinor.opents.*;

@SuppressWarnings("serial")
public class MyTabuList extends ComplexTabuList implements TabuSearchListener{
	
	public MyTabuList ( int tenure, int[] attrDim ) {
		super(tenure, attrDim);
	}
	
	@Override
	public void improvingMoveMade(TabuSearchEvent arg0) {}

	@Override
	public void newBestSolutionFound(TabuSearchEvent arg0) {}

	@Override
	public void newCurrentSolutionFound(TabuSearchEvent arg0) {}

	@Override
	public void noChangeInValueMoveMade(TabuSearchEvent arg0) {}

	@Override
	public void tabuSearchStarted(TabuSearchEvent arg0) {}

	@Override
	public void tabuSearchStopped(TabuSearchEvent arg0) {}

	@Override
	public void unimprovingMoveMade(TabuSearchEvent arg0) {}

}
