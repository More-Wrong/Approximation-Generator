/**
 * 
 */
class ConstantExpression{
	constructor(constant){
		this.constant = constant;
	}
	getValue(){
		return this.constant.value;
	}
	getPrecise(){
		return this.constant.precise;
	}
	toString(){
		return this.constant.name;
	}
	isConstant(){return true;}
	isUnaryFunction(){return false;}
	isBinaryFunction(){return false;}
}
this.ConstantExpression = ConstantExpression;
class UnaryFunctionExpression{
	constructor(func){
		this.func = func;
		this.child = null;
		this.precise = null;
	}
	setChild(child){
		this.child = child;
		this.precise = null;
		this.value = this.func.func(child.getValue());
	}
	getChild(){
		return this.child;
	}
	getValue(){
		return this.value;
	}
	undo(t){
		return this.func.undo(t);
	}
	getPrecise(){
		if(this.precise==null){
			this.precise = this.func.precise(this.child.getPrecise());
		}
		return this.precise;
	}
	toString(){
		return this.func.name+" ("+this.child.toString()+")";
	}
	isConstant(){return false;}
	isUnaryFunction(){return true;}
	isBinaryFunction(){return false;}
}
this.UnaryFunctionExpression = UnaryFunctionExpression;
class BinaryFunctionExpression{
	constructor(func){
		this.func = func;
		this.a = null;
		this.b = null;
		this.precise = null;
	}
	setA(a){
		this.a = a;
		this.precise = null;
		if(this.b!==null){
			this.value = this.func.func(this.a.getValue(),this.b.getValue());
		}
	}
	setB(b){
		this.b = b;
		this.precise = null;
		if(this.a!==null){
			this.value = this.func.func(this.a.getValue(),this.b.getValue());
		}
	}
	getA(){
		return this.a;
	}
	getB(){
		return this.b;
	}
	getValue(){
		return this.value;
	}
	undoA(t){
		return this.func.undoA(t, this.b.getValue());
	}
	undoB(t){
		return this.func.undoB(t, this.a.getValue());
	}
	getPrecise(){
		if(this.precise==null){
			this.precise = this.func.precise(this.a.getPrecise(), this.b.getPrecise());
		}
		return this.precise;
	}
	toString(){
		return "("+this.a.toString()+") "+this.func.name+" ("+this.b.toString()+")";
	}
	isConstant(){return false;}
	isUnaryFunction(){return false;}
	isBinaryFunction(){return true;}
}
this.BinaryFunctionExpression = BinaryFunctionExpression;