package com.wittsfamily.approximations.finder.cleaning;

import java.util.List;

import com.wittsfamily.approximations.finder.ExpressionRenderer;
import com.wittsfamily.approximations.generator.UnaryFunction;

public class CleaningFunction implements CleaningExpression {
	private final UnaryFunction func;
	private CleaningExpression parent;
	private CleaningExpression child;

	public CleaningFunction(UnaryFunction func, CleaningExpression child) {
		this.func = func;
		this.child = child;
		child.setParent(this);
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
	public CleaningExpressionType getType() {
		return CleaningExpressionType.FUNCTION;
	}

	@Override
	public boolean cleanOne(boolean convertPowers) {
		boolean didSomething = false;
		if (child.getType() == CleaningExpressionType.NEGATE) {
			switch (func) {
			case TAN:
			case ARCTAN:
			case SIN:
			case ARCSIN:
			case SINH:
			case ARSINH:
			case TANH:
			case ARTANH:
				parent.replaceChild(this, new CleaningNegate(this));
			case COS:
			case COSH:
				child = child.getExpressions().get(0);
				child.setParent(this);
				didSomething = true;
				break;
			default:
				break;
			}
		}
		return child.cleanOne(convertPowers) || didSomething;
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
		b.append(" ");
		b.append(r.function(func));
		b.append(r.bracket(child.render(r)));
		return b.toString();
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
		switch (func) {
		case TAN:
		case ARCTAN:
		case SIN:
		case ARCSIN:
		case SINH:
		case ARSINH:
		case TANH:
		case ARTANH:
			return child.canNegate();
		default:
			return false;
		}
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
		CleaningFunction other = (CleaningFunction) obj;
		if (!child.equals(other.child))
			return false;
		if (!func.equals(other.func))
			return false;
		return true;
	}

	@Override
	public double asDouble() {
		return func.getValue(child.asDouble());
	}

	@Override
	public CleaningFunction clone() {
		CleaningFunction f = new CleaningFunction(func, child.clone());
		return f;
	}
}
