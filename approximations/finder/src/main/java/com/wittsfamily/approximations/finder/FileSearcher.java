package com.wittsfamily.approximations.finder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;

import com.wittsfamily.approximations.generator.Expression;
import com.wittsfamily.approximations.generator.Parser;

public class FileSearcher {
    private final RandomAccessFile searchFile;
    private final Parser parser = new Parser();

    public FileSearcher(File file) throws FileNotFoundException {
        this.searchFile = new RandomAccessFile(file, "r");
    }

    public byte[] getBytesAt(long pos) throws IOException, ParseException {
        if (pos * 12 + 12 > searchFile.length() || pos < 0) {
            return null;
        }
        byte[] tmp = new byte[12];
        searchFile.seek(pos * 12);
        searchFile.read(tmp);
        return tmp;
    }

    public Expression getValueAt(long pos) throws IOException, ParseException {
        byte[] arr = getBytesAt(pos);
        if (arr == null) {
            return null;
        }
        return parser.parseExpression(arr, 0, 12);
    }

    public long binarySearch(double d) throws IOException, ParseException {
        long pos = (searchFile.length() / 12) / 2;
        long posHigh = searchFile.length() / 12;
        long posLow = 0;
        byte[] tmp = new byte[12];
        searchFile.seek(pos * 12);
        searchFile.read(tmp);
        double mostRecent = parser.parseNumeric(tmp);
        while (true) {
            if (mostRecent == d) {
                return pos;
            } else if (mostRecent > d) {
                if (posLow >= pos - 1) {
                    return pos;
                }
                posHigh = pos;
                pos = (posHigh + posLow) / 2;
                searchFile.seek(pos * 12);
                searchFile.read(tmp);
                mostRecent = parser.parseNumeric(tmp);
            } else {
                if (posHigh <= pos + 1) {
                    return pos;
                }
                posLow = pos;
                pos = (posHigh + posLow) / 2;
                searchFile.seek(pos * 12);
                searchFile.read(tmp);
                mostRecent = parser.parseNumeric(tmp);
            }
        }
    }

    public void close() throws IOException {
        searchFile.close();
    }
}
