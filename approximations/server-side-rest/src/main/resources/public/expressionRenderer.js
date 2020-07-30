/**
 * 
 */
class LaTeXRenderer{
	render(cleanedExpression, target, uncleaned, sfNum){
		var targetStr = target.toPrecision(sfNum).toString().replace("e+", "\u00D710^{").replace("e-", "\u00D710^{-");
		if(targetStr.includes("{")){
			targetStr+="}";
		}
		var actualStr = uncleaned.getPrecise().toPrecision(sfNum).toString().replace("e+", "\u00D710^{").replace("e-", "\u00D710^{-");
		if(actualStr.includes("{")){
			actualStr+="}";
		}
		
		var expStr = cleanedExpression.toRendered(this);
		return "<div class=\"maths\">\\(\\displaystyle{"+targetStr + "\\approx " + expStr + "}\\)</div>"+
			"<div class=\"maths-actual\">\\(\\displaystyle{"+actualStr + " = " + expStr + "}\\)</div>";
	}
	plus(){
		return "+";
	}
	minus(){
		return "-";
	}
	negate(){
		return "-";
	}
	invTimes(){
		return " ";
	}
	times(){
		return "\\times ";
	}
	fraction(a, b){
		return "\\frac{" + a + "}{" + b + "}";
	}
	slantedFraction(a, b){
		if(a=="1"){
			if(b=="2"){
				return "\u00BD";
			}else if(b=="3"){
				return "\\unicode[1,1][Ubuntu]{x2153}";
			}else if(b=="4"){
				return "\u00BC";
			}else if(b=="5"){
				return "\\unicode[1,1][Ubuntu]{x2155}";
			}else if(b=="6"){
				return "\\unicode[1,1][Ubuntu]{x2159}";
			}else if(b=="7"){
				return "\\unicode[1,1][Ubuntu]{x2150}";
			}else if(b=="8"){
				return "\\unicode[1,1][Ubuntu]{x215B}";
			}else if(b=="9"){
				return "\\unicode[1,1][Ubuntu]{x2151}";
			}else if(b=="10"){
				return "\\unicode[1,1][Ubuntu]{x2152}";
			}
		}else if(a=="2"){
			if(b=="3"){
				return "\\unicode[1,1][Ubuntu]{x2154}";
			}else if(b=="5"){
				return "\\unicode[1,1][Ubuntu]{x2156}";
			}
		}else if(a=="3"){
			if(b=="4"){
				return "\u00BE";
			}else if(b=="5"){
				return "\\unicode[1,1][Ubuntu]{x2157}";
			}else if(b=="8"){
				return "\\unicode[1,1][Ubuntu]{x215C}";
			}
		}else if(a=="4"){
			if(b=="5"){
				return "\\unicode[1,1][Ubuntu]{x2158}";
			}
		}else if(a=="5"){
			if(b=="6"){
				return "\\unicode[1,1][Ubuntu]{x215A}";
			}else if(b=="8"){
				return "\\unicode[1,1][Ubuntu]{x215D}";
			}
		}else if(a=="7"){
			if(b=="8"){
				return "\\unicode[1,1][Ubuntu]{x215E}";
			}
		}
		return "^{" + a + "}\u2044_{" + b + "}";
	}
	up(a, b){
		return a+"\\uparrow\\uparrow "+b;
	}
	lBracket(){
		return "\\left(";
	}
	rBracket(){
		return "\\right)";
	}
	bracket(child){
		return "\\left("+child+"\\right)";
	}
	sqrt(child){
		return "\\sqrt{"+child+"}";
	}
	nthRoot(a, b){
		return "\\sqrt["+a+"]{"+b+"}";
	}
	power(a, b){
		return a+"^{"+b+"}";
	}
	log(a, b){
		return "log_{"+a+"}"+b;
	}
	integer(a){
		return a;
	}
	e(){
		return "e ";
	}
	pi(){
		return "\\pi ";
	}
	goldenRatio(){
		return "\\phi ";
	}
	factorial(child){
		return child+"!";
	}
	func(func, child){
		return func+" "+child;
	}
}
class UnicodeRenderer{
	render(cleanedExpression, target){
		var targetStr = target.toSD(20).toString().replace("e+", "\u00D710^").replace("e-", "\u00D710^-");
		return targetStr + " \u2248 " + cleanedExpression.toRendered(this);
	}
	plus(){
		return "+";
	}
	minus(){
		return "-";
	}
	negate(){
		return "-";
	}
	invTimes(){
		return "";
	}
	times(){
		return "\u00D7";
	}
	fraction(a, b){
		return "(" + a + ")/(" + b + ")";
	}
	slantedFraction(a, b){
		if(a=="1"){
			if(b=="2"){
				return "\u00BD";
			}else if(b=="3"){
				return "\u2153";
			}else if(b=="4"){
				return "\u00BC";
			}else if(b=="5"){
				return "\u2155";
			}else if(b=="6"){
				return "\u2159";
			}else if(b=="7"){
				return "\u2150";
			}else if(b=="8"){
				return "\u215B";
			}else if(b=="9"){
				return "\u2151";
			}else if(b=="10"){
				return "\u2152";
			}
		}else if(a=="2"){
			if(b=="3"){
				return "\u2154";
			}else if(b=="5"){
				return "\u2156";
			}
		}else if(a=="3"){
			if(b=="4"){
				return "\u00BE";
			}else if(b=="5"){
				return "\u2157";
			}else if(b=="8"){
				return "\u215C";
			}
		}else if(a=="4"){
			if(b=="5"){
				return "\u2158";
			}
		}else if(a=="5"){
			if(b=="6"){
				return "\u215A";
			}else if(b=="8"){
				return "\u215D";
			}
		}else if(a=="7"){
			if(b=="8"){
				return "\u215E";
			}
		}
		return " " + a + "/" + b + " ";
	}
	up(a, b){
		return a+" \u2191\u2191 "+b;
	}
	lBracket(){
		return "(";
	}
	rBracket(){
		return ")";
	}
	bracket(child){
		return "("+child+")";
	}
	sqrt(child){
		if(child.length == 1){
			return "\u221A"+child+"";
		}else{
			return "\u221A("+child+")";
		}
	}
	nthRoot(a, b){
		if(a=="3"){
			if(b.length == 1){
				return "\u221B "+b+"";
			}else{
				return "\u221B("+b+")";
			}
		}else if(a=="4"){
			if(b.length == 1){
				return "\u221C "+b+"";
			}else{
				return "\u221C("+b+")";
			}
		}else if(b.length == 1){
			return b+"^(1/"+a+")";
		}else{
			return "("+b+")^(1/"+a+")";
		}
	}
	power(a, b){
		var str = a;
		if(b=="2"){
			return str+"\u00B2";
		}else if(b=="3"){
			return str+"\u00B3";
		}else if(b=="4"){
			return str+"\u2074";
		}else if(b=="5"){
			return str+"\u2075";
		}else if(b=="6"){
			return str+"\u2076";
		}else if(b=="7"){
			return str+"\u2077";
		}else if(b=="8"){
			return str+"\u2078";
		}else if(b=="9"){
			return str+"\u2079";
		}else if(b.length == 1){
			return str+"^"+b;
		}else{
			return str+"^("+b+")";
		}
	}
	log(a, b){
		var str = "log";
		if(a=="2"){
			str += "\u2082";
		} else if(a=="3") {
			str += "\u2083";
		} else if(a=="4") {
			str += "\u2084";
		} else if(a=="5") {
			str += "\u2085";
		} else if(a=="6") {
			str += "\u2086";
		} else if(a=="7") {
			str += "\u2087";
		} else if(a=="8") {
			str += "\u2088";
		} else if(a=="9") {
			str += "\u2089";
		} else if(a.length == 1){
			str += "_"+a+" ";
		}else{
			str += "_("+a+") ";
		}
		return str + b;
	}
	integer(a){
		return ""+a;
	}
	e(){
		return "e";
	}
	pi(){
		return "\u03C0";
	}
	goldenRatio(){
		return "\u03D5";
	}
	factorial(child){
		return child+"!";
	}
	func(func, child){
		return func+" "+child;
	}
}
this.LaTeXRenderer = LaTeXRenderer;
this.UnicodeRenderer = UnicodeRenderer;