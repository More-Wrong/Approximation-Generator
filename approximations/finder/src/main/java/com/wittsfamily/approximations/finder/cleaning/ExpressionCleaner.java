package com.wittsfamily.approximations.finder.cleaning;

import com.wittsfamily.approximations.generator.BinaryFunctionExpression;
import com.wittsfamily.approximations.generator.Constant;
import com.wittsfamily.approximations.generator.ConstantExpression;
import com.wittsfamily.approximations.generator.Expression;
import com.wittsfamily.approximations.generator.UnaryFunctionExpression;

public class ExpressionCleaner {
	public CleaningRoot clean(Expression e) {
		CleaningExpression ce = parseExpression(e);
		CleaningRoot r = new CleaningRoot(ce);
		boolean convertPowers = true;
		int i = 0;
		do {
			//System.out.println("4: " + r.toLaTeX());
			do {
				do {
					i++;
					while (r.cleanOne(convertPowers) && i < 10)
						i++;
					//System.out.println("1: " + r.toLaTeX());
				} while (r.cleanTwo() && i < 10);
				//System.out.println("2: " + r.toLaTeX());
			} while (r.cleanThree() && i < 10);
			//System.out.println("3: " + r.toLaTeX());
			convertPowers = false;
		} while ((i < 20 || r.cleanFour()) && r.cleanFour() && i < 10);
		while (r.cleanOne(convertPowers) && i < 10)
			i++;
		//System.out.println("Clean complete...");
		return r;
	}

	private CleaningExpression parseExpression(Expression e) {
		if (e.isBinaryFunction()) {
			switch (((BinaryFunctionExpression) e).getFunction()) {
			case ADD:
				CleaningAdd ca = new CleaningAdd();
				ca.addExpression(parseExpression(((BinaryFunctionExpression) e).getA()));
				ca.addExpression(parseExpression(((BinaryFunctionExpression) e).getB()));
				return ca;
			case DIVIDE:
				CleaningMultiply cd = new CleaningMultiply();
				cd.addExpression(parseExpression(((BinaryFunctionExpression) e).getA()));
				cd.addExpression(new CleaningInvert(parseExpression(((BinaryFunctionExpression) e).getB())));
				return cd;
			case LOG:
				return new CleaningLog(parseExpression(((BinaryFunctionExpression) e).getA()),
						parseExpression(((BinaryFunctionExpression) e).getB()));
			case MULTIPLY:
				CleaningMultiply cm = new CleaningMultiply();
				cm.addExpression(parseExpression(((BinaryFunctionExpression) e).getA()));
				cm.addExpression(parseExpression(((BinaryFunctionExpression) e).getB()));
				return cm;
			case POWER:
				return new CleaningPower(parseExpression(((BinaryFunctionExpression) e).getA()),
						parseExpression(((BinaryFunctionExpression) e).getB()));
			case SUBTRACT:
				CleaningAdd cs = new CleaningAdd();
				cs.addExpression(parseExpression(((BinaryFunctionExpression) e).getA()));
				cs.addExpression(new CleaningNegate(parseExpression(((BinaryFunctionExpression) e).getB())));
				return cs;
			case UP:
				return new CleaningUp(parseExpression(((BinaryFunctionExpression) e).getA()),
						(CleaningInteger) parseExpression(((BinaryFunctionExpression) e).getB()));
			default:
				throw new IllegalArgumentException("Unknown Binary Function: " + e);

			}
		} else if (e.isUnaryFunction()) {
			switch (((UnaryFunctionExpression) e).getFunction()) {
			case EXP:
				return new CleaningPower(new CleaningReal(Constant.E),
						parseExpression(((UnaryFunctionExpression) e).getChild()));
			case ROOT:
				return new CleaningPower(parseExpression(((UnaryFunctionExpression) e).getChild()),
						new CleaningInvert(new CleaningInteger(2)));
			case SQUARE:
				return new CleaningPower(parseExpression(((UnaryFunctionExpression) e).getChild()),
						new CleaningInteger(2));
			case LN:
				return new CleaningLog(new CleaningReal(Constant.E),
						parseExpression(((UnaryFunctionExpression) e).getChild()));
			case INVERSE:
				return new CleaningInvert(parseExpression(((UnaryFunctionExpression) e).getChild()));
			case NEGATE:
				return new CleaningNegate(parseExpression(((UnaryFunctionExpression) e).getChild()));
			case FACTORIAL:
				return new CleaningFactorial(
						(CleaningInteger) parseExpression(((UnaryFunctionExpression) e).getChild()));
			case SIN:
			case SINH:
			case TAN:
			case TANH:
			case ARCCOS:
			case ARCOSH:
			case ARCSIN:
			case ARCTAN:
			case ARSINH:
			case ARTANH:
			case COS:
			case COSH:
				return new CleaningFunction(((UnaryFunctionExpression) e).getFunction(),
						parseExpression(((UnaryFunctionExpression) e).getChild()));
			default:
				throw new IllegalArgumentException("Unknown Unary Function: " + e);
			}
		} else {
			switch (((ConstantExpression) e).getConst()) {
			case E:
			case PI:
			case GOLDEN_RATIO:
				return new CleaningReal(((ConstantExpression) e).getConst());
			default:
				return new CleaningInteger((int) ((ConstantExpression) e).getConst().getValue());
			}
		}

	}
}
