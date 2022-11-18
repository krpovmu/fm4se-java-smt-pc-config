package de.buw.fm4se.java_smt;

import java.math.BigInteger;

import org.sosy_lab.common.ShutdownManager;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.Model;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;

/**
 * We encode a simple Emoji math puzzle (slide 52 of Lecture 4) 
 * for the SMT solver and let it solve it
 *
 */
public class EmojiSolver {

	public static void main(String[] args) throws Exception {
		// setting up SMT solver related stuff
		Configuration config = Configuration.fromCmdLineArguments(args);
		LogManager logger = BasicLogManager.create(config);
		ShutdownManager shutdown = ShutdownManager.create();

		// we use PRINCESS SMT solver as SMTINTERPOL did not support integer multiplication
		SolverContext context = SolverContextFactory.createSolverContext(config, logger, shutdown.getNotifier(),
				Solvers.PRINCESS);

		FormulaManager fmgr = context.getFormulaManager();

		IntegerFormulaManager imgr = fmgr.getIntegerFormulaManager();

		// declaring integer variables similar to "(declare-const ... Int)"
		IntegerFormula doubleRainbow = imgr.makeVariable("doubleRainbow");
		IntegerFormula rainbow = imgr.makeVariable("rainbow");
		IntegerFormula rain = imgr.makeVariable("rain");
		IntegerFormula thunder = imgr.makeVariable("thunder");
		IntegerFormula result = imgr.makeVariable("result");
		
		// construction constraints
		BooleanFormula line1 = imgr.equal(imgr.add(imgr.subtract(doubleRainbow, rainbow), rainbow), imgr.makeNumber(12));
		BooleanFormula line2 = imgr.equal(imgr.subtract(imgr.subtract(doubleRainbow, rain), rain), imgr.makeNumber(4));
		BooleanFormula line3 = imgr.equal(imgr.subtract(imgr.multiply(rain, rainbow), thunder), imgr.makeNumber(22));
		BooleanFormula line4 = imgr.equal(imgr.subtract(imgr.divide(doubleRainbow, thunder), rain), result);
		
		// now feed all lines to the solver
		try (ProverEnvironment prover = context.newProverEnvironment(ProverOptions.GENERATE_MODELS)) {
			prover.addConstraint(line1);
			prover.addConstraint(line2);
			prover.addConstraint(line3);
			prover.addConstraint(line4);
			// (check-sat)
			boolean isUnsat = prover.isUnsat();
			if (!isUnsat) {
				// (get-model)
				Model model = prover.getModel();
				// print whole model
				System.out.println(model);
				// get a specific model
				BigInteger value = model.evaluate(result);
				System.out.println("Result is: " + value);
			}
		}
	}

}
