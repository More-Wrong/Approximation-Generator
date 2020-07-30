# Approximation-Generator

This is a program to generate expressions, given their value, relying on a large file containing expressions in numeric order.

There are several parts to it:
  - The generator: this creates the file, given options on size, and values to contain
  - The finder: this can search the file, and find expressions for a given number, as well as 'optimise' those expressions, to make them closer to the target value.
  - The server-side-rest interface: this provides a rest API for a website to connect to, providing lookups on the files.
  - The website: a website based on the contained JavaScript, providing a pretty ui.

The JavaScript relies heavily on the [decimal.js](https://github.com/MikeMcl/decimal.js) library, which is included here (to make the website work).
The rest of the website is based on wordpress, and none of it is included here, for security reasons, although a basic html page is present, sufficient to make it work.

The finder and generator accept command line arguments, and self document with the -h option.

The finder accepts the -f option for the large source file, the -n option for the target number, the -d option for the depth of optimisation, and the -t option for the target files, which are created with -i.svg on the end, for i being the number of the file. (This defaults to expressions/expresion, as there can be a large number of them) The results are produced as SVG files, by the [jlatexmath](https://github.com/opencollab/jlatexmath) library, while the LaTeX used, and the unicode equivalent, is printed to stdout. If any option isn't given, a swing option dialogue is used, so it can be run without a command line, but then no output is produced. The maths is done with the [ApFloat](http://www.apfloat.org/) library.

The generator can be run to produce different file sizes by limiting the maximum cost, a cost of around 45 gives 800Mb of file. The -f option specifies the target file. The -n option forces it to ignore certain functions or values: 
  - e for e and e^a (E for ln)
  - ^ for a^b and e^a
  - p for pi
  - g for golden ratio
  - s for sin (S for arcsin)
  - c for cos (C for arccos)
  - t for tan (T for arctan)
  - h for sinh (H for arsinh)
  - i for cosh (I for arcosh)
  - j for tanh (J for artanh)
  - \* for multiply and division
  - \+ for addition and subtraction
  - \/ for invert
  - \- for negate
  - 1...9 for the relevant number
  - 0 for 10
  - L for 11
  - P for 13
  - ! for factorial
  - | for tetration
  - q for square
  - Q for square root

The server-side-rest system uses Spring to create a simple rest interface, returning the results of a lookup.
The website then uses JavaScript to process the results. The website also self documents here.

The options for the generator are heavily inspired by [RIES](https://mrob.com/pub/ries/index.html), which is a similar thing to this, only without the backing file.
