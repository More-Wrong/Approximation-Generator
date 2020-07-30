/**
 * 
 */
class CleaningMultiply {
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
		return CleaningExpressionType.MULTIPLY;
	}

	getParent() {
		return this.parent;
	}

	setParent(parent) {
		this.parent = parent;
	}

	 cleanOne(convertPowers) {
		var hasDoneSomething = false;
		for (var i = 0; i < this.expressions.length; i++) {
			var child = this.expressions[i];
			hasDoneSomething = child.cleanOne(convertPowers) || hasDoneSomething;
		}

		for (var i = 0; i < this.expressions.length; i++) {
			var child = this.expressions[i];
			if (child.getType() == CleaningExpressionType.INTEGER && child.asInt() == 1) {
				this.expressions.splice(i--, 1);
				hasDoneSomething = true;
			} else if (child.getType() == CleaningExpressionType.INTEGER && child.asInt() == 0) {
				this.parent.replaceChild(this, new CleaningInteger(0));
				return true;
			}
		}
		if (this.expressions.length == 1) {
			this.parent.replaceChild(this, this.expressions[0]);
			return true;
		} else if (this.expressions.length == 0) {
			this.parent.replaceChild(this, new CleaningInteger(1));
			return true;
		}
		var shouldInv = true;
		for (var child of this.expressions) {
			if (child.getType() != CleaningExpressionType.INVERT) {
				shouldInv = false;
				break;
			}
		}
		if (shouldInv && convertPowers) {
			this.invert();
			this.parent.replaceChild(this, new CleaningInvert(this));
		}
		var toAdd = [];
		var negationCount = 0;
		for (var i = 0; i < this.expressions.length; i++) {
			var child = this.expressions[i];
			if (child.getType() == CleaningExpressionType.MULTIPLY) {
				for (var childChild of child.getExpressions()) {
					toAdd.push(childChild);
					childChild.setParent(this);
				}
				this.expressions.splice(i--, 1);
				hasDoneSomething = true;
			} else if (child.getType() == CleaningExpressionType.INVERT) {
				var childChild = child.child;
				if (childChild.getType() == CleaningExpressionType.MULTIPLY) {
					for (var childChildChild of childChild.getExpressions()) {
						toAdd.push(new CleaningInvert(childChildChild));
					}
					hasDoneSomething = true;
					this.expressions.splice(i--, 1);
				}
			} else if (child.getType() == CleaningExpressionType.NEGATE) {
				var childChild = child.child;
				childChild.setParent(this);
				toAdd.push(childChild);
				negationCount++;
				this.expressions.splice(i--, 1);
				hasDoneSomething = true;
			}
		}
		hasDoneSomething = hasDoneSomething || toAdd.length!=0;
		for (var newChild of toAdd) {
			newChild.setParent(this);
		}
		this.expressions.push(... toAdd);
		if (negationCount % 2 == 1) {
			hasDoneSomething = true;
			if (this.parent.getType() == CleaningExpressionType.NEGATE) {
				this.parent.getParent().replaceChild(this.parent, this);
			} else {
				this.parent.replaceChild(this, new CleaningNegate(this));
			}
		}
		return hasDoneSomething;
	}

	cleanTwo() {
		var hasDoneSomething = false;
		for (var i = 0; i < this.expressions.length; i++) {
			var child = this.expressions[i];
			hasDoneSomething = child.cleanTwo() || hasDoneSomething;
		}
		var baseShareExps = [];
		var newChild = null;
		for (var child of this.expressions) {
			var target = child;
			if (child.getType() == CleaningExpressionType.POWER) {
				target = child.getBase();
			} else if (child.getType() == CleaningExpressionType.INVERT) {
				if (child.child.getType() == CleaningExpressionType.POWER) {
					target = child.child.getBase();
				} else {
					target = child.child;
				}
			}
			baseShareExps = [];
			baseShareExps.push(child);
			for (var otherChild of this.expressions) {
				if (otherChild != child) {
					if (otherChild.equals(target)) {
						baseShareExps.push(otherChild);
					} else if (otherChild.getType() == CleaningExpressionType.POWER
							&& otherChild.getBase().equals(target)) {
						baseShareExps.push(otherChild);
					} else if (otherChild.getType() == CleaningExpressionType.INVERT) {
						var childChild = otherChild.child;
						if (childChild.equals(target)) {
							baseShareExps.push(otherChild);
						} else if (childChild.getType() == CleaningExpressionType.POWER
								&& childChild.getBase().equals(target)) {
							baseShareExps.push(otherChild);
						}
					}
				}
			}
			if (baseShareExps.length > 1) {
				var intPower = 0;
				var num = 0;
				for (var targetChild of baseShareExps) {
					if (targetChild.getType() == CleaningExpressionType.POWER) {
						num++;
					} else if (targetChild.getType() == CleaningExpressionType.INVERT) {
						if (targetChild.child.getType() == CleaningExpressionType.POWER) {
							num++;
						} else {
							intPower--;
						}
					} else {
						intPower++;
					}
				}
				if (intPower != 0) {
					num++;
				}
				if (num == 1) {
					if (intPower < 0) {
						newChild = new CleaningPower(target, new CleaningNegate(new CleaningInteger(-intPower)));
						break;
					} else if (intPower > 0) {
						newChild = new CleaningPower(target, new CleaningInteger(intPower));
						break;
					} else {
						var pow = null;
						for (var targetChild of baseShareExps) {
							if (targetChild.getType() == CleaningExpressionType.POWER) {
								pow = targetChild.getPower();
							} else if (targetChild.getType() == CleaningExpressionType.INVERT) {
								if (targetChild.child.getType() == CleaningExpressionType.POWER) {
									pow = new CleaningNegate(child.getPower());
								}
							}
						}
						newChild = new CleaningPower(target, pow);
						break;
					}
				} else if (num > 1) {
					var a = new CleaningAdd();
					for (var targetChild of baseShareExps) {
						if (targetChild.getType() == CleaningExpressionType.POWER) {
							a.addExpression(targetChild.getPower());
						} else if (targetChild.getType() == CleaningExpressionType.INVERT) {
							if (targetChild.child.getType() == CleaningExpressionType.POWER) {
								a.addExpression(new CleaningNegate(targetChild.child.getPower()));
							}
						}
					}
					if (intPower < 0) {
						a.addExpression(new CleaningNegate(new CleaningInteger(-intPower)));
					} else if (intPower > 0) {
						a.addExpression(new CleaningInteger(intPower));
					}
					newChild = new CleaningPower(target, a);
					break;
				}
			}
		}
		if (newChild != null) {
			this.expressions = this.expressions.filter(e=>!baseShareExps.includes(e));
			this.expressions.push(newChild);
			newChild.setParent(this);
			hasDoneSomething = true;
		}

		return hasDoneSomething;
	}

	cleanThree() {
		var hasDoneAnything = false;
		for (var i = 0; i < this.expressions.length; i++) {
			var exp = this.expressions[i];
			hasDoneAnything = exp.cleanThree() || hasDoneAnything;
		}
		var logs = [];
		var invLogs = [];
		for (var child of this.expressions) {
			if (child.getType() == CleaningExpressionType.LOG) {
				logs.push(child);
			} else if (child.getType() == CleaningExpressionType.INVERT
					&& child.child.getType() == CleaningExpressionType.LOG) {
				invLogs.push(child);
			}
		}
		var toRemove = null;
		for (var target of logs) {
			var base = target.getBase();
			var matchingLogs = [];
			var matchingInvLogs = [];
			for (var log of logs) {
				if (log != target && log.getChild().equals(base)) {
					matchingLogs.push(log);
				}
			}
			for (var invLog of invLogs) {
				var log = invLog.child;
				if (log.getBase().equals(base)) {
					matchingInvLogs.push(invLog);
				}
			}
			if (matchingInvLogs.length > 0) {
				toRemove = matchingInvLogs[0];
				var log = matchingInvLogs[0].child;
				target.replaceChild(base, log.getChild());
				hasDoneAnything = true;
				break;
			} else if (matchingLogs.length > 0) {
				toRemove = matchingLogs[0];
				target.replaceChild(base, matchingLogs[0].getBase());
				hasDoneAnything = true;
				break;
			}
		}
		if (toRemove != null) {
			this.expressions.splice(this.expressions.indexOf(toRemove));
		}
		toRemove = null;
		for (var target of logs) {
			var base = target.getChild();
			var matchingLogs = [];
			var matchingInvLogs = [];
			for (var log of logs) {
				if (log != target && log.getBase().equals(base)) {
					matchingLogs.push(log);
				}
			}
			for (var invLog of invLogs) {
				var log = invLog.child;
				if (log.getChild().equals(base)) {
					matchingInvLogs.push(invLog);
				}
			}
			if (matchingInvLogs.length > 0) {
				toRemove = matchingInvLogs[0];
				var log = matchingInvLogs[0].child;
				target.replaceChild(base, log.getBase());
				hasDoneAnything = true;
				break;
			} else if (matchingLogs.length > 0) {
				toRemove = matchingLogs[0];
				target.replaceChild(base, matchingLogs[0].getChild());
				hasDoneAnything = true;
				break;
			}
		}
		if (toRemove != null) {
			this.expressions.splice(this.expressions.indexOf(toRemove), 1);
		}
		return hasDoneAnything;
	}

	cleanFour() {
		var hasDoneAnything = false;
		for (var i = 0; i < this.expressions.length; i++) {
			var exp = this.expressions[i];
			hasDoneAnything = exp.cleanFour() || hasDoneAnything;
		}
		if (this.parent.getType() == CleaningExpressionType.INVERT) {
			this.parent.getParent().replaceChild(this.parent, this);
			this.invert();
			hasDoneAnything = true;
		}
		var numerator = 1;
		var denominator = 1;
		for (var i = 0; i < this.expressions.length; i++) {
			var child = this.expressions[i];
			if (child.getType() == CleaningExpressionType.INTEGER) {
				numerator *= child.asInt();
				this.expressions.splice(i--, 1);
			} else if (child.getType() == CleaningExpressionType.INVERT
					&& child.child.getType() == CleaningExpressionType.INTEGER) {
				denominator *= child.child.asInt();
				this.expressions.splice(i--, 1);
			}
		}
		if (numerator == 1 && denominator == 1) {
			// do nothing
		}else if (numerator != 1 && denominator == 1) {
			this.expressions.push(new CleaningInteger(numerator));
		} else if (denominator != 1 && numerator == 1) {
			this.expressions.push(new CleaningInvert(new CleaningInteger(denominator)));
		} else if (numerator == denominator) {
			hasDoneAnything = true;
			// no not replace numbers, simply remove both
		} else if (numerator % denominator == 0) {
			hasDoneAnything = true;
			this.expressions.push(new CleaningInteger(numerator / denominator));
		} else if (denominator % numerator == 0) {
			hasDoneAnything = true;
			this.expressions.push(new CleaningInvert(new CleaningInteger(denominator / numerator)));
		} else if (numerator != 1 && denominator != 1) {
			var maxTest = Math.sqrt(Math.min(denominator, numerator));
			for (var i = 2; i < maxTest + 2; i++) {
				while (numerator % i == 0 && denominator % i == 0) {
					numerator /= i;
					denominator /= i;
					maxTest /= Math.sqrt(i);
					hasDoneAnything = true;
				}
			}
			this.expressions.push(new CleaningInteger(numerator));
			this.expressions.push(new CleaningInvert(new CleaningInteger(denominator)));
		}
		var hasDenominatorAnyway = false;
		for (var child of this.expressions) {
			child.setParent(this);
			if (child.getType() == CleaningExpressionType.INVERT
					&& child.child.getType() != CleaningExpressionType.POWER) {
				hasDenominatorAnyway = true;
			}
		}
		if (!hasDenominatorAnyway) {
			var invs = [];
			for (var child of this.expressions) {
				if (child.getType() == CleaningExpressionType.INVERT
						&& child.child.getType() == CleaningExpressionType.POWER) {
					invs.push(child);
					var lPow = child.child;
					lPow.replaceChild(lPow.getPower(), new CleaningNegate(lPow.getPower()));

				}
			}
			this.expressions = this.expressions.filter(e=>!invs.includes(e));
			for (var inv of invs) {
				this.expressions.push(inv.child);
				hasDoneAnything = true;
			}
		}
		return hasDoneAnything;
	}

	canInvert() {
		for (var child of this.expressions) {
			if (child.getType() == CleaningExpressionType.INVERT) {
				return true;
			}
		}
		return false;
	}

	invert() {
		var newExpressions = [];
		for (var child of this.expressions) {
			if (child.getType() == CleaningExpressionType.INVERT) {
				newExpressions.push(child.child);
			} else {
				var exp = new CleaningInvert(child);
				child.setParent(exp);
				newExpressions.push(exp);
			}
		}
		for (var exp of newExpressions) {
			exp.setParent(this);
		}
		this.expressions = newExpressions;
	}

	toRendered(r) {
		var numeratorNum = 0;
		var denominatorNum = 0;
		var onlyConsts = true;
		for (var child of this.expressions) {
			if (child.getType() == CleaningExpressionType.INVERT) {
				denominatorNum++;
				if (child.child.getType() != CleaningExpressionType.INTEGER
						&& child.child.getType() != CleaningExpressionType.REAL) {
					onlyConsts = false;
				}
			} else {
				numeratorNum++;
				if (child.getType() != CleaningExpressionType.INTEGER
						&& child.getType() != CleaningExpressionType.REAL) {
					onlyConsts = false;
				}
			}

		}
		var numerator1 = "";
		var denominator1 = "";
		var numerator2 = "";
		var denominator2 = "";
		var numerator3 = "";
		var denominator3 = "";
		var numerator4 = "";
		var denominator4 = "";
		var numerator5 = "";
		var denominator5 = "";

		for (var child of this.expressions) {
			var target = null;
			var targetNum = 0;
			var br = false;
			var powSpec = false;
			var enTimes = false;
			var toWrite = child;
			var current = child;
			if (child.getType() == CleaningExpressionType.INVERT) {
				current = child.child;
				toWrite = current;
				if (current.getType() == CleaningExpressionType.POWER) {
					current = current.getBase();
					powSpec = true;
				}
				targetNum = denominatorNum;
				if (current.getType() == CleaningExpressionType.INTEGER) {
					enTimes = true;
					if (powSpec) {
						target = denominator2;
					} else {
						target = denominator1;
					}
				} else if (current.getType() == CleaningExpressionType.REAL) {
					if (powSpec) {
						target = denominator4;
					} else {
						target = denominator3;
					}
				} else {
					target = denominator5;
					br = !powSpec;
					powSpec = false;
				}
			} else {
				if (current.getType() == CleaningExpressionType.POWER) {
					current = current.getBase();
					powSpec = true;
				}
				targetNum = numeratorNum;
				if (current.getType() == CleaningExpressionType.INTEGER) {
					enTimes = true;
					if (powSpec) {
						target = numerator2;
					} else {
						target = numerator1;
					}
				} else if (current.getType() == CleaningExpressionType.REAL) {
					if (powSpec) {
						target = numerator4;
					} else {
						target = numerator3;
					}
				} else {
					target = numerator5;
					br = !powSpec;
					powSpec = false;
				}
			}
			var toAppend = "";
			if (target.length != 0) {
				if(enTimes){
					toAppend+=r.times();
				}else{
					toAppend+=r.invTimes();
				}
			}
			if (br && targetNum != 1) {
				toAppend+=r.lBracket();
			}
			toAppend+=toWrite.toRendered(r);
			if (br && targetNum != 1) {
				toAppend+=r.rBracket();
			}
			if (child.getType() == CleaningExpressionType.INVERT) {
				if (current.getType() == CleaningExpressionType.INTEGER) {
					enTimes = true;
					if (powSpec) {
						denominator2+=toAppend;
					} else {
						denominator1+=toAppend;
					}
				} else if (current.getType() == CleaningExpressionType.REAL) {
					if (powSpec) {
						denominator4+=toAppend;
					} else {
						denominator3+=toAppend;
					}
				} else {
					denominator5+=toAppend;
				}
			} else {
				if (current.getType() == CleaningExpressionType.INTEGER) {
					enTimes = true;
					if (powSpec) {
						numerator2+=toAppend;
					} else {
						numerator1+=toAppend;
					}
				} else if (current.getType() == CleaningExpressionType.REAL) {
					if (powSpec) {
						numerator4+=toAppend;
					} else {
						numerator3+=toAppend;
					}
				} else {
					numerator5+=toAppend;
				}
			}
		}
		var numerator = "";
		numerator+=numerator1;
		if (numerator1.length > 0 && numerator2.length > 0) {
			numerator+=r.times();
		}
		numerator += numerator2;
		if (numerator.length > 0 && numerator3.length > 0) {
			numerator+=r.invTimes();
		}
		numerator += numerator3;
		if (numerator.length > 0 && numerator4.length > 0) {
			numerator+=r.invTimes();
		}
		numerator += numerator4;
		if (numerator.length > 0 && numerator5.length > 0) {
			numerator+=r.invTimes();
		}
		numerator += numerator5;

		var denominator = "";
		denominator += denominator1;
		if (denominator1.length > 0 && denominator2.length > 0) {
			denominator += r.times();
		}
		denominator += denominator2;
		if (denominator.length > 0 && denominator3.length > 0) {
			denominator += r.invTimes();
		}
		denominator += denominator3;
		if (denominator.length > 0 && denominator4.length > 0) {
			denominator += r.invTimes();
		}
		denominator += denominator4;
		if (denominator.length > 0 && denominator5.length > 0) {
			denominator += r.invTimes();
		}
		denominator += denominator5;

		if (onlyConsts && (numeratorNum == 1 || numeratorNum == 0) && denominatorNum == 1) {
			return r.slantedFraction(numeratorNum == 0?r.integer(1):numerator, denominator);
		}else if (denominator.length != 0) {
			return r.fraction(numeratorNum == 0?r.integer(1):numerator, denominator);
		} else {
			return numerator;
		}
	}

	replaceChild(oldExp, newExp) {
		this.expressions.splice(this.expressions.indexOf(oldExp),1);
		this.expressions.push(newExp);
		newExp.setParent(this);
	}

	equals(obj) {
		if (!(obj instanceof CleaningMultiply)) {
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
			if (child.canNegate()) {
				return true;
			}
		}
		return false;
	}

	negate() {
		for (var child of this.expressions) {
			if (child.canNegate()) {
				child.negate();
				break;
			}
		}
	}

	asDouble() {
		var d = 1;
		for (var exp of this.expressions) {
			d *= exp.asDouble();
		}
		return d;
	}

	clone() {
		var clone = new CleaningMultiply();
		for (var child of this.expressions) {
			clone.addExpression(child.clone());
		}
		return clone;
	}

}
this.CleaningMultiply = CleaningMultiply;