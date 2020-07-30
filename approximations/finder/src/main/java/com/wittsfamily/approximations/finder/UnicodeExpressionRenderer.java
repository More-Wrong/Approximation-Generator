package com.wittsfamily.approximations.finder;

import com.wittsfamily.approximations.generator.UnaryFunction;

public class UnicodeExpressionRenderer implements ExpressionRenderer {
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
        return "(" + render + ")";
    }

    @Override
    public String integer(int value) {
        return value + "";
    }

    @Override
    public String slantedFraction(String a, String b) {
        if (a.equals("1")) {
            if (b.equals("2")) {
                return "\u00BD";
            } else if (b.equals("3")) {
                return "\u2153";
            } else if (b.equals("4")) {
                return "\u00BC";
            } else if (b.equals("5")) {
                return "\u2155";
            } else if (b.equals("6")) {
                return "\u2159";
            } else if (b.equals("7")) {
                return "\u2150";
            } else if (b.equals("8")) {
                return "\u215B";
            } else if (b.equals("9")) {
                return "\u2151";
            } else if (b == "10") {
                return "\u2152";
            }
        } else if (a.equals("2")) {
            if (b.equals("3")) {
                return "\u2154";
            } else if (b.equals("5")) {
                return "\u2156";
            }
        } else if (a.equals("3")) {
            if (b.equals("4")) {
                return "\u00BE";
            } else if (b.equals("5")) {
                return "\u2157";
            } else if (b.equals("8")) {
                return "\u215C";
            }
        } else if (a.equals("4")) {
            if (b.equals("5")) {
                return "\u2158";
            }
        } else if (a.equals("5")) {
            if (b.equals("6")) {
                return "\u215A";
            } else if (b.equals("8")) {
                return "\u215D";
            }
        } else if (a.equals("7")) {
            if (b.equals("8")) {
                return "\u215E";
            }
        }
        return "" + a + "/" + b + "";
    }

    @Override
    public String fraction(String a, String b) {
        return "(" + a + ")/(" + b + ")";
    }

    @Override
    public String ln(String string) {
        return "ln" + string;
    }

    @Override
    public String log(String render, String render2) {
        return " log_{" + render + "}" + render2;
    }

    @Override
    public String times() {
        return "\u00D7";
    }

    @Override
    public String negate(String target) {
        return "-" + target;
    }

    @Override
    public String root(String a, String b) {
        if (a.equals("3")) {
            if (b.length() == 1) {
                return "\u221B " + b + "";
            } else {
                return "\u221B(" + b + ")";
            }
        } else if (a.equals("4")) {
            if (b.length() == 1) {
                return "\u221C " + b + "";
            } else {
                return "\u221C(" + b + ")";
            }
        } else if (b.length() == 1) {
            return b + "^(1/" + a + ")";
        } else {
            return "(" + b + ")^(1/" + a + ")";
        }
    }

    @Override
    public String sqrt(String child) {
        if (child.length() == 1) {
            return "\u221A" + child + "";
        } else {
            return "\u221A(" + child + ")";
        }
    }

    @Override
    public String power(String a, String b) {
        if (b.equals("2")) {
            return a + "\u00B2";
        } else if (b.equals("3")) {
            return a + "\u00B3";
        } else if (b.equals("4")) {
            return a + "\u2074";
        } else if (b.equals("5")) {
            return a + "\u2075";
        } else if (b.equals("6")) {
            return a + "\u2076";
        } else if (b.equals("7")) {
            return a + "\u2077";
        } else if (b.equals("8")) {
            return a + "\u2078";
        } else if (b.equals("9")) {
            return a + "\u2079";
        } else if (b.length() == 1) {
            return a + "^" + b;
        } else {
            return a + "^(" + b + ")";
        }
    }

    @Override
    public String e() {
        return "e";
    }

    @Override
    public String pi() {
        return "\u03C0";
    }

    @Override
    public String gr() {
        return "\u03D5";
    }

    @Override
    public String tetrate(String a, String b) {
        return a + " \u2191\u2191 " + b;
    }

}
