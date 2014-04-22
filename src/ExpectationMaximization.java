import java.util.ArrayList;


public class ExpectationMaximization {
	
	public int convergenceCondition = 20;
	
	public GraphicalModel model;
	
	// The table of Factor should be initialized before executing this function
	public void initializeParameters(GraphicalModel model) {
		this.model = model;
		
	}
	
	public GraphicalModel runExpectationMaximization() {
		for (int i = 0; i < convergenceCondition; i++) {
			ComputeESS computeESS = new ComputeESS();
			computeESS.initializeDataStructure(model);
			
			ArrayList<ArrayList<ArrayList<Double>>> M = computeESS.runComputeESS();
			
			if(null == M) {
				System.out.println("Error: runComputeESS()");
				System.exit(0);
			}
			
			for (int j = 0; j < M.size(); j++) {
				Factor factor = model.getFactor(j);
				Variable var = factor.getNodeVariable();
				
				ArrayList<ArrayList<Double>> MrespectVariable = M.get(j);
				
				// calculate the M[ui], sum the M[xi, ui]
				double[] M_ui = new double[MrespectVariable.get(0).size()];
				for (int k = 0; k < MrespectVariable.size(); k++) {
					ArrayList<Double> Mvar = MrespectVariable.get(k);
					
					for (int l = 0; l < Mvar.size(); l++) {
						M_ui[k] += Mvar.get(l);
					}
				}
				
				// for each instantiation of xi
				for (int k = 0; k < MrespectVariable.size(); k++) {
					ArrayList<Double> Mvar = MrespectVariable.get(k);
					
					// for each instantiation of ui
					for (int l = 0; l < Mvar.size(); l++) {
						
					}
				}
			}
		}
		
		return model;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
