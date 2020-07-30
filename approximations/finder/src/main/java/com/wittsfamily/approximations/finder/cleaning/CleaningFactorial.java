package com.wittsfamily.approximations.finder.cleaning;

import java.util.List;

import com.wittsfamily.approximations.finder.ExpressionRenderer;

public class CleaningFactorial implements CleaningExpression {
	private CleaningInteger child;
	private CleaningExpression parent;

	public CleaningFactorial(CleaningInteger child) {
		this.parent = null;
		this.child = child;
		child.setParent(this);
	}

	@Override
	public CleaningExpressionType getType() {
		return CleaningExpressionType.FACTORIAL;
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
		if (child.asInt() == 0 || child.asInt() == 1) {
			parent.replaceChild(this, new CleaningInteger(1));
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
		StringBuilder b = new StringBuilder();
		b.append(child.render(r));
		b.append(r.factorial());
		return b.toString();
	}

	@Override
	public void replaceChild(CleaningExpression oldExp, CleaningExpression newExp) {
		child = (CleaningInteger) newExp;
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
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CleaningFactorial other = (CleaningFactorial) obj;
		if (!child.equals(other.child))
			return false;
		return true;
	}

	@Override
	public double asDouble() {
		double d = 1;
		for (int i = 1; i <= child.asInt(); i++) {
			d *= i;
		}
		return d;
	}

	@Override
	public CleaningFactorial clone() {
		CleaningFactorial f = new CleaningFactorial(new CleaningInteger(child.asInt()));
		return f;
	}
}
