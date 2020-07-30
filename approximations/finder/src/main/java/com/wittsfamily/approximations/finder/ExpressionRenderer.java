package com.wittsfamily.approximations.finder;

import com.wittsfamily.approximations.generator.UnaryFunction;

public interface ExpressionRenderer {

    String plus();

    String factorial();

    String function(UnaryFunction func);

    String bracket(String render);

    String integer(int value);

    String slantedFraction(String integer, String render);

    String fraction(String integer, String render);

    String ln(String string);

    String log(String render, String render2);

    String times();

    String negate(String target);

    String root(String render, String render2);

    String sqrt(String render);

    String power(String render, String render2);

    String e();

    String pi();

    String gr();

    String tetrate(String bracket, String integer);

}
