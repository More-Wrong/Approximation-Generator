/**
 * 
 */
class CleaningInvert {
	constructor(child) {
		this.parent = null;
		this.child = child;
		child.setParent(this);
	}

	getType() {
		return CleaningExpressionType.INVERT;
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
		if (this.child.getType() == CleaningExpressionType.NEGATE) {
			this.parent.replaceChild(this, new CleaningNegate(this));
			this.child = this.child.child;
			this.child.setParent(this);
			return true;
		} else if (this.child.getType() == CleaningExpressionType.INVERT) {
			this.parent.replaceChild(this, this.child.child);
			return true;
		} else if (this.child.getType() == CleaningExpressionType.MULTIPLY && this.child.canInvert()) {
			this.child.invert();
			this.child.setParent(this.parent);
			this.parent.replaceChild(this, this.child);
			return true;
		} else if (this.child.getType() == CleaningExpressionType.INTEGER && this.child.asInt() == 1) {
			this.parent.replaceChild(this, this.child);
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
		var b = "";
		if (this.child.getType() == CleaningExpressionType.INTEGER || this.child.getType() == CleaningExpressionType.REAL) {
			b = r.slantedFraction(r.integer(1), this.child.toRendered(r));
		} else {
			b = r.fraction(r.integer(1), this.child.toRendered(r));
		}
		return b;
	}

	replaceChild(oldExp, newExp) {
		this.child = newExp;
		newExp.setParent(this);
	}

	canNegate() {
		return this.child.canNegate();
	}

	negate() {
		this.child.negate();
	}

	equals(obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CleaningInvert))
			return false;
		if (!this.child.equals(obj.child))
			return false;
		return true;
	}

	asDouble() {
		return 1 / this.child.asDouble();
	}

	clone() {
		return new CleaningInvert(this.child.clone());
	}
}

class CleaningNegate{
	constructor(child) {
		this.parent = null;
		this.child = child;
		child.setParent(this);
	}

	getType() {
		return CleaningExpressionType.NEGATE;
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
		if (this.child.getType() == CleaningExpressionType.NEGATE) {
			this.parent.replaceChild(this, this.child.child);
			return true;
		}
		if (this.child.getType() == CleaningExpressionType.INTEGER && this.child.asInt() == 0) {
			this.parent.replaceChild(this, this.child);
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
		if (this.child.canNegate()) {
			this.child.negate();
			this.parent.replaceChild(this, this.child);
			return true;
		}
		return this.child.cleanFour();
	}

	toRendered(r) {
		var b = r.negate();
		if (this.child.getType() == CleaningExpressionType.ADD) {
			b += r.bracket(this.child.toRendered(r));
		}else{
			b += this.child.toRendered(r);
		}
		return b;
	}

	replaceChild(oldExp, newExp) {
		this.child = newExp;
		this.child.setParent(this);
	}

	canNegate() {
		return true;
	}

	negate() {
		this.child.setParent(this.parent);
		this.parent.replaceChild(this, this.child);
	}

	equals(obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CleaningNegate))
			return false;
		if (!this.child.equals(obj.child))
			return false;
		return true;
	}

	asDouble() {
		return -this.child.asDouble();
	}

	clone() {
		return new CleaningNegate(this.child.clone());
	}
}
class CleaningUp {
	constructor(target, power) {
		this.parent = null;
		this.power = power;
		power.setParent(this);
		this.target = target;
		target.setParent(this);
	}

	getPower() {
		return power;
	}

	getTarget() {
		return target;
	}

	getType() {
		return CleaningExpressionType.UP;
	}

	getExpressions() {
		return [this.power, this.target];
	}

	getParent() {
		return this.parent;
	}

	setParent(parent) {
		this.parent = parent;
	}

	cleanOne(convertPowers) {
		if (this.power.asInt() == 0) {
			this.parent.replaceChild(this, new CleaningInteger(1));
			return true;
		} else if (this.power.asInt() == 1) {
			this.parent.replaceChild(this, this.target);
			return true;
		} else if (this.power.asInt() == 2) {
			var pow = new CleaningPower(this.target.clone(), this.target);
			this.parent.replaceChild(this, pow);
			return true;
		} else if (this.target.getType() == CleaningExpressionType.INTEGER && this.target.asInt() == 0) {
			this.parent.replaceChild(this, new CleaningInteger(0));
		} else if (this.target.getType() == CleaningExpressionType.INTEGER && this.target.asInt() == 1) {
			this.parent.replaceChild(this, new CleaningInteger(1));
		}
		return this.target.cleanOne(convertPowers);
	}

	cleanTwo() {
		return this.target.cleanTwo();
	}

	cleanThree() {
		return this.target.cleanThree();
	}

	cleanFour() {
		return this.target.cleanFour();
	}

	toRendered(r) {
		return r.up(r.bracket(this.target.toRendered(r)), this.power.toRendered(r));
	}

	replaceChild(oldExp, newExp) {
		if (oldExp == this.power) {
			this.power = newExp;
		} else {
			this.target = newExp;
		}
		newExp.setParent(this);
	}

	canNegate() {
		return false;
	}

	equals(obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CleaningUp))
			return false;
		if (!this.power.equals(obj.power))
			return false;
		if (!this.target.equals(obj.target))
			return false;
		return true;
	}

	asDouble() {
		return BinaryFunction.UP.getValue(this.target.asDouble(), this.power.asDouble());
	}

	clone() {
		return new CleaningUp(this.target.clone(), this.power.clone());
	}
}
this.CleaningUp = CleaningUp;
this.CleaningNegate = CleaningNegate;
this.CleaningInvert = CleaningInvert;