package com.wittsfamily.approximations.finder;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.wittsfamily.approximations.generator.Parser;

public class FileRangeFinder {
	private final FileSearcher searcher;
	private final Parser p = new Parser();

	public FileRangeFinder(FileSearcher searcher) {
		this.searcher = searcher;
	}

	public List<byte[]> find(double value, int range) throws IOException, ParseException {
		try {
			long bestPos = searcher.binarySearch(value);
			byte[] best = searcher.getBytesAt(bestPos);
			List<byte[]> val = new ArrayList<>();
			if (Arrays.equals(best, new byte[12])) {
				return List.of();
			}
			val.add(best);
			for (int i = 1; i < range; i++) {
				byte[] low = searcher.getBytesAt(bestPos - i);
				if (low != null) {
					val.add(low);
				}
				byte[] high = searcher.getBytesAt(bestPos + i);
				if (high != null) {
					val.add(high);
				}
			}
			if (p.parseNumeric(best) > value) {
				byte[] low = searcher.getBytesAt(bestPos - range);
				if (low != null) {
					val.add(low);
				}
			} else {
				byte[] high = searcher.getBytesAt(bestPos + range);
				if (high != null) {
					val.add(high);
				}
			}
			return val;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
