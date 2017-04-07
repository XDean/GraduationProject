package xdean.graduation.index;

import xdean.graduation.index.base.DoubleIndex;

public class MaxDrawdown implements DoubleIndex {

  final boolean feedAccumulet;
  double accumuleted = 1;
  double max = 1;
  double min = 1;
  double maxDrawdown;

  public MaxDrawdown(boolean feedAccumulet) {
    this.feedAccumulet = feedAccumulet;
  }

  @Override
  public void accept(Double returnRate) {
    if (feedAccumulet) {
      accumuleted = 1 + returnRate;
    } else {
      accumuleted *= (1 + returnRate);
    }
    if (accumuleted < min) {
      min = accumuleted;
    }
    if (accumuleted > max) {
      max = (min = accumuleted);
    }
    double newDrawdown = 1 - min / max;
    if (newDrawdown > maxDrawdown) {
      maxDrawdown = newDrawdown;
    }
  }

  @Override
  public Double get() {
    return maxDrawdown;
  }
}
