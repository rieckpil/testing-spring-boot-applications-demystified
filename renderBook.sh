#!/bin/bash

set -e

echo "Converting manuscript to Pandoc format..."
./convert-to-pandoc.sh

echo "Creating PDF book..."
./create-pdf-book.sh

echo "Opening the generated PDF..."
if [[ "$OSTYPE" == "darwin"* ]]; then
  open *.pdf
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
  xdg-open *.pdf
elif [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
  start *.pdf
else
  echo "Please manually open the generated PDF file"
fi

echo "Book rendering complete!"