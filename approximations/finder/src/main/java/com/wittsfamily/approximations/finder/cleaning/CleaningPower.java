package com.wittsfamily.approximations.finder.cleaning;

import java.util.List;

import com.wittsfamily.approximations.finder.ExpressionRenderer;

public class CleaningPower implements CleaningExpression {
	private CleaningExpression base;
	private CleaningExpression power;
	private CleaningExpression parent;

	public CleaningPower(CleaningExpression base, CleaningExpression power) {
		this.parent = null;
		this.base = base;
		base.setParent(this);
		this.power = power;
		power.setParent(this);
	}

	@Override
	public CleaningExpressionType getType() {
		return CleaningExpressionType.POWER;
	}

	@Override
	public List<CleaningExpression> getExpressions() {
		return List.of(power);
	}

	@Override
	public CleaningExpression getParent() {
		return parent;
	}

	@Override
	public void setParent(CleaningExpression parent) {
		this.parent = parent;
	}

	public CleaningExpression getBase() {
		return base;
	}

	public CleaningExpression getPower() {
		return power;
	}

	@Override
	public boolean cleanOne(boolean convertPowers) {
		boolean didSomething = power.cleanOne(convertPowers);
		didSomething = base.cleanOne(convertPowers) || didSomething;
		if (base.getType() == CleaningExpressionType.NEGATE && power.getType() == CleaningExpressionType.INTEGER) {
			if (((CleaningInteger) power).asInt() % 2 == 0) {
				base = base.getExpressions().get(0);
				base.setParent(this);
				didSomething = true;
			} else {
				base = base.getExpressions().get(0);
				base.setParent(this);
				parent.replaceChild(this, new CleaningNegate(this));
				didSomething = true;
			}
		} else if (power.getType() == CleaningExpressionType.NEGATE && convertPowers) {
			power = power.getExpressions().get(0);
			power.setParent(this);
			parent.replaceChild(this, new CleaningInvert(this));
			didSomething = true;
		} else if (base.getType() == CleaningExpressionType.INVERT) {
			base = base.getExpressions().get(0);
			base.setParent(this);
			parent.replaceChild(this, new CleaningInvert(this));
			didSomething = true;
		} else if (power.getType() == CleaningExpressionType.INTEGER && ((CleaningInteger) power).asInt() == 0) {
			parent.replaceChild(this, new CleaningInteger(1));
			return true;
		} else if (power.getType() == CleaningExpressionType.INTEGER && ((CleaningInteger) power).asInt() == 1) {
			parent.replaceChild(this, base);
			return true;
		} else if (base.getType() == CleaningExpressionType.INTEGER && ((CleaningInteger) base).asInt() == 1) {
			parent.replaceChild(this, new CleaningInteger(1));
			return true;
		} else if (base.getType() == CleaningExpressionType.INTEGER && ((CleaningInteger) base).asInt() == 0) {
			parent.replaceChild(this, new CleaningInteger(0));
			return true;
		}
		return didSomething;
	}

	@Override
	public boolean cleanTwo() {
		boolean didSomething = power.cleanTwo();
		didSomething = base.cleanTwo() || didSomething;
		if (base.getType() == CleaningExpressionType.POWER) {
			CleaningPower chPow = (CleaningPower) base;
			CleaningMultiply newPow = new CleaningMultiply();
			newPow.setParent(this);
			newPow.addExpression(power);
			newPow.addExpression(chPow.power);
			base = chPow.base;
			base.setParent(this);
			power = newPow;
			didSomething = true;
		}
		return didSomething;
	}

	@Override
	public boolean cleanThree() {
		boolean didSomething = power.cleanThree();
		didSomething = base.cleanThree() || didSomething;
		CleaningMultiply powMult = null;

		if (power.getType() == CleaningExpressionType.MULTIPLY) {
			powMult = (CleaningMultiply) power;
		} else if (power.getType() == CleaningExpressionType.INVERT
				&& power.getExpressions().get(0).getType() == CleaningExpressionType.MULTIPLY) {
			powMult = (CleaningMultiply) power.getExpressions().get(0);
		}
		CleaningLog log = null;
		if (powMult != null) {
			for (CleaningExpression pow : powMult.getExpressions()) {
				if (pow.getType() == CleaningExpressionType.LOG
						&& (powMult == power ? ((CleaningLog) pow).getBase() : ((CleaningLog) pow).getChild())
								.equals(base)) {
					log = (CleaningLog) pow;
				} else if (pow.getType() == CleaningExpressionType.INVERT
						&& pow.getExpressions().get(0).getType() == CleaningExpressionType.LOG) {
					CleaningLog l = (CleaningLog) pow.getExpressions().get(0);
					if ((powMult == power ? l.getChild() : l.getBase()).equals(base)) {
						log = l;
					}
				}
				if (log != null) {
					powMult.getExpressions().remove(pow);
					CleaningExpression newBase;
					if ((log == pow && powMult == power) || (log != pow && powMult != power)) {
						newBase = log.getChild();
					} else {
						newBase = log.getBase();
					}
					base = newBase;
					base.setParent(this);
					return true;
				}
			}
		} else if (power.getType() == CleaningExpressionType.LOG && (((CleaningLog) power).getBase()).equals(base)) {
			parent.replaceChild(this, ((CleaningLog) power).getChild());
			didSomething = true;
		} else if (power.getType() == CleaningExpressionType.INVERT
				&& power.getExpressions().get(0).getType() == CleaningExpressionType.LOG
				&& (((CleaningLog) power.getExpressions().get(0)).getChild()).equals(base)) {
			parent.replaceChild(this, ((CleaningLog) power.getExpressions().get(0)).getBase());
			didSomething = true;
		}
		return didSomething;
	}

	@Override
	public boolean cleanFour() {
		boolean didSomething = power.cleanFour();
		didSomething = base.cleanFour() || didSomething;
		if (base.getType() == CleaningExpressionType.INTEGER && power.getType() == CleaningExpressionType.INTEGER) {
			if (Math.pow(base.asDouble(), power.asDouble()) <= 100) {
				parent.replaceChild(this, new CleaningInteger((int) Math.pow(base.asDouble(), power.asDouble())));
				return true;
			}
		} else if (parent.getType() == CleaningExpressionType.INVERT
				&& parent.getParent().getType() != CleaningExpressionType.MULTIPLY) {
			parent.getParent().replaceChild(parent, this);
			power = new CleaningNegate(power);
			power.setParent(this);
			didSomething = true;
		}
		return didSomething;
	}

	@Override
	public String render(ExpressionRenderer r) {
		if (power.getType() == CleaningExpressionType.NEGATE
				&& power.getExpressions().get(0).getType() == CleaningExpressionType.INTEGER
				&& ((CleaningInteger) power.getExpressions().get(0)).asInt() == 1
				&& (base.getType() == CleaningExpressionType.INTEGER
						|| base.getType() == CleaningExpressionType.REAL)) {
			return r.slantedFraction(r.integer(1), base.render(r));
		}
		if (power.getType() == CleaningExpressionType.INVERT) {
			CleaningExpression cc = power.getExpressions().get(0);
			if (cc.getType() == CleaningExpressionType.REAL || cc.getType() == CleaningExpressionType.INTEGER) {
				if (cc.getType() != CleaningExpressionType.INTEGER || ((CleaningInteger) cc).asInt() != 2) {
					return r.root(cc.render(r), base.render(r));
				} else {
					return r.sqrt(base.render(r));
				}
			}
		}
		StringBuilder b = new StringBuilder();
		if (base.getType() == CleaningExpressionType.INTEGER || base.getType() == CleaningExpressionType.REAL) {
			return r.power(base.render(r), power.render(r));
		} else {
			return r.power(r.bracket(base.render(r)), power.render(r));
		}
	}

	@Override
	public void replaceChild(CleaningExpression oldExp, CleaningExpression newExp) {
		if (oldExp == base) {
			base = newExp;
		} else {
			power = newExp;
		}
		newExp.setParent(this);
	}

	@Override
	public boolean canNegate() {
		return power.getType() == CleaningExpressionType.INTEGER && ((CleaningInteger) power).asInt() % 2 == 1
				&& base.canNegate();
	}

	@Override
	public void negate() {
		base.negate();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CleaningPower other = (CleaningPower) obj;
		if (!power.equals(other.power))
			return false;
		if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public double asDouble() {
		return Math.pow(base.asDouble(), power.asDouble());
	}

	@Override
	public CleaningPower clone() {
		CleaningPower c = new CleaningPower(base.clone(), power.clone());
		return c;
	}
}
