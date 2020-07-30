package com.wittsfamily.approximations.generator;

import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.Deque;

public class Parser {
	public double parseNumeric(byte[] target) throws ParseException {
		return parseNumeric(target, 0, target.length);
	}

	public double parseNumeric(byte[] target, int from, int len) throws ParseException {
		Deque<Double> stack = new ArrayDeque<>();
		int i = 0;
		while (true) {
			int currentVal = 0;
			int length = 0;
			for (int j = 0; i < len * 8 && j < 4; j++, i++) {
				currentVal <<= 1;
				currentVal |= ((target[from + i / 8] >>> (7 - i % 8)) & 1);
				length++;
			}
			while (i < len * 8) {
				if (constLookup(currentVal, length) != null) {
					stack.push(constLookup(currentVal, length).getValue());
					break;
				} else if (unaryLookup(currentVal, length) != null) {
					stack.push(unaryLookup(currentVal, length).getValue(stack.pop()));
					break;
				} else if (binaryLookup(currentVal, length) != null) {
					double b = stack.pop();
					stack.push(binaryLookup(currentVal, length).getValue(stack.pop(), b));
					break;
				}
				if (length == 9) {
					break;
				}
				currentVal <<= 1;
				currentVal |= ((target[from + i / 8] >>> (7 - i % 8)) & 1);
				length++;
				i++;
			}
			if (i >= len * 8) {
				if (stack.size() != 1) {
					System.out.println();
					for (int j = 0; j < target.length; j++) {
						System.out.print(Integer.toHexString(Byte.toUnsignedInt(target[j])) + "  ");
					}
					System.out.println();
					System.out.println();
					for (int j = from; j < from + len; j++) {
						System.out.print(Integer.toHexString(Byte.toUnsignedInt(target[j])) + "  ");
					}
					System.out.println();
					System.out.println("from: "+from);
					System.out.println();
					throw new ParseException("No function found, but multiple arguments remain: " + stack, len * 8);
				} else {
					return stack.pop();
				}
			}
		}
	}

	public Expression parseExpression(byte[] target, int from, int len) throws ParseException {
		Deque<Expression> stack = new ArrayDeque<>();
		int i = 0;
		while (true) {
			int currentVal = 0;
			int length = 0;
			for (int j = 0; i < len * 8 && j < 4; j++, i++) {
				currentVal <<= 1;
				currentVal |= ((target[from + i / 8] >>> (7 - i % 8)) & 1);
				length++;
			}
			while (i < len * 8) {
				if (constLookup(currentVal, length) != null) {
					stack.push(new ConstantExpression(constLookup(currentVal, length)));
					break;
				} else if (unaryLookup(currentVal, length) != null) {
					UnaryFunctionExpression v = new UnaryFunctionExpression(unaryLookup(currentVal, length));
					v.setChild(stack.pop());
					stack.push(v);
					break;
				} else if (binaryLookup(currentVal, length) != null) {
					BinaryFunctionExpression v = new BinaryFunctionExpression(binaryLookup(currentVal, length));
					v.setB(stack.pop());
					v.setA(stack.pop());
					stack.push(v);
					break;
				}
				if (length == 9) {
					break;
				}
				currentVal <<= 1;
				currentVal |= ((target[from + i / 8] >>> (7 - i % 8)) & 1);
				length++;
				i++;
			}
			if (i >= len * 8) {
				if (stack.size() != 1) {
					for (int j = from; j < from + len; j++) {
						System.out.print(Integer.toHexString(Byte.toUnsignedInt(target[j])) + "  ");
					}
					System.out.println();
					System.out.println("from: "+from);
					System.out.println();
					throw new ParseException("No function found, but multiple arguments remain: " + stack, len);
				} else {
					return stack.pop();
				}
			}
		}
	}

	public BinaryFunction binaryLookup(int val, int length) {
		for (BinaryFunction f : BinaryFunction.values()) {
			if (f.getLength() == length && f.getStore() == val) {
				return f;
			}
		}
		return null;
	}

	public UnaryFunction unaryLookup(int val, int length) {
		for (UnaryFunction f : UnaryFunction.values()) {
			if (f.getLength() == length && f.getStore() == val) {
				return f;
			}
		}
		return null;
	}

	public Constant constLookup(int val, int length) {
		for (Constant f : Constant.values()) {
			if (f.getLength() == length && f.getStore() == val) {
				return f;
			}
		}
		return null;
	}

}
