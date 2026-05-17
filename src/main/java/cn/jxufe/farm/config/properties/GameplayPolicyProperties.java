package cn.jxufe.farm.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "farm.gameplay.policy")
public class GameplayPolicyProperties {

    private Plot plot = new Plot();

    public Plot getPlot() {
        return plot;
    }

    public void setPlot(Plot plot) {
        this.plot = plot;
    }

    public static class Plot {

        private Unlock unlock = new Unlock();
        private Expand expand = new Expand();
        private DefaultInit defaults = new DefaultInit();

        public Unlock getUnlock() {
            return unlock;
        }

        public void setUnlock(Unlock unlock) {
            this.unlock = unlock;
        }

        public Expand getExpand() {
            return expand;
        }

        public void setExpand(Expand expand) {
            this.expand = expand;
        }

        public DefaultInit getDefaults() {
            return defaults;
        }

        public void setDefaults(DefaultInit defaults) {
            this.defaults = defaults;
        }
    }

    public static class Unlock {

        private int freePlotIndexLimit = 3;
        private long baseCostCoin = 80L;
        private long costStepCoin = 40L;

        public int getFreePlotIndexLimit() {
            return freePlotIndexLimit;
        }

        public void setFreePlotIndexLimit(int freePlotIndexLimit) {
            this.freePlotIndexLimit = freePlotIndexLimit;
        }

        public long getBaseCostCoin() {
            return baseCostCoin;
        }

        public void setBaseCostCoin(long baseCostCoin) {
            this.baseCostCoin = baseCostCoin;
        }

        public long getCostStepCoin() {
            return costStepCoin;
        }

        public void setCostStepCoin(long costStepCoin) {
            this.costStepCoin = costStepCoin;
        }
    }

    public static class Expand {

        private int freePlotCountLimit = 3;
        private long baseCostCoin = 100L;
        private long costStepCoin = 50L;

        public int getFreePlotCountLimit() {
            return freePlotCountLimit;
        }

        public void setFreePlotCountLimit(int freePlotCountLimit) {
            this.freePlotCountLimit = freePlotCountLimit;
        }

        public long getBaseCostCoin() {
            return baseCostCoin;
        }

        public void setBaseCostCoin(long baseCostCoin) {
            this.baseCostCoin = baseCostCoin;
        }

        public long getCostStepCoin() {
            return costStepCoin;
        }

        public void setCostStepCoin(long costStepCoin) {
            this.costStepCoin = costStepCoin;
        }
    }

    public static class DefaultInit {

        private short totalPlotCount = 6;
        private short unlockedPlotCount = 1;

        public short getTotalPlotCount() {
            return totalPlotCount;
        }

        public void setTotalPlotCount(short totalPlotCount) {
            this.totalPlotCount = totalPlotCount;
        }

        public short getUnlockedPlotCount() {
            return unlockedPlotCount;
        }

        public void setUnlockedPlotCount(short unlockedPlotCount) {
            this.unlockedPlotCount = unlockedPlotCount;
        }
    }
}
