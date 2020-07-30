var text2 = document.getElementById("text2");

var input =  document.getElementById("numInput");

input.addEventListener('input', updateValue);
var isRunningUpdate = false;
var shouldReRun = false;

function updateValue(e) {
	var str = input.value.replace("x", "*").replace("\u00D7", "*").replace("\u2062", "*");
	str = str.replace("\u2212", "-").replace("\u002D", "-").replace("\uFE63", "-").replace("\uFF0D", "-");
	str = str.replace(/ /g, "").replace(/,/g, "").replace("*10^", "*10").replace("*10", "e");
	if(isFinite(Number(str))){
		var sfStr = str.replace(".", "").replace(/^0*/, "").replace(/e.*$/, "");
		var sfNum = sfStr.length;
		var num = new Decimal(str);
		if(!isRunningUpdate){
			if(num==0){
				text2.innerHTML = "Please enter a non-zero value";
			} else {
				isRunningUpdate = true;
				flattenAllPromiseArrays(findCandidatesFor(num, 0, true)).then(exs=>displayResults(exs, num, true, Math.min(20, sfNum))).then(a=>MathJax.typeset());
			}
		}else{
			shouldReRun = true;
		}
	}else{
		text2.innerHTML = "Not a valid number";
	}
}
function updatePrecise(){
	var str = input.value.replace("x", "*").replace("\u00D7", "*").replace("\u2062", "*");
	str = str.replace(/\u2212/g, "-").replace(/\u002D/g, "-").replace(/\uFE63/g, "-").replace(/\uFF0D/g, "-");
	str = str.replace(/ /g, "").replace(/,/g, "").replace("*10^", "*10").replace("*10", "e");
	if(isFinite(Number(str))){
		var sfStr = str.replace(".", "");
		var sfNum = sfStr.length;
		var num = new Decimal(str);
		isRunningUpdate = true;
		shouldReRun = false;
		flattenAllPromiseArrays(findCandidatesFor(num, 1, true)).then(exs=>displayResults(exs, num, false, Math.min(20, sfNum))).then(a=>MathJax.typeset());
	}else{
		text2.innerHTML = "Not a valid number";
	}
}

function displayResults(found, num, easy, sfNum){
	var ht2 = "";
	var els = [];
	var strs = [];
	found.forEach(f=>{
		var clf = clean(f);
		var ltxStr = new LaTeXRenderer().render(clf, num, f, sfNum);
		if(!strs.includes(ltxStr)){
			var el = {};
			el.ltxStr = ltxStr;
			el.ex = f;
			el.clex = clf;
			strs.push(ltxStr);
			els.push(el);
		}
	});
	els.forEach(el=>{
		var clex = el.clex;
		var ex = el.ex;
		var ltxStr = el.ltxStr;
		var precision = num.div(num.sub(ex.getPrecise())).abs().toNumber();
		if(precision>100){

			var precise = ex.getPrecise();
			var precisionStr;
			var sfStr;
			if(precise.equals(num)){
				precisionStr = "To computed precision (1 part in 10<sup>20</sup> \u00B1 rounding)";
				sfStr = "To "+20+" s.f.";
			}else{
				var err = num.div(precise.sub(num)).abs();
				var accuracyStr = err.toExponential(2).replace("e+", "\u00D710<sup>").replace("e-", "\u00D7 10<sup>-");
				if(accuracyStr.includes("<sup>")){
					accuracyStr+="</sup>";
				}
				sfStr = "To "+err.log().trunc()+" s.f.";
				precisionStr = "1 part in "+accuracyStr;
			}
			ht2 += "<button type=\"button\" class=\"swap-expression\" title=\"Show calculated value for comparison\">⇅</button>"+ltxStr+"<br>"+sfStr + "<button type=\"button\" class=\"collapsible\" title=\"More info on expression\"><img src=\"/approx-content/downarrow.svg\" alt=\"v\"></button><div class=\"content\">"+
			precisionStr+"<br>Expression value: "+precise+"<br>Unicode version: "+new UnicodeRenderer().render(clex, num)+"<br>Uncleaned: "+ex+"</div><hr>";
		}
	});
	
	if(easy){
		ht2 += "<button onclick=\"updatePrecise()\">Try Harder</button>";
	}

	text2.innerHTML = ht2;
	var coll = document.getElementsByClassName("collapsible");
	var i;

	for (i = 0; i < coll.length; i++) {
		  coll[i].addEventListener("click", function() {
		    this.classList.toggle("active");
		    var content = this.nextElementSibling;
		    if (content.style.display === "block") {
				this.innerHTML = "<img src=\"/approx-content/downarrow.svg\" alt=\"v\">";
				content.style.display = "none";
		    } else {
		    	this.innerHTML = "<img src=\"/approx-content/uparrow.svg\" alt=\"^\">";
	    		content.style.display = "block";
		    }
		  });
		}
	var swapExp = document.getElementsByClassName("swap-expression");
	for (i = 0; i < swapExp.length; i++) {
		swapExp[i].addEventListener("click", function() {
		    this.classList.toggle("active");
		    var content = this.nextElementSibling;
		    if (content.style.display === "inline") {
				content.style.display = "none";
				content.nextElementSibling.style.display = "inline";
		    } else {
	    		content.style.display = "inline";
				content.nextElementSibling.style.display = "none";
		    }
		  });
		}
	isRunningUpdate = false;
	if(shouldReRun){
		updateValue(1);
		shouldReRun = false;
	}
}
function _base64ToArrayBuffer(base64) {
    var binary_string = window.atob(base64);
    var len = binary_string.length;
    var bytes = new Uint8Array(len);
    for (var i = 0; i < len; i++) {
        bytes[i] = binary_string.charCodeAt(i);
    }
    return bytes;
}
updateValue(1);