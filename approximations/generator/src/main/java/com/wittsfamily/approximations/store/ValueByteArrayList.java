package com.wittsfamily.approximations.store;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;

import com.wittsfamily.approximations.generator.Parser;

public class ValueByteArrayList implements ValueList {
	private final byte[] array;
	private int pos = 0;
	private final Parser p = new Parser();

	public ValueByteArrayList(byte[] array) {
		this.array = array;
	}

	@Override
	public double getValueAsDouble() throws ParseException {
		return p.parseNumeric(array, pos * 12, 12);
	}

	@Override
	public byte[] getValue() {
		return Arrays.copyOfRange(array, pos * 12, pos * 12 + 12);
	}

	@Override
	public void setValue(byte[] val, int srcPos) {
		System.arraycopy(val, srcPos, array, pos * 12, 12);
	}

	@Override
	public void moveAlong() {
		pos++;
	}

	@Override
	public void moveValueTo(ValueList other) throws IOException {
		other.setValue(array, pos * 12);
	}

	public int getPos() {
		return pos;
	}

	@Override
	public boolean isAtEnd() {
		return pos * 12 >= array.length;
	}

	@Override
	public void resetPos() {
		pos = 0;
	}

	@Override
	public int getValueCost() throws ParseException, IOException {
		return p.parseExpression(array, pos * 12, 12).getCost();
	}

}
