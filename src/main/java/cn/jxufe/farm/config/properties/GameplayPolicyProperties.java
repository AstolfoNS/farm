package cn.jxufe.farm.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "farm.gameplay.policy")
public class GameplayPolicyProperties {

  private Plot plot = new Plot();

  @Setter
  @Getter
  public static class Plot {

    private Unlock unlock = new Unlock();

    private DefaultInit defaults = new DefaultInit();
  }

  @Setter
  @Getter
  public static class Unlock {

    private int freePlotIndexLimit = 3;

    private long baseCostCoin = 80L;

    private long costStepCoin = 40L;

    private long baseRequiredExperience = 100L;

    private long requiredExperienceStep = 100L;
  }

  @Setter
  @Getter
  public static class DefaultInit {

    private short totalPlotCount = 6;

    private short unlockedPlotCount = 1;
  }
}
