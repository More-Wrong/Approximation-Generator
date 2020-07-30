package com.wittsfamily.approximations.finder.cleaning;

import java.util.Collections;
import java.util.List;

import com.wittsfamily.approximations.finder.ExpressionRenderer;
import com.wittsfamily.approximations.generator.Constant;

public class CleaningReal implements CleaningExpression {
	private CleaningExpression parent;
	private Constant value;

	public CleaningReal(Constant value) {
		this.parent = null;
		this.value = value;
	}

	public Constant asConstant() {
		return value;
	}

	@Override
	public CleaningExpressionType getType() {
		return CleaningExpressionType.REAL;
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
		switch (value) {
		case E:
			return r.e();
		case PI:
			return r.pi();
		case GOLDEN_RATIO:
			return r.gr();

		default:
			return "";
		}
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
		return value.getValue();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CleaningReal other = (CleaningReal) obj;
		if (value != other.value)
			return false;
		return true;
	}

	@Override
	public CleaningReal clone() {
		CleaningReal f = new CleaningReal(value);
		return f;
	}

}
