#!/bin/bash
# filepath: /autofs/unityaccount/cremi/argomes/v3b/clear_images.sh
IMAGE_DIR="backend/src/main/resources/images"

mkdir -p "$IMAGE_DIR"

rm -f "$IMAGE_DIR"/*

echo "Cleared all images from $IMAGE_DIR"