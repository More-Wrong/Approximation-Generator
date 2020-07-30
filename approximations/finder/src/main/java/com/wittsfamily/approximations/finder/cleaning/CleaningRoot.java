package com.wittsfamily.approximations.finder.cleaning;

import java.util.List;

import com.wittsfamily.approximations.finder.ExpressionRenderer;

public class CleaningRoot implements CleaningExpression {
	private CleaningExpression child;

	public CleaningRoot(CleaningExpression child) {
		child.setParent(this);
		this.child = child;
	}

	@Override
	public CleaningExpression getParent() {
		return null;
	}

	@Override
	public void setParent(CleaningExpression parent) {
	}

	@Override
	public CleaningExpressionType getType() {
		return CleaningExpressionType.ROOT;
	}

	@Override
	public boolean cleanOne(boolean convertPowers) {
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
		return child.render(r);
	}

	@Override
	public List<CleaningExpression> getExpressions() {
		return List.of(child);
	}

	@Override
	public void replaceChild(CleaningExpression oldExp, CleaningExpression newExp) {
		child = newExp;
		child.setParent(this);
	}

	@Override
	public boolean canNegate() {
		return false;
	}

	@Override
	public void negate() {
	}

	@Override
	public double asDouble() {
		return child.asDouble();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CleaningRoot other = (CleaningRoot) obj;
		if (child == null) {
			if (other.child != null)
				return false;
		} else if (!child.equals(other.child))
			return false;
		return true;
	}

	@Override
	public CleaningRoot clone() {
		CleaningRoot c = new CleaningRoot(child.clone());
		return c;
	}

}
