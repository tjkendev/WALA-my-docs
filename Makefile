TDIR := docs

CMD := asciidoc
SOURCES := $(wildcard *.adoc)
TARGETS := $(addprefix $(TDIR)/, $(SOURCES:.adoc=.html))

all: $(TARGETS)

$(TDIR)/%.html: %.adoc
	$(CMD) -o $@ $<
