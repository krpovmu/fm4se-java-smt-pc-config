package de.buw.fm4se.java_smt.pcconfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

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
import org.sosy_lab.java_smt.api.SolverContext;

public class PcConfigGeneratorAndSolver {

	public static void main(String[] args) throws Exception {

		// Mandatory components
		String[] mandatoryKindComponents = { "CPU", "motherboard", "RAM", "storage" };
		List<String> kindComponentsMandatory = Arrays.asList(mandatoryKindComponents);

		// Kind of constrains
		String[] kindConstrains = { "requires", "excludes" };
		List<String> kindConstrainsList = Arrays.asList(kindConstrains);

		// Setting up SMT solver related stuff
		Configuration config = Configuration.fromCmdLineArguments(args);
		LogManager logger = BasicLogManager.create(config);
		ShutdownManager shutdown = ShutdownManager.create();

		// We use PRINCESS SMT solver as SMTINTERPOL did not support integer
		// multiplication
		SolverContext context = SolverContextFactory.createSolverContext(config, logger, shutdown.getNotifier(),
				Solvers.PRINCESS);

		FormulaManager fmgr = context.getFormulaManager();
		IntegerFormulaManager imgr = fmgr.getIntegerFormulaManager();
		BooleanFormulaManager bmgr = fmgr.getBooleanFormulaManager();

		Scanner scan = new Scanner(System.in);
		System.out.print("Please enter a budget: ");
		int budget = scan.nextInt();
		scan.close();

		// get all component categories
		Map<String, BooleanFormula> boolComponents = new HashMap<>();
		Set<Object> kindComponents = PcConfigReader.getTypeComponents();
		BooleanFormula orValues = null;
		BooleanFormula constrain02RequiresAndExcludes = null;
		BooleanFormula constrain03OnlyOneElementMandatoryPerPC = null;
		List<BooleanFormula> orList = new ArrayList<>();

		// Create Map of Boolean objects of all components in the system
		for (Object kind : kindComponents) {
			Map<String, Integer> allComponentsByType = PcConfigReader.getComponents(kind.toString());
			for (String key : allComponentsByType.keySet()) {
				BooleanFormula componentObject = bmgr.makeVariable(key);
				boolComponents.put(key, componentObject);
			}
		}

		// #3 Constrain: Every valid PC needs at least component from each of these
		// categories: CPU, motherboard, RAM, and storage
		for (String mandatoryComponent : kindComponentsMandatory) {
			Map<String, Integer> allMandatoryComponents = PcConfigReader.getComponents(mandatoryComponent);
			List<String> componentsByName = new ArrayList<>();

			// Get all components by categories
			for (String component : allMandatoryComponents.keySet()) {
				componentsByName.add(component.toString());
			}

			// Create bool objects dinamically per category
			List<BooleanFormula> objectsOrComponent = new ArrayList<>();
			for (int i = 0; i < componentsByName.size(); i++) {
				BooleanFormula component = boolComponents.get(componentsByName.get(i));
				objectsOrComponent.add(component);
			}
			orValues = bmgr.or(objectsOrComponent);
			orList.add(orValues);
		}
		constrain03OnlyOneElementMandatoryPerPC = bmgr.and(orList);

		// #2 Contrain: Constraints between components of kind requires and excludes
		// (similar to those in feature models) can be read from another file.

		List<BooleanFormula> requiresList = new ArrayList<>();
		List<BooleanFormula> excludeList = new ArrayList<>();

		for (String typeConstrain : kindConstrainsList) {
			List<String[]> constrains = PcConfigReader.getConstraints(typeConstrain);

			if (typeConstrain == "requires") {

				Map<String, BooleanFormula> requiresComponents = new HashMap<>();
				
				for (String[] constrain : constrains) {
					System.out.println(constrain[0] + " " + constrain[1]);
				}
				
			} else if (typeConstrain == "excludes") {

				for (String[] constrain : constrains) {
					System.out.println(constrain[0] + " " + constrain[1]);
				}
			} else {
				System.out.println("Type of constrain prohibited ... Perejil");
			}
		}
	}

// INFO this is just to see how to access the information from the files
//		System.out.println("\nAvailable components:");
//		printComponents("CPU");
//		printComponents("motherboard");
//		printComponents("RAM");
//		printComponents("GPU");
//		printComponents("storage");
//		printComponents("opticalDrive");
//		printComponents("cooler");

//		System.out.println("\nConstraints:");
//		printConstraints("requires");
//		printConstraints("excludes");
//
//		System.out.print("\nSearching for a configuration costing at most " + budget);

	// TODO implement the translation to SMT and the interpretation of the model

//	}

//	private static void printConstraints(String kind) {
//		for (String[] pair : PcConfigReader.getConstraints(kind)) {
//			System.out.println(pair[0] + " " + kind + " " + pair[1]);
//		}
//	}

//	private static Map printComponents(String type) {
//		Map<String, Integer> components = PcConfigReader.getComponents(type);
//		for (String cmp : compoents.keySet()) {
//			System.out.println(cmp + " costs " + compoents.get(cmp));
//		}
//		return components;
//	}

}
