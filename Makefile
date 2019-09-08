md   := $(filter-out README.md, $(wildcard **/*.md))
scss := $(wildcard resources/css/*.scss)

all: slides styles

slides: $(md:.md=.html)
styles: $(scss:.scss=.css)

%.html: %.md resources/html/*.html
	@pandoc -t revealjs \
			--standalone \
			--highlight=zenburn \
			--slide-level=3 \
			--include-in-header=resources/html/revealjs-header.html \
			--variable=revealjs-url:/resources/js/reveal.js \
			--variable=theme:solarized \
			--variable=controls:false \
			--variable=history:true \
			--variable=transition:fade \
			--variable=viewDistance:10 \
			--variable=center:true \
			--variable=width:\"90%\" \
			--variable=height:\"100%\" \
			--variable=margin:0 \
			--variable=minScale:1 \
			--variable=maxScale:1 \
			-i "$(<)" -o "$(@)"

%.css: %.scss
	@sass --no-source-map $< $@

clean:
	@rm -f $(md:.md=.html) $(scss:.scss=.css)

watch:
	watchexec --exts md,js,scss,html make
