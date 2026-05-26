#!/usr/bin/env sh
set -eu

roots="src/main/resources/static/resources/imgs src/main/resources/static/resources/sounds"
pattern='^[a-z0-9]+(-[a-z0-9]+)*\.[a-z0-9]+$'
failed=0

for root in $roots; do
  if [ ! -d "$root" ]; then
    continue
  fi
  while IFS= read -r file; do
    name=$(basename "$file")
    echo "$name" | grep -Eq "$pattern" || {
      if [ "$failed" -eq 0 ]; then
        echo "Resource naming check failed. Non-kebab-case files:"
        failed=1
      fi
      echo " - $file"
    }
  done <<EOF
$(find "$root" -type f)
EOF
done

if [ "$failed" -eq 1 ]; then
  exit 1
fi

echo "Resource naming check passed."
