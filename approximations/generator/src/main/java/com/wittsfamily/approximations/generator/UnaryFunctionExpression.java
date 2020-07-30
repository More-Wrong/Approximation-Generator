package com.wittsfamily.approximations.generator;

import org.apfloat.Apfloat;

public class UnaryFunctionExpression extends Expression {
	private final UnaryFunction function;
	private Expression a = null;

	public UnaryFunctionExpression(UnaryFunction function) {
		this.function = function;
	}

	public void setChild(Expression a) {
		this.a = a;
		setValue(function.getValue(a.asDouble()));
		apValue = null;
	}

	public UnaryFunction getFunction() {
		return function;
	}

	public Expression getChild() {
		return a;
	}

	@Override
	public boolean isBinaryFunction() {
		return false;
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public boolean isUnaryFunction() {
		return true;
	}

	@Override
	public int getCost() {
		return function.getCost(a) + a.getCost();
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(function.name().toLowerCase());
		buffer.append('(');
		buffer.append(a.toString());
		buffer.append(')');
		return buffer.toString();
	}

	@Override
	public int writeToBytes(byte[] target, int pos) {
		pos = a.writeToBytes(target, pos);
		if (8 - pos % 8 > function.getLength()) {
			target[pos / 8] |= function.getStore() << (8 - pos % 8 - function.getLength());
		} else {
			target[pos / 8] |= (function.getStore() >>> (pos % 8 + function.getLength() - 8)) & 0xFF;
			target[pos / 8 + 1] |= (function.getStore() << (16 - pos % 8 - function.getLength())) & 0xFF;
		}
		return pos + function.getLength();
	}

	public double undoFunction(double target) {
		return function.undoFunction(target);
	}

	@Override
	public Apfloat asApfloat() {
		if (apValue == null) {
			apValue = function.getAccurateValue(a.asApfloat());
		}
		return apValue;
	}

	public Apfloat undoApfloat(Apfloat target) {
		return function.accurateUndoFunction(target);
	}

}
