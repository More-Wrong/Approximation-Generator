package com.wittsfamily.approximations.finder.cleaning;

import java.util.List;

import com.wittsfamily.approximations.finder.ExpressionRenderer;

public class CleaningNegate implements CleaningExpression {
	private CleaningExpression child;
	private CleaningExpression parent;

	public CleaningNegate(CleaningExpression child) {
		this.parent = null;
		this.child = child;
		child.setParent(this);
	}

	@Override
	public CleaningExpressionType getType() {
		return CleaningExpressionType.NEGATE;
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
			parent.replaceChild(this, child.getExpressions().get(0));
			return true;
		}
		if (child.getType() == CleaningExpressionType.INTEGER && ((CleaningInteger) child).asInt() == 0) {
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
		if (child.canNegate()) {
			child.negate();
			parent.replaceChild(this, child);
			return true;
		}
		return child.cleanFour();
	}

	@Override
	public String render(ExpressionRenderer r) {
		if (child.getType() == CleaningExpressionType.ADD) {
			return r.negate(r.bracket(child.render(r)));
		} else {
			return r.negate(child.render(r));
		}
	}

	@Override
	public void replaceChild(CleaningExpression oldExp, CleaningExpression newExp) {
		child = newExp;
		child.setParent(this);
	}

	@Override
	public boolean canNegate() {
		return true;
	}

	@Override
	public void negate() {
		child.setParent(parent);
		parent.replaceChild(this, child);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CleaningNegate other = (CleaningNegate) obj;
		if (!child.equals(other.child))
			return false;
		return true;
	}

	@Override
	public double asDouble() {
		return -child.asDouble();
	}

	@Override
	public CleaningNegate clone() {
		CleaningNegate c = new CleaningNegate(child.clone());
		return c;
	}
}
