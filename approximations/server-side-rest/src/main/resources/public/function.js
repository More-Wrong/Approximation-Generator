/**
 * 
 */
Decimal.config({ precision: 20 });

class Constant{
	constructor(value, store, length, precise, name)
	{
		this.value = value;
		this.store = store;
		this.length = length;
		this.precise = precise;
		this.name = name;
	}
}
Constant.ONE = new Constant(1, 0, 4, new Decimal(1), "1");
Constant.TWO = new Constant(2, 1, 4, new Decimal(2), "2");
Constant.THREE = new Constant(3, 0x10, 5, new Decimal(3), "3");
Constant.FOUR = new Constant(4, 0x17, 5, new Decimal(4), "4");
Constant.FIVE = new Constant(5, 0x11, 5, new Decimal(5), "5");
Constant.SIX = new Constant(6, 0x12, 5, new Decimal(6), "6");
Constant.SEVEN = new Constant(7, 0x18, 5, new Decimal(7), "7");
Constant.EIGHT = new Constant(8, 0x13, 5, new Decimal(8), "8");
Constant.NINE = new Constant(9, 0x19, 5, new Decimal(9), "9");
Constant.TEN = new Constant(10, 0x5, 4, new Decimal(10), "10");
Constant.E = new Constant(Math.E, 0x3, 4, new Decimal(1).exp(), "e");
Constant.PI = new Constant(Math.PI, 0x4, 4, new Decimal(1).atan().mul(new Decimal(4)), "pi");
Constant.GOLDEN_RATIO = new Constant((Math.sqrt(5)+1)/2, 0x2, 4, new Decimal(5).sqrt().add(new Decimal(1)).div(new Decimal(2)), "gr");
Constant.ELEVEN = new Constant(11, 0x3C, 6, new Decimal(11), "11");
Constant.THIRTEEN = new Constant(13, 0x7A, 7, new Decimal(13), "13");

Constant.values = [Constant.ONE, Constant.TWO, Constant.THREE, Constant.FOUR, Constant.FIVE, Constant.SIX, Constant.SEVEN, Constant.EIGHT, Constant.NINE, Constant.TEN, Constant.E, Constant.PI, Constant.GOLDEN_RATIO, Constant.ELEVEN, Constant.THIRTEEN];
this.Constant = Constant;

class UnaryFunction{
	constructor(func, store, length, undo, precise, name)
	{
		this.func = func;
		this.store = store;
		this.length = length;
		this.undo = undo;
		this.precise = precise;
		this.name = name;
	}
}
UnaryFunction.INVERSE = new UnaryFunction(function(a){return 1/a;}, 0x7, 4, function(a){return 1/a;}, 
		function(a){return Constant.ONE.precise.div(a);}, "1/");

UnaryFunction.LN = new UnaryFunction(function(a){return Math.log(a);}, 0x1A, 5, function(a){return Math.exp(a);}, 
		function(a){return a.ln();}, "ln");

UnaryFunction.NEGATE = new UnaryFunction(function(a){return -a;}, 0x6, 4, function(a){return -a;}, 
		function(a){return a.neg();}, "-");

UnaryFunction.EXP = new UnaryFunction(function(a){return Math.exp(a);}, 0x15, 5, function(a){return Math.log(a);}, 
		function(a){return a.exp();}, "e^");

UnaryFunction.SQUARE = new UnaryFunction(function(a){return a*a;}, 0x14, 5, function(a){return Math.sqrt(a);}, 
		function(a){return a.mul(a);}, "sqr");

UnaryFunction.ROOT = new UnaryFunction(function(a){return Math.sqrt(a);}, 0x16, 5, function(a){return a*a;}, 
		function(a){return a.sqrt();}, "sqrt");

UnaryFunction.SIN = new UnaryFunction(function(a){return Math.sin(a);}, 0xF8, 8, function(a){return Math.asin(a);}, 
		function(a){return a.sin();}, "sin");

UnaryFunction.COS = new UnaryFunction(function(a){return Math.cos(a);}, 0xF9, 8, function(a){return Math.acos(a);}, 
		function(a){return a.cos();}, "cos");

UnaryFunction.TAN = new UnaryFunction(function(a){return Math.tan(a);}, 0x1F4, 9, function(a){return Math.atan(a);}, 
		function(a){return a.tan();}, "tan");

UnaryFunction.ARCSIN = new UnaryFunction(function(a){return Math.asin(a);}, 0x1F6, 9, function(a){return Math.sin(a);}, 
		function(a){return a.asin();}, "arcsin");

UnaryFunction.ARCCOS = new UnaryFunction(function(a){return Math.acos(a);}, 0x1F7, 9, function(a){return Math.cos(a);}, 
		function(a){return a.acos();}, "arccos");

UnaryFunction.ARCTAN = new UnaryFunction(function(a){return Math.atan(a);}, 0x1F8, 9, function(a){return Math.tan(a);}, 
		function(a){return a.atan();}, "arctan");

UnaryFunction.SINH = new UnaryFunction(function(a){return Math.sinh(a);}, 0x1F9, 9, function(a){return Math.asinh(a);}, 
		function(a){return a.sinh();}, "sinh");

UnaryFunction.COSH = new UnaryFunction(function(a){return Math.cosh(a);}, 0x1FA, 9, function(a){return Math.acosh(a);}, 
		function(a){return a.cosh();}, "cosh");

UnaryFunction.TANH = new UnaryFunction(function(a){return Math.tanh(a);}, 0x1FB, 9, function(a){return Math.atanh(a);}, 
		function(a){return a.tanh();}, "tanh");

UnaryFunction.ARSINH = new UnaryFunction(function(a){return Math.asinh(a);}, 0x1FC, 9, function(a){return Math.sinh(a);}, 
		function(a){return a.asinh();}, "arsinh");

UnaryFunction.ARCOSH = new UnaryFunction(function(a){return Math.acosh(a);}, 0x1FD, 9, function(a){return Math.cosh(a);}, 
		function(a){return a.acosh();}, "arcosh");

UnaryFunction.ARTANH = new UnaryFunction(function(a){return Math.atanh(a);}, 0x1FE, 9, function(a){return Math.tanh(a);}, 
		function(a){return a.atanh();}, "artanh");

UnaryFunction.FACTORIAL = new UnaryFunction(function(a){
	var current = 1;
	for(i=2;i<=a;i++){
		current*=i;
	}
	return current;}, 0x7B, 7, function(a){return NaN;}, 
		function(a){
		var current = Constant.ONE.precise;
		for(i=2;i<=a.toNumber();i++){
			current=current.mul(new Decimal(i));
		}
		return current;}, "factorial");
UnaryFunction.values = [UnaryFunction.INVERSE, UnaryFunction.LN, UnaryFunction.NEGATE, UnaryFunction.EXP, UnaryFunction.SQUARE, UnaryFunction.ROOT, UnaryFunction.SIN, UnaryFunction.COS, UnaryFunction.TAN
, UnaryFunction.ARCSIN, UnaryFunction.ARCCOS, UnaryFunction.ARCTAN, UnaryFunction.SINH, UnaryFunction.COSH, UnaryFunction.TANH, UnaryFunction.ARSINH, UnaryFunction.ARCOSH, UnaryFunction.ARTANH, UnaryFunction.FACTORIAL];

this.UnaryFunction = UnaryFunction;

class BinaryFunction{
	constructor(func, store, length, undoA, undoB, precise, name)
	{
		this.func = func;
		this.store = store;
		this.length = length;
		this.undoA = undoA;
		this.undoB = undoB;
		this.precise = precise;
		this.name = name
	}
}
BinaryFunction.ADD = new BinaryFunction(function(a,b){return a+b;},0x36,6,function(t,b){return t-b;},function(t,a){return t-a;},
		function(a,b){return a.add(b);}, "+");

BinaryFunction.SUBTRACT = new BinaryFunction(function(a,b){return a-b;},0x37,6,function(t,b){return t+b;},function(t,a){return a-t;},
		function(a,b){return a.sub(b);}, "-");

BinaryFunction.MULTIPLY = new BinaryFunction(function(a,b){return a*b;},0x38,6,function(t,b){return t/b;},function(t,a){return t/a;},
		function(a,b){return a.mul(b);}, "*");

BinaryFunction.DIVIDE = new BinaryFunction(function(a,b){return a/b;},0x39,6,function(t,b){return t*b;},function(t,a){return a/t;},
		function(a,b){return a.div(b);}, "/");

BinaryFunction.POWER = new BinaryFunction(function(a,b){return Math.pow(a,b);},0x3A,6,function(t,b){return Math.pow(t,1/b);},function(t,a){return Math.log(t)/Math.log(a);},
		function(a,b){return a.pow(b);}, "^");

BinaryFunction.LOG = new BinaryFunction(function(a,b){return Math.log(b)/Math.log(a);},0x3B,6,function(t,b){return Math.pow(b, t);},function(t,a){return Math.pow(a, 1 / t);},
		function(a,b){return b.log(a);}, "log");

BinaryFunction.UP = new BinaryFunction(function(a,b){
	var current = a;
	for(i=1;i<b;i++){
		current=Math.pow(a,current);
	}
	return current;}, 0x1F5, 9, function(a){return NaN;}, function(a){return NaN;}, 
		function(a, b){
		var current = a;
		for(i=1;i<b.toNumber();i++){
			current=a.pow(current);
		}
		return current;}, "^^");


BinaryFunction.values = [BinaryFunction.ADD, BinaryFunction.SUBTRACT, BinaryFunction.MULTIPLY, BinaryFunction.DIVIDE, BinaryFunction.POWER, BinaryFunction.LOG, BinaryFunction.UP];

this.BinaryFunction = BinaryFunction;