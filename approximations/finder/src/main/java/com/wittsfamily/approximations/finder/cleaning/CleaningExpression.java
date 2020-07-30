package com.wittsfamily.approximations.finder.cleaning;

import java.util.List;

import com.wittsfamily.approximations.finder.ExpressionRenderer;

public interface CleaningExpression {
	CleaningExpression getParent();

	void setParent(CleaningExpression parent);

	CleaningExpressionType getType();

	boolean cleanOne(boolean convertPowers); // flattens Multiplies and Adds, negates to outside
						// multiplies and functions, or removes if unnecessary, including on (-a)^5, not
						// on (-a)^e etc also a^(-b) -> 1/(a^b), generally moves inversions to outside

	boolean cleanTwo(); // combines powers, including (a^b)^c -> a^(b*c) and (a^b)*(a^c) -> a^(b+c), and
						// a*(a^b) -> a^(b+1), and a^c*b^c -> (a*b)^c

	boolean cleanThree(); // combines logs, moving log(b)+log(c) -> log(c*b)),also
							// moves powers out of logs, and logs out of powers so log(a^b) -> b*log(a),
							// also moves log(b)/log(a) -> log_a(b)
							// and a^(b*log_a(c)) -> c*a^b

	boolean cleanFour(); // adds and multiplies integers together, will not create integers above 100,
							// will leave multiplies/adds instead, combined into groups, < 100 each.
							// Also cancels 4/4, or 4-4, or 4e-4e, or(4+e)/(4+e), 4^2->16, 4^-2->1/16, etc.
							// Also 1/(a^b) -> a^(-b), and log_a(b)->1/log_b(a) if a is more costly than b
							// also sink negations into additions.

	String render(ExpressionRenderer r); // creates final string. Will remove multiply signs between 3*e, 3*(1+pi),
						// (1+pi)*(1-pi), etc.
						// Will group divides, move minus to top if constant, out if not

	public List<CleaningExpression> getExpressions();

	void replaceChild(CleaningExpression oldExp, CleaningExpression newExp);

	public boolean canNegate();

	public void negate();

	public double asDouble();

	public CleaningExpression clone();
}
