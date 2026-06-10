package cn.jxufe.farm.service.support;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cn.jxufe.farm.common.exception.ServiceException;
import org.junit.jupiter.api.Test;

class FileUploadPolicyTest {

  @Test
  void validateAllowsImageForSeedCover() {
    assertThatCode(() -> FileUploadPolicy.validate("seed-cover", ".png", "image/png"))
        .doesNotThrowAnyException();
  }

  @Test
  void validateRejectsScriptExtension() {
    assertThatThrownBy(() -> FileUploadPolicy.validate("other", ".js", "application/javascript"))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining("扩展名");
  }

  @Test
  void validateRejectsMismatchedContentType() {
    assertThatThrownBy(() -> FileUploadPolicy.validate("avatar", ".png", "text/plain"))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining("文件类型");
  }
}
