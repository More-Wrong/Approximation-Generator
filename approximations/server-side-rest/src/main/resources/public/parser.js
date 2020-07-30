/**
 * 
 */
function parseNumeric(target) {
	var len = target.length;
	var stck = [];
	var i = 0;
	while (true) {
		var currentVal = 0;
		var length = 0;
		for (j = 0; i < len * 8 && j < 4; j++, i++) {
			currentVal <<= 1;
			currentVal |= ((target[Math.floor(i / 8)] >>> (7 - i % 8)) & 1);
			length++;
		}
		while (i < len * 8) {
			if (constLookup(currentVal, length) != null) {
				stck.push(constLookup(currentVal, length).value);
				break;
			} else if (unaryLookup(currentVal, length) != null) {
				stck.push(unaryLookup(currentVal, length).func(stck.pop()));
				break;
			} else if (binaryLookup(currentVal, length) != null) {
				b = stck.pop();
				stck.push(binaryLookup(currentVal, length)
						.func(stck.pop(), b));
				break;
			}
			if (length == 9) {
				break;
			}
			currentVal <<= 1;
			currentVal |= ((target[Math.floor(i / 8)] >>> (7 - i % 8)) & 1);
			length++;
			i++;
		}
		if (i >= len * 8) {
			return stck.pop();
		}
	}
}

function parseExpression(target) {
	var len = target.length; 
	var stck = [];
	var i = 0;
	while (true) {
		var currentVal = 0;
		var length = 0;
		for (j = 0; i < len * 8 && j < 4; j++, i++) {
			currentVal <<= 1;
			currentVal |= ((target[Math.floor(i / 8)] >>> (7 - i % 8)) & 1);
			length++;
		}
		while (i < len * 8) {
			if (constLookup(currentVal, length) != null) {
				stck.push(new ConstantExpression(constLookup(currentVal,
						length)));
				break;
			} else if (unaryLookup(currentVal, length) != null) {
				v = new UnaryFunctionExpression(unaryLookup(currentVal, length));
				v.setChild(stck.pop());
				stck.push(v);
				break;
			} else if (binaryLookup(currentVal, length) != null) {
				v = new BinaryFunctionExpression(binaryLookup(currentVal,
						length));
				v.setB(stck.pop());
				v.setA(stck.pop());
				stck.push(v);
				break;
			}
			if (length == 9) {
				break;
			}
			currentVal <<= 1;
			currentVal |= ((target[Math.floor(i / 8)] >>> (7 - i % 8)) & 1);
			length++;
			i++;
		}
		if (i >= len * 8) {
			return stck.pop();
		}
	}
}

function binaryLookup(val, length) {
	return BinaryFunction.values.find(f => f.length == length && f.store == val);
}

function unaryLookup(val, length) {
	return UnaryFunction.values.find(f => f.length == length && f.store == val);
}

function constLookup(val, length) {
	return Constant.values.find(f => f.length == length && f.store == val);
}
this.parseNumeric = parseNumeric;
this.parseExpression = parseExpression;