/**
 * 
 */

class CleaningAdd {
	constructor(){
		this.expressions = [];
		this.parent = null;
	}
	addExpression(child) {
		this.expressions.push(child);
		child.setParent(this);
	}

	getExpressions() {
		return this.expressions;
	}

	getType() {
		return CleaningExpressionType.ADD;
	}

	getParent() {
		return this.parent;
	}
	
	setParent(parent) {
		this.parent = parent;
	}

	cleanOne(convertPowers) {
		var hasDoneAnything = false;
		for (var i = 0; i < this.expressions.length; i++) {
			var child = this.expressions[i];
			if (child.getType() == CleaningExpressionType.INTEGER && child.asInt() == 0) {
				this.expressions.splice(i--, 1);
				hasDoneAnything = true;
			}
		}
		if (this.expressions.length == 1) {
			this.parent.replaceChild(this, this.expressions[0]);
			return true;
		} else if (this.expressions.length == 0) {
			this.parent.replaceChild(this, new CleaningInteger(0));
			return true;
		}
		for (var i = 0; i < this.expressions.length; i++) {
			var exp = this.expressions[i];
			hasDoneAnything = exp.cleanOne(convertPowers) || hasDoneAnything;
		}
		return hasDoneAnything;
	}

	cleanTwo() {
		var hasDoneAnything = false;
		for (var i = 0; i < this.expressions.length; i++) {
			var exp = this.expressions[i];
			hasDoneAnything = exp.cleanTwo() || hasDoneAnything;
		}
		return hasDoneAnything;
	}

	cleanThree() {
		var hasDoneAnything = false;
		for (var i = 0; i < this.expressions.length; i++) {
			var exp = this.expressions[i];
			hasDoneAnything = exp.cleanThree() || hasDoneAnything;
		}
		var logs = [];
		var negLogs = [];
		for (var i = 0; i < this.expressions.length; i++) {
			var child = this.expressions[i];
			if (child.getType() == CleaningExpressionType.LOG) {
				logs.push(child);
			} else if (child.getType() == CleaningExpressionType.NEGATE
					&& child.child.getType() == CleaningExpressionType.LOG) {
				negLogs.push(child);
			}
		}
		var toRemove = [];
		var newChild = null;
		for (var i = 0; i < logs.length; i++) {
			var log = logs[i];
			var matchingLogs = [];
			var matchingNegLogs = [];
			var base = log.getBase();
			for (var j = 0; j < logs.length; j++) {
				var otherLog = logs[j];
				if (otherLog.getBase().equals(base) && otherLog != log) {
					matchingLogs.push(otherLog);
				}
			}
			for (var j = 0; j < negLogs.length; j++) {
				var negLog = negLogs[j];
				if (negLog.child.getBase().equals(base)) {
					matchingNegLogs.push(negLog);
				}
			}
			if (matchingLogs.length + matchingNegLogs.length > 1) {
				var mult = new CleaningMultiply();
				toRemove.push(... matchingLogs);
				toRemove.push(... matchingNegLogs);
				for (var j = 0; j < matchingLogs.length; j++) {
					var l = matchingLogs[j];
					mult.addExpression(l.getChild());
				}
				for (var j = 0; j < matchingNegLogs.length; j++) {
					var l = matchingNegLogs[j];
					mult.addExpression(new CleaningInvert(l.child.getChild()));
				}
				newChild = new CleaningLog(base, mult);
				break;
			}
		}
		if (newChild == null) {
			for (var i = 0; i < negLogs.length; i++) {
				var negate = negLogs[i];
				var matchingNegLogs = [];
				var base = negate.child.getBase();
				for (var j = 0; j < negLogs.length; j++) {
					var negLog = negLogs[j];
					if (negLog.child.getBase().equals(base)) {
						matchingNegLogs.push(negLog);
					}
				}
				if (matchingNegLogs.length > 1) {
					var mult = new CleaningMultiply();
					toRemove.push(... matchingNegLogs);
					for (var j = 0; j < matchingNegLogs.length; j++) {
						var l = matchingNegLogs[j];
						mult.addExpression(l.child.getChild());
					}
					newChild = new CleaningNegate(new CleaningLog(base, mult));
					break;
				}
			}

		}
		if (newChild != null) {
			hasDoneAnything = true;
			newChild.setParent(this);
			this.expressions = this.expressions.filter(e=>!toRemove.includes(e));
			this.expressions.push(newChild);
		}
		return hasDoneAnything;
	}
	
	cleanFour() {
		var didSomething = false;
		var totalIntVal = 0;
		var reals = [];
		var negReals = [];
		for (var i = 0; i < this.expressions.length; i++) {
			var child = this.expressions[i];
			if (child.getType() == CleaningExpressionType.INTEGER) {
				totalIntVal += child.asInt();
				this.expressions.splice(i--, 1);
			} else if (child.getType() == CleaningExpressionType.REAL) {
				reals.push(child);
				this.expressions.splice(i--, 1);
			} else if (child.getType() == CleaningExpressionType.MULTIPLY && child.getExpressions().length == 2) {
				if (child.getExpressions()[0].getType() == CleaningExpressionType.INTEGER
						&& child.getExpressions()[1].getType() == CleaningExpressionType.REAL) {
					var num = child.getExpressions()[0].asInt();
					for (var j = 0; j < num; j++) {
						reals.push(child.getExpressions()[1]);
					}
					this.expressions.splice(i--, 1);
				} else if (child.getExpressions()[1].getType() == CleaningExpressionType.INTEGER
						&& child.getExpressions()[0].getType() == CleaningExpressionType.REAL) {
					var num = child.getExpressions()[1].asInt();
					for (var j = 0; j < num; j++) {
						reals.push(child.getExpressions()[0]);
					}
					this.expressions.splice(i--, 1);
				}
			} else if (child.getType() == CleaningExpressionType.NEGATE) {
				var cc = child.child;
				if (cc.getType() == CleaningExpressionType.INTEGER) {
					totalIntVal -= cc.asInt();
					this.expressions.splice(i--, 1);
				} else if (cc.getType() == CleaningExpressionType.REAL) {
					negReals.push(cc);
					this.expressions.splice(i--, 1);
				} else if (cc.getType() == CleaningExpressionType.MULTIPLY && cc.getExpressions().length == 2) {
					if (cc.getExpressions()[0].getType() == CleaningExpressionType.INTEGER
							&& cc.getExpressions()[1].getType() == CleaningExpressionType.REAL) {
						var num = cc.getExpressions()[0].asInt();
						for (var j = 0; j < num; j++) {
							negReals.push(cc.getExpressions()[1]);
						}
						this.expressions.splice(i--, 1);
					} else if (cc.getExpressions()[1].getType() == CleaningExpressionType.INTEGER
							&& cc.getExpressions()[0].getType() == CleaningExpressionType.REAL) {
						var num = cc.getExpressions()[1].asInt();
						for (var j = 0; j < num; j++) {
							negReals.push(cc.getExpressions()[0]);
						}
						this.expressions.splice(i--, 1);
					}
				}
			}
		}
		if (totalIntVal != 0) {
			if (totalIntVal > 0) {
				this.expressions.push(new CleaningInteger(totalIntVal));
			} else {
				this.expressions.push(new CleaningNegate(new CleaningInteger(-totalIntVal)));
			}
		}
		for (var i = 0; i < reals.length; i++) {
			var r = reals[i];
			if (negReals.includes(r)) {
				negReals.splice(negReals.indexOf(r), 1);
				reals.splice(i--, 1);
				didSomething = true;
			}
		}
		while (reals.length > 0) {
			var r = reals[0];
			reals.splice(0, 1);
			var num = 1;
			for (var i = 0; i < reals.length; i++) {
				if (reals[i].equals(r)) {
					reals.splice(i--, 1);
					num++;
					didSomething = true;
				}
			}
			for (var i = 0; i < negReals.length; i++) {
				if (negReals[i].equals(r)) {
					negReals.splice(i--, 1);
					num--;
					didSomething = true;
				}
			}
			if(Math.abs(num)==1){
				if (num > 0) {
					this.expressions.push(r);
				} else {
					var n = new CleaningNegate(r);
					n.setParent(this);
					this.expressions.push(n);
				}
			}else if (num != 0) {
				var mult = new CleaningMultiply();
				mult.addExpression(r);
				if (num > 0) {
					mult.addExpression(new CleaningInteger(num));
					mult.setParent(this);
					this.expressions.push(mult);
				} else {
					mult.addExpression(new CleaningInteger(-num));
					var n = new CleaningNegate(mult);
					n.setParent(this);
					this.expressions.push(n);
				}
			}
		}
		while (negReals.length > 0) {
			var r = negReals[0];
			negReals.splice(0, 1);
			var num = -1;
			for (var i = 0; i < negReals.length; i++) {
				if (negReals[i].equals(r)) {
					negReals.splice(i--, 1);
					num--;
					didSomething = true;
				}
			}
			if (Math.abs(num) == 1) {
				var n = new CleaningNegate(r);
				n.setParent(this);
				this.expressions.push(n);
			} else {
			var mult = new CleaningMultiply();
				mult.addExpression(r);
				mult.addExpression(new CleaningInteger(-num));
				var n = new CleaningNegate(mult);
				n.setParent(this);
				this.expressions.push(n);
			}
		}
		for (var i = 0; i < this.expressions.length; i++) {
			didSomething = this.expressions[i].cleanFour() || didSomething;
		}
		return didSomething;
	}

	toRendered(r) {
		var pos = "";
		var neg = "";
		var hasPrev = false;
		for (var exp of this.expressions) {
			if (exp.getType() == CleaningExpressionType.NEGATE) {
				neg+=r.minus();
				neg+=exp.child.toRendered(r);
			} else {
				if (hasPrev) {
					pos+=r.plus();
				}
				pos+=exp.toRendered(r);
				hasPrev = true;
			}
		}
		return pos + neg;
	}

	replaceChild(oldExp, newExp) {
		this.expressions.splice(this.expressions.indexOf(oldExp),1);
		this.expressions.push(newExp);
		newExp.setParent(this);
	}

	equals(obj) {
		if (!(obj instanceof CleaningAdd)) {
			return false;
		}
		if (obj.expressions.length != this.expressions.length) {
			return false;
		}
		if (obj.expressions.every(e=>this.expressions.includes(e))) {
			var exps = obj.expressions.slice(obj.expressions.length);
			for (var child of this.expressions) {
				if (!exps.includes(e=>e.equals(child))) {
					return false;
				}
				exps = exps.filter(e=>!e.equals(child));
			}
			return true;
		}
		return false;
	}

	canNegate() {
		for (var child of this.expressions) {
			if (child.getType() == CleaningExpressionType.NEGATE) {
				return true;
			}
		}
		return false;
	}

	negate() {
		var newExpressions = [];
		for (var child of this.expressions) {
			if (child.getType() == CleaningExpressionType.NEGATE) {
				newExpressions.push(child.child);
			} else {
				newExpressions.push(new CleaningNegate(child));
			}
		}
		for (var newExp of newExpressions) {
			newExp.setParent(this);
		}
		this.expressions = newExpressions;

	}

	asDouble() {
		var d = 0;
		for (var exp of this.expressions) {
			d += exp.asDouble();
		}
		return d;
	}

	clone() {
		var clone = new CleaningAdd();
		for (var child of this.expressions) {
			clone.addExpression(child.clone());
		}
		return clone;
	}
}
this.CleaningAdd = CleaningAdd;