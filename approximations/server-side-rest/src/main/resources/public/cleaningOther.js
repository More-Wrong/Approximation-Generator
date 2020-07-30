/**
 * 
 */
class CleaningFactorial {

	constructor(child){
		this.child = child;
		child.setParent(this);
		this.parent = null;
	}

	getType() {
		return CleaningExpressionType.FACTORIAL;
	}

	getExpressions() {
		return [this.child];
	}

	getParent() {
		return this.parent;
	}

	setParent(parent) {
		this.parent = parent;
	}

	cleanOne(convertPowers) {
		if (this.child.asInt() == 0 || this.child.asInt() == 1) {
			this.parent.replaceChild(this, new CleaningInteger(1));
			return true;
		}
		return this.child.cleanOne(convertPowers);
	}

	cleanTwo() {
		return this.child.cleanTwo();
	}

	cleanThree() {
		return this.child.cleanThree();
	}

	cleanFour() {
		return this.child.cleanFour();
	}

	toRendered(r) {
		return r.factorial(this.child.toRendered(r));
	}

	replaceChild(oldExp, newExp) {
		this.child = newExp;
		this.child.setParent(this);
	}

	canNegate() {
		return false;
	}

	equals(obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CleaningFactorial))
			return false;
		if (!this.child.equals(obj.child))
			return false;
		return true;
	}

	asDouble() {
		var d = 1;
		for (var i = 1; i <= this.child.asInt(); i++) {
			d *= i;
		}
		return d;
	}

	clone() {
		return new CleaningFactorial(new CleaningInteger(this.child.asInt()));
	}
}


class CleaningFunction {
	constructor(func, child) {
		this.func = func;
		this.child = child;
		child.setParent(this);
		this.parent = null;
	}

	getParent() {
		return this.parent;
	}

	setParent(parent) {
		this.parent = parent;
	}

	getType() {
		return CleaningExpressionType.FUNCTION;
	}

	cleanOne(convertPowers) {
		var didSomething = false;
		if (this.child.getType() == CleaningExpressionType.NEGATE) {
			switch (this.func) {
			case CleaningExpressionType.TAN:
			case CleaningExpressionType.ARCTAN:
			case CleaningExpressionType.SIN:
			case CleaningExpressionType.ARCSIN:
			case CleaningExpressionType.SINH:
			case CleaningExpressionType.ARSINH:
			case CleaningExpressionType.TANH:
			case CleaningExpressionType.ARTANH:
				parent.replaceChild(this, new CleaningNegate(this));
			case CleaningExpressionType.COS:
			case CleaningExpressionType.COSH:
				child = child.getExpressions().get(0);
				child.setParent(this);
				didSomething = true;
				break;
			default:
				break;
			}
		}
		return this.child.cleanOne(convertPowers) || didSomething;
	}

	cleanTwo() {
		return this.child.cleanTwo();
	}

	cleanThree() {
		return this.child.cleanThree();
	}

	cleanFour() {
		return this.child.cleanFour();
	}

	toRendered(r) {
		return r.func(this.func.name.toLowerCase(), r.lBracket()+this.child.toRendered(r)+r.rBracket());
	}

	getExpressions() {
		return [this.child];
	}

	replaceChild(oldExp, newExp) {
		this.child = newExp;
		this.child.setParent(this);
	}

	canNegate() {
		switch (this.func) {
		case CleaningExpressionType.TAN:
		case CleaningExpressionType.ARCTAN:
		case CleaningExpressionType.SIN:
		case CleaningExpressionType.ARCSIN:
		case CleaningExpressionType.SINH:
		case CleaningExpressionType.ARSINH:
		case CleaningExpressionType.TANH:
		case CleaningExpressionType.ARTANH:
			return this.child.canNegate();
		default:
			return false;
		}
	}

	negate() {
		this.child.negate();
	}

	equals(obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CleaningFunction))
			return false;
		if (!this.child.equals(obj.child))
			return false;
		if (!this.func.equals(obj.func))
			return false;
		return true;
	}

	asDouble() {
		return this.func.getValue(this.child.asDouble());
	}

	clone() {
		return new CleaningFunction(this.func, this.child.clone());
	}
}

class CleaningInteger {
	constructor(value) {
		this.parent = null;
		this.value = value;
	}

	asInt() {
		return this.value;
	}

	getType() {
		return CleaningExpressionType.INTEGER;
	}

	getParent() {
		return parent;
	}

	setParent(parent) {
		this.parent = parent;
	}

	cleanOne(convertPowers) {
		return false;
	}

	cleanTwo() {
		return false;
	}

	cleanThree() {
		return false;
	}

	cleanFour() {
		return false;
	}

	toRendered(r) {
		return r.integer(this.value);
	}
	
	canNegate() {
		return false;
	}

	asDouble() {
		return this.value;
	}

	equals(obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CleaningInteger))
			return false;
		if (this.value != obj.value)
			return false;
		return true;
	}

	clone() {
		return new CleaningInteger(this.value);
	}

}
class CleaningReal {
	constructor(value) {
		this.parent = null;
		this.value = value;
	}

	asConstant() {
		return this.value;
	}

	getType() {
		return CleaningExpressionType.REAL;
	}

	getExpressions() {
		return [];
	}

	getParent() {
		return this.parent;
	}

	setParent(parent) {
		this.parent = parent;
	}

	cleanOne(convertPowers) {
		return false;
	}

	cleanTwo() {
		return false;
	}

	cleanThree() {
		return false;
	}

	cleanFour() {
		return false;
	}

	toRendered(r) {
		switch (this.value) {
		case Constant.E:
			return r.e();
		case Constant.PI:
			return r.pi();
		case Constant.GOLDEN_RATIO:
			return r.goldenRatio();
		default:
			return "";
		}
	}

	canNegate() {
		return false;
	}

	asDouble() {
		return value.getValue();
	}

	equals(obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CleaningReal))
			return false;
		if (this.value != obj.value)
			return false;
		return true;
	}

	clone() {
		return new CleaningReal(this.value);
	}
}


class CleaningRoot {
	constructor(child) {
		child.setParent(this);
		this.child = child;
	}

	getParent() {
		return null;
	}

	getType() {
		return CleaningExpressionType.ROOT;
	}

	cleanOne(convertPowers) {
		return this.child.cleanOne(convertPowers);
	}

	cleanTwo() {
		return this.child.cleanTwo();
	}

	cleanThree() {
		return this.child.cleanThree();
	}

	cleanFour() {
		return this.child.cleanFour();
	}

	toRendered(r) {
		return this.child.toRendered(r);
	}

	getExpressions() {
		return [this.child];
	}

	replaceChild(oldExp, newExp) {
		this.child = newExp;
		this.child.setParent(this);
	}

	canNegate() {
		return false;
	}

	asDouble() {
		return this.child.asDouble();
	}
	equals(obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CleaningRoot))
			return false;
		if (this.child == null) {
			if (obj.child != null)
				return false;
		} else if (!this.child.equals(obj.child))
			return false;
		return true;
	}

	clone() {
		return new CleaningRoot(this.child.clone());
	}

}
this.CleaningRoot = CleaningRoot;
this.CleaningReal = CleaningReal;
this.CleaningInteger = CleaningInteger;
this.CleaningFunction = CleaningFunction;
this.CleaningFactorial = CleaningFactorial;