#!/bin/bash

set -e  # Exit on error

cd manuscript-pandoc/generated/

pandoc \
 ../000-settings.md \
 00-the-great-entrance.md \
 01-unit-testing-boss.md \
 02-sliced-testing-hydra.md \
 03-integration-testing-final-boss.md \
 04-quest-items.md \
 05-exiting-the-maze.md \
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
 --syntax-highlighting=idiomatic \
 -o ../../main-content.pdf

cd ../..

# Verify files exist
if [ ! -f "main-content.pdf" ]; then
  echo "Error: main-content.pdf was not created by pandoc"
  exit 1
fi

if [ ! -f "cover.pdf" ]; then
  echo "Error: cover.pdf not found"
  exit 1
fi

# Merge cover and content, then compress using ghostscript
echo "Merging cover with content and compressing..."
gs -sDEVICE=pdfwrite \
   -dNOPAUSE \
   -dBATCH \
   -dSAFER \
   -dCompatibilityLevel=1.4 \
   -dPDFSETTINGS=/prepress \
   -sOutputFile=testing-spring-boot-applications-demystified.pdf \
   cover.pdf \
   main-content.pdf

echo "PDF created successfully with front cover and compressed!"
