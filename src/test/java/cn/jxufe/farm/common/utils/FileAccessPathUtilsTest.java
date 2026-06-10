package cn.jxufe.farm.common.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FileAccessPathUtilsTest {

  @Test
  void normalizeIncomingRelativePathStripsPublicPrefixQueryAndFragment() {
    String actual =
        FileAccessPathUtils.normalizeIncomingRelativePath(
            "http://localhost:8080/oss/seed-cover/2026/06/11/a.png?x=1#top", "/oss");

    assertThat(actual).isEqualTo("seed-cover/2026/06/11/a.png");
  }

  @Test
  void normalizePublicPrefixAlwaysStartsWithoutTrailingSlash() {
    assertThat(FileAccessPathUtils.normalizePublicPrefix("oss/")).isEqualTo("/oss");
    assertThat(FileAccessPathUtils.normalizePublicPrefix("")).isEqualTo("/oss");
  }

  @Test
  void extractExtensionUsesLowerCaseExtensionOnly() {
    assertThat(FilePathUtils.extractExtension("Cover.PNG")).isEqualTo(".png");
    assertThat(FilePathUtils.extractExtension("no-extension")).isEmpty();
  }
}
