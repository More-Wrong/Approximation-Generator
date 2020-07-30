package com.wittsfamily.approximations.generator;

import org.apfloat.Apfloat;

public abstract class Expression {
	private double value;
	protected Apfloat apValue = null;

	protected void setValue(double value) {
		this.value = value;
	}

	public abstract boolean isBinaryFunction();

	public abstract boolean isUnaryFunction();

	public abstract boolean isConstant();

	public abstract int getCost();

	public double asDouble() {
		return value;
	}

	public abstract Apfloat asApfloat();

	public abstract int writeToBytes(byte[] target, int pos);

}
