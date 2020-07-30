package com.wittsfamily.approximations.finder.cleaning;

import java.util.List;

import com.wittsfamily.approximations.finder.ExpressionRenderer;

public class CleaningInvert implements CleaningExpression {
	private CleaningExpression child;
	private CleaningExpression parent;

	public CleaningInvert(CleaningExpression child) {
		this.parent = null;
		this.child = child;
		child.setParent(this);
	}

	@Override
	public CleaningExpressionType getType() {
		return CleaningExpressionType.INVERT;
	}

	@Override
	public List<CleaningExpression> getExpressions() {
		return List.of(child);
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
		if (child.getType() == CleaningExpressionType.NEGATE) {
			parent.replaceChild(this, new CleaningNegate(this));
			child = child.getExpressions().get(0);
			child.setParent(this);
			return true;
		} else if (child.getType() == CleaningExpressionType.INVERT) {
			parent.replaceChild(this, child.getExpressions().get(0));
			return true;
		} else if (child.getType() == CleaningExpressionType.MULTIPLY && ((CleaningMultiply) child).canInvert()) {
			((CleaningMultiply) child).invert();
			child.setParent(parent);
			parent.replaceChild(this, child);
			return true;
		} else if (child.getType() == CleaningExpressionType.INTEGER && ((CleaningInteger) child).asInt() == 1) {
			parent.replaceChild(this, child);
			return true;
		}
		return child.cleanOne(convertPowers);
	}

	@Override
	public boolean cleanTwo() {
		return child.cleanTwo();
	}

	@Override
	public boolean cleanThree() {
		return child.cleanThree();
	}

	@Override
	public boolean cleanFour() {
		return child.cleanFour();
	}

	@Override
	public String render(ExpressionRenderer r) {
		if (child.getType() == CleaningExpressionType.INTEGER || child.getType() == CleaningExpressionType.REAL) {
			return r.slantedFraction(r.integer(1), child.render(r));
		} else {
			return r.fraction(r.integer(1), child.render(r));
		}
	}

	@Override
	public void replaceChild(CleaningExpression oldExp, CleaningExpression newExp) {
		child = newExp;
		newExp.setParent(this);
	}

	@Override
	public boolean canNegate() {
		return child.canNegate();
	}

	@Override
	public void negate() {
		child.negate();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CleaningInvert other = (CleaningInvert) obj;
		if (!child.equals(other.child))
			return false;
		return true;
	}

	@Override
	public double asDouble() {
		return 1 / child.asDouble();
	}

	@Override
	public CleaningInvert clone() {
		CleaningInvert c = new CleaningInvert(child.clone());
		return c;
	}
}
