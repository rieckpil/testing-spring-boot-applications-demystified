#!/bin/bash

for file in manuscript/*.md; do
  awk '/^```/ {
    if (in_code == 0) {  # Opening code block
      if ($0 !~ /^```(java|groovy|shell|xml|dockerfile|text|yaml|yml|json)/) {
        print "Invalid opening code block: " FILENAME ":" NR ":" $0
        exit 1
      } else {
        in_code = 1
      }
    } else {  # Closing code block
      if ($0 !~ /^```$/) {
        print "Invalid closing code block: " FILENAME ":" NR ":" $0
        exit 1
      } else {
        in_code = 0
      }
    }
  }' "$file"
done
