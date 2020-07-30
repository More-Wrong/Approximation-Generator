package com.wittsfamily.approximations.generator;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

public enum Constant {
	ONE(1, 1, 0, 4, new Apfloat(1, 50)), TWO(1, 2, 0x1, 4, new Apfloat(2, 50)),
	THREE(4, 3, 0x10, 5, new Apfloat(3, 50)), FOUR(4, 4, 0x17, 5, new Apfloat(4, 50)),
	FIVE(3, 5, 0x11, 5, new Apfloat(5, 50)), SIX(4, 6, 0x12, 5, new Apfloat(6, 50)),
	SEVEN(5, 7, 0x18, 5, new Apfloat(7, 50)), EIGHT(4, 8, 0x13, 5, new Apfloat(8, 50)),
	NINE(6, 9, 0x19, 5, new Apfloat(9, 50)), TEN(2, 10, 0x5, 4, new Apfloat(10, 50)),
	E(1, Math.E, 0x3, 4, ApfloatMath.exp(new Apfloat(1, 50))), PI(1, Math.PI, 0x4, 4, ApfloatMath.pi(50)),
	GOLDEN_RATIO(1, (1 + Math.sqrt(5)) / 2, 0x2, 4,
			ApfloatMath.sqrt(new Apfloat(5, 50)).add(new Apfloat(1, 50)).divide(new Apfloat(2, 50))),
	ELEVEN(9, 11, 0x3C, 6, new Apfloat(11, 50)), THIRTEEN(9, 13, 0x7A, 7, new Apfloat(13, 50));

	private final int cost;
	private final double value;
	private final int store;
	private final int length;
	private final Apfloat precise;

	private Constant(int cost, double value, int store, int length, Apfloat precise) {
		this.cost = cost;
		this.value = value;
		this.store = store;
		this.length = length;
		this.precise = precise;
	}

	public int getCost() {
		return cost;
	}

	public double getValue() {
		return value;
	}

	public int getStore() {
		return store;
	}

	public int getLength() {
		return length;
	}

	public Apfloat getPrecise() {
		return precise;
	}
}
