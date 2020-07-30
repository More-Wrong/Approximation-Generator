package com.wittsfamily.approximations.generator;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import com.wittsfamily.approximations.generator.Parser;
import com.wittsfamily.approximations.store.FileTarget;

public class Generator {
	public Expression v;

	public int c = 0;

	public static void main(String[] args)
			throws InterruptedException, ExecutionException, ParseException, IOException, TimeoutException {

//		Parser p = new Parser();
//		UnaryFunctionValue v1 = new UnaryFunctionValue(UnaryFunction.SQUARE);
//		UnaryFunctionValue v1s1 = new UnaryFunctionValue(UnaryFunction.SIN);
//		UnaryFunctionValue v1s2 = new UnaryFunctionValue(UnaryFunction.NEGATE);
//		UnaryFunctionValue v1s3 = new UnaryFunctionValue(UnaryFunction.LN);
//		v1s3.setChild(new ConstantValue(Constant.FIVE));
//		v1s2.setChild(v1s3);
//		v1s1.setChild(v1s2);
//		v1.setChild(v1s1);
//		UnaryFunctionValue v2 = new UnaryFunctionValue(UnaryFunction.LN);
//		UnaryFunctionValue v2s1 = new UnaryFunctionValue(UnaryFunction.ROOT);
//		BinaryFunctionValue v2s2 = new BinaryFunctionValue(BinaryFunction.ADD);
//		UnaryFunctionValue v2s3 = new UnaryFunctionValue(UnaryFunction.INVERSE);
//		v2s3.setChild(new ConstantValue(Constant.E));
//		v2s2.setA(v2s3);
//		v2s2.setB(new ConstantValue(Constant.SEVEN));
//		v2s1.setChild(v2s2);
//		v2.setChild(v2s1);
//		System.out.println(v2.asDouble());
//		byte[] bytes = new byte[12];
//		int pos = v2.writeToBytes(bytes, 0);
//		for (int i = 0; i < 8 - pos % 8; i++) {
//			bytes[pos / 8] |= 1 << i;
//		}
//		for (int i = pos / 8 + 1; i < bytes.length; i++) {
//			bytes[i] = (byte) 0xFF;
//		}
//		System.out.println(p.parseNumeric(bytes));

		FileTarget ft = new FileTarget("approximation-generator-map-dump", new File("/var/tmp/AAG.tmp"));
		int cost = 50;
		ExecutorService pool = Executors.newFixedThreadPool(8);
		Future<?> mergeF = pool.submit(() -> ft.performFileMerges());
		List<Future<?>> resps = new ArrayList<>();
		BlockingQueue<TreeMap<Double, byte[]>> mapTransfer = new ArrayBlockingQueue<>(5);
		for (BinaryFunction f : BinaryFunction.values()) {
			if (f != BinaryFunction.ADD && f != BinaryFunction.SUBTRACT) {
				resps.add(pool.submit(() -> run(mapTransfer, e -> e == f, f.name(), cost)));
			}
		}
		for (UnaryFunction f : UnaryFunction.values()) {
			if (f != UnaryFunction.INVERSE && f != UnaryFunction.NEGATE) {
				resps.add(pool.submit(() -> run(mapTransfer, e -> e == f, f.name(), cost)));
			}
		}
		resps.add(pool.submit(() -> run(mapTransfer, e -> e.getClass() == Constant.class, "Consts", cost)));
		TreeMap<Double, byte[]> m = new TreeMap<>();
		Parser p = new Parser();
		while (!resps.isEmpty()) {
			TreeMap<Double, byte[]> tmpMap = mapTransfer.poll(10000, TimeUnit.MILLISECONDS);
			if (tmpMap != null) {
				while (tmpMap != null) {
					System.out.println(tmpMap.size());
					System.out.println(m.size());
					System.out.println();
					m.putAll(tmpMap);
					for (Entry<Double, byte[]> entry : tmpMap.entrySet()) {
						m.compute(entry.getKey(), (d, b) -> {
							try {
								if (b == null || p.parseExpression(b, 0, 12).getCost() > p
										.parseExpression(entry.getValue(), 0, 12).getCost()) {
									return entry.getValue();
								} else {
									return b;
								}
							} catch (ParseException e) {
								throw new RuntimeException(e);
							}
						});
					}
					if (m.size() > 5000000) {
						tmpMap = null;
					} else {
						tmpMap = mapTransfer.poll(1000, TimeUnit.MILLISECONDS);
					}
				}
				// m.putAll(tmpMap);
				System.out.println("dumping map: " + m.size());
				ft.dumpMap(m);
				System.out.println("dump complete");
				m = new TreeMap<>();
				// if(!ft.checkFile()) {
				// System.out.println("oops... file invalid");
				// }
			}
			System.out.println("remaining elements: " + mapTransfer.size());
			for (Iterator<Future<?>> iterator = resps.iterator(); iterator.hasNext();) {
				Future<?> future = iterator.next();
				if (future.isDone()) {
					future.get();
					iterator.remove();
				}
			}
		}
		mergeF.cancel(true);
		pool.shutdown();
		if (!pool.awaitTermination(12000, TimeUnit.SECONDS)) {
			throw new TimeoutException("Threadpool timeout exceeded");
		}
		System.out.println("merging maps");
		ft.combineFiles();
		System.out.println("merge complete");
		System.out.println(ft.checkFile());
		System.out.println(m.size());
	}

	private static void run(BlockingQueue<TreeMap<Double, byte[]>> mapTransfer, Predicate<Object> test, String name,
			int cost) {
		try {
			Parser p = new Parser();
			TreeMap<Double, byte[]> m = new TreeMap<>();
			Generator g = new Generator();
			g.v = new ConstantExpression(Constant.ONE);
			Expression a = g.v;
			while (a != null) {
				g.v = a;
				UnaryFunctionExpression t = null;
				if (a.asDouble() < 0) {
					t = new UnaryFunctionExpression(UnaryFunction.NEGATE);
					t.setChild(a);
					a = t;
				}
				if (a.asDouble() > 1) {
					t = new UnaryFunctionExpression(UnaryFunction.INVERSE);
					t.setChild(a);
					a = t;
				}
				final Expression tmp = a;
				m.compute(a.asDouble(), (d, b) -> {
					try {
						if (b == null || p.parseExpression(b, 0, 12).getCost() > tmp.getCost()) {
							byte[] bytes = new byte[12];
							int pos = tmp.writeToBytes(bytes, 0);
							for (int i = 0; i < 8 - pos % 8; i++) {
								bytes[pos / 8] |= 1 << i;
							}
							for (int i = pos / 8 + 1; i < bytes.length; i++) {
								bytes[i] = (byte) 0xFF;
							}
							return bytes;
						} else {
							return b;
						}
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
				});
				if (m.size() >= 2000000) {
					mapTransfer.put(m);
					m = new TreeMap<Double, byte[]>();
				}
				a = g.findChild(test, g.v, cost);
			}
			mapTransfer.put(m);
		} catch (InterruptedException e) {
		}
	}

	private Expression generateNextUnary(UnaryFunctionExpression v, int maxRemainingCost) {
		Expression ch = findChild(o -> v.getFunction().canBeChildOperation(o), v.getChild(), maxRemainingCost);
		if (ch != null) {
			v.setChild(ch);
		}
		return ch;
	}

	private Expression generateNextBinary(BinaryFunctionExpression v, int maxRemainingCost) {
		Expression curVal = null;
		while (true) {
			if (v.getA() != null) {
				curVal = findChild(o -> v.getFunction().canBeChildB(o), v.getB(),
						maxRemainingCost - v.getA().getCost());
			}
			if (curVal != null) {
				v.setB(curVal);
				return curVal;
			} else {
				v.setB(null);
				Expression a = findChild(o -> v.getFunction().canBeChildA(o), v.getA(), maxRemainingCost);
				if (a == null) {
					return null;
				}
				v.setA(a);
			}
		}
	}

	private Expression findChild(Predicate<Object> test, Expression child, int maxRemainingCost) {
		if (child == null || child.isConstant()) {
			child = findConst(test, (ConstantExpression) child, maxRemainingCost);
			if (child != null) {
				return child;
			}
		}
		if (child == null || child.isUnaryFunction()) {
			child = findUnary(test, (UnaryFunctionExpression) child, maxRemainingCost);
			if (child != null) {
				return child;
			}
		}
		if (child == null || child.isBinaryFunction()) {
			return findBinary(test, (BinaryFunctionExpression) child, maxRemainingCost);
		}
		return null;

	}

	private Expression findConst(Predicate<Object> test, ConstantExpression child, int maxRemainingCost) {
		int val;
		if (child != null) {
			val = child.getConst().ordinal();
		} else {
			val = 0;
		}
		if (child != null) {
			val++;
			while (val < Constant.values().length
					&& (Constant.values()[val].getCost() > maxRemainingCost || !test.test(Constant.values()[val]))) {
				val++;
			}
		}
		if (val >= Constant.values().length) {
			return null;
		} else {
			return new ConstantExpression(Constant.values()[val]);
		}
	}

	private Expression findUnary(Predicate<Object> test, UnaryFunctionExpression child, int maxRemainingCost) {
		Expression v = null;
		if (child != null) {
			do {
				v = generateNextUnary(child, maxRemainingCost - child.getFunction().getMinCost());
			} while (v != null && child.getCost() > maxRemainingCost);

		}
		if (v == null) {
			int current = 0;
			if (child != null) {
				current = child.getFunction().ordinal() + 1;
			}
			while (current < UnaryFunction.values().length) {
				while (current < UnaryFunction.values().length
						&& (UnaryFunction.values()[current].getMinCost() >= maxRemainingCost
								|| !test.test(UnaryFunction.values()[current]))) {
					current++;
				}
				if (current < UnaryFunction.values().length) {
					child = new UnaryFunctionExpression(UnaryFunction.values()[current]);
					do {
						v = generateNextUnary(child, maxRemainingCost - child.getFunction().getMinCost());
					} while (v != null && child.getCost() > maxRemainingCost);
					if (child != null) {
						return child;
					}
				}
				current++;
			}
			return null;
		} else {
			return child;
		}
	}

	private Expression findBinary(Predicate<Object> test, BinaryFunctionExpression child, int maxRemainingCost) {
		Expression v = null;
		if (child != null) {
			do {
				v = generateNextBinary(child, maxRemainingCost - child.getFunction().getMinCost());
			} while (v != null && child.getCost() > maxRemainingCost);
		}
		if (v == null) {
			int current = 0;
			if (child != null) {
				current = child.getFunction().ordinal() + 1;
			}
			if (maxRemainingCost > 30) {
				int a = 0;
			}
			while (current < BinaryFunction.values().length) {
				while (current < BinaryFunction.values().length
						&& (BinaryFunction.values()[current].getMinCost() >= maxRemainingCost
								|| !test.test(BinaryFunction.values()[current]))) {
					current++;
				}
				if (current < BinaryFunction.values().length) {
					child = new BinaryFunctionExpression(BinaryFunction.values()[current]);
					do {
						v = generateNextBinary(child, maxRemainingCost - child.getFunction().getMinCost());
					} while (v != null && child.getCost() > maxRemainingCost);
					if (v != null && child.getCost() <= maxRemainingCost) {
						return child;
					}
				}
				current++;
			}
			return null;
		} else {
			return child;
		}
	}
}
