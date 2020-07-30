package com.wittsfamily.approximations.finder;

import com.wittsfamily.approximations.generator.UnaryFunction;

public class LaTeXExpressionRenderer implements ExpressionRenderer {

	@Override
	public String plus() {
		return "+";
	}

	@Override
	public String factorial() {
		return "!";
	}

	@Override
	public String function(UnaryFunction func) {
		return func.name().toLowerCase();
	}

	@Override
	public String bracket(String render) {
		return "\\left(" + render + "\\right)";
	}

	@Override
	public String integer(int value) {
		return value + "";
	}

	@Override
	public String slantedFraction(String integer, String render) {
		return "\\sfrac{" + integer + "}{" + render + "}";
	}

	@Override
	public String fraction(String integer, String render) {
		return "\\frac{" + integer + "}{" + render + "}";
	}

	@Override
	public String ln(String string) {
		return " ln " + string;
	}

	@Override
	public String log(String render, String render2) {
		return " log_{" + render + "}" + render2;
	}

	@Override
	public String times() {
		return "\\times ";
	}

	@Override
	public String negate(String target) {
		return "-" + target;
	}

	@Override
	public String root(String render, String render2) {
		return "\\sqrt[" + render + "]{" + render2 + "}";
	}

	@Override
	public String sqrt(String render) {
		return "\\sqrt{" + render + "}";
	}

	@Override
	public String power(String render, String render2) {
		return render + "^{" + render2 + "}";
	}

	@Override
	public String e() {
		return " e ";
	}

	@Override
	public String pi() {
		return "\\pi ";
	}

	@Override
	public String gr() {
		return "\\phi ";
	}

	@Override
	public String tetrate(String s, String integer) {
		return s + "\\uparrow\\uparrow " + integer;
	}

}
