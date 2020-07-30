package com.wittsfamily.approximations.generator;

import org.apfloat.Apfloat;

public class BinaryFunctionExpression extends Expression {
	private final BinaryFunction function;
	private Expression a = null;
	private Expression b = null;

	public BinaryFunctionExpression(BinaryFunction function) {
		this.function = function;
	}

	public void setA(Expression a) {
		apValue = null;
		this.a = a;
		if (b != null && a != null) {
			super.setValue(function.getValue(a.asDouble(), b.asDouble()));
		}
	}

	public void setB(Expression b) {
		apValue = null;
		this.b = b;
		if (b != null && a != null) {
			super.setValue(function.getValue(a.asDouble(), b.asDouble()));
		}
	}

	public BinaryFunction getFunction() {
		return function;
	}

	public Expression getA() {
		return a;
	}

	public Expression getB() {
		return b;
	}

	@Override
	public boolean isBinaryFunction() {
		return true;
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public boolean isUnaryFunction() {
		return false;
	}

	@Override
	public int getCost() {
		return function.getCost(a, b) + a.getCost() + b.getCost();
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append('(');
		buffer.append(a.toString());
		buffer.append(')');
		buffer.append(function.name().toLowerCase());
		buffer.append('(');
		buffer.append(b.toString());
		buffer.append(')');
		return buffer.toString();
	}

	@Override
	public int writeToBytes(byte[] target, int pos) {
		pos = a.writeToBytes(target, pos);
		pos = b.writeToBytes(target, pos);
		if (8 - pos % 8 > function.getLength()) {
			target[pos / 8] |= function.getStore() << (8 - pos % 8 - function.getLength());
		} else {
			target[pos / 8] |= (function.getStore() >>> (pos % 8 + function.getLength() - 8)) & 0xFF;
			target[pos / 8 + 1] |= (function.getStore() << (16 - pos % 8 - function.getLength())) & 0xFF;
		}
		return pos + function.getLength();
	}

	public double undoA(double target) {
		return function.undoA(target, b.asDouble());
	}

	public double undoB(double target) {
		return function.undoB(target, a.asDouble());
	}

	@Override
	public Apfloat asApfloat() {
		if (apValue == null) {
			apValue = function.getAccurateValue(a.asApfloat(), b.asApfloat());
		}
		return apValue;
	}

	public Apfloat undoApfloatA(Apfloat target) {
		return function.undoAccurateA(target, b.asApfloat());
	}

	public Apfloat undoApfloatB(Apfloat target) {
		return function.undoAccurateB(target, b.asApfloat());
	}
}
