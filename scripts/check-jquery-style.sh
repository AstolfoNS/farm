#!/usr/bin/env sh
set -eu

targets="src/main/resources/static/js/app src/main/resources/static/html/app.html"

failed=0

for target in $targets; do
  [ -e "$target" ] || continue
  for pattern in "document.getElementById" "querySelector(" "querySelectorAll(" "addEventListener(" "fetch("; do
    result=$(rg -n --fixed-strings "$pattern" "$target" 2>/dev/null || true)
    if [ -n "$result" ]; then
      if [ "$failed" -eq 0 ]; then
        echo "jQuery style check failed. Forbidden API usage found:"
        failed=1
      fi
      echo "$result"
    fi
  done
done

if [ "$failed" -eq 1 ]; then
  exit 1
fi

echo "jQuery style check passed."
