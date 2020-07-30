/**
 * 
 */
function clean(e) {
	var ce = changeToCleaning(e);
	var r = new CleaningRoot(ce);
	var convertPowers = true;
	var i = 0;
	do {
		// console.log("4: " + r.toRendered(new UnicodeRenderer()));
		do {
			do {
				i++;
				while (r.cleanOne(convertPowers) && i < 200)
					i++;
				// console.log("1: " + r.toRendered(new UnicodeRenderer()));
			} while (r.cleanTwo() && i < 200);
			// console.log("2: " + r.toRendered(new UnicodeRenderer()));
		} while (r.cleanThree() && i < 200);
		// console.log("3: " + r.toRendered(new UnicodeRenderer()));
		convertPowers = false;
	} while ((i < 20 || r.cleanFour()) && r.cleanFour() && i < 200);
	while (r.cleanOne(convertPowers) && i < 200)
		i++;
	return r;
}
function changeToCleaning(e) {
		if (e.isBinaryFunction()) {
			switch (e.func) {
			case BinaryFunction.ADD:
				var ca = new CleaningAdd();
				ca.addExpression(changeToCleaning(e.getA()));
				ca.addExpression(changeToCleaning(e.getB()));
				return ca;
			case BinaryFunction.DIVIDE:
				var cd = new CleaningMultiply();
				cd.addExpression(changeToCleaning(e.getA()));
				cd.addExpression(new CleaningInvert(changeToCleaning(e.getB())));
				return cd;
			case BinaryFunction.LOG:
				return new CleaningLog(changeToCleaning(e.getA()),
						changeToCleaning(e.getB()));
			case BinaryFunction.MULTIPLY:
				var cm = new CleaningMultiply();
				cm.addExpression(changeToCleaning(e.getA()));
				cm.addExpression(changeToCleaning(e.getB()));
				return cm;
			case BinaryFunction.POWER:
				return new CleaningPower(changeToCleaning(e.getA()),
						changeToCleaning(e.getB()));
			case BinaryFunction.SUBTRACT:
				var cs = new CleaningAdd();
				cs.addExpression(changeToCleaning(e.getA()));
				cs.addExpression(new CleaningNegate(changeToCleaning(e.getB())));
				return cs;
			case BinaryFunction.UP:
				return new CleaningUp(changeToCleaning(e.getA()),
						changeToCleaning(e.getB()));
		}
	} else if (e.isUnaryFunction()) {
		switch (e.func) {
		case UnaryFunction.EXP:
			return new CleaningPower(new CleaningReal(Constant.E),
					changeToCleaning(e.getChild()));
		case UnaryFunction.ROOT:
			return new CleaningPower(changeToCleaning(e.getChild()),
					new CleaningInvert(new CleaningInteger(2)));
		case UnaryFunction.SQUARE:
			return new CleaningPower(changeToCleaning(e.getChild()),
					new CleaningInteger(2));
		case UnaryFunction.LN:
			return new CleaningLog(new CleaningReal(Constant.E),
					changeToCleaning(e.getChild()));
		case UnaryFunction.INVERSE:
			return new CleaningInvert(changeToCleaning(e.getChild()));
		case UnaryFunction.NEGATE:
			return new CleaningNegate(changeToCleaning(e.getChild()));
		case UnaryFunction.FACTORIAL:
			return new CleaningFactorial(
					changeToCleaning(e.getChild()));
		case UnaryFunction.SIN:
		case UnaryFunction.SINH:
		case UnaryFunction.TAN:
		case UnaryFunction.TANH:
		case UnaryFunction.ARCCOS:
		case UnaryFunction.ARCOSH:
		case UnaryFunction.ARCSIN:
		case UnaryFunction.ARCTAN:
		case UnaryFunction.ARSINH:
		case UnaryFunction.ARTANH:
		case UnaryFunction.COS:
		case UnaryFunction.COSH:
			return new CleaningFunction(e.func,
					changeToCleaning(e.getChild()));
		}
	} else {
		switch (e.constant) {
		case Constant.E:
		case Constant.PI:
		case Constant.GOLDEN_RATIO:
			return new CleaningReal(e.constant);
		default:
			return new CleaningInteger(e.constant.value);
		}
	}
}
class CleaningExpressionType{
}
CleaningExpressionType.ADD = 0;
CleaningExpressionType.MULTIPLY = 1;
CleaningExpressionType.POWER = 2;
CleaningExpressionType.LOG = 3;
CleaningExpressionType.NEGATE = 4;
CleaningExpressionType.INVERT = 5;
CleaningExpressionType.FUNCTION = 6;
CleaningExpressionType.FACTORIAL = 7;
CleaningExpressionType.UP = 8;
CleaningExpressionType.INTEGER = 9;
CleaningExpressionType.REAL = 10;
CleaningExpressionType.ROOT = 11;
this.CleaningExpressionType = CleaningExpressionType;
this.changeToCleaning = changeToCleaning;
this.clean = clean;