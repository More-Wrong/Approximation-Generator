package com.wittsfamily.approximations.finder.cleaning;

import java.util.Collections;
import java.util.List;

import com.wittsfamily.approximations.finder.ExpressionRenderer;

public class CleaningInteger implements CleaningExpression {
	private CleaningExpression parent;
	private int value;

	public CleaningInteger(int value) {
		this.parent = null;
		this.value = value;
	}

	public int asInt() {
		return value;
	}

	@Override
	public CleaningExpressionType getType() {
		return CleaningExpressionType.INTEGER;
	}

	@Override
	public List<CleaningExpression> getExpressions() {
		return Collections.emptyList();
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
		return false;
	}

	@Override
	public boolean cleanTwo() {
		return false;
	}

	@Override
	public boolean cleanThree() {
		return false;
	}

	@Override
	public boolean cleanFour() {
		return false;
	}

	@Override
	public String render(ExpressionRenderer r) {
		return r.integer(value);
	}

	@Override
	public void replaceChild(CleaningExpression oldExp, CleaningExpression newExp) {
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
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CleaningInteger other = (CleaningInteger) obj;
		if (value != other.value)
			return false;
		return true;
	}

	@Override
	public CleaningInteger clone() {
		CleaningInteger f = new CleaningInteger(value);
		return f;
	}

}
