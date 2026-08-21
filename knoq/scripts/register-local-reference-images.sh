#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
BASE_URL="${KNOQ_BASE_URL:-http://localhost:8080}"
IMAGE_ROOT="${PROJECT_DIR}/src/main/resources/static/demo/products"

upload_image() {
  local product_id="$1"
  local image_name="$2"
  local image_path="${IMAGE_ROOT}/${product_id}/${image_name}.png"

  curl --fail --silent --show-error \
    --request POST \
    "${BASE_URL}/products/${product_id}/reference-image" \
    --form "image=@${image_path}"

  echo "Registered ${product_id}/${image_name}.png"
}

for image_name in front side top; do
  upload_image "prod_1" "${image_name}"
  upload_image "prod_2" "${image_name}"
  upload_image "prod_3" "${image_name}"
  upload_image "prod_4" "${image_name}"
  upload_image "prod_5" "${image_name}"
  upload_image "prod_6" "${image_name}"
  upload_image "prod_7" "${image_name}"
  upload_image "prod_8" "${image_name}"
  upload_image "prod_9" "${image_name}"
done

upload_image "prod_1" "detail"

echo "All local recognition reference images were registered."
