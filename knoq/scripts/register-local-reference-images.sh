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
  upload_image "prod_12" "${image_name}"
  upload_image "prod_33" "${image_name}"
  upload_image "prod_34" "${image_name}"
  upload_image "prod_35" "${image_name}"
  upload_image "prod_36" "${image_name}"
  upload_image "prod_37" "${image_name}"
  upload_image "prod_38" "${image_name}"
  upload_image "prod_39" "${image_name}"
  upload_image "prod_40" "${image_name}"
done

upload_image "prod_12" "detail"

echo "All local recognition reference images were registered."
