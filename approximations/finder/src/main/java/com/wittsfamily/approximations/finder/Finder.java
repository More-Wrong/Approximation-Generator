package com.wittsfamily.approximations.finder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;
import org.scilab.forge.jlatexmath.DefaultTeXFont;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import org.scilab.forge.jlatexmath.cyrillic.CyrillicRegistration;
import org.scilab.forge.jlatexmath.greek.GreekRegistration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import com.wittsfamily.approximations.finder.cleaning.CleaningExpression;
import com.wittsfamily.approximations.finder.cleaning.ExpressionCleaner;
import com.wittsfamily.approximations.generator.ApfloatWithInf;
import com.wittsfamily.approximations.generator.Expression;

public class Finder {
    public static void main(String[] args) throws IOException, ParseException {
        for (String string : args) {
            if (string.equals("-h")) {
                System.out.println("This is the help page for the Approximation Generator Finder program.\n"
                        + " The -f option can be used to specify the location of the large file, as created by the Generator program."
                        + " \nThe -n option specifies the number to be found. This should be quoted, and can contain and number of "
                        + "commas or spaces, and may be of the form \"12000\", \"1.2e+4\", \"12*10^3\", \"0.12×105\", all of which are equivalent.\n"
                        + "The -d option specifies the depth of optimisation used to make the number closer to the target."
                        + " The higher this number, the longer the process takes. Numbers above 3 are not reccomended, as they will take a very long time.\n"
                        + "The -t option specifies the target location. The files will have \"-i.svg\" for i being the number of the file."
                        + " This defaults to expressions/expression and creates the directory if it does not exist."
                        + " A directory is reccomended, as there can be a lot of files.\n"
                        + "If an option nessesary for operation is not specified, an option pane will be produced asking for the option");
                System.exit(0);
            }
        }
        Preferences p = Preferences.userNodeForPackage(Finder.class);
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
            fileChooser.setCurrentDirectory(new File(p.get("defaultDirectory", System.getProperty("user.home"))));
            int result = fileChooser.showOpenDialog(null);
            if (result != JFileChooser.APPROVE_OPTION) {
                System.exit(1);
            }
            file = fileChooser.getSelectedFile();
            p.put("defaultDirectory", file.getParent());
        } else {
            file = new File(args[fnPos]);
        }
        ExpressionOptimiser f = new ExpressionOptimiser(file);
        ExpressionCleaner cl = new ExpressionCleaner();
        int numPos = -1;
        for (int i = 0; i < args.length; i++) {
            String string = args[i];
            if (args[i].equals("-n")) {
                numPos = i + 1;
                break;
            }
        }
        String number;
        if (numPos == -1) {
            number = JOptionPane.showInputDialog(null, "Please enter a number to approximate", "Enter number", JOptionPane.PLAIN_MESSAGE);
        } else {
            number = args[numPos];
        }
        number = number.replaceAll("[, ]", "").replaceAll("[-\u2212\u002D\uFE63\uFF0D]", "-").replaceAll("[*x\u2062]", "*").replaceFirst("\\*10\\^", "*10").replaceFirst("\\*10",
                "e");
        Apfloat target = new Apfloat(number, 50);

        int targetPos = -1;
        for (int i = 0; i < args.length; i++) {
            String string = args[i];
            if (args[i].equals("-t")) {
                targetPos = i + 1;
                break;
            }
        }
        String targetFile = "expressions/expression";
        if (targetPos != -1) {
            targetFile = args[targetPos];
        }

        int depthPos = -1;
        for (int i = 0; i < args.length; i++) {
            String string = args[i];
            if (args[i].equals("-d")) {
                depthPos = i + 1;
                break;
            }
        }
        int depth;
        if (depthPos == -1) {
            depth = Integer.parseInt(JOptionPane.showInputDialog(null, "Please enter a depth of optimisation", "Enter Optimisation", JOptionPane.PLAIN_MESSAGE));
        } else {
            depth = Integer.parseInt(args[depthPos]);
        }

        int j = 0;
        System.out.println("target value: " + target);
        List<Expression> vals = f.findCandidateExpressions(target, depth, true);
        List<String> laTeXStrs = new ArrayList<>();
        LaTeXExpressionRenderer lr = new LaTeXExpressionRenderer();
        UnicodeExpressionRenderer ur = new UnicodeExpressionRenderer();
        for (Expression value : vals) {
            Apfloat fl = value.asApfloat();
            Apfloat precision;
            if (ApfloatMath.abs(target.subtract(fl)).signum() == 0) {
                precision = ApfloatWithInf.INF;
            } else {
                precision = ApfloatMath.abs(target).divide(ApfloatMath.abs(target.subtract(fl)));
            }
            CleaningExpression exp = cl.clean(value);
            String ltxStr = exp.render(lr);
            System.out.println("LaTeX string: " + ltxStr);
            System.out.println("Unicode string: " + exp.render(ur));
            if (precision == ApfloatWithInf.INF) {
                String text = target.toString(true) + "\\approx " + ltxStr + " \\\\\\text{To system precision (1/10^{50}, \\pm rounding error)}";
                laTeXStrs.add(text);
            } else if (precision.compareTo(new Apfloat(1000)) > 0) {
                String text = target.toString(true) + "\\approx " + ltxStr + " \\\\\\text{To one part in: }";
                Apfloat log10 = ApfloatMath.log(precision, new Apfloat(10, 10));
                text = text + precision.divide(new Apfloat(Math.pow(10, log10.intValue()), 3)).toString() + "\\times 10^{" + log10.intValue() + "}";
                laTeXStrs.add(text);
            }
        }
        for (String text : laTeXStrs) {
            toSVG(text, targetFile + "-" + j++, true);
        }
    }

    public static void toSVG(String latex, String file, boolean fontAsShapes) throws IOException {
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);
        SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);

        SVGGraphics2D g2 = new SVGGraphics2D(ctx, fontAsShapes);

        DefaultTeXFont.registerAlphabet(new CyrillicRegistration());
        DefaultTeXFont.registerAlphabet(new GreekRegistration());

        TeXFormula formula = new TeXFormula(latex);
        TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20);
        icon.setInsets(new Insets(5, 5, 5, 5));
        g2.setSVGCanvasSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
        g2.setColor(Color.white);
        g2.fillRect(0, 0, icon.getIconWidth(), icon.getIconHeight());

        JLabel jl = new JLabel();
        jl.setForeground(new Color(0, 0, 0));
        icon.paintIcon(jl, g2, 0, 0);

        boolean useCSS = true;
        File f = new File(file);
        f.getParentFile().mkdirs();
        FileOutputStream svgs = new FileOutputStream(f);
        Writer out = new OutputStreamWriter(svgs, "UTF-8");
        g2.stream(out, useCSS);
        svgs.flush();
        svgs.close();
    }
}
