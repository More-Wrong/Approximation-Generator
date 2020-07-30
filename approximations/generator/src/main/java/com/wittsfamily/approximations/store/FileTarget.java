package com.wittsfamily.approximations.store;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.wittsfamily.approximations.generator.Parser;

public class FileTarget {
	private final String tmpName;
	private final File finalFile;
	private LinkedBlockingQueue<File> targetFiles = new LinkedBlockingQueue<>();

	public FileTarget(String tmpName, File finalFile) throws IOException {
		this.tmpName = tmpName;
		this.finalFile = finalFile;
		finalFile.delete();
		finalFile.createNewFile();
	}

	public void dumpMap(TreeMap<Double, byte[]> map) throws IOException {
		File file = File.createTempFile(tmpName, null);
		System.out.println("writing file: " + file);
		file.delete();
		file.createNewFile();
		RandomAccessFile f = new RandomAccessFile(file, "rw");
		f.seek(0);
		for (Entry<Double, byte[]> entry : map.entrySet()) {
			f.write(entry.getValue());
		}
		f.close();
		file.deleteOnExit();
		targetFiles.add(file);
	}

	public void performFileMerges() {
		try {
			while (!Thread.currentThread().isInterrupted()) {
				File smallest = null;
				for (File file : targetFiles) {
					if ((smallest == null || smallest.length() > file.length())) {
						smallest = file;
					}
				}
				File nextSmallest = null;
				for (File file : targetFiles) {
					if ((nextSmallest == null || nextSmallest.length() > file.length())
							&& file.length() > smallest.length()) {
						nextSmallest = file;
					}
				}
				if (smallest != null && nextSmallest != null && smallest.length() + nextSmallest.length() < 120000000
						&& (nextSmallest.length() * 1.0 / smallest.length() < 5 || nextSmallest.length() < 10000000)) {
					System.out.println("begining merge");
					RandomAccessFile target = new RandomAccessFile(nextSmallest, "rw");
					RandomAccessFile current = new RandomAccessFile(smallest, "r");
					byte[] from = new byte[(int) current.length()];
					current.read(from);
					current.close();
					System.out.println("merging: " + smallest);
					smallest.delete();
					targetFiles.remove(smallest);
					if (from.length != 0) {
						fileMerge(from, target);
					}
				} else if (smallest != null) {
					RandomAccessFile target = new RandomAccessFile(finalFile, "rw");
					RandomAccessFile current = new RandomAccessFile(smallest, "r");
					byte[] from = new byte[(int) current.length()];
					current.read(from);
					current.close();
					smallest.delete();
					targetFiles.remove(smallest);
					if (from.length != 0) {
						fileMerge(from, target);
					}
					target.close();
				} else {
					Thread.sleep(1000);
				}
			}
		} catch (InterruptedException e) {
			// exit nicely...
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void combineFiles() throws IOException, ParseException {
		RandomAccessFile target = new RandomAccessFile(finalFile, "rw");
		while (!targetFiles.isEmpty()) {
			File targetFile = null;
			for (File file : targetFiles) {
				if (targetFile == null || targetFile.length() > file.length()) {
					targetFile = file;
				}
			}
			RandomAccessFile current = new RandomAccessFile(targetFile, "r");
			byte[] from = new byte[(int) current.length()];
			current.read(from);
			current.close();
			targetFile.delete();
			targetFiles.remove(targetFile);
			if (from.length != 0) {
				fileMerge(from, target);
			}
		}
		targetFiles.clear();
		target.close();
	}

	private void fileMerge(byte[] from, RandomAccessFile to) throws IOException, ParseException {
		if (to.length() == 0) {
			to.write(from);
		} else {
			System.out.println();
			System.out.println("merging:  " + from.length);
			System.out.println("into:  " + to.length());
			byte[] space = new byte[from.length + 48];
			ValueByteArrayList spaceFrom = new ValueByteArrayList(space);
			ValueByteArrayList spaceTo = new ValueByteArrayList(space);
			ValueByteArrayList mergeFrom = new ValueByteArrayList(from);
			ValueFileList fileTo = new ValueFileList(to);
			double toVal = fileTo.getValueAsDouble();
			double fromVal = mergeFrom.getValueAsDouble();
			boolean recalcTo = false;
			while (true) {
				if (toVal < fromVal) {
					if (spaceTo.getPos() != 0) {
						if (!fileTo.isAtEnd() && spaceTo.isAtEnd()) {
							spaceTo.resetPos();
						}
						if (spaceFrom.isAtEnd()) {
							spaceFrom.resetPos();
						}
						if (!fileTo.isAtEnd()) {
							fileTo.moveValueTo(spaceTo);
							spaceTo.moveAlong();
						}
						spaceFrom.moveValueTo(fileTo);
						spaceFrom.moveAlong();
						if (spaceFrom.getPos() != spaceTo.getPos()) {
							if (spaceFrom.isAtEnd()) {
								spaceFrom.resetPos();
							}
							toVal = spaceFrom.getValueAsDouble();
						}
					} else {
						recalcTo = true;
					}
				} else if (fromVal < toVal) {
					if (!fileTo.isAtEnd() && spaceTo.isAtEnd()) {
						spaceTo.resetPos();
					}
					if (!fileTo.isAtEnd()) {
						fileTo.moveValueTo(spaceTo);
						spaceTo.moveAlong();
					}
					mergeFrom.moveValueTo(fileTo);
					mergeFrom.moveAlong();
					if (!mergeFrom.isAtEnd()) {
						fromVal = mergeFrom.getValueAsDouble();
					}
				} else {
					int fromCost = mergeFrom.getValueCost();
					int toCost;
					if (spaceTo.getPos() == 0) {
						toCost = fileTo.getValueCost();
					} else {
						toCost = spaceFrom.getValueCost();
					}
					if (fromCost >= toCost) {
						mergeFrom.moveAlong();
						if (spaceTo.getPos() != 0) {
							if (!fileTo.isAtEnd() && spaceTo.isAtEnd()) {
								spaceTo.resetPos();
							}
							if (spaceFrom.isAtEnd()) {
								spaceFrom.resetPos();
							}
							if (!fileTo.isAtEnd()) {
								fileTo.moveValueTo(spaceTo);
								spaceTo.moveAlong();
							}
							spaceFrom.moveValueTo(fileTo);
							spaceFrom.moveAlong();
						}
					} else {
						if (spaceTo.getPos() != 0) {
							if (!fileTo.isAtEnd() && spaceTo.isAtEnd()) {
								spaceTo.resetPos();
							}
							if (spaceFrom.isAtEnd()) {
								spaceFrom.resetPos();
							}
							if (!fileTo.isAtEnd()) {
								fileTo.moveValueTo(spaceTo);
								spaceTo.moveAlong();
							}
							spaceFrom.moveAlong();
						}
						mergeFrom.moveValueTo(fileTo);
						mergeFrom.moveAlong();
					}
					if (!mergeFrom.isAtEnd()) {
						fromVal = mergeFrom.getValueAsDouble();
					}
					if (spaceTo.getPos() != 0 && spaceFrom.getPos() != spaceTo.getPos()) {
						if (spaceFrom.isAtEnd()) {
							spaceFrom.resetPos();
						}
						toVal = spaceFrom.getValueAsDouble();
					} else {
						recalcTo = true;
					}
				}
				fileTo.moveAlong();
				if (!fileTo.isAtEnd() && recalcTo) {
					toVal = fileTo.getValueAsDouble();
					recalcTo = false;
				}
				if (mergeFrom.isAtEnd()) {
					if (spaceTo.getPos() != 0) {
						while (spaceTo.getPos() != spaceFrom.getPos()) {
							if (spaceFrom.isAtEnd()) {
								spaceFrom.resetPos();
							}
							if (!fileTo.isAtEnd()) {
								if (spaceTo.isAtEnd()) {
									spaceTo.resetPos();
								}
								fileTo.moveValueTo(spaceTo);
								spaceTo.moveAlong();
							}
							spaceFrom.moveValueTo(fileTo);
							fileTo.moveAlong();
							spaceFrom.moveAlong();
						}
					}
					break;
				}
				if (fileTo.isAtEnd() && spaceTo.getPos() == spaceFrom.getPos()) {
					break;
				}
			}
		}
	}

	public boolean checkFile() throws IOException, ParseException {
		RandomAccessFile target = new RandomAccessFile(finalFile, "r");
		double prev = -10;
		double current = -10;
		for (ValueFileList fileTo = new ValueFileList(target); !fileTo.isAtEnd(); fileTo.moveAlong()) {
			current = fileTo.getValueAsDouble();
			if (current < prev) {
				System.out.println(prev + "   " + current);
				fileTo.moveBack();
				Parser p = new Parser();
				System.out.println(p.parseExpression(fileTo.getValue(), 0, 12));
				fileTo.moveAlong();
				System.out.println(p.parseExpression(fileTo.getValue(), 0, 12));
			}
			prev = current;
		}
		return true;
	}
}
