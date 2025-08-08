#!/bin/bash

cd manuscript-pandoc/generated/

pandoc \
 ../000-settings.md \
 00-introduction.md \
 01-spring-boot-testing-fundamentals.md \
 02-spring-boot-testing-sliced.md \
 03-spring-boot-testing-full.md \
 04-spring-boot-testing-pitfalls-and-best-practices.md \
 05-outro.md \
 changelog.md \
 --toc \
 --toc-depth=3 \
 -H ../config.tex \
 -V linkcolor=ideablue \
 -V colorlinks=true \
 -V mainfont="Fira Sans" \
 -V monofont="SauceCodePro Nerd Font" \
 --top-level-division=section \
 --pdf-engine=xelatex \
 --pdf-engine-opt=-shell-escape \
 --listings \
 -o ../../testing-spring-boot-applications-demystified.pdf

cd ../..
