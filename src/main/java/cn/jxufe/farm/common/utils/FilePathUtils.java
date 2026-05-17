package cn.jxufe.farm.common.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class FilePathUtils {

    private static final List<String> ALLOWED_CATEGORIES = List.of("avatar", "seed-stage", "seed-icon", "other");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private FilePathUtils() {
    }

    public static String normalizeCategory(String rawCategory) {
        if (rawCategory == null || rawCategory.isBlank()) {
            return "other";
        }
        String category = rawCategory.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_CATEGORIES.contains(category)) {
            return "other";
        }
        return category;
    }

    public static String buildDatePath() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public static String buildStoredFileName(String extensionWithDot) {
        String extension = extensionWithDot == null ? "" : extensionWithDot.trim();
        if (!extension.isBlank() && !extension.startsWith(".")) {
            extension = "." + extension;
        }
        return System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "") + extension;
    }

    public static String sanitizeToRelativePath(String inputPath) {
        if (inputPath == null || inputPath.isBlank()) {
            return "";
        }
        String normalized = inputPath.trim().replace("\\", "/");
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    public static String extractExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot).toLowerCase(Locale.ROOT);
    }
}
