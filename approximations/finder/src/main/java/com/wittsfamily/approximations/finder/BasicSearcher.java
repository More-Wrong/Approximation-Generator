package com.wittsfamily.approximations.finder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.wittsfamily.approximations.generator.BinaryFunction;
import com.wittsfamily.approximations.generator.BinaryFunctionExpression;
import com.wittsfamily.approximations.generator.ConstantExpression;
import com.wittsfamily.approximations.generator.Expression;
import com.wittsfamily.approximations.generator.Parser;
import com.wittsfamily.approximations.generator.UnaryFunction;
import com.wittsfamily.approximations.generator.UnaryFunctionExpression;

public class BasicSearcher {
    private final FileRangeFinder searcher;
    private final Parser p = new Parser();

    public BasicSearcher(File f) throws FileNotFoundException {
        searcher = new FileRangeFinder(new FileSearcher(f));
    }

    public BasicSearcher(FileRangeFinder f) throws FileNotFoundException {
        searcher = f;
    }

    public List<Expression> findCandidateExpressions(double d, int layers, boolean isStart) throws IOException, ParseException {
        double trueTarget = d;
        boolean negate = false;
        boolean invert = false;
        if (trueTarget < 0) {
            trueTarget = -trueTarget;
            negate = true;
        }
        if (trueTarget > 1) {
            trueTarget = 1 / trueTarget;
            invert = true;
        }
        List<Expression> candidates = new ArrayList<>();
        List<Expression> baseCandidates = new ArrayList<>();
        for (byte[] s : searcher.find(trueTarget, isStart ? 6 : 2)) {
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
                double err = d - expression.asDouble();
                double bestErr = err;
                Expression bestFound = null;
                for (Expression ex2 : findCandidateExpressions(err, Math.min(1, layers - 1), false)) {
                    double f = ex2.asDouble();
                    if (Math.abs(err - f) < bestErr) {
                        bestErr = Math.abs(err - f);
                        BinaryFunctionExpression ex = new BinaryFunctionExpression(BinaryFunction.ADD);
                        ex.setA(cloneExpression(expression));
                        ex.setB(ex2);
                        bestFound = ex;
                        break;
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

    private List<Expression> findAlternatives(Expression found, double trueTarget, boolean negate, boolean invert, int layers) throws IOException, ParseException {
        if (invert && found.isUnaryFunction() && ((UnaryFunctionExpression) found).getFunction() == UnaryFunction.INVERSE) {
            found = ((UnaryFunctionExpression) found).getChild();
            invert = false;
            trueTarget = 1 / (trueTarget);
        }
        if (negate && found.isUnaryFunction() && ((UnaryFunctionExpression) found).getFunction() == UnaryFunction.NEGATE) {
            found = ((UnaryFunctionExpression) found).getChild();
            negate = false;
            trueTarget = -trueTarget;
        }
        double foundVal = found.asDouble();
        // Try multiplicative solutions...
        List<Expression> candidates = new ArrayList<>();
        Expression bestMult = null;
        double propError = trueTarget / foundVal;
        double bestErr = propError;
        for (Expression expression : findCandidateExpressions(propError, Math.min(1, layers - 1), false)) {
            if (Math.abs(trueTarget / expression.asDouble() - propError) < bestErr) {
                BinaryFunctionExpression ex = new BinaryFunctionExpression(BinaryFunction.MULTIPLY);
                ex.setA(expression);
                ex.setB(cloneExpression(found));
                bestMult = ex;
                bestErr = Math.abs(trueTarget / expression.asDouble() - propError);
            }
        }
        if (bestMult != null) {
            candidates.add(correctForInvNeg(bestMult, negate, invert));
        }

        candidates.addAll(tryReplacement(found, trueTarget, negate, invert, layers));
        return candidates;
    }

    private List<Expression> tryReplacement(Expression found, double trueTarget, boolean negate, boolean invert, int layers) throws IOException, ParseException {
        List<Expression> candidates = new ArrayList<>();
        if (found.isUnaryFunction()) {
            UnaryFunctionExpression unFound = (UnaryFunctionExpression) found;
            double internalTarget = unFound.undoFunction(trueTarget);
            if (Double.isFinite(internalTarget)) {
                Expression bestRep = null;
                double bestErr = Math.abs(trueTarget - unFound.asDouble()) / Math.abs(trueTarget);
                for (Expression expression : findCandidateExpressions(internalTarget, layers - 1, false)) {
                    UnaryFunctionExpression ex = new UnaryFunctionExpression(unFound.getFunction());
                    ex.setChild(expression);
                    if (Math.abs(trueTarget - ex.asDouble()) / Math.abs(trueTarget) < bestErr) {
                        bestRep = ex;
                        bestErr = Math.abs(trueTarget - ex.asDouble()) / Math.abs(trueTarget);
                    }
                }
                if (bestRep != null) {
                    candidates.add(correctForInvNeg(bestRep, negate, invert));
                }
                if (layers > 0) {
                    bestRep = null;
                    bestErr = Math.abs(trueTarget - unFound.asDouble()) / Math.abs(trueTarget);
                    for (Expression expression : tryReplacement(unFound.getChild(), internalTarget, false, false, layers)) {
                        UnaryFunctionExpression ex = new UnaryFunctionExpression(unFound.getFunction());
                        ex.setChild(expression);
                        if (Math.abs(trueTarget - ex.asDouble()) / Math.abs(trueTarget) < bestErr) {
                            bestRep = ex;
                            bestErr = Math.abs(trueTarget - ex.asDouble()) / Math.abs(trueTarget);
                        }
                    }
                    if (bestRep != null) {
                        candidates.add(correctForInvNeg(bestRep, negate, invert));
                    }
                }
            }
        } else if (found.isBinaryFunction()) {
            BinaryFunctionExpression biFound = (BinaryFunctionExpression) found;
            double internalTargetA = biFound.undoA(trueTarget);
            if (Double.isFinite(internalTargetA)) {
                Expression bestRep = null;
                double bestErr = Math.abs(internalTargetA - biFound.getA().asDouble()) / Math.abs(internalTargetA);
                for (Expression expression : findCandidateExpressions(internalTargetA, layers - 1, false)) {
                    if (Math.abs(internalTargetA - expression.asDouble()) / Math.abs(internalTargetA) < bestErr) {
                        bestRep = expression;
                        bestErr = Math.abs(internalTargetA - expression.asDouble()) / Math.abs(internalTargetA);
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
                    bestErr = Math.abs(internalTargetA - biFound.getA().asDouble()) / Math.abs(internalTargetA);
                    for (Expression expression : tryReplacement(biFound.getA(), internalTargetA, false, false, layers)) {
                        if (Math.abs(internalTargetA - expression.asDouble()) / Math.abs(internalTargetA) < bestErr) {
                            bestRep = expression;
                            bestErr = Math.abs(internalTargetA - expression.asDouble()) / Math.abs(internalTargetA);
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
            double internalTargetB = biFound.undoB(trueTarget);
            if (Double.isFinite(internalTargetB)) {
                Expression bestRep = null;
                double bestErr = Math.abs(internalTargetB - biFound.getB().asDouble()) / Math.abs(internalTargetB);
                for (Expression expression : findCandidateExpressions(internalTargetB, layers - 1, false)) {
                    if (Math.abs(internalTargetB - expression.asDouble()) / Math.abs(internalTargetB) < bestErr) {
                        bestRep = expression;
                        bestErr = Math.abs(internalTargetB - expression.asDouble()) / Math.abs(internalTargetB);
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
                    bestErr = Math.abs(internalTargetB - biFound.getB().asDouble()) / Math.abs(internalTargetB);
                    for (Expression expression : tryReplacement(biFound.getB(), internalTargetB, false, false, layers)) {
                        if (Math.abs(internalTargetB - expression.asDouble()) / Math.abs(internalTargetB) < bestErr) {
                            bestRep = expression;
                            bestErr = Math.abs(internalTargetB - expression.asDouble()) / Math.abs(internalTargetB);
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
