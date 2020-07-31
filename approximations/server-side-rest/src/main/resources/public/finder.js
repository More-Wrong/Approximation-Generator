/**
 * 
 */



 
function findCandidatesFor(value, layers, initial) {
	var correctedTarget = value;
	var negate = false;
	var invert = false;
	if(correctedTarget<0){
		correctedTarget = correctedTarget.neg();
		negate = true;
	}
	if(correctedTarget>1){
		correctedTarget = new Decimal(1).div(correctedTarget);
		invert = true;
	}
	var arPrArExpr = [];
	var directExps = fetchExpressionsFor(correctedTarget.toNumber(), initial?3:1);
	arPrArExpr.push(directExps.then(e=>e.map(ex=>correctForInvNeg(ex, negate, invert))));
	if (layers > 0) {
		arPrArExpr.push(mapAllPromiseArrays(directExps, ex=>findAlternatives(ex, correctedTarget, negate, invert, layers)));
		arPrArExpr.push(mapAllPromiseArrays(arPrArExpr[0], e=>findAdditiveCorrections(value, e, layers)));
		arPrArExpr.push(mapAllPromiseArrays(arPrArExpr[1], e=>findAdditiveCorrections(value, e, layers)));
	}
	return arPrArExpr;
}

async function findAdditiveCorrections(value, toCorrect, layers){
	var err = value.sub(toCorrect.getPrecise());
	var bestErr = err;
	var bestFound = null;
	var res = await flattenAllPromiseArrays(findCandidatesFor(err, Math.min(1, layers - 1), false));
	for(var ex2 of res){
		var thisErr = err.sub(ex2.getPrecise()).abs();
		if (thisErr.comparedTo(bestErr)<0) {
			bestFound = ex2;
			bestErr = thisErr;
			break;
		}
	}
	if(bestFound!=null){
		var expr = new BinaryFunctionExpression(BinaryFunction.ADD);
		expr.setA(cloneExpression(toCorrect));
		expr.setB(bestFound);
		return expr;
	}
	return [];
}
async function findAlternatives(fnd, tT, neg, inv, layers) {
	var negate = neg;
	var invert = inv;
	var found = fnd;
	var trueTarget = tT;
	if (invert && found.isUnaryFunction()
			&& found.func == UnaryFunction.INVERSE) {
		found = found.getChild();
		invert = false;
		trueTarget = new Decimal(1).div(trueTarget);
	}
	if (negate && found.isUnaryFunction()
			&& found.func == UnaryFunction.NEGATE) {
		found = found.getChild();
		negate = false;
		trueTarget = trueTarget.neg();
	}
	var foundVal = found.getPrecise();
	var candidates = await flattenAllPromiseArrays(tryReplacement(found, trueTarget.toNumber(), negate, invert, layers));
	var bestMult = null;
	var propError = trueTarget.div(foundVal);
	var bestErr = foundVal.sub(trueTarget).abs();
	candidates.push(callOnAllPromiseArrays(findCandidatesFor(propError, Math.min(1, layers - 1), false), mult=>{
		var mval = mult.getPrecise();
		if (foundVal.mul(mval).sub(trueTarget).abs().comparedTo(bestErr)<0) {
			bestMult = mult;
			bestErr = foundVal.mul(mval).sub(trueTarget).abs();
		}
	}).then(e=>{
		if(bestMult!=null){
			var target = new BinaryFunctionExpression(BinaryFunction.MULTIPLY);
			target.setA(bestMult);
			target.setB(cloneExpression(found));
			return correctForInvNeg(target, negate, invert);
		}else{
			return [];
		}
	}));
	return candidates;
}

async function tryReplacement(found, trueTarget, negate, invert, layers) {
	var candidates = [];
	if (found.isUnaryFunction()) {
		internalTarget = found.undo(trueTarget);
		await replaceSingle(candidates, internalTarget, found.getChild(), layers, e => {
			var ex = new UnaryFunctionExpression(found.func);
			ex.setChild(e);
			return correctForInvNeg(ex, negate, invert);
		});
	} else if (found.isBinaryFunction()) {
		internalTargetA = found.undoA(trueTarget);
		await replaceSingle(candidates, internalTargetA, found.getA(), layers, e => {
			var ex = new BinaryFunctionExpression(found.func);
			ex.setA(e);
			ex.setB(cloneExpression(found.getB()));
			return correctForInvNeg(ex, negate, invert);
		});
		internalTargetB = found.undoB(trueTarget);
		await replaceSingle(candidates, internalTargetB, found.getB(), layers, e => {
			var ex = new BinaryFunctionExpression(found.func);
			ex.setB(e);
			ex.setA(cloneExpression(found.getA()));
			return correctForInvNeg(ex, negate, invert);
		});
	}
	return candidates;
}

async function replaceSingle(candidates, internalTarget, child, layers, attachToParent){
	if (isFinite(internalTarget)) {
		var bestRep = null;
		var bestErr = Math.abs(internalTarget - child.getValue());
		candidates.push(callOnAllPromiseArrays(findCandidatesFor(new Decimal(internalTarget), layers - 1, false), expression=>{
			if(Math.abs(internalTarget - expression.getValue()) < bestErr){
				bestRep = expression;
				bestErr = Math.abs(internalTarget - expression.getValue());
			}
		}).then(e=>{
			if (bestRep != null) {
				return attachToParent(bestRep);
			} else {
				return [];
			}
		}));
		if (layers > 0) {
			var bestSubRep = null;
			var bestSubErr = Math.abs(internalTarget - child.getValue());
			candidates.push(callOnAllPromiseArrays(tryReplacement(child, internalTarget, false, false, layers), expression=>{
				if(Math.abs(internalTarget - expression.getValue()) < bestSubErr){
					bestSubRep = expression;
					bestSubErr = Math.abs(internalTarget - expression.getValue());
				}
			}).then(e=>{
				if (bestSubRep != null) {
					return attachToParent(bestSubRep);
				} else {
					return [];
				}
			}));
		}
	}
}

function mapAllPromiseArrays(target, func){
	if(target instanceof Array){
		return target.map(e=>mapAllPromiseArrays(e, func));
	} else if(target instanceof Promise){
		return target.then(r=>mapAllPromiseArrays(r, func));
	}else{
		return func(target);
	}
}

async function flattenAllPromiseArrays(target){
	var results = [];
	if(target instanceof Array){
		var t = await Promise.all(target.map(e=>{
			return  flattenAllPromiseArrays(e);
		}));
		results.push(... t);
	} else if(target instanceof Promise){
		var t = await target;
		var a = await flattenAllPromiseArrays(t);
		results.push(... a);
	}else{
		results.push(target);
	}
	return results.flat(100);
}

function callOnAllPromiseArrays(target, func){
	if(target instanceof Array){
		return Promise.all(target.map(e=>callOnAllPromiseArrays(e, func)));
	} else if(target instanceof Promise){
		return target.then(r=>callOnAllPromiseArrays(r, func));
	}else{
		return func(target);
	}
}



async function fetchExpressionsFor(value, num){		// returns
												// Future<List<Expression>>
	var json = await fetch("/approx/lookup/"+value+"?range="+num+"&target="=targetFileSelector.value).then(resp=>resp.json());
	var vals = [];
	if(json.values){
		vals = json.values.map(e=>parseExpression(_base64ToArrayBuffer(e)));
	}
	return vals;
}

function cloneExpression(target) {		// returns Expression
	if (target.isBinaryFunction()) {
		var a = new BinaryFunctionExpression(target.func);
		a.setA(cloneExpression(target.getA()));
		a.setB(cloneExpression(target.getB()));
		return a;
	} else if (target.isUnaryFunction()) {
		var a = new UnaryFunctionExpression(target.func);
		a.setChild(cloneExpression(target.getChild()));
		return a;
	} else {
		return new ConstantExpression(target.constant);
	}
}

function correctForInvNeg(tgt, negate, invert) {		// returns Expression
	var target = tgt;
	if (invert) {
		if (target.isUnaryFunction() && target.func == UnaryFunction.INVERSE) {
			target = target.getChild();
		} else {
			var tmp = target;
			target = new UnaryFunctionExpression(UnaryFunction.INVERSE);
			target.setChild(tmp);
		}
	}
	if (negate) {
		if (target.isUnaryFunction() && target.func == UnaryFunction.NEGATE) {
			target = target.getChild();
		} else {
			var tmp = target;
			target = new UnaryFunctionExpression(UnaryFunction.NEGATE);
			target.setChild(tmp);
		}
	}
	return target;
}
