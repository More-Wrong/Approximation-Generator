package com.wittsfamily.approximations.finder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatRuntimeException;

import com.wittsfamily.approximations.generator.ApfloatWithInf;
import com.wittsfamily.approximations.generator.BinaryFunction;
import com.wittsfamily.approximations.generator.BinaryFunctionExpression;
import com.wittsfamily.approximations.generator.ConstantExpression;
import com.wittsfamily.approximations.generator.Expression;
import com.wittsfamily.approximations.generator.Parser;
import com.wittsfamily.approximations.generator.UnaryFunction;
import com.wittsfamily.approximations.generator.UnaryFunctionExpression;

public class ExpressionOptimiser {
	private final FileRangeFinder searcher;
	private final Parser p = new Parser();
	public final BasicSearcher finder;

	public ExpressionOptimiser(File f) throws FileNotFoundException {
		searcher = new FileRangeFinder(new FileSearcher(f));
		finder = new BasicSearcher(searcher);
	}

	public List<Expression> findCandidateExpressions(Apfloat d, int layers, boolean isStart)
			throws IOException, ParseException {
		if (layers <= 1) {
			return finder.findCandidateExpressions(d.doubleValue(), layers, isStart);
		}
		Apfloat trueTarget = d;
		boolean negate = false;
		boolean invert = false;
		if (trueTarget.signum() < 0) {
			trueTarget = trueTarget.negate();
			negate = true;
		}
		if (trueTarget.compareTo(Apfloat.ONE) > 0) {
			trueTarget = new Apfloat(1, 1000).divide(trueTarget);
			invert = true;
		}
		List<Expression> candidates = new ArrayList<>();
		List<Expression> baseCandidates = new ArrayList<>();
		for (byte[] s : searcher.find(trueTarget.doubleValue(), isStart ? 6 : 2)) {
			Expression exp = p.parseExpression(s, 0, 12);
			baseCandidates.add(exp);
			candidates.add(correctForInvNeg(exp, negate, invert));
		}
		if (layers > 0) {
			for (Expression ex : baseCandidates) {
				candidates.addAll(findAlternatives(ex, trueTarget, negate, invert, layers));
			}
			List<Expression> newCandidates = new ArrayList<>();
			for (Expression expression : candidates) {
				Apfloat err = d.subtract(expression.asApfloat());
				Apfloat bestErr = err;
				Expression bestFound = null;
				for (Expression ex2 : findCandidateExpressions(err, Math.min(1, layers - 1), false)) {
					try {
						Apfloat f = ex2.asApfloat();
						if (ApfloatWithInf.abs(err.subtract(f)).compareTo(bestErr) < 0) {
							bestErr = ApfloatWithInf.abs(ApfloatWithInf.subtract(err, f));
							BinaryFunctionExpression ex = new BinaryFunctionExpression(BinaryFunction.ADD);
							ex.setA(cloneExpression(expression));
							ex.setB(ex2);
							bestFound = ex;
						}
					} catch (ApfloatRuntimeException e) {
						// ignore...
					}
				}
				if (bestFound != null) {
					newCandidates.add(bestFound);
				}
			}
			candidates.addAll(newCandidates);
		}

		return candidates;
	}

	private List<Expression> findAlternatives(Expression found, Apfloat trueTarget, boolean negate, boolean invert,
			int layers) throws IOException, ParseException {
		if (invert && found.isUnaryFunction()
				&& ((UnaryFunctionExpression) found).getFunction() == UnaryFunction.INVERSE) {
			found = ((UnaryFunctionExpression) found).getChild();
			invert = false;
			trueTarget = ApfloatWithInf.invert(trueTarget);
		}
		if (negate && found.isUnaryFunction()
				&& ((UnaryFunctionExpression) found).getFunction() == UnaryFunction.NEGATE) {
			found = ((UnaryFunctionExpression) found).getChild();
			negate = false;
			trueTarget = trueTarget.negate();
		}
		Apfloat foundVal = found.asApfloat();
		Apfloat currentErr = ApfloatWithInf.divide(ApfloatWithInf.abs(ApfloatWithInf.subtract(trueTarget, foundVal)),
				ApfloatWithInf.abs(trueTarget));
		List<Expression> candidates = new ArrayList<>();
		Expression bestMult = null;
		Apfloat bestErr = currentErr;
		Apfloat propError = ApfloatWithInf.divide(ApfloatWithInf.subtract(trueTarget, foundVal),
				ApfloatWithInf.add(trueTarget, ApfloatWithInf.ONE));
		for (Expression expression : findCandidateExpressions(propError, layers - 1, false)) {
			BinaryFunctionExpression ex = new BinaryFunctionExpression(BinaryFunction.MULTIPLY);
			ex.setA(expression);
			ex.setB(cloneExpression(found));
			try {
				Apfloat err = ApfloatWithInf.divide(
						ApfloatWithInf.abs(ApfloatWithInf.subtract(trueTarget, ex.asApfloat())),
						ApfloatWithInf.abs(trueTarget));
				if (err.compareTo(bestErr) < 0) {
					bestMult = ex;
					bestErr = err;
				}
			} catch (ApfloatRuntimeException e) {
				// ignore...
			}
		}
		if (bestMult != null) {
			candidates.add(correctForInvNeg(bestMult, negate, invert));
		}

		candidates.addAll(tryReplacement(found, trueTarget, negate, invert, layers));
		return candidates;
	}

	private List<Expression> tryReplacement(Expression found, Apfloat trueTarget, boolean negate, boolean invert,
			int layers) throws IOException, ParseException {
		List<Expression> candidates = new ArrayList<>();
		if (found.isUnaryFunction()) {
			UnaryFunctionExpression unFound = (UnaryFunctionExpression) found;
			Apfloat internalTarget = unFound.undoApfloat(trueTarget);
			if (internalTarget != ApfloatWithInf.NaN) {
				Expression bestRep = null;
				Apfloat bestErr = ApfloatWithInf.divide(
						ApfloatWithInf.abs(ApfloatWithInf.subtract(internalTarget, unFound.getChild().asApfloat())),
						ApfloatWithInf.abs(internalTarget));
				for (Expression expression : findCandidateExpressions(internalTarget, layers - 1, false)) {
					Apfloat err = ApfloatWithInf.divide(
							ApfloatWithInf.abs(ApfloatWithInf.subtract(internalTarget, expression.asApfloat())),
							ApfloatWithInf.abs(internalTarget));
					if (err.compareTo(bestErr) < 0) {
						bestRep = expression;
						bestErr = err;
					}
				}
				if (bestRep != null) {
					UnaryFunctionExpression ex = new UnaryFunctionExpression(unFound.getFunction());
					ex.setChild(bestRep);
					candidates.add(correctForInvNeg(ex, negate, invert));
				}
				if (layers > 0) {
					bestRep = null;
					bestErr = ApfloatWithInf.divide(
							ApfloatWithInf.abs(ApfloatWithInf.subtract(internalTarget, unFound.getChild().asApfloat())),
							ApfloatWithInf.abs(internalTarget));
					for (Expression expression : tryReplacement(unFound.getChild(), internalTarget, false, false,
							layers)) {
						Apfloat err = ApfloatWithInf.divide(
								ApfloatWithInf.abs(ApfloatWithInf.subtract(internalTarget, expression.asApfloat())),
								ApfloatWithInf.abs(internalTarget));
						if (err.compareTo(bestErr) < 0) {
							bestRep = expression;
							bestErr = err;
						}
					}
					if (bestRep != null) {
						UnaryFunctionExpression ex = new UnaryFunctionExpression(unFound.getFunction());
						ex.setChild(bestRep);
						candidates.add(correctForInvNeg(ex, negate, invert));
					}
				}
			}
		} else if (found.isBinaryFunction()) {
			BinaryFunctionExpression biFound = (BinaryFunctionExpression) found;
			Apfloat internalTargetA = biFound.undoApfloatA(trueTarget);
			if (internalTargetA != ApfloatWithInf.NaN) {
				Expression bestRep = null;
				Apfloat bestErr = ApfloatWithInf.divide(
						ApfloatWithInf.abs(ApfloatWithInf.subtract(internalTargetA, biFound.getA().asApfloat())),
						ApfloatWithInf.abs(internalTargetA));
				for (Expression expression : findCandidateExpressions(internalTargetA, layers - 1, false)) {
					try {
						Apfloat err = ApfloatWithInf.divide(
								ApfloatWithInf.abs(ApfloatWithInf.subtract(internalTargetA, expression.asApfloat())),
								ApfloatWithInf.abs(internalTargetA));
						if (err.compareTo(bestErr) < 0) {
							bestRep = expression;
							bestErr = err;
						}
					} catch (ApfloatRuntimeException e) {
						// ignore...
					}
				}
				if (bestRep != null) {
					BinaryFunctionExpression ex = new BinaryFunctionExpression(biFound.getFunction());
					ex.setA(bestRep);
					ex.setB(cloneExpression(biFound.getB()));
					candidates.add(correctForInvNeg(ex, negate, invert));
				}
				if (layers > 0) {
					bestRep = null;
					bestErr = ApfloatWithInf.divide(
							ApfloatWithInf.abs(ApfloatWithInf.subtract(internalTargetA, biFound.getA().asApfloat())),
							ApfloatWithInf.abs(internalTargetA));
					for (Expression expression : tryReplacement(biFound.getA(), internalTargetA, false, false,
							layers)) {
						try {
							Apfloat err = ApfloatWithInf.divide(
									ApfloatWithInf
											.abs(ApfloatWithInf.subtract(internalTargetA, expression.asApfloat())),
									ApfloatWithInf.abs(internalTargetA));
							if (err.compareTo(bestErr) < 0) {
								bestRep = expression;
								bestErr = err;
							}
						} catch (ApfloatRuntimeException e) {
							// ignore...
						}
					}
					if (bestRep != null) {
						BinaryFunctionExpression ex = new BinaryFunctionExpression(biFound.getFunction());
						ex.setA(bestRep);
						ex.setB(cloneExpression(biFound.getB()));
						candidates.add(correctForInvNeg(ex, negate, invert));
					}
				}
			}
			Apfloat internalTargetB = biFound.undoApfloatA(trueTarget);
			if (internalTargetB != ApfloatWithInf.NaN) {
				Expression bestRep = null;
				Apfloat bestErr = ApfloatWithInf.divide(
						ApfloatWithInf.abs(ApfloatWithInf.subtract(internalTargetB, biFound.getB().asApfloat())),
						ApfloatWithInf.abs(internalTargetB));
				for (Expression expression : findCandidateExpressions(internalTargetB, layers - 1, false)) {
					try {
						Apfloat err = ApfloatWithInf.divide(
								ApfloatWithInf.abs(ApfloatWithInf.subtract(internalTargetB, expression.asApfloat())),
								ApfloatWithInf.abs(internalTargetB));
						if (err.compareTo(bestErr) < 0) {
							bestRep = expression;
							bestErr = err;
						}
					} catch (ApfloatRuntimeException e) {
						// ignore...
					}
				}
				if (bestRep != null) {
					BinaryFunctionExpression ex = new BinaryFunctionExpression(biFound.getFunction());
					ex.setB(bestRep);
					ex.setA(cloneExpression(biFound.getA()));
					candidates.add(correctForInvNeg(ex, negate, invert));
				}
				if (layers > 0) {
					bestRep = null;
					bestErr = ApfloatWithInf.divide(
							ApfloatWithInf.abs(ApfloatWithInf.subtract(internalTargetB, biFound.getB().asApfloat())),
							ApfloatWithInf.abs(internalTargetB));
					for (Expression expression : tryReplacement(biFound.getB(), internalTargetB, false, false,
							layers)) {
						try {
							Apfloat err = ApfloatWithInf.divide(
									ApfloatWithInf
											.abs(ApfloatWithInf.subtract(internalTargetB, expression.asApfloat())),
									ApfloatWithInf.abs(internalTargetB));
							if (err.compareTo(bestErr) < 0) {
								bestRep = expression;
								bestErr = err;
							}
						} catch (ApfloatRuntimeException e) {
							// ignore...
						}
					}
					if (bestRep != null) {
						BinaryFunctionExpression ex = new BinaryFunctionExpression(biFound.getFunction());
						ex.setB(bestRep);
						ex.setA(cloneExpression(biFound.getA()));
						candidates.add(correctForInvNeg(ex, negate, invert));
					}
				}
			}
		}
		return candidates;
	}

	private Expression cloneExpression(Expression target) {
		if (target.isBinaryFunction()) {
			BinaryFunctionExpression bfTarget = (BinaryFunctionExpression) target;
			BinaryFunctionExpression a = new BinaryFunctionExpression(bfTarget.getFunction());
			a.setA(cloneExpression(bfTarget.getA()));
			a.setB(cloneExpression(bfTarget.getB()));
			return a;
		} else if (target.isUnaryFunction()) {
			UnaryFunctionExpression ufTarget = (UnaryFunctionExpression) target;
			UnaryFunctionExpression a = new UnaryFunctionExpression(ufTarget.getFunction());
			a.setChild(cloneExpression(ufTarget.getChild()));
			return a;
		} else {
			ConstantExpression cTarget = (ConstantExpression) target;
			ConstantExpression a = new ConstantExpression(cTarget.getConst());
			return a;
		}
	}

	private Expression correctForInvNeg(Expression target, boolean negate, boolean invert) {
		if (invert) {
			if (target.isUnaryFunction() && ((UnaryFunctionExpression) target).getFunction() == UnaryFunction.INVERSE) {
				target = ((UnaryFunctionExpression) target).getChild();
			} else {
				Expression tmp = target;
				target = new UnaryFunctionExpression(UnaryFunction.INVERSE);
				((UnaryFunctionExpression) target).setChild(tmp);
			}
		}
		if (negate) {
			if (target.isUnaryFunction() && ((UnaryFunctionExpression) target).getFunction() == UnaryFunction.NEGATE) {
				target = ((UnaryFunctionExpression) target).getChild();
			} else {
				Expression tmp = target;
				target = new UnaryFunctionExpression(UnaryFunction.NEGATE);
				((UnaryFunctionExpression) target).setChild(tmp);
			}
		}
		return target;
	}
}
