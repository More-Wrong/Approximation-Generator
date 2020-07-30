/**
 * 
 */
class CleaningPower {
	constructor(base, power) {
		this.parent = null;
		this.base = base;
		base.setParent(this);
		this.power = power;
		power.setParent(this);
	}

	getType() {
		return CleaningExpressionType.POWER;
	}

	getExpressions() {
		return [this.power];
	}

	getParent() {
		return parent;
	}

	setParent(parent) {
		this.parent = parent;
	}

	getBase() {
		return this.base;
	}

	getPower() {
		return this.power;
	}

	cleanOne(convertPowers) {
		var didSomething = this.power.cleanOne(convertPowers);
		didSomething = this.base.cleanOne(convertPowers) || didSomething;
		if (this.base.getType() == CleaningExpressionType.NEGATE && this.power.getType() == CleaningExpressionType.INTEGER) {
			if (this.power.asInt() % 2 == 0) {
				this.base = this.base.child;
				this.base.setParent(this);
				didSomething = true;
			} else {
				this.base = this.base.child;
				this.base.setParent(this);
				this.parent.replaceChild(this, new CleaningNegate(this));
				didSomething = true;
			}
		} else if (this.power.getType() == CleaningExpressionType.NEGATE && convertPowers) {
			this.power = this.power.child;
			this.power.setParent(this);
			this.parent.replaceChild(this, new CleaningInvert(this));
			didSomething = true;
		} else if (this.base.getType() == CleaningExpressionType.INVERT) {
			this.base = this.base.child;
			this.base.setParent(this);
			this.parent.replaceChild(this, new CleaningInvert(this));
			didSomething = true;
		} else if (this.power.getType() == CleaningExpressionType.INTEGER && this.power.asInt() == 0) {
			this.parent.replaceChild(this, new CleaningInteger(1));
			return true;
		} else if (this.power.getType() == CleaningExpressionType.INTEGER && this.power.asInt() == 1) {
			this.parent.replaceChild(this, this.base);
			return true;
		} else if (this.base.getType() == CleaningExpressionType.INTEGER && this.base.asInt() == 1) {
			this.parent.replaceChild(this, new CleaningInteger(1));
			return true;
		} else if (this.base.getType() == CleaningExpressionType.INTEGER && this.base.asInt() == 0) {
			this.parent.replaceChild(this, new CleaningInteger(0));
			return true;
		}
		return didSomething;
	}

	cleanTwo() {
		var didSomething = this.power.cleanTwo();
		didSomething = this.base.cleanTwo() || didSomething;
		if (this.base.getType() == CleaningExpressionType.POWER) {
			var chPow = this.base;
			var newPow = new CleaningMultiply();
			newPow.setParent(this);
			newPow.addExpression(this.power);
			newPow.addExpression(chPow.power);
			this.base = chPow.base;
			this.base.setParent(this);
			this.power = newPow;
			didSomething = true;
		}
		return didSomething;
	}

	cleanThree() {
		var didSomething = this.power.cleanThree();
		didSomething = this.base.cleanThree() || didSomething;
		var powMult = null;
		if (this.power.getType() == CleaningExpressionType.MULTIPLY) {
			powMult = this.power;
		} else if (this.power.getType() == CleaningExpressionType.INVERT
				&& this.power.child.getType() == CleaningExpressionType.MULTIPLY) {
			powMult = this.power.child;
		}
		var log = null;
		if (powMult != null) {
			for (var pow of powMult.getExpressions()) {
				if (pow.getType() == CleaningExpressionType.LOG
						&& (powMult == this.power ? pow.getBase() : pow.getChild())
								.equals(this.base)) {
					log = pow;
				} else if (pow.getType() == CleaningExpressionType.INVERT
						&& pow.child.getType() == CleaningExpressionType.LOG) {
					var l = pow.child;
					if ((powMult == this.power ? l.getChild() : l.getBase()).equals(this.base)) {
						log = l;
					}
				}
				if (log != null) {
					powMult.getExpressions().splice(powMult.getExpressions().indexOf(pow));
					var newBase;
					if ((log == pow && powMult == this.power) || (log != pow && powMult != this.power)) {
						newBase = log.getChild();
					} else {
						newBase = log.getBase();
					}
					this.base = newBase;
					this.base.setParent(this);
					return true;
				}
			}
		} else if (this.power.getType() == CleaningExpressionType.LOG && this.power.getBase().equals(this.base)) {
			this.parent.replaceChild(this, this.power.getChild());
			didSomething = true;
		} else if (this.power.getType() == CleaningExpressionType.INVERT
				&& this.power.child.getType() == CleaningExpressionType.LOG
				&& this.power.child.getChild().equals(this.base)) {
			this.parent.replaceChild(this, this.power.child.getBase());
			didSomething = true;
		}
		return didSomething;
	}

	cleanFour() {
		var didSomething = this.power.cleanFour();
		didSomething = this.base.cleanFour() || didSomething;
		if (this.base.getType() == CleaningExpressionType.INTEGER && this.power.getType() == CleaningExpressionType.INTEGER) {
			if (Math.pow(this.base.asDouble(), this.power.asDouble()) <= 100) {
				this.parent.replaceChild(this, new CleaningInteger(Math.pow(this.base.asDouble(), this.power.asDouble())));
				return true;
			}
		} else if (this.parent.getType() == CleaningExpressionType.INVERT
				&& this.parent.getParent().getType() != CleaningExpressionType.MULTIPLY) {
			this.parent.getParent().replaceChild(this.parent, this);
			this.power = new CleaningNegate(this.power);
			this.power.setParent(this);
			didSomething = true;
		}
		return didSomething;
	}

	toRendered(r) {
		if (this.power.getType() == CleaningExpressionType.NEGATE
				&& this.power.child.getType() == CleaningExpressionType.INTEGER
				&& this.power.child.asInt() == 1
				&& (this.base.getType() == CleaningExpressionType.INTEGER
						|| this.base.getType() == CleaningExpressionType.REAL)) {
			return r.slantedFraction(r.integer(1), this.base.toRendered(r));
		}
		if (this.power.getType() == CleaningExpressionType.INVERT) {
			var cc = this.power.child;
			if (cc.getType() == CleaningExpressionType.REAL || cc.getType() == CleaningExpressionType.INTEGER) {
				if (cc.getType() != CleaningExpressionType.INTEGER || cc.asInt() != 2) {
					return r.nthRoot(cc.toRendered(r), this.base.toRendered(r));
				}else{
					return r.sqrt(this.base.toRendered(r));
				}
			}
		}
		var b = "";
		if (this.base.getType() == CleaningExpressionType.INTEGER || this.base.getType() == CleaningExpressionType.REAL) {
			b = this.base.toRendered(r);
		} else {
			b = r.bracket(this.base.toRendered(r));
		}
		return r.power(b, this.power.toRendered(r));
	}

	replaceChild(oldExp, newExp) {
		if (oldExp == this.base) {
			this.base = newExp;
		} else {
			this.power = newExp;
		}
		newExp.setParent(this);
	}

	canNegate() {
		return this.power.getType() == CleaningExpressionType.INTEGER && this.power.asInt() % 2 == 1
				&& this.base.canNegate();
	}

	negate() {
		this.base.negate();
	}

	equals(obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CleaningPower))
			return false;
		if (!this.power.equals(obj.power))
			return false;
		if (!this.base.equals(obj.base))
			return false;
		return true;
	}

	asDouble() {
		return Math.pow(this.base.asDouble(), this.power.asDouble());
	}

	clone() {
		return new CleaningPower(this.base.clone(), this.power.clone());
	}
}
class CleaningLog {
	constructor(base, child) {
		this.parent = null;
		this.base = base;
		this.child = child;
		base.setParent(this);
		child.setParent(this);
	}

	getBase() {
		return this.base;
	}

	getChild() {
		return this.child;
	}

	getType() {
		return CleaningExpressionType.LOG;
	}

	getExpressions() {
		return [this.base, this.child];
	}

	getParent() {
		return this.parent;
	}

	setParent(parent) {
		this.parent = parent;
	}

	cleanOne(convertPowers) {
		var didSomething = false;
		if (this.child.getType() == CleaningExpressionType.INVERT) {
			this.child = this.child.child;
			this.child.setParent(this);
			this.parent.replaceChild(this, new CleaningNegate(this));
			didSomething = true;
		}
		if (this.child.getType() == CleaningExpressionType.INTEGER && this.child.asInt() == 1) {
			if (this.base.getType() == CleaningExpressionType.INTEGER && this.base.asDouble() == 1.0) {
				this.parent.replaceChild(this, new CleaningInteger(1));
			} else {
				this.parent.replaceChild(this, new CleaningInteger(0));
			}
			return true;
		} else if (this.child.getType() == CleaningExpressionType.INTEGER && this.child.asInt() == 0) {
			if (this.base.getType() == CleaningExpressionType.INTEGER && this.base.asDouble() == 0.0) {
				this.parent.replaceChild(this, new CleaningInteger(1));
			} else {
				this.parent.replaceChild(this, new CleaningNegate(new CleaningInvert(new CleaningInteger(0))));
			}
			return true;
		} else if (this.base.getType() == CleaningExpressionType.INTEGER && this.base.asInt() == 1) {
			this.parent.replaceChild(this, new CleaningInvert(new CleaningInteger(0)));
			return true;
		} else if (this.base.getType() == CleaningExpressionType.INTEGER && this.base.asInt() == 0) {
			this.parent.replaceChild(this, new CleaningInteger(0));
			return true;
		} else if (this.base.equals(this.child)) {
			this.parent.replaceChild(this, new CleaningInteger(1));
			return true;
		}
		didSomething = this.child.cleanOne(convertPowers) || didSomething;
		return this.base.cleanOne(convertPowers) || didSomething;
	}

	cleanTwo() {
		var didSomething = this.child.cleanTwo();
		return this.base.cleanTwo() || didSomething;
	}

	cleanThree() {
		var didSomething = false;
		if (this.child.getType() == CleaningExpressionType.POWER) {
			var pow = this.child.getPower();
			this.child = this.child.getBase();
			this.child.setParent(this);
			var mult = new CleaningMultiply();
			mult.addExpression(pow);
			this.parent.replaceChild(this, mult);
			mult.addExpression(this);
			didSomething = true;
		}
		if (this.base.getType() == CleaningExpressionType.POWER) {
			var pow = this.base.getPower();
			this.base = this.base.getBase();
			this.base.setParent(this);
			var mult = new CleaningMultiply();
			mult.addExpression(new CleaningInvert(pow));
			this.parent.replaceChild(this, mult);
			mult.addExpression(this);
			didSomething = true;
		}
		didSomething = this.child.cleanThree() || didSomething;
		return this.base.cleanThree() || didSomething;
	}

	cleanFour() {
		var didSomething = false;
		if ((this.child.getType() == CleaningExpressionType.INTEGER || this.child.getType() == CleaningExpressionType.REAL)
				&& (this.base.getType() != CleaningExpressionType.INTEGER
						&& this.base.getType() != CleaningExpressionType.REAL)) {
			var tmp = this.base;
			this.base = this.child;
			this.child = tmp;
			this.parent.replaceChild(this, new CleaningInvert(this));
			didSomething = true;
		} else if (!((this.base.getType() == CleaningExpressionType.INTEGER || this.base.getType() == CleaningExpressionType.REAL)
				&& (this.child.getType() != CleaningExpressionType.INTEGER
						&& this.child.getType() != CleaningExpressionType.REAL))
				&& this.parent.getType() == CleaningExpressionType.INVERT) {
			this.parent.getParent().replaceChild(this.parent, this);
			var tmp = this.base;
			this.base = this.child;
			this.child = tmp;
			didSomething = true;
		}
		didSomething = this.child.cleanFour() || didSomething;
		return this.base.cleanFour() || didSomething;
	}

	toRendered(r) {
		var b = "";
		b += "{";
		if (this.child.getType() != CleaningExpressionType.REAL && this.child.getType() != CleaningExpressionType.INTEGER) {
			b = r.bracket(this.child.toRendered(r));
		}else{
			b = this.child.toRendered(r);
		}
		if (this.base.getType() == CleaningExpressionType.REAL && this.base.asConstant() == Constant.E) {
			return r.func("ln", b);
		} else {
			return r.log(this.base.toRendered(r), b);
		}
	}

	replaceChild(oldExp, newExp) {
		if (oldExp == this.base) {
			this.base = newExp;
		} else {
			this.child = newExp;
		}
		newExp.setParent(this);
	}

	canNegate() {
		if (this.base.getType() == CleaningExpressionType.MULTIPLY && this.base.canInvert()) {
			return true;
		} else if (this.child.getType() == CleaningExpressionType.MULTIPLY && this.child.canInvert()) {
			return true;
		}
		return false;
	}

	negate() {
		if (this.base.getType() == CleaningExpressionType.MULTIPLY && this.base.canInvert()) {
			this.base.invert();
		} else if (this.child.getType() == CleaningExpressionType.MULTIPLY && this.child.canInvert()) {
			this.child.invert();
		}
	}

	equals(obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CleaningLog))
			return false;
		if (!this.child.equals(obj.child))
			return false;
		if (!this.base.equals(obj.base))
			return false;
		return true;
	}

	asDouble() {
		return Math.log(this.child.asDouble()) / Math.log(this.base.asDouble());
	}

	clone() {
		return new CleaningLog(this.base.clone(), this.child.clone());
	}
}
this.CleaningPower = CleaningPower;
this.CleaningLog = CleaningLog;