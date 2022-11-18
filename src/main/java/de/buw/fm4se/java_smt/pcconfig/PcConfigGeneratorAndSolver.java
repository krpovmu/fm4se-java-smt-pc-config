package de.buw.fm4se.java_smt.pcconfig;

import java.util.Map;
import java.util.Scanner;

import org.sosy_lab.common.ShutdownManager;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.Model;
import org.sosy_lab.java_smt.api.Model.ValueAssignment;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;

public class PcConfigGeneratorAndSolver {

	public static void main(String[] args) throws Exception {
		
		Scanner scan = new Scanner(System.in);
		System.out.print("Please enter a budget: ");
		int budget = scan.nextInt();
		scan.close();
		
		
		// INFO this is just to see how to access the information from the files
		System.out.println("\nAvailable components:");
		printComponents("CPU");
		printComponents("motherboard");
		printComponents("RAM");
		printComponents("GPU");
		printComponents("storage");
		printComponents("opticalDrive");
		printComponents("cooler");
		
		System.out.println("\nConstraints:");
		printConstraints("requires");
		printConstraints("excludes");
		
		System.out.print("\nSearching for a configuration costing at most " + budget);
		
		// TODO implement the translation to SMT and the interpretation of the model
		
	}

	private static void printConstraints(String kind) {
		for (String[] pair : PcConfigReader.getConstraints(kind)) {
			System.out.println(pair[0] + " " + kind + " " + pair[1]);
		}		
	}

	private static void printComponents(String type) {
		Map<String, Integer> compoents = PcConfigReader.getComponents(type);
		for (String cmp : compoents.keySet()) {
			System.out.println(cmp + " costs " + compoents.get(cmp));
		}
	}

}
