package com.wittsfamily.approximations.finder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.wittsfamily.approximations.generator.Parser;

@Component
public class FileRangeFinder {
    private final FileSearcher searcher;
    private final Parser p = new Parser();

    public FileRangeFinder(@Value("${file}") String file) throws FileNotFoundException {
        this.searcher = new FileSearcher(new File(file));
    }

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
