#!/bin/sh
# skrypt do generacji doumentu PDF
# wygenerowanie pierwszego pliku aux
# pdflatex -file-line-error-style -output-directory=tmp -aux-directory=tmp -include-directory=tex tex/main.tex
# pdflatex -file-line-error-style -output-directory=tmp -aux-directory=tmp -include-directory=tex tex/main.tex
pdflatex -file-line-error-style tex/main.tex
pdflatex -file-line-error-style tex/main.tex
# tworzenie odwo³añ do bibliografii
bibtex -min-crossrefs -1 main
#bibtex -min-crossrefs -1 bk
#bibtex -min-crossrefs -1 st
#bibtex -min-crossrefs -1 doc
#bibtex -min-crossrefs -1 web
# utworzenie indeksu
#makeindex *.idx -o main.ind
# aktualizacja aux
# pdflatex -file-line-error-style -output-directory=tmp -aux-directory=tmp -include-directory=tex tex/main.tex
pdflatex -file-line-error-style tex/main.tex
# stworzenie poprawnych odnosników
# pdflatex -file-line-error-style -output-directory=tmp -aux-directory=tmp -include-directory=tex tex/main.tex
pdflatex -file-line-error-style tex/main.tex
# i interaktywny pdf gotowy
mv main.pdf rso_doc.pdf
