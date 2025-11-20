#!/bin/bash

set -e

echo "Converting manuscript to Pandoc format..."
./convert-to-pandoc.sh

echo "Creating PDF book..."
./create-pdf-book.sh

echo "Book rendering complete!"
