#!/bin/bash

# Define source and output directories
source_dir="manuscript"
output_dir="manuscript-pandoc/generated"

# Create the output directory if it doesn’t exist
mkdir -p "$output_dir"
mkdir -p "$output_dir/resources"

# Find all .md files in the source directory and process them
find "$source_dir" -type f -name "*.md" | while read -r file; do
    # Get the relative path by removing the source directory prefix
    relative_path="${file#$source_dir/}"

    # Create the corresponding output directory
    out_dir="$output_dir/$(dirname "$relative_path")"
    mkdir -p "$out_dir"

    cp -r "$source_dir/resources" "$output_dir"

    # Define the output file path
    out_file="$output_dir/$relative_path"

    # Use sed to:
    # 1. Remove lines that are only {pagebreak} (with optional whitespace)
    # 2. Remove metadata tags like {id: ...} before headers
    sed '/^[[:space:]]*{pagebreak}[[:space:]]*$/d' "$file" | \
    sed '/^[[:space:]]*{.*}[[:space:]]*$/d' | \
    sed 's/^{.*}[[:space:]]*\(#.*\)$/\1/' > "$out_file"
done

echo "Markdown files have been modified and saved to '$output_dir'."
