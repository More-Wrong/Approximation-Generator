package com.wittsfamily.approximations.finder.cleaning;

import java.util.List;

import com.wittsfamily.approximations.finder.ExpressionRenderer;
import com.wittsfamily.approximations.generator.Constant;

public class CleaningLog implements CleaningExpression {
	private CleaningExpression base;
	private CleaningExpression child;
	private CleaningExpression parent;

	public CleaningLog(CleaningExpression base, CleaningExpression child) {
		this.parent = null;
		this.base = base;
		this.child = child;
		base.setParent(this);
		child.setParent(this);
	}

	public CleaningExpression getBase() {
		return base;
	}

	public CleaningExpression getChild() {
		return child;
	}

	@Override
	public CleaningExpressionType getType() {
		return CleaningExpressionType.LOG;
	}

	@Override
	public List<CleaningExpression> getExpressions() {
		return List.of(base, child);
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
		boolean didSomething = false;
		if (child.getType() == CleaningExpressionType.INVERT) {
			child = child.getExpressions().get(0);
			child.setParent(this);
			parent.replaceChild(this, new CleaningNegate(this));
			didSomething = true;
		}
		if (child.getType() == CleaningExpressionType.INTEGER && ((CleaningInteger) child).asInt() == 1) {
			if (base.getType() == CleaningExpressionType.INTEGER && ((CleaningInteger) base).asDouble() == 1.0) {
				parent.replaceChild(this, new CleaningInteger(1));
			} else {
				parent.replaceChild(this, new CleaningInteger(0));
			}
			return true;
		} else if (child.getType() == CleaningExpressionType.INTEGER && ((CleaningInteger) child).asInt() == 0) {
			if (base.getType() == CleaningExpressionType.INTEGER && ((CleaningInteger) base).asDouble() == 0.0) {
				parent.replaceChild(this, new CleaningInteger(1));
			} else {
				parent.replaceChild(this, new CleaningNegate(new CleaningInvert(new CleaningInteger(0))));
			}
			return true;
		} else if (base.getType() == CleaningExpressionType.INTEGER && ((CleaningInteger) base).asInt() == 1) {
			parent.replaceChild(this, new CleaningInvert(new CleaningInteger(0)));
			return true;
		} else if (base.getType() == CleaningExpressionType.INTEGER && ((CleaningInteger) base).asInt() == 0) {
			parent.replaceChild(this, new CleaningInteger(0));
			return true;
		} else if (base.equals(child)) {
			parent.replaceChild(this, new CleaningInteger(1));
			return true;
		}
		didSomething = child.cleanOne(convertPowers) || didSomething;
		return base.cleanOne(convertPowers) || didSomething;
	}

	@Override
	public boolean cleanTwo() {
		boolean didSomething = child.cleanTwo();
		return base.cleanTwo() || didSomething;
	}

	@Override
	public boolean cleanThree() {
		boolean didSomething = false;
		if (child.getType() == CleaningExpressionType.POWER) {
			CleaningExpression pow = ((CleaningPower) child).getPower();
			child = ((CleaningPower) child).getBase();
			child.setParent(this);
			CleaningMultiply mult = new CleaningMultiply();
			mult.addExpression(pow);
			parent.replaceChild(this, mult);
			mult.addExpression(this);
			didSomething = true;
		}
		if (base.getType() == CleaningExpressionType.POWER) {
			CleaningExpression pow = ((CleaningPower) base).getPower();
			base = ((CleaningPower) base).getBase();
			base.setParent(this);
			CleaningMultiply mult = new CleaningMultiply();
			mult.addExpression(new CleaningInvert(pow));
			parent.replaceChild(this, mult);
			mult.addExpression(this);
			didSomething = true;
		}
		didSomething = child.cleanThree() || didSomething;
		return base.cleanThree() || didSomething;
	}

	@Override
	public boolean cleanFour() {
		boolean didSomething = false;
		if ((child.getType() == CleaningExpressionType.INTEGER || child.getType() == CleaningExpressionType.REAL)
				&& (base.getType() != CleaningExpressionType.INTEGER
						&& base.getType() != CleaningExpressionType.REAL)) {
			CleaningExpression tmp = base;
			base = child;
			child = tmp;
			parent.replaceChild(this, new CleaningInvert(this));
			didSomething = true;
		} else if (!((base.getType() == CleaningExpressionType.INTEGER || base.getType() == CleaningExpressionType.REAL)
				&& (child.getType() != CleaningExpressionType.INTEGER
						&& child.getType() != CleaningExpressionType.REAL))
				&& parent.getType() == CleaningExpressionType.INVERT) {
			parent.getParent().replaceChild(parent, this);
			CleaningExpression tmp = base;
			base = child;
			child = tmp;
			didSomething = true;
		}
		didSomething = child.cleanFour() || didSomething;
		return base.cleanFour() || didSomething;
	}

	@Override
	public String render(ExpressionRenderer r) {
		StringBuilder b = new StringBuilder();
		if (base.getType() == CleaningExpressionType.REAL && ((CleaningReal) base).asConstant() == Constant.E) {
			if (child.getType() != CleaningExpressionType.REAL && child.getType() != CleaningExpressionType.INTEGER) {
				return r.ln(child.render(r));
			}else {
				return r.ln(r.bracket(child.render(r)));
			}
		} else {
			if (child.getType() != CleaningExpressionType.REAL && child.getType() != CleaningExpressionType.INTEGER) {
				return r.log(base.render(r), child.render(r));
			}else {
				return r.log(base.render(r), r.bracket(child.render(r)));
			}
		}
	}

	@Override
	public void replaceChild(CleaningExpression oldExp, CleaningExpression newExp) {
		if (oldExp == base) {
			base = newExp;
		} else {
			child = newExp;
		}
		newExp.setParent(this);
	}

	@Override
	public boolean canNegate() {
		if (base.getType() == CleaningExpressionType.MULTIPLY && ((CleaningMultiply) base).canInvert()) {
			return true;
		} else if (child.getType() == CleaningExpressionType.MULTIPLY && ((CleaningMultiply) child).canInvert()) {
			return true;
		}
		return false;
	}

	@Override
	public void negate() {
		if (base.getType() == CleaningExpressionType.MULTIPLY && ((CleaningMultiply) base).canInvert()) {
			((CleaningMultiply) base).invert();
		} else if (child.getType() == CleaningExpressionType.MULTIPLY && ((CleaningMultiply) child).canInvert()) {
			((CleaningMultiply) child).invert();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CleaningLog other = (CleaningLog) obj;
		if (!child.equals(other.child))
			return false;
		if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public double asDouble() {
		return Math.log(child.asDouble()) / Math.log(base.asDouble());
	}

	@Override
	public CleaningLog clone() {
		CleaningLog c = new CleaningLog(base.clone(), child.clone());
		return c;
	}
}
