package com.wittsfamily.approximations.finder.cleaning;

import java.util.List;

import com.wittsfamily.approximations.finder.ExpressionRenderer;
import com.wittsfamily.approximations.generator.BinaryFunction;

public class CleaningUp implements CleaningExpression {
	private CleaningInteger power;
	private CleaningExpression target;
	private CleaningExpression parent;

	public CleaningUp(CleaningExpression target, CleaningInteger power) {
		this.parent = null;
		this.power = power;
		power.setParent(this);
		this.target = target;
		target.setParent(this);
	}

	public CleaningInteger getPower() {
		return power;
	}

	public CleaningExpression getTarget() {
		return target;
	}

	@Override
	public CleaningExpressionType getType() {
		return CleaningExpressionType.UP;
	}

	@Override
	public List<CleaningExpression> getExpressions() {
		return List.of(power, target);
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
		if (power.asInt() == 0) {
			parent.replaceChild(this, new CleaningInteger(1));
			return true;
		} else if (power.asInt() == 1) {
			parent.replaceChild(this, target);
			return true;
		} else if (power.asInt() == 2) {
			CleaningPower pow = new CleaningPower(target.clone(), target);
			parent.replaceChild(this, pow);
			return true;
		} else if (target.getType() == CleaningExpressionType.INTEGER && ((CleaningInteger) target).asInt() == 0) {
			parent.replaceChild(this, new CleaningInteger(0));
		} else if (target.getType() == CleaningExpressionType.INTEGER && ((CleaningInteger) target).asInt() == 1) {
			parent.replaceChild(this, new CleaningInteger(1));
		}
		return target.cleanOne(convertPowers);
	}

	@Override
	public boolean cleanTwo() {
		return target.cleanTwo();
	}

	@Override
	public boolean cleanThree() {
		return target.cleanThree();
	}

	@Override
	public boolean cleanFour() {
		return target.cleanFour();
	}

	@Override
	public String render(ExpressionRenderer r) {
		return r.tetrate(r.bracket(target.render(r)), r.integer(power.asInt()));
	}

	@Override
	public void replaceChild(CleaningExpression oldExp, CleaningExpression newExp) {
		if (oldExp == power) {
			power = (CleaningInteger) newExp;
		} else {
			target = newExp;
		}
		newExp.setParent(this);
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
		CleaningUp other = (CleaningUp) obj;
		if (!power.equals(other.power))
			return false;
		if (!target.equals(other.target))
			return false;
		return true;
	}

	@Override
	public double asDouble() {
		return BinaryFunction.UP.getValue(target.asDouble(), power.asDouble());
	}

	@Override
	public CleaningUp clone() {
		CleaningUp c = new CleaningUp(target.clone(), power.clone());
		return c;
	}
}
