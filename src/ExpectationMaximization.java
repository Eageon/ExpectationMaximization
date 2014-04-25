import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;

public class ExpectationMaximization {

	public int convergenceCondition = 20;
	public LinkedList<Evidence> evidenceSet;
	public int numEvidence;
	public GraphicalModel model;

	public double logLikelihood = 0.0;

	public ExpectationMaximization(GraphicalModel model) {
		initializeParameters(model);
	}

	// The table of Factor should be initialized before executing this function
	public void initializeParameters(GraphicalModel model) {
		this.model = model;

	}

	public GraphicalModel runExpectationMaximization() {
		
		for (int i = 0; i < convergenceCondition; i++) {
			ComputeESS computeESS = new ComputeESS();
			computeESS.initializeDataStructure(model);
			computeESS.setEvidenceSet(evidenceSet);

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

					if (M_ui[k / var.domainSize()] == 0.0) {
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
			while (null != (line = reader.readLine())) {
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
			e.printStackTrace();
		}

		return logLikelihood;
	}

	public void readEvidenceSet(BufferedReader reader) {
		evidenceSet = new LinkedList<>();

		String line = null;
		try {
			while (null != (line = reader.readLine())) {
				String[] observed = line.split(" ");
				Evidence evidence = new Evidence(model.variables);
				evidence.setData(observed);
				evidenceSet.add(evidence);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readTrainingDataOnFile(String training_data) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(training_data));
			String preamble = reader.readLine();
			String[] tokens = preamble.split(" ");

			if (model.variables.size() != Integer.valueOf(tokens[0])) {
				System.out
						.println("uai and training data don't match on number of variables");
				System.exit(0);
			}

			numEvidence = Integer.valueOf(tokens[1]);
			readEvidenceSet(reader);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void dumpNetworkAsUAI(String output_uai) {
		PrintStream writer = null;
		try {
			writer = new PrintStream(output_uai);
			writer.println("BAYES");
			// number of variables
			writer.println(model.variables.size());
			// domain size of variables
			for (int i = 0; i < model.variables.size(); i++) {
				writer.print(model.getVariable(i).domainSize());
				if (i != model.variables.size() - 1) {
					writer.print(" ");
				}
			}
			writer.println();

			// number of factors
			writer.println(model.factors.size());
			// scope of factors
			for (int i = 0; i < model.factors.size(); i++) {
				Factor factor = model.getFactor(i);
				for (int j = 0; j < factor.variables.size(); j++) {
					writer.print(factor.getVariable(j).index);
					if (i != factor.variables.size() - 1) {
						writer.print(" ");
					}
				}
				writer.println();
			}
			writer.println();

			DecimalFormat roundFormat = new DecimalFormat("#.########");
			// CPTs
			for (int i = 0; i < model.factors.size(); i++) {
				Factor factor = model.getFactor(i);
				int domainSize = factor.getNodeVariable().domainSize();
				writer.println(factor.table.size());
				for (int j = 0; j < factor.table.size(); j++) {
					writer.print(roundFormat.format(factor.getTabelValue(j)));
					if ((j % domainSize) == (domainSize - 1)) {
						writer.println();
					} else {
						writer.print(" ");
					}
				}
				writer.println();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != writer) {
				writer.close();
			}
		}
	}

	public double testLikelihoodOnFile(String test_data) {
		double logLikelihood = -1.0;

		try {
			BufferedReader reader = new BufferedReader(
					new FileReader(test_data));
			String preamble = reader.readLine();
			String[] tokens = preamble.split(" ");

			if (model.variables.size() != Integer.valueOf(tokens[0])) {
				System.out
						.println("uai and test data don't match on number of variables");
				System.exit(0);
			}

			int numData = Integer.valueOf(tokens[1]);
			logLikelihood = testLikelihoodOnData(reader);

			System.out.println("Number of test data = " + numData);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return logLikelihood;
	}

	public static void main(String[] args) {

		if (4 != args.length) {
			System.out
					.println("java -jar FODParam <input-uai-file> <training-data> <test-data> <output-uai-file>");
			System.exit(-1);
		}

		String input_uai = args[0];
		String training_data = args[1];
		String test_data = args[2];
		String output_uai = args[3];

		GraphicalModel model = new GraphicalModel(input_uai, false);
		model.initTabelWithoutSettingValue();
		ExpectationMaximization expectMax = new ExpectationMaximization(model);
		
		model.initTabelWithoutSettingValue();
		FODParam fodParam = new FODParam(model);

		fodParam.readTrainingDataOnFile(training_data);
		fodParam.runFODParam();
		System.out.println(fodParam.logLikelihood);

		expectMax.readTrainingDataOnFile(training_data);
		expectMax.runExpectationMaximization();
		System.out.println(expectMax.logLikelihood);

		double logLikelihood = expectMax.testLikelihoodOnFile(test_data);

		// FileOutputStream output = new FileOutputStream(output_uai);
		System.out.println("____________________________");
		System.out.println("log likelihood difference = " + logLikelihood);
		System.out.println("____________________________");

		expectMax.dumpNetworkAsUAI(output_uai);
	}

}
