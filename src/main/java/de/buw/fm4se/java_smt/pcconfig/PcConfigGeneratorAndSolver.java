package de.buw.fm4se.java_smt.pcconfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;

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
		BooleanFormulaManager bmgr = fmgr.getBooleanFormulaManager();
		IntegerFormulaManager componentPrice = fmgr.getIntegerFormulaManager();

		Scanner scan = new Scanner(System.in);
		System.out.print("Please enter a budget: ");
		int budget = scan.nextInt();
		scan.close();

		// get all component categories
		Map<String, BooleanFormula> boolComponents = new HashMap<>();
		Map<BooleanFormula, Integer> boolComponentsWithPrices = new HashMap<>();
		Set<Object> kindComponents = PcConfigReader.getTypeComponents();
		BooleanFormula orValues = null;
		BooleanFormula constrain02Requires = null;
		BooleanFormula constrain02Excludes = null;
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

		// Create Map of Boolean objects with prices
		for (Object kind : kindComponents) {
			Map<String, Integer> allComponentsByType = PcConfigReader.getComponents(kind.toString());
			for (String key : allComponentsByType.keySet()) {
				boolComponentsWithPrices.put(boolComponents.get(key), allComponentsByType.get(key));
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
		// Constrain
		constrain03OnlyOneElementMandatoryPerPC = bmgr.and(orList);

		// #2 Contrain: Constraints between components of kind requires and excludes
		// (similar to those in feature models) can be read from another file.
		List<BooleanFormula> requiresList = new ArrayList<>();
		List<BooleanFormula> excludeList = new ArrayList<>();

		for (String typeConstrain : kindConstrainsList) {
			List<String[]> constrains = PcConfigReader.getConstraints(typeConstrain);
			// Requires constrains
			if (typeConstrain == "requires") {
				Map<BooleanFormula, BooleanFormula> componentsByConstrainType = new HashMap<>();
				for (String[] constrain : constrains) {
					BooleanFormula brandComponent = boolComponents.get(constrain[1]);
					BooleanFormula componentObject = boolComponents.get(constrain[0]);
					componentsByConstrainType.put(componentObject, brandComponent);
				}
				Map<Object, List<Object>> componentsByBrand = componentsByConstrainType.entrySet().stream()
						.collect(Collectors.groupingBy(Map.Entry::getValue,
								Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
				for (Object componentsList : componentsByBrand.keySet()) {
					if (componentsByBrand.get(componentsList).size() > 1) {
						BooleanFormula key = null;
						List<BooleanFormula> orElements = new ArrayList<>();
						for (Object orComponent : componentsByBrand.get(componentsList)) {
							key = componentsByConstrainType.get(orComponent);
							orElements.add((BooleanFormula) orComponent);
						}
						requiresList.add(bmgr.implication((bmgr.or(orElements)), key));
					} else {
						requiresList.add(bmgr.implication((BooleanFormula) componentsByBrand.get(componentsList).get(0),
								componentsByConstrainType.get(componentsByBrand.get(componentsList).get(0))));
					}
				}
				// Constrain creation
				constrain02Requires = bmgr.and(requiresList);

				// Excludes Constrains
			} else if (typeConstrain == "excludes") {
				for (String[] constrain : constrains) {
					BooleanFormula componentA = boolComponents.get(constrain[0]);
					BooleanFormula componentB = boolComponents.get(constrain[1]);
					excludeList.add(bmgr.xor(componentA, componentB));
//					excludeList.add(bmgr.implication(componentA, bmgr.not(componentB)));
				}
				// Constrain creation
				constrain02Excludes = bmgr.and(excludeList);

			} else {
				System.out.println("type of constrain unknown.");
			}
		}
		// Prices per component
		List<IntegerFormula> costings = new ArrayList<>();
		for (BooleanFormula objectKey : boolComponentsWithPrices.keySet()) {
			costings.add(bmgr.ifThenElse(objectKey, componentPrice.makeNumber(boolComponentsWithPrices.get(objectKey)),
					componentPrice.makeNumber(0)));
		}

		// Create Budget object as a constrain and compare
		BooleanFormula budget_Total = componentPrice.lessOrEquals(componentPrice.sum(costings),
				componentPrice.makeNumber(budget));

		// now feed all lines to the solver
		try (ProverEnvironment prover = context.newProverEnvironment(ProverOptions.GENERATE_MODELS)) {
			prover.addConstraint(constrain03OnlyOneElementMandatoryPerPC);
			prover.addConstraint(constrain02Requires);
			prover.addConstraint(constrain02Excludes);
			prover.addConstraint(budget_Total);

			boolean isUnsat = prover.isUnsat();
			if (!isUnsat) {
				Model model = prover.getModel();
				// print whole model
				System.out.println(model);
			} else {
				System.out.println("problem is UNSAT :-(");
			}
		}
	}
}
