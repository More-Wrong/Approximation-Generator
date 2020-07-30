package com.wittsfamily.approximations.finder.cleaning;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.wittsfamily.approximations.finder.ExpressionRenderer;

public class CleaningMultiply implements CleaningExpression {
	private List<CleaningExpression> expressions = new ArrayList<>();
	private CleaningExpression parent;

	public void addExpression(CleaningExpression child) {
		expressions.add(child);
		child.setParent(this);
	}

	public List<CleaningExpression> getExpressions() {
		return expressions;
	}

	@Override
	public CleaningExpressionType getType() {
		return CleaningExpressionType.MULTIPLY;
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
		boolean hasDoneSomething = false;
		for (int i = 0; i < expressions.size(); i++) {
			CleaningExpression child = expressions.get(i);
			hasDoneSomething = child.cleanOne(convertPowers) || hasDoneSomething;
		}
		for (Iterator<CleaningExpression> iterator = expressions.iterator(); iterator.hasNext();) {
			CleaningExpression child = iterator.next();
			if (child.getType() == CleaningExpressionType.INTEGER && ((CleaningInteger) child).asInt() == 1) {
				iterator.remove();
				hasDoneSomething = true;
			} else if (child.getType() == CleaningExpressionType.INTEGER && ((CleaningInteger) child).asInt() == 0) {
				parent.replaceChild(this, new CleaningInteger(0));
				return true;
			}
		}
		if (expressions.size() == 1) {
			parent.replaceChild(this, expressions.get(0));
			return true;
		} else if (expressions.size() == 0) {
			parent.replaceChild(this, new CleaningInteger(1));
			return true;
		}
		boolean shouldInv = true;
		for (CleaningExpression child : expressions) {
			if (child.getType() != CleaningExpressionType.INVERT) {
				shouldInv = false;
				break;
			}
		}
		if (shouldInv && convertPowers) {
			invert();
			parent.replaceChild(this, new CleaningInvert(this));
		}
		List<CleaningExpression> toAdd = new ArrayList<>();
		int negationCount = 0;
		for (Iterator<CleaningExpression> iterator = expressions.iterator(); iterator.hasNext();) {
			CleaningExpression child = iterator.next();
			if (child.getType() == CleaningExpressionType.MULTIPLY) {
				for (CleaningExpression childChild : child.getExpressions()) {
					toAdd.add(childChild);
					childChild.setParent(this);
				}
				iterator.remove();
				hasDoneSomething = true;
			} else if (child.getType() == CleaningExpressionType.INVERT) {
				CleaningExpression childChild = child.getExpressions().get(0);
				if (childChild.getType() == CleaningExpressionType.MULTIPLY) {
					for (CleaningExpression childChildChild : childChild.getExpressions()) {
						toAdd.add(new CleaningInvert(childChildChild));
					}
					hasDoneSomething = true;
					iterator.remove();
				}
			} else if (child.getType() == CleaningExpressionType.NEGATE) {
				CleaningExpression childChild = child.getExpressions().get(0);
				childChild.setParent(this);
				toAdd.add(childChild);
				negationCount++;
				iterator.remove();
				hasDoneSomething = true;
			}
		}
		hasDoneSomething = hasDoneSomething || !toAdd.isEmpty();
		for (CleaningExpression newChild : toAdd) {
			newChild.setParent(this);
		}
		expressions.addAll(toAdd);
		if (negationCount % 2 == 1) {
			hasDoneSomething = true;
			if (parent.getType() == CleaningExpressionType.NEGATE) {
				parent.getParent().replaceChild(parent, this);
			} else {
				parent.replaceChild(this, new CleaningNegate(this));
			}
		}
		return hasDoneSomething;
	}

	@Override
	public boolean cleanTwo() {
		boolean hasDoneSomething = false;
		for (int i = 0; i < expressions.size(); i++) {
			CleaningExpression child = expressions.get(i);
			hasDoneSomething = child.cleanTwo() || hasDoneSomething;
		}
		List<CleaningExpression> baseShareExps = new ArrayList<>();
		CleaningExpression newChild = null;
		for (CleaningExpression child : expressions) {
			CleaningExpression target = child;
			if (child.getType() == CleaningExpressionType.POWER) {
				target = ((CleaningPower) child).getBase();
			} else if (child.getType() == CleaningExpressionType.INVERT) {
				if (child.getExpressions().get(0).getType() == CleaningExpressionType.POWER) {
					target = ((CleaningPower) child.getExpressions().get(0)).getBase();
				} else {
					target = child.getExpressions().get(0);
				}
			}
			baseShareExps.clear();
			baseShareExps.add(child);
			for (CleaningExpression otherChild : expressions) {
				if (otherChild != child) {
					if (otherChild.equals(target)) {
						baseShareExps.add(otherChild);
					} else if (otherChild.getType() == CleaningExpressionType.POWER
							&& ((CleaningPower) otherChild).getBase().equals(target)) {
						baseShareExps.add(otherChild);
					} else if (otherChild.getType() == CleaningExpressionType.INVERT) {
						CleaningExpression childChild = otherChild.getExpressions().get(0);
						if (childChild.equals(target)) {
							baseShareExps.add(otherChild);
						} else if (childChild.getType() == CleaningExpressionType.POWER
								&& ((CleaningPower) childChild).getBase().equals(target)) {
							baseShareExps.add(otherChild);
						}
					}
				}
			}
			if (baseShareExps.size() > 1) {
				int intPower = 0;
				int num = 0;
				for (CleaningExpression targetChild : baseShareExps) {
					if (targetChild.getType() == CleaningExpressionType.POWER) {
						num++;
					} else if (targetChild.getType() == CleaningExpressionType.INVERT) {
						if (targetChild.getExpressions().get(0).getType() == CleaningExpressionType.POWER) {
							num++;
						} else {
							intPower--;
						}
					} else {
						intPower++;
					}
				}
				if (intPower != 0) {
					num++;
				}
				if (num == 1) {
					if (intPower < 0) {
						newChild = new CleaningPower(target, new CleaningNegate(new CleaningInteger(-intPower)));
						break;
					} else if (intPower > 0) {
						newChild = new CleaningPower(target, new CleaningInteger(intPower));
						break;
					} else {
						CleaningExpression pow = null;
						for (CleaningExpression targetChild : baseShareExps) {
							if (targetChild.getType() == CleaningExpressionType.POWER) {
								pow = ((CleaningPower) targetChild).getPower();
							} else if (targetChild.getType() == CleaningExpressionType.INVERT) {
								if (targetChild.getExpressions().get(0).getType() == CleaningExpressionType.POWER) {
									pow = new CleaningNegate(((CleaningPower) child).getPower());
								}
							}
						}
						newChild = new CleaningPower(target, pow);
						break;
					}
				} else if (num > 1) {
					CleaningAdd a = new CleaningAdd();
					for (CleaningExpression targetChild : baseShareExps) {
						if (targetChild.getType() == CleaningExpressionType.POWER) {
							a.addExpression(((CleaningPower) targetChild).getPower());
						} else if (targetChild.getType() == CleaningExpressionType.INVERT) {
							if (targetChild.getExpressions().get(0).getType() == CleaningExpressionType.POWER) {
								a.addExpression(new CleaningNegate(
										((CleaningPower) targetChild.getExpressions().get(0)).getPower()));
							}
						}
					}
					if (intPower < 0) {
						a.addExpression(new CleaningNegate(new CleaningInteger(-intPower)));
					} else if (intPower > 0) {
						a.addExpression(new CleaningInteger(intPower));
					}
					newChild = new CleaningPower(target, a);
					break;
				}
			}
		}
		if (newChild != null) {
			expressions.removeAll(baseShareExps);
			expressions.add(newChild);
			newChild.setParent(this);
			hasDoneSomething = true;
		}

		return hasDoneSomething;
	}

	@Override
	public boolean cleanThree() {
		boolean hasDoneAnything = false;
		for (int i = 0; i < expressions.size(); i++) {
			CleaningExpression exp = expressions.get(i);
			hasDoneAnything = exp.cleanThree() || hasDoneAnything;
		}
		List<CleaningLog> logs = new ArrayList<>();
		List<CleaningInvert> invLogs = new ArrayList<>();
		for (CleaningExpression child : expressions) {
			if (child.getType() == CleaningExpressionType.LOG) {
				logs.add((CleaningLog) child);
			} else if (child.getType() == CleaningExpressionType.INVERT
					&& child.getExpressions().get(0).getType() == CleaningExpressionType.LOG) {
				invLogs.add((CleaningInvert) child);
			}
		}
		CleaningExpression toRemove = null;
		for (CleaningLog target : logs) {
			CleaningExpression base = target.getBase();
			List<CleaningLog> matchingLogs = new ArrayList<>();
			List<CleaningInvert> matchingInvLogs = new ArrayList<>();
			for (CleaningLog log : logs) {
				if (log != target && log.getChild().equals(base)) {
					matchingLogs.add(log);
				}
			}
			for (CleaningInvert invLog : invLogs) {
				CleaningLog log = (CleaningLog) invLog.getExpressions().get(0);
				if (log.getBase().equals(base)) {
					matchingInvLogs.add(invLog);
				}
			}
			if (matchingInvLogs.size() > 0) {
				toRemove = matchingInvLogs.get(0);
				CleaningLog log = (CleaningLog) matchingInvLogs.get(0).getExpressions().get(0);
				target.replaceChild(base, log.getChild());
				hasDoneAnything = true;
				break;
			} else if (matchingLogs.size() > 0) {
				toRemove = matchingLogs.get(0);
				target.replaceChild(base, matchingLogs.get(0).getBase());
				hasDoneAnything = true;
				break;
			}
		}
		if (toRemove != null) {
			expressions.remove(toRemove);
		}
		toRemove = null;
		for (CleaningLog target : logs) {
			CleaningExpression base = target.getChild();
			List<CleaningLog> matchingLogs = new ArrayList<>();
			List<CleaningInvert> matchingInvLogs = new ArrayList<>();
			for (CleaningLog log : logs) {
				if (log != target && log.getBase().equals(base)) {
					matchingLogs.add(log);
				}
			}
			for (CleaningInvert invLog : invLogs) {
				CleaningLog log = (CleaningLog) invLog.getExpressions().get(0);
				if (log.getChild().equals(base)) {
					matchingInvLogs.add(invLog);
				}
			}
			if (matchingInvLogs.size() > 0) {
				toRemove = matchingInvLogs.get(0);
				CleaningLog log = (CleaningLog) matchingInvLogs.get(0).getExpressions().get(0);
				target.replaceChild(base, log.getBase());
				hasDoneAnything = true;
				break;
			} else if (matchingLogs.size() > 0) {
				toRemove = matchingLogs.get(0);
				target.replaceChild(base, matchingLogs.get(0).getChild());
				hasDoneAnything = true;
				break;
			}
		}
		if (toRemove != null) {
			expressions.remove(toRemove);
		}
		return hasDoneAnything;
	}

	@Override
	public boolean cleanFour() {
		boolean hasDoneAnything = false;
		for (int i = 0; i < expressions.size(); i++) {
			CleaningExpression exp = expressions.get(i);
			hasDoneAnything = exp.cleanFour() || hasDoneAnything;
		}
		if (parent.getType() == CleaningExpressionType.INVERT) {
			parent.getParent().replaceChild(parent, this);
			invert();
			hasDoneAnything = true;
		}
		int numerator = 1;
		int denominator = 1;
		for (Iterator<CleaningExpression> iterator = expressions.iterator(); iterator.hasNext();) {
			CleaningExpression child = iterator.next();
			if (child.getType() == CleaningExpressionType.INTEGER) {
				numerator *= ((CleaningInteger) child).asInt();
				iterator.remove();
			} else if (child.getType() == CleaningExpressionType.INVERT
					&& child.getExpressions().get(0).getType() == CleaningExpressionType.INTEGER) {
				denominator *= ((CleaningInteger) child.getExpressions().get(0)).asInt();
				iterator.remove();
			}
		}
		if (numerator == 1 && denominator == 1) {
			// do nothing
		} else if (numerator != 1 && denominator == 1) {
			expressions.add(new CleaningInteger(numerator));
		} else if (denominator != 1 && numerator == 1) {
			expressions.add(new CleaningInvert(new CleaningInteger(denominator)));
		} else if (numerator == denominator) {
			hasDoneAnything = true;
			// no not replace numbers, simply remove both
		} else if (numerator % denominator == 0) {
			hasDoneAnything = true;
			expressions.add(new CleaningInteger(numerator / denominator));
		} else if (denominator % numerator == 0) {
			hasDoneAnything = true;
			expressions.add(new CleaningInvert(new CleaningInteger(denominator / numerator)));
		} else if (numerator != 1 && denominator != 1) {
			double maxTest = Math.sqrt(Math.min(denominator, numerator));
			for (int i = 2; i < maxTest + 2; i++) {
				while (numerator % i == 0 && denominator % i == 0) {
					numerator /= i;
					denominator /= i;
					maxTest /= Math.sqrt(i);
					hasDoneAnything = true;
				}
			}
			expressions.add(new CleaningInteger(numerator));
			expressions.add(new CleaningInvert(new CleaningInteger(denominator)));
		}
		boolean hasDenominatorAnyway = false;
		for (CleaningExpression child : expressions) {
			child.setParent(this);
			if (child.getType() == CleaningExpressionType.INVERT
					&& child.getExpressions().get(0).getType() != CleaningExpressionType.POWER) {
				hasDenominatorAnyway = true;
			}
		}
		if (!hasDenominatorAnyway) {
			List<CleaningExpression> invs = new ArrayList<>();
			for (CleaningExpression child : expressions) {
				if (child.getType() == CleaningExpressionType.INVERT
						&& child.getExpressions().get(0).getType() == CleaningExpressionType.POWER) {
					invs.add(child);
					CleaningPower lPow = (CleaningPower) child.getExpressions().get(0);
					lPow.replaceChild(lPow.getPower(), new CleaningNegate(lPow.getPower()));

				}
			}
			for (CleaningExpression inv : invs) {
				expressions.remove(inv);
				inv.getExpressions().get(0).setParent(this);
				expressions.add(inv.getExpressions().get(0));
				hasDoneAnything = true;
			}
		}
		return hasDoneAnything;
	}

	public boolean canInvert() {
		for (CleaningExpression child : expressions) {
			if (child.getType() == CleaningExpressionType.INVERT) {
				return true;
			}
		}
		return false;
	}

	public void invert() {
		List<CleaningExpression> newExpressions = new ArrayList<>();
		for (CleaningExpression child : expressions) {
			if (child.getType() == CleaningExpressionType.INVERT) {
				newExpressions.add(child.getExpressions().get(0));
			} else {
				CleaningExpression exp = new CleaningInvert(child);
				child.setParent(exp);
				newExpressions.add(exp);
			}
		}
		for (CleaningExpression exp : newExpressions) {
			exp.setParent(this);
		}
		expressions = newExpressions;
	}

	@Override
	public String render(ExpressionRenderer r) {
		int numeratorNum = 0;
		int denominatorNum = 0;
		boolean onlyConsts = true;
		for (CleaningExpression child : expressions) {
			if (child.getType() == CleaningExpressionType.INVERT) {
				denominatorNum++;
				if (child.getExpressions().get(0).getType() != CleaningExpressionType.INTEGER
						&& child.getExpressions().get(0).getType() != CleaningExpressionType.REAL) {
					onlyConsts = false;
				}
			} else {
				numeratorNum++;
				if (child.getType() != CleaningExpressionType.INTEGER
						&& child.getType() != CleaningExpressionType.REAL) {
					onlyConsts = false;
				}
			}

		}
		StringBuilder numerator1 = new StringBuilder();
		StringBuilder denominator1 = new StringBuilder();
		StringBuilder numerator2 = new StringBuilder();
		StringBuilder denominator2 = new StringBuilder();
		StringBuilder numerator3 = new StringBuilder();
		StringBuilder denominator3 = new StringBuilder();
		StringBuilder numerator4 = new StringBuilder();
		StringBuilder denominator4 = new StringBuilder();
		StringBuilder numerator5 = new StringBuilder();
		StringBuilder denominator5 = new StringBuilder();

		for (CleaningExpression child : expressions) {
			StringBuilder target = null;
			int targetNum = 0;
			boolean br = false;
			boolean powSpec = false;
			boolean enTimes = false;
			CleaningExpression toWrite = child;
			if (child.getType() == CleaningExpressionType.INVERT) {
				CleaningExpression current = child.getExpressions().get(0);
				toWrite = current;
				if (current.getType() == CleaningExpressionType.POWER) {
					current = ((CleaningPower) current).getBase();
					powSpec = true;
				}
				targetNum = denominatorNum;
				if (current.getType() == CleaningExpressionType.INTEGER) {
					enTimes = true;
					if (powSpec) {
						target = denominator2;
					} else {
						target = denominator1;
					}
				} else if (current.getType() == CleaningExpressionType.REAL) {
					if (powSpec) {
						target = denominator4;
					} else {
						target = denominator3;
					}
				} else {
					target = denominator5;
					powSpec = false;
					br = true;
				}
			} else {
				CleaningExpression current = child;
				if (current.getType() == CleaningExpressionType.POWER) {
					current = ((CleaningPower) current).getBase();
					powSpec = true;
				}
				targetNum = numeratorNum;
				if (current.getType() == CleaningExpressionType.INTEGER) {
					enTimes = true;
					if (powSpec) {
						target = numerator2;
					} else {
						target = numerator1;
					}
				} else if (current.getType() == CleaningExpressionType.REAL) {
					if (powSpec) {
						target = numerator4;
					} else {
						target = numerator3;
					}
				} else {
					target = numerator5;
					powSpec = false;
					br = true;
				}
			}
			if (enTimes && target.length() != 0) {
				target.append(r.times());
			}
			if (br && targetNum != 1) {
				target.append(r.bracket(toWrite.render(r)));
			} else {
				target.append(toWrite.render(r));
			}
		}
		StringBuilder numerator = new StringBuilder();
		numerator.append(numerator1);
		if (numerator1.length() > 0 && numerator2.length() > 0) {
			numerator.append(r.times());
		}
		numerator.append(numerator2);
		numerator.append(numerator3);
		numerator.append(numerator4);
		numerator.append(numerator5);

		StringBuilder denominator = new StringBuilder();
		denominator.append(denominator1);
		if (denominator1.length() > 0 && denominator2.length() > 0) {
			numerator.append(r.times());
		}
		denominator.append(denominator2);
		denominator.append(denominator3);
		denominator.append(denominator4);
		denominator.append(denominator5);

		if (onlyConsts && (numeratorNum == 1 || numeratorNum == 0) && denominatorNum == 1) {
			return r.slantedFraction(numerator.length() == 0 ? r.integer(1) : numerator.toString(),
					denominator.toString());
		}
		if (denominator.length() != 0) {
			return r.fraction(numerator.length() == 0 ? r.integer(1) : numerator.toString(), denominator.toString());
		} else {
			return numerator.toString();
		}
	}

	@Override
	public void replaceChild(CleaningExpression oldExp, CleaningExpression newExp) {
		expressions.removeIf(c -> c == oldExp);
		expressions.add(newExp);
		newExp.setParent(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != CleaningMultiply.class) {
			return false;
		}
		CleaningMultiply other = (CleaningMultiply) obj;
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
			if (child.canNegate()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void negate() {
		for (CleaningExpression child : expressions) {
			if (child.canNegate()) {
				child.negate();
				break;
			}
		}
	}

	@Override
	public double asDouble() {
		double d = 1;
		for (CleaningExpression exp : expressions) {
			d *= exp.asDouble();
		}
		return d;
	}

	@Override
	public CleaningMultiply clone() {
		CleaningMultiply clone = new CleaningMultiply();
		for (CleaningExpression child : expressions) {
			clone.addExpression(child.clone());
		}
		return clone;
	}

}
