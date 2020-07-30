package com.wittsfamily.approximations.generator;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.wittsfamily.approximations.store.FileTarget;

public class Main {
    public static void main(String[] args) throws InterruptedException, ExecutionException, ParseException, IOException, TimeoutException {
        for (String string : args) {
            if (string.equals("-h")) {
                System.out.println("This is the help page for the Approximation Generator Generator program."
                        + " This program generates the file used by other parts of the Approximation Generator.\n"
                        + "It takes the argument -f for the destination file, to put the results in, and the argument -n for excluded values or functions. The exclusions are as follows:\n"
                        + "  - e for e and e^a (E for ln)\n" + "  - ^ for a^b and e^a\n" + "  - p for pi\n" + "  - g for golden ratio\n" + "  - s for sin (S for arcsin)\n"
                        + "  - c for cos (C for arccos)\n" + "  - t for tan (T for arctan)\n" + "  - h for sinh (H for arsinh)\n" + "  - i for cosh (I for arcosh)\n"
                        + "  - j for tanh (J for artanh)\n" + "  - \\* for multiply and division\n" + "  - \\+ for addition and subtraction\n" + "  - \\/ for invert\n"
                        + "  - \\- for negate\n" + "  - \\L for log\n" + "  - d for integers\n" + "  - ! for factorial\n" + "  - | for tetration\n" + "  - q for square\n"
                        + "  - Q for square root");
                System.exit(0);
            }
        }
        int fnPos = -1;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-f")) {
                fnPos = i + 1;
                break;
            }
        }
        File file;
        if (fnPos == -1) {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showSaveDialog(null);
            if (result != JFileChooser.APPROVE_OPTION) {
                System.exit(1);
            }
            file = fileChooser.getSelectedFile();
        } else {
            file = new File(args[fnPos]);
        }
        int costPos = -1;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-c")) {
                costPos = i + 1;
                break;
            }
        }
        int cost;
        if (costPos == -1) {
            cost = Integer.parseInt(JOptionPane.showInputDialog(null, "Please enter a maximum cost for generator", "Enter cost", JOptionPane.PLAIN_MESSAGE));
        } else {
            cost = Integer.parseInt(args[costPos]);
        }
        String excl = "";
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-n")) {
                excl = args[i].substring(2);
                break;
            }
        }
        Set<Object> excludes = new HashSet<>();
        for (char c : excl.toCharArray()) {
            switch (c) {
            case 'e':
                excludes.add(Constant.E);
                excludes.add(UnaryFunction.EXP);
                break;
            case 'E':
                excludes.add(UnaryFunction.LN);
                break;
            case '^':
                excludes.add(BinaryFunction.POWER);
                excludes.add(UnaryFunction.EXP);
                break;
            case 'p':
                excludes.add(Constant.PI);
                break;
            case 'g':
                excludes.add(Constant.GOLDEN_RATIO);
                break;
            case 'd':
                for (Constant co : Constant.values()) {
                    if (co != Constant.E && co != Constant.PI && co != Constant.GOLDEN_RATIO) {
                        excludes.add(co);
                    }
                }
                break;
            case 's':
                excludes.add(UnaryFunction.SIN);
                break;
            case 'c':
                excludes.add(UnaryFunction.COS);
                break;
            case 't':
                excludes.add(UnaryFunction.TAN);
                break;
            case 'S':
                excludes.add(UnaryFunction.ARCSIN);
                break;
            case 'C':
                excludes.add(UnaryFunction.ARCCOS);
                break;
            case 'T':
                excludes.add(UnaryFunction.ARCTAN);
                break;
            case 'h':
                excludes.add(UnaryFunction.SINH);
                break;
            case 'i':
                excludes.add(UnaryFunction.COSH);
                break;
            case 'j':
                excludes.add(UnaryFunction.TANH);
                break;
            case 'H':
                excludes.add(UnaryFunction.ARSINH);
                break;
            case 'I':
                excludes.add(UnaryFunction.ARCOSH);
                break;
            case 'J':
                excludes.add(UnaryFunction.ARTANH);
                break;
            case '*':
                excludes.add(BinaryFunction.MULTIPLY);
                excludes.add(BinaryFunction.DIVIDE);
                break;
            case 'L':
                excludes.add(BinaryFunction.LOG);
                break;
            case '+':
                excludes.add(BinaryFunction.ADD);
                excludes.add(BinaryFunction.SUBTRACT);
                break;
            case '/':
                excludes.add(BinaryFunction.DIVIDE);
                excludes.add(UnaryFunction.INVERSE);
                break;
            case '-':
                excludes.add(BinaryFunction.SUBTRACT);
                excludes.add(UnaryFunction.NEGATE);
                break;
            case '!':
                excludes.add(UnaryFunction.FACTORIAL);
                break;
            case 'q':
                excludes.add(UnaryFunction.SQUARE);
                break;
            case 'Q':
                excludes.add(UnaryFunction.ROOT);
                break;
            case '|':
                excludes.add(BinaryFunction.UP);
                break;
            }
        }
        System.out.println(excludes);
        Generator.Generate(o -> !excludes.contains(o), new FileTarget("approximation-generator-temporary", file), cost);
    }
}
