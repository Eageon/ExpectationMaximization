import java.util.ArrayList;
import java.util.LinkedList;

public class ComputeESS {

	ArrayList<ArrayList<Double>> M;
	ArrayList<Variable> variables;
	ArrayList<Factor> factors;
	LinkedList<Evidence> evidenceSet;
	
	GraphicalModel model = null;
	
	public void initializeDataStructure(GraphicalModel model) {
		this.model = model;
		variables = model.variables;
		factors = model.factors;
		int varNum = model.variables.size();
		M = new ArrayList<>(varNum);

		// for each CPT is equivalent to for each variable
		for (int i = 0; i < model.factors.size(); i++) {
			Factor factor = model.factors.get(i);
			Variable var = factor.getVariable(factor.numScopes() - 1);

			ArrayList<Double> MrepectVariableFactorStyle = new ArrayList<>(factor.table.size());
			
			for (int j = 0; j < factor.table.size(); j++) {			
				MrepectVariableFactorStyle.add(0.0);
			}
			
			M.add(MrepectVariableFactorStyle);
		}
	}
	
	public void setEvidence(LinkedList<Evidence> evidenceSet) {
		this.evidenceSet = evidenceSet;
	}
	
	public void collectProbabilities() {
		// for each evidence
		for (Evidence om : evidenceSet) {
			
			// clear previous evidence and set present evidence
			model.setSoftEvidence(om.varRef, om.observedData, true);
			// for each variable
			// for each factor is equivalent to for each variable
			for (int i = 0; i < factors.size(); i++) {
				ArrayList<Double> MrepectVariableFactorStyle = M.get(i);
				
				Factor factor = factors.get(i);
				Variable var = factor.getNodeVariable();
				// for each pair of instantiation of xi and ui
				for (int j = 0; j < factor.table.size(); j++) {
					// TODO waiting for optimization
					int[] values = factor.tableIndexToVaraibleValue(j);
					
					// if the x and u collide with o evidence, 
					// I must not setSoftEvidence and return 0 immediately
					if(om.isCollision(factor.variables, values)) {
						// make the function run 2^m completion, where m is the number of missing varaibles
						continue;
					}
					
					// soft order and cluster should be computed before
					// the evidence already set
					// if not collide, the running 
					model.setSoftEvidence(factor.variables, values, false);
					// must computer soft order before this execution
					double result = model.runSoftProcess();
					double prev = MrepectVariableFactorStyle.get(j);
					
					MrepectVariableFactorStyle.set(j, prev + result);
				}
			}
		}
	}
	
	public ArrayList<ArrayList<Double>> runComputeESS() {
		if(null == model) {
			return null;
		}
		
		this.collectProbabilities();
		
		return M;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
