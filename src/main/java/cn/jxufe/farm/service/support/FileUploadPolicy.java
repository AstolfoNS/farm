package cn.jxufe.farm.service.support;

import cn.jxufe.farm.common.enums.BizErrorCode;
import cn.jxufe.farm.common.exceptions.ServiceException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class FileUploadPolicy {

  private static final Set<String> IMAGE_EXTENSIONS =
      Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp");
  private static final Set<String> IMAGE_CONTENT_TYPES =
      Set.of("image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp");
  private static final Set<String> OTHER_EXTENSIONS =
      Set.of(
          ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp", ".pdf", ".txt", ".json", ".csv",
          ".zip");
  private static final Set<String> OTHER_CONTENT_TYPES =
      Set.of(
          "image/jpeg",
          "image/png",
          "image/gif",
          "image/webp",
          "image/bmp",
          "application/pdf",
          "text/plain",
          "application/json",
          "text/csv",
          "application/zip",
          "application/x-zip-compressed");
  private static final Map<String, AllowedFileTypes> CATEGORY_POLICIES =
      Map.of(
          "avatar", imageTypes(),
          "seed-cover", imageTypes(),
          "seed-stage", imageTypes(),
          "seed-icon", imageTypes(),
          "soil-cover", imageTypes(),
          "plot-cover", imageTypes(),
          "other", new AllowedFileTypes(OTHER_EXTENSIONS, OTHER_CONTENT_TYPES));

  private FileUploadPolicy() {}

  public static void validate(String category, String extension, String contentType) {
    AllowedFileTypes allowed =
        CATEGORY_POLICIES.getOrDefault(category, CATEGORY_POLICIES.get("other"));
    String normalizedExtension = safeLower(extension);
    String normalizedContentType = safeLower(contentType);
    if (normalizedExtension.isBlank() || !allowed.extensions().contains(normalizedExtension)) {
      throw new ServiceException(BizErrorCode.PARAM_INVALID, "不支持的上传文件扩展名");
    }
    if (!normalizedContentType.isBlank()
        && !allowed.contentTypes().contains(normalizedContentType)) {
      throw new ServiceException(BizErrorCode.PARAM_INVALID, "不支持的上传文件类型");
    }
  }

  private static AllowedFileTypes imageTypes() {
    return new AllowedFileTypes(IMAGE_EXTENSIONS, IMAGE_CONTENT_TYPES);
  }

  private static String safeLower(String value) {
    return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
  }

  private record AllowedFileTypes(Set<String> extensions, Set<String> contentTypes) {}
}
