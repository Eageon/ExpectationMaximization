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

			ArrayList<ArrayList<Double>> M = computeESS.runComputeESS();

			if (null == M) {
				System.out.println("Error: runComputeESS()");
				System.exit(0);
			}

			for (int j = 0; j < M.size(); j++) {
				Factor factor = model.getFactor(j);
				Variable var = factor.getNodeVariable();

				ArrayList<Double> MrespectVariableFactorStyle = M.get(j);

				// calculate the M[ui], sum the M[xi, ui]
				double[] M_ui = new double[MrespectVariableFactorStyle.size()
						/ var.domainSize()];
				for (int k = 0; k < MrespectVariableFactorStyle.size(); k++) {
					double Mvar = MrespectVariableFactorStyle.get(k);
					M_ui[k / var.domainSize()] += Mvar;
				}

				// for each instantiation of xi
				for (int k = 0; k < MrespectVariableFactorStyle.size(); k++) {
					double Mvar = MrespectVariableFactorStyle.get(k);

					// for each instantiation of ui
					factor.setTableValue(k, Mvar / M_ui[k / var.domainSize()]);
				}
			}
		}

		return model;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
