package com.wittsfamily.approximations.store;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;

import com.wittsfamily.approximations.generator.Parser;

public class ValueFileList implements ValueList {
	private final RandomAccessFile file;
	private long pos = 0;
	private final long length;
	private final Parser p = new Parser();

	public ValueFileList(RandomAccessFile file) throws IOException {
		this.file = file;
		length = file.length();
	}

	@Override
	public double getValueAsDouble() throws ParseException, IOException {
		file.seek(pos * 12);
		byte[] tmp = new byte[12];
		file.read(tmp);
		return p.parseNumeric(tmp);
	}

	@Override
	public byte[] getValue() throws IOException {
		byte[] tmp = new byte[12];
		file.seek(pos * 12);
		file.read(tmp);
		return tmp;
	}

	@Override
	public void setValue(byte[] val, int srcPos) throws IOException {
		file.seek(pos * 12);
		file.write(val, srcPos, 12);
	}

	@Override
	public void moveAlong() {
		pos++;
	}

	@Override
	public void moveValueTo(ValueList other) throws IOException {
		other.setValue(getValue(), 0);
	}

	@Override
	public boolean isAtEnd() throws IOException {
		return pos * 12 >= length;
	}

	@Override
	public void resetPos() {
		pos = 0;
	}

	@Override
	public int getValueCost() throws ParseException, IOException {
		file.seek(pos * 12);
		byte[] tmp = new byte[12];
		file.read(tmp);
		return p.parseExpression(tmp, 0, 12).getCost();
	}

	public void moveBack() {
		pos--;
	}
}
