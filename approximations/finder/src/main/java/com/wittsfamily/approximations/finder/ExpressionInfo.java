package com.wittsfamily.approximations.finder;

public class ExpressionInfo {
    private final String laTeX;
    private final String unicode;
    private final String uncleaned;
    private final String precision;
    private final String preciseValue;

    public ExpressionInfo(String laTeX, String unicode, String uncleaned, String precision, String preciseValue) {
        this.laTeX = laTeX;
        this.unicode = unicode;
        this.uncleaned = uncleaned;
        this.precision = precision;
        this.preciseValue = preciseValue;
    }

    public String getLaTeX() {
        return laTeX;
    }

    public String getUnicode() {
        return unicode;
    }

    public String getUncleaned() {
        return uncleaned;
    }

    public String getPrecision() {
        return precision;
    }

    public String getPreciseValue() {
        return preciseValue;
    }

}
