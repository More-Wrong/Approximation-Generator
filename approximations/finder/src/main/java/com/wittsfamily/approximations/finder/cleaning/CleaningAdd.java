package com.wittsfamily.approximations.finder.cleaning;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.wittsfamily.approximations.finder.ExpressionRenderer;

public class CleaningAdd implements CleaningExpression {
    private List<CleaningExpression> expressions = new ArrayList<>();
    private CleaningExpression parent;

    public void addExpression(CleaningExpression child) {
        expressions.add(child);
        child.setParent(this);
    }

    @Override
    public List<CleaningExpression> getExpressions() {
        return expressions;
    }

    @Override
    public CleaningExpressionType getType() {
        return CleaningExpressionType.ADD;
    }

    @Override
    public CleaningExpression getParent() {
        return parent;
    }

    @Override
    public void setParent(CleaningExpression parent) {
        this.parent = parent;
    }

    @Override
    public boolean cleanOne(boolean convertPowers) {
        boolean hasDoneAnything = false;
        for (Iterator<CleaningExpression> iterator = expressions.iterator(); iterator.hasNext();) {
            CleaningExpression child = iterator.next();
            if (child.getType() == CleaningExpressionType.INTEGER && ((CleaningInteger) child).asInt() == 0) {
                iterator.remove();
                hasDoneAnything = true;
            }
        }
        if (expressions.size() == 1) {
            parent.replaceChild(this, expressions.get(0));
            return true;
        } else if (expressions.size() == 0) {
            parent.replaceChild(this, new CleaningInteger(0));
            return true;
        }
        for (int i = 0; i < expressions.size(); i++) {
            CleaningExpression exp = expressions.get(i);
            hasDoneAnything = exp.cleanOne(convertPowers) || hasDoneAnything;
        }
        return hasDoneAnything;
    }

    @Override
    public boolean cleanTwo() {
        boolean hasDoneAnything = false;
        for (int i = 0; i < expressions.size(); i++) {
            CleaningExpression exp = expressions.get(i);
            hasDoneAnything = exp.cleanTwo() || hasDoneAnything;
        }
        return hasDoneAnything;
    }

    @Override
    public boolean cleanThree() {
        boolean hasDoneAnything = false;
        for (int i = 0; i < expressions.size(); i++) {
            CleaningExpression exp = expressions.get(i);
            hasDoneAnything = exp.cleanThree() || hasDoneAnything;
        }
        List<CleaningLog> logs = new ArrayList<>();
        List<CleaningNegate> negLogs = new ArrayList<>();
        for (CleaningExpression child : expressions) {
            if (child.getType() == CleaningExpressionType.LOG) {
                logs.add((CleaningLog) child);
            } else if (child.getType() == CleaningExpressionType.NEGATE && child.getExpressions().get(0).getType() == CleaningExpressionType.LOG) {
                negLogs.add((CleaningNegate) child);
            }
        }
        List<CleaningExpression> toRemove = new ArrayList<>();
        CleaningExpression newChild = null;
        for (CleaningLog log : logs) {
            List<CleaningLog> matchingLogs = new ArrayList<>();
            List<CleaningNegate> matchingNegLogs = new ArrayList<>();
            CleaningExpression base = log.getBase();
            for (CleaningLog otherLog : logs) {
                if (otherLog.getBase().equals(base) && otherLog != log) {
                    matchingLogs.add(otherLog);
                }
            }
            for (CleaningNegate negLog : negLogs) {
                if (((CleaningLog) negLog.getExpressions().get(0)).getBase().equals(base)) {
                    matchingNegLogs.add(negLog);
                }
            }
            if (matchingLogs.size() + matchingNegLogs.size() > 1) {
                CleaningMultiply mult = new CleaningMultiply();
                toRemove.addAll(matchingLogs);
                toRemove.addAll(matchingNegLogs);
                for (CleaningLog l : matchingLogs) {
                    mult.addExpression(l.getChild());
                }
                for (CleaningNegate l : matchingNegLogs) {
                    mult.addExpression(new CleaningInvert(((CleaningLog) l.getExpressions().get(0)).getChild()));
                }
                newChild = new CleaningLog(base, mult);
                break;
            }
        }
        if (newChild == null) {
            for (CleaningNegate negate : negLogs) {
                List<CleaningNegate> matchingNegLogs = new ArrayList<>();
                CleaningExpression base = ((CleaningLog) negate.getExpressions().get(0)).getBase();
                for (CleaningNegate negLog : negLogs) {
                    if (((CleaningLog) negLog.getExpressions().get(0)).getBase().equals(base)) {
                        matchingNegLogs.add(negLog);
                    }
                }
                if (matchingNegLogs.size() > 1) {
                    CleaningMultiply mult = new CleaningMultiply();
                    toRemove.addAll(matchingNegLogs);
                    for (CleaningNegate l : matchingNegLogs) {
                        mult.addExpression(((CleaningLog) l.getExpressions().get(0)).getChild());
                    }
                    newChild = new CleaningNegate(new CleaningLog(base, mult));
                    break;
                }
            }

        }
        if (newChild != null) {
            hasDoneAnything = true;
            newChild.setParent(this);
            expressions.removeAll(toRemove);
            expressions.add(newChild);
        }
        return hasDoneAnything;
    }

    @Override
    public boolean cleanFour() {
        boolean didSomething = false;
        int totalIntVal = 0;
        List<CleaningReal> reals = new ArrayList<>();
        List<CleaningReal> negReals = new ArrayList<>();
        for (Iterator<CleaningExpression> iterator = expressions.iterator(); iterator.hasNext();) {
            CleaningExpression child = iterator.next();
            if (child.getType() == CleaningExpressionType.INTEGER) {
                totalIntVal += ((CleaningInteger) child).asInt();
                iterator.remove();
            } else if (child.getType() == CleaningExpressionType.REAL) {
                reals.add((CleaningReal) child);
                iterator.remove();
            } else if (child.getType() == CleaningExpressionType.MULTIPLY && child.getExpressions().size() == 2) {
                if (child.getExpressions().get(0).getType() == CleaningExpressionType.INTEGER && child.getExpressions().get(1).getType() == CleaningExpressionType.REAL) {
                    int num = ((CleaningInteger) child.getExpressions().get(0)).asInt();
                    for (int i = 0; i < num; i++) {
                        reals.add((CleaningReal) child.getExpressions().get(1));
                    }
                    iterator.remove();
                } else if (child.getExpressions().get(1).getType() == CleaningExpressionType.INTEGER && child.getExpressions().get(0).getType() == CleaningExpressionType.REAL) {
                    int num = ((CleaningInteger) child.getExpressions().get(1)).asInt();
                    for (int i = 0; i < num; i++) {
                        reals.add((CleaningReal) child.getExpressions().get(0));
                    }
                    iterator.remove();
                }
            } else if (child.getType() == CleaningExpressionType.NEGATE) {
                CleaningExpression cc = child.getExpressions().get(0);
                if (cc.getType() == CleaningExpressionType.INTEGER) {
                    totalIntVal -= ((CleaningInteger) cc).asInt();
                    iterator.remove();
                } else if (cc.getType() == CleaningExpressionType.REAL) {
                    negReals.add((CleaningReal) cc);
                    iterator.remove();
                } else if (cc.getType() == CleaningExpressionType.MULTIPLY && cc.getExpressions().size() == 2) {
                    if (cc.getExpressions().get(0).getType() == CleaningExpressionType.INTEGER && cc.getExpressions().get(1).getType() == CleaningExpressionType.REAL) {
                        int num = ((CleaningInteger) cc.getExpressions().get(0)).asInt();
                        for (int i = 0; i < num; i++) {
                            negReals.add((CleaningReal) cc.getExpressions().get(1));
                        }
                        iterator.remove();
                    } else if (cc.getExpressions().get(1).getType() == CleaningExpressionType.INTEGER && cc.getExpressions().get(0).getType() == CleaningExpressionType.REAL) {
                        int num = ((CleaningInteger) cc.getExpressions().get(1)).asInt();
                        for (int i = 0; i < num; i++) {
                            negReals.add((CleaningReal) cc.getExpressions().get(0));
                        }
                        iterator.remove();
                    }
                }
            }
        }
        if (totalIntVal != 0) {
            if (totalIntVal > 0) {
                expressions.add(new CleaningInteger(totalIntVal));
            } else {
                expressions.add(new CleaningNegate(new CleaningInteger(-totalIntVal)));
            }
        }
        for (Iterator<CleaningReal> iterator = reals.iterator(); iterator.hasNext();) {
            CleaningReal r = iterator.next();
            if (negReals.contains(r)) {
                negReals.remove(r);
                iterator.remove();
                didSomething = true;
            }
        }
        while (reals.size() > 0) {
            CleaningReal r = reals.get(0);
            reals.remove(0);
            int num = 1;
            for (int i = 0; i < reals.size(); i++) {
                if (reals.get(i).equals(r)) {
                    reals.remove(i--);
                    num++;
                    didSomething = true;
                }
            }
            for (int i = 0; i < negReals.size(); i++) {
                if (negReals.get(i).equals(r)) {
                    negReals.remove(i--);
                    num--;
                    didSomething = true;
                }
            }
            if (Math.abs(num) == 1) {
                if (num > 0) {
                    expressions.add(r);
                } else {
                    CleaningNegate n = new CleaningNegate(r);
                    n.setParent(this);
                    expressions.add(n);
                }
            } else if (num != 0) {
                CleaningMultiply mult = new CleaningMultiply();
                mult.addExpression(r);
                if (num > 0) {
                    mult.addExpression(new CleaningInteger(num));
                    mult.setParent(this);
                    expressions.add(mult);
                } else {
                    mult.addExpression(new CleaningInteger(-num));
                    CleaningNegate n = new CleaningNegate(mult);
                    n.setParent(this);
                    expressions.add(n);
                }
            }
        }
        while (negReals.size() > 0) {
            CleaningReal r = negReals.get(0);
            negReals.remove(0);
            int num = -1;
            for (int i = 0; i < negReals.size(); i++) {
                if (negReals.get(i).equals(r)) {
                    negReals.remove(i--);
                    num--;
                    didSomething = true;
                }
            }
            if (Math.abs(num) == 1) {
                CleaningNegate n = new CleaningNegate(r);
                n.setParent(this);
                expressions.add(n);
            } else {
                CleaningMultiply mult = new CleaningMultiply();
                mult.addExpression(r);
                mult.addExpression(new CleaningInteger(-num));
                CleaningNegate n = new CleaningNegate(mult);
                n.setParent(this);
                expressions.add(n);
            }
        }
        for (int i = 0; i < expressions.size(); i++) {
            didSomething = expressions.get(i).cleanFour() || didSomething;
        }
        return didSomething;
    }

    @Override
    public String render(ExpressionRenderer r) {
        StringBuilder pos = new StringBuilder();
        StringBuilder neg = new StringBuilder();
        boolean hasPrev = false;
        for (CleaningExpression exp : expressions) {
            if (exp.getType() == CleaningExpressionType.NEGATE) {
                neg.append(exp.render(r));
            } else {
                if (hasPrev) {
                    pos.append(r.plus());
                }
                pos.append(exp.render(r));
                hasPrev = true;
            }
        }
        return pos.toString() + neg.toString();
    }

    @Override
    public void replaceChild(CleaningExpression oldExp, CleaningExpression newExp) {
        expressions.removeIf(c -> c == oldExp);
        expressions.add(newExp);
        newExp.setParent(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != CleaningAdd.class) {
            return false;
        }
        CleaningAdd other = (CleaningAdd) obj;
        if (other.expressions.size() != expressions.size()) {
            return false;
        }
        if (other.expressions.containsAll(expressions)) {
            List<CleaningExpression> exps = new ArrayList<>(other.expressions);
            for (CleaningExpression child : expressions) {
                if (!exps.remove(child)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean canNegate() {
        for (CleaningExpression child : expressions) {
            if (child.getType() == CleaningExpressionType.NEGATE) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void negate() {
        List<CleaningExpression> newExpressions = new ArrayList<>();
        for (CleaningExpression child : expressions) {
            if (child.getType() == CleaningExpressionType.NEGATE) {
                newExpressions.add(child.getExpressions().get(0));
            } else {
                newExpressions.add(new CleaningNegate(child));
            }
        }
        for (CleaningExpression newExp : newExpressions) {
            newExp.setParent(this);
        }
        expressions = newExpressions;

    }

    @Override
    public double asDouble() {
        double d = 0;
        for (CleaningExpression exp : expressions) {
            d += exp.asDouble();
        }
        return d;
    }

    @Override
    public CleaningAdd clone() {
        CleaningAdd clone = new CleaningAdd();
        for (CleaningExpression child : expressions) {
            clone.addExpression(child.clone());
        }
        return clone;
    }

}
