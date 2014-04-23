import java.io.BufferedReader;
import java.io.IOException;
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

					if(M_ui[k / var.domainSize()] == 0.0) {
						factor.setTableValue(k, 0.0);
						continue;
					}
					
					// for each instantiation of ui
					factor.setTableValue(k, Mvar / M_ui[k / var.domainSize()]);
				}
			}
		}

		return model;
	}
	
	public double testLikelihoodOnData(BufferedReader reader) {
		String line = null;
		double logLikelihood = 0.0;
		
		try {
			while(null != (line = reader.readLine())) {
				String[] values = line.split(" ");
				
				Evidence evidence = new Evidence(model.variables);
				evidence.setData(values);
				evidence.makeEvidenceBeTrue();
				
				double result = 1.0;
				for (Factor factor : model.factors) {
					result *= factor.underlyProbability();
				}
				
				logLikelihood += Math.log(result) / Math.log(2);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return logLikelihood;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
