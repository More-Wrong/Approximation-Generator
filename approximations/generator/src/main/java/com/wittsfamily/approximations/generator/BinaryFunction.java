package com.wittsfamily.approximations.generator;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import org.apfloat.Apfloat;

import com.wittsfamily.approximations.generator.ApfloatWithInf;

public enum BinaryFunction {
	ADD((a, b) -> 10, (a, o) -> o != UnaryFunction.NEGATE, (a, o) -> o != UnaryFunction.NEGATE, 10, (a, b) -> a + b,
			0x36, 6, (t, b) -> t - b, (t, a) -> t - a, (a, b) -> ApfloatWithInf.add(a, b),
			(t, b) -> ApfloatWithInf.subtract(t, b), (t, a) -> ApfloatWithInf.subtract(t, a)),
	SUBTRACT((a, b) -> 10, (a, o) -> o != UnaryFunction.NEGATE, (a, o) -> o != UnaryFunction.NEGATE, 10,
			(a, b) -> a - b, 0x37, 6, (t, b) -> t + b, (t, a) -> a - t, (a, b) -> ApfloatWithInf.subtract(a, b),
			(t, b) -> ApfloatWithInf.add(t, b), (t, a) -> ApfloatWithInf.subtract(a, t)),
	MULTIPLY((a, b) -> 10, (a, o) -> o != Constant.ONE && o != UnaryFunction.NEGATE && o != UnaryFunction.INVERSE,
			(a, o) -> o != Constant.ONE && o != UnaryFunction.NEGATE && o != UnaryFunction.INVERSE, 10, (a, b) -> a * b,
			0x38, 6, (t, b) -> t / b, (t, a) -> t / a, (a, b) -> ApfloatWithInf.multiply(a, b),
			(t, b) -> ApfloatWithInf.divide(t, b), (t, a) -> ApfloatWithInf.divide(t, a)),
	DIVIDE((a, b) -> Math.abs(b.asDouble()) < 1.0e-300 ? 10000 : 10,
			(a, o) -> o != Constant.ONE && o != UnaryFunction.NEGATE && o != UnaryFunction.INVERSE,
			(a, o) -> o != Constant.ONE && o != UnaryFunction.NEGATE && o != UnaryFunction.INVERSE, 10, (a, b) -> a / b,
			0x39, 6, (t, b) -> t * b, (t, a) -> a / t, (a, b) -> ApfloatWithInf.divide(a, b),
			(t, b) -> ApfloatWithInf.multiply(t, b), (t, a) -> ApfloatWithInf.divide(a, t)),
	POWER((a, b) -> a.asDouble() < 0 && !b.isConstant() ? 10000 : 10,
			(a, o) -> o != Constant.ONE && o != Constant.E && o != UnaryFunction.INVERSE,
			(a, o) -> o != Constant.ONE && o != Constant.TWO && o != UnaryFunction.NEGATE, 10, (a, b) -> Math.pow(a, b),
			0x3A, 6, (t, b) -> Math.pow(t, 1 / b), (t, a) -> Math.log(t) / Math.log(a),
			(a, b) -> ApfloatWithInf.pow(a, b), (t, b) -> ApfloatWithInf.pow(t, ApfloatWithInf.invert(b)),
			(t, a) -> ApfloatWithInf.log(a, t)),
	LOG((a, b) -> b.asDouble() < 1.0e-300 || a.asDouble() < 1.0e-300 || a.asDouble() == 1 ? 10000 : 10,
			(a, o) -> o != Constant.ONE && o != Constant.E && o != UnaryFunction.INVERSE && o != BinaryFunction.POWER,
			(a, o) -> o != Constant.ONE && o != UnaryFunction.INVERSE && o != BinaryFunction.POWER, 10,
			(a, b) -> Math.log(b) / Math.log(a), 0x3B, 6, (t, b) -> Math.pow(b, t), (t, a) -> Math.pow(a, 1 / t),
			(a, b) -> ApfloatWithInf.log(a, b), (t, b) -> ApfloatWithInf.pow(b, ApfloatWithInf.invert(t)),
			(t, a) -> ApfloatWithInf.pow(a, t)),
	UP((a, b) -> b.isConstant() && a.asDouble() > 0 ? 15 : 10000, (a, o) -> o != Constant.ONE,
			(a, o) -> o.getClass() == Constant.class && o != Constant.E && o != Constant.PI && o != Constant.E
					&& o != Constant.GOLDEN_RATIO && o != Constant.ONE,
			15, (ad, bd) -> {
				double a = ad;
				double b = bd;
				if (!Double.isFinite(b)) {
					return Double.NaN;
				}
				double current = a;
				for (int i = 1; i < b; i++) {
					current = Math.pow(a, current);
				}
				return current;
			}, 0x1F5, 9, (t, b) -> Double.NaN, (t, a) -> Double.NaN, (a, b) -> {
				Apfloat current = ApfloatWithInf.abs(a);
				for (int i = 1; i < b.intValue(); i++) {
					current = ApfloatWithInf.pow(ApfloatWithInf.abs(a), current);
				}
				return current;
			}, (t, b) -> ApfloatWithInf.NaN, (t, a) -> ApfloatWithInf.NaN);

	private final BiFunction<Expression, Expression, Integer> cost;
	private final BiPredicate<BinaryFunction, Object> canBeChildA;
	private final BiPredicate<BinaryFunction, Object> canBeChildB;
	private final int minCost;
	private final BiFunction<Double, Double, Double> function;
	private final int store;
	private final int length;
	private final BiFunction<Double, Double, Double> undoA;
	private final BiFunction<Double, Double, Double> undoB;
	private final BiFunction<Apfloat, Apfloat, Apfloat> accurateFunction;
	private final BiFunction<Apfloat, Apfloat, Apfloat> accurateUndoA;
	private final BiFunction<Apfloat, Apfloat, Apfloat> accurateUndoB;

	private BinaryFunction(BiFunction<Expression, Expression, Integer> cost,
			BiPredicate<BinaryFunction, Object> canBeChildA, BiPredicate<BinaryFunction, Object> canBeChildB,
			int minCost, BiFunction<Double, Double, Double> function, int store, int length,
			BiFunction<Double, Double, Double> undoA, BiFunction<Double, Double, Double> undoB,
			BiFunction<Apfloat, Apfloat, Apfloat> accurateFunction, BiFunction<Apfloat, Apfloat, Apfloat> accurateUndoA,
			BiFunction<Apfloat, Apfloat, Apfloat> accurateUndoB) {
		this.cost = cost;
		this.canBeChildA = canBeChildA;
		this.canBeChildB = canBeChildB;
		this.minCost = minCost;
		this.function = function;
		this.store = store;
		this.length = length;
		this.undoA = undoA;
		this.undoB = undoB;
		this.accurateFunction = accurateFunction;
		this.accurateUndoA = accurateUndoA;
		this.accurateUndoB = accurateUndoB;
	}

	public boolean canBeChildA(Object o) {
		return canBeChildA.test(this, o);
	}

	public boolean canBeChildB(Object o) {
		return canBeChildB.test(this, o);
	}

	public int getCost(Expression a, Expression b) {
		return cost.apply(a, b);
	}

	public double getValue(double a, double b) {
		return function.apply(a, b);
	}

	public int getMinCost() {
		return minCost;
	}

	public int getStore() {
		return store;
	}

	public int getLength() {
		return length;
	}

	public double undoA(double t, double b) {
		return undoA.apply(t, b);
	}

	public double undoB(double t, double a) {
		return undoB.apply(t, a);
	}

	public Apfloat getAccurateValue(Apfloat a, Apfloat b) {
		return accurateFunction.apply(a, b);
	}

	public Apfloat undoAccurateA(Apfloat t, Apfloat b) {
		return accurateUndoA.apply(t, b);
	}

	public Apfloat undoAccurateB(Apfloat t, Apfloat a) {
		return accurateUndoB.apply(t, a);
	}
}
