Functional Programming in Scala
===============================

### Build

Requirements:

 * **pandoc**: https://pandoc.org/installing.html
 * **sass**: https://sass-lang.com/install
 * **watchexec**: https://github.com/watchexec/watchexec

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
