package com.wittsfamily.approximations.store;

import java.io.IOException;
import java.text.ParseException;

public interface ValueList {
	public double getValueAsDouble() throws ParseException, IOException;
	public int getValueCost() throws ParseException, IOException;

	public byte[] getValue() throws IOException;

	public void setValue(byte[] val, int srcPos) throws IOException;

	public void moveAlong();

	public void moveValueTo(ValueList other) throws IOException;

	public boolean isAtEnd() throws IOException;

	public void resetPos();
}
