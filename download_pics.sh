#!/bin/bash

COUNT=10
WIDTH=800
HEIGHT=600
OUTPUT_DIR="backend/src/main/resources/images"

function show_help {
    echo "Usage: $0 [OPTIONS]"
    echo "Download random images from picsum.photos"
    echo ""
    echo "Options:"
    echo "  -c, --count NUMBER    Number of images to download (default: 10)"
    echo "  -w, --width NUMBER    Width of images (default: 800)"
    echo "  -h, --height NUMBER   Height of images (default: 600)"
    echo "  -o, --output DIR      Output directory (default: 'images')"
    echo "  --help                Display this help message"
}

while [[ $# -gt 0 ]]; do
    case $1 in
        -c|--count)
            COUNT="$2"
            shift 2
        ;;
        -w|--width)
            WIDTH="$2"
            shift 2
        ;;
        -h|--height)
            HEIGHT="$2"
            shift 2
        ;;
        -o|--output)
            OUTPUT_DIR="$2"
            shift 2
        ;;
        --help)
            show_help
            exit 0# Download images
            
        ;;
        *)
            echo "Unknown option: $1"
            show_help
            exit 1
        ;;
    esac
done

mkdir -p "$OUTPUT_DIR"

echo "Downloading $COUNT images (${WIDTH}x${HEIGHT}) to $OUTPUT_DIR/"
for i in $(seq 1 $COUNT); do
    filename=$(printf "%s/image%03d.jpg" "$OUTPUT_DIR" $i)
    echo "Downloading image $i/$COUNT: $filename"
    curl -L -s "https://picsum.photos/$WIDTH/$HEIGHT" -o "$filename"
    
    # Add a small delay to avoid rate limiting
    sleep 0.5
done

echo "Download complete! $COUNT images saved to $OUTPUT_DIR/"