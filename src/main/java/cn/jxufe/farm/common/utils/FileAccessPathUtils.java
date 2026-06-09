package cn.jxufe.farm.common.utils;

import java.net.URI;

public final class FileAccessPathUtils {

  private FileAccessPathUtils() {}

  public static String normalizePublicPrefix(String prefix) {
    if (prefix == null || prefix.isBlank()) {
      return "/oss";
    }
    String trimmed = prefix.trim();
    if (!trimmed.startsWith("/")) {
      trimmed = "/" + trimmed;
    }
    if (trimmed.endsWith("/")) {
      trimmed = trimmed.substring(0, trimmed.length() - 1);
    }
    return trimmed;
  }

  public static String normalizeIncomingRelativePath(String input, String publicPrefix) {
    if (input == null || input.isBlank()) {
      return "";
    }
    String raw = input.trim();
    try {
      URI uri = URI.create(raw);
      if (uri.getScheme() != null && uri.getPath() != null) {
        raw = uri.getPath();
      }
    } catch (Exception ignored) {
    }
    int queryIndex = raw.indexOf('?');
    if (queryIndex >= 0) {
      raw = raw.substring(0, queryIndex);
    }
    int fragmentIndex = raw.indexOf('#');
    if (fragmentIndex >= 0) {
      raw = raw.substring(0, fragmentIndex);
    }
    String relative = FilePathUtils.sanitizeToRelativePath(raw);
    String normalizedPrefix =
        FilePathUtils.sanitizeToRelativePath(normalizePublicPrefix(publicPrefix));
    if (!normalizedPrefix.isBlank() && relative.startsWith(normalizedPrefix + "/")) {
      return relative.substring(normalizedPrefix.length() + 1);
    }
    return relative;
  }
}
