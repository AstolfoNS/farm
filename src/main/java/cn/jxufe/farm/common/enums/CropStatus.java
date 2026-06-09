package cn.jxufe.farm.common.enums;

import lombok.Getter;

@Getter
public enum CropStatus {
  GROWING((short) 1, "生长中"),
  RIPE((short) 2, "成熟"),
  WITHERED((short) 3, "枯萎");

  private final short code;

  private final String label;

  CropStatus(short code, String label) {
    this.code = code;
    this.label = label;
  }

  public static boolean isRipe(Short code) {
    return code != null && RIPE.code == code;
  }

  public static boolean isWithered(Short code) {
    return code != null && WITHERED.code == code;
  }

  public static short growingCodeWhenNull(Short code) {
    return code == null ? GROWING.code : code;
  }
}
