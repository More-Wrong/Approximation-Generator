package com.wittsfamily.approximations.generator;

import org.apfloat.Apfloat;

public class ConstantExpression extends Expression {
	private final Constant constant;

	public ConstantExpression(Constant value) {
		super.setValue(value.getValue());
		this.constant = value;
		apValue = value.getPrecise();
	}

	public Constant getConst() {
		return constant;
	}

	@Override
	public boolean isBinaryFunction() {
		return false;
	}

	@Override
	public boolean isConstant() {
		return true;
	}

	@Override
	public boolean isUnaryFunction() {
		return false;
	}

	@Override
	public int getCost() {
		return constant.getCost();
	}

	@Override
	public String toString() {
		return constant.name().toLowerCase();
	}

	@Override
	public int writeToBytes(byte[] target, int pos) {
		if (8 - pos % 8 > constant.getLength()) {
			target[pos / 8] |= constant.getStore() << (8 - pos % 8 - constant.getLength());
		} else {
			target[pos / 8] |= (constant.getStore() >>> (pos % 8 + constant.getLength() - 8)) & 0xFF;
			target[pos / 8 + 1] |= (constant.getStore() << (16 - pos % 8 - constant.getLength())) & 0xFF;
		}
		return pos + constant.getLength();
	}

	@Override
	public Apfloat asApfloat() {
		return apValue;
	}

}
