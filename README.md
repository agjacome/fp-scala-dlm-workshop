FP in Scala - DLM Team Workshop
===============================

### Build

Requirements:

 * **pandoc**: https://pandoc.org/installing.html
 * **sass**: https://sass-lang.com/install
 * (optional) **watchexec**: https://github.com/watchexec/watchexec

Build slides:

```
$ make all 
```

### Visualize

A local HTTP server will be needed to view the HTML slides (as resources are
referenced by absolute URLs to `/resources/...`).

To run a simple Python HTTP server, execute the following in the root of the
project:

```
$ python3 -m http.server 8000 
```

This will start a new server accessible trough http://localhost:8000, where all
the HTML rendered files could be accessed. For example, [the slides for the
first session](http://localhost:8000/01_functions/slides.html).

### Print as PDF

In order to print the slides as PDF, a query parameter `print-pdf` can be
appended to the URL of any HTML file. This will change the style of the slides
to make them available to be pdf-printed.

For example, for the slides of the first session:

http://localhost:8000/01_functions/slides.html?print-pdf

Then, any usual print-to-pdf mechanism can be used.
