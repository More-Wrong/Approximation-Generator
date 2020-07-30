package com.wittsfamily.approximations.generator;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.apfloat.Apfloat;

import com.wittsfamily.approximations.generator.ApfloatWithInf;

public enum UnaryFunction {
	INVERSE((i, a) -> (a.asDouble() == 0.0 || Math.abs(a.asDouble()) == 1.0 ? 10000 : 4),
			(i, o) -> o != i && o != Constant.TWO && o != Constant.ONE && o != BinaryFunction.LOG
					&& o != BinaryFunction.DIVIDE,
			4, a -> 1 / a, 0x7, 4, a -> 1 / a, f -> ApfloatWithInf.invert(f), f -> ApfloatWithInf.invert(f)),
	LN((l, a) -> a.asDouble() <= 0 ? 10000 : 6,
			(l, o) -> o != INVERSE && o != BinaryFunction.MULTIPLY && o != BinaryFunction.DIVIDE
					&& o != BinaryFunction.POWER,
			6, a -> Math.log(a), 0x1A, 5, a -> Math.exp(a), f -> ApfloatWithInf.ln(f), f -> ApfloatWithInf.exp(f)),
	NEGATE((n, a) -> 3, (n, o) -> o != n && o != INVERSE && o != BinaryFunction.SUBTRACT, 3, a -> -a, 0x6, 4, a -> -a,
			f -> ApfloatWithInf.negate(f), f -> ApfloatWithInf.negate(f)),
	EXP((l, a) -> 5, (l, o) -> o != NEGATE && o != Constant.ONE && o != LN, 6, a -> Math.exp(a), 0x15, 5,
			a -> Math.log(a), f -> ApfloatWithInf.exp(f), f -> ApfloatWithInf.ln(f)),
	SQUARE((l, a) -> 5, (l, o) -> o != Constant.ONE && o != INVERSE && o != NEGATE, 6, a -> a * a, 0x14, 5,
			a -> Math.sqrt(a), f -> ApfloatWithInf.pow(f, 2), f -> ApfloatWithInf.sqrt(f)),
	ROOT((l, a) -> a.asDouble() < 0 ? 10000 : 5, (l, o) -> o != Constant.ONE && o != INVERSE && o != SQUARE, 6,
			a -> Math.sqrt(a), 0x16, 5, a -> a * a, f -> ApfloatWithInf.sqrt(f), f -> ApfloatWithInf.pow(f, 2)),
	SIN((s, a) -> a.asDouble() > 3100 ? 10000 : 15, (s, o) -> o != NEGATE, 15, a -> Math.sin(a), 0xF8, 8,
			a -> Math.asin(a), f -> ApfloatWithInf.sin(f), f -> ApfloatWithInf.asin(f)),
	COS((s, a) -> a.asDouble() > 3100 ? 10000 : 15, (s, o) -> o != NEGATE, 15, a -> Math.cos(a), 0xF9, 8,
			a -> Math.acos(a), f -> ApfloatWithInf.cos(f), f -> ApfloatWithInf.acos(f)),
	TAN((s, a) -> a.asDouble() > 3100 ? 10000 : 17, (s, o) -> o != NEGATE, 17, a -> Math.tan(a), 0x1F4, 9,
			a -> Math.atan(a), f -> ApfloatWithInf.tan(f), f -> ApfloatWithInf.atan(f)),
	ARCSIN((s, a) -> a.asDouble() > 1 || a.asDouble() < -1 ? 10000 : 15, (s, o) -> o != SIN, 15, a -> Math.asin(a),
			0x1F6, 9, a -> Math.sin(a), f -> ApfloatWithInf.asin(f), f -> ApfloatWithInf.sin(f)),
	ARCCOS((s, a) -> a.asDouble() > 1 || a.asDouble() < -1 ? 10000 : 15, (s, o) -> o != COS, 15, a -> Math.acos(a),
			0x1F7, 9, a -> Math.cos(a), f -> ApfloatWithInf.acos(f), f -> ApfloatWithInf.cos(f)),
	ARCTAN((s, a) -> 18, (s, o) -> o != TAN, 18, a -> Math.atan(a), 0x1F8, 9, a -> Math.tan(a),
			f -> ApfloatWithInf.atan(f), f -> ApfloatWithInf.tan(f)),
	SINH((s, a) -> 20, (s, o) -> o != NEGATE, 20, a -> Math.sinh(a), 0x1F9, 9, a -> Math.log(a + Math.sqrt(a * a + 1)),
			f -> ApfloatWithInf.sinh(f), f -> ApfloatWithInf.asinh(f)),
	COSH((s, a) -> 20, (s, o) -> o != NEGATE, 20, a -> Math.cosh(a), 0x1FA, 9, a -> Math.log(a + Math.sqrt(a * a - 1)),
			f -> ApfloatWithInf.cosh(f), f -> ApfloatWithInf.acosh(f)),
	TANH((s, a) -> 25, (s, o) -> o != NEGATE, 25, a -> Math.tanh(a), 0x1FB, 9, a -> Math.log((1 + a) / (1 - a)) / 2,
			f -> ApfloatWithInf.tanh(f), f -> ApfloatWithInf.atanh(f)),
	ARSINH((s, a) -> 25, (s, o) -> o != SINH, 25, a -> Math.log(a + Math.sqrt(a * a + 1)), 0x1FC, 9, a -> Math.sinh(a),
			f -> ApfloatWithInf.asinh(f), f -> ApfloatWithInf.sinh(f)),
	ARCOSH((s, a) -> a.asDouble() < 1 ? 10000 : 25, (s, o) -> o != COSH, 25, a -> Math.log(a + Math.sqrt(a * a - 1)),
			0x1FD, 9, a -> Math.cosh(a), f -> ApfloatWithInf.acosh(f), f -> ApfloatWithInf.cosh(f)),
	ARTANH((s, a) -> a.asDouble() > 1 || a.asDouble() < -1 ? 10000 : 25, (s, o) -> o != TANH, 25,
			a -> Math.log((1 + a) / (1 - a)) / 2, 0x1FE, 9, a -> Math.tanh(a), f -> ApfloatWithInf.tanh(f),
			f -> ApfloatWithInf.atanh(f)),
	FACTORIAL(
			(s, a) -> 15, (s, o) -> o.getClass() == Constant.class && o != Constant.E && o != Constant.PI
					&& o != Constant.E && o != Constant.GOLDEN_RATIO && o != Constant.ONE && o != Constant.TWO,
			15, ad -> {
				double a = ad;
				double current = 1;
				for (int i = 2; i <= a; i++) {
					current *= i;
				}
				return current;
			}, 0x7B, 7, a -> Double.NaN, f -> {
				Apfloat current = new Apfloat(1, 50);
				for (int i = 2; i <= f.intValue(); i++) {
					current = ApfloatWithInf.multiply(current, new Apfloat(i, 50));
				}
				return current;
			}, f -> ApfloatWithInf.NaN);

	private final int minCost;
	private final BiFunction<UnaryFunction, Expression, Integer> cost;
	private final BiPredicate<UnaryFunction, Object> canBeChildOp;
	private final Function<Double, Double> function;
	private final int store;
	private final int length;
	private final Function<Double, Double> undo;
	private final Function<Apfloat, Apfloat> accurateFunction;
	private final Function<Apfloat, Apfloat> accurateUndo;

	private UnaryFunction(BiFunction<UnaryFunction, Expression, Integer> cost,
			BiPredicate<UnaryFunction, Object> canBeChildOp, int minCost, Function<Double, Double> function, int store,
			int length, Function<Double, Double> undo, Function<Apfloat, Apfloat> accurateFunction,
			Function<Apfloat, Apfloat> accurateUndo) {
		this.cost = cost;
		this.minCost = minCost;
		this.canBeChildOp = canBeChildOp;
		this.function = function;
		this.store = store;
		this.length = length;
		this.undo = undo;
		this.accurateFunction = accurateFunction;
		this.accurateUndo = accurateUndo;
	}

	public boolean canBeChildOperation(Object o) {
		return canBeChildOp.test(this, o);
	}

	public int getMinCost() {
		return minCost;
	}

	public int getCost(Expression a) {
		return cost.apply(this, a);
	}

	public Function<Double, Double> getFunction() {
		return function;
	}

	public double getValue(double value) {
		return function.apply(value);
	}

	public int getStore() {
		return store;
	}

	public int getLength() {
		return length;
	}

	public double undoFunction(double give) {
		return undo.apply(give);
	}

	public Apfloat getAccurateValue(Apfloat value) {
		return accurateFunction.apply(value);
	}

	public Apfloat accurateUndoFunction(Apfloat value) {
		return accurateUndo.apply(value);
	}
}
