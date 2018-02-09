TDIR := docs

CMD := asciidoctor
SOURCES := $(wildcard *.adoc)
TARGETS := $(addprefix $(TDIR)/, $(SOURCES:.adoc=.html))

all: $(TARGETS)

$(TDIR)/%.html: %.adoc
	$(CMD) -a stylesheet=foundation.css -a source-highlighter=prettify -o $@ $<
