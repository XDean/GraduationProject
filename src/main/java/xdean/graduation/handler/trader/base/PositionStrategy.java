package xdean.graduation.handler.trader.base;

import java.util.function.UnaryOperator;

public interface PositionStrategy {

  /**
   * @param d Instantaneous Volatility
   * @return position adjustment
   */
  double open(double d);

  double close(double d);

  public static PositionStrategy ALL_OUT = create(d -> 2d, d -> 0d);

  public static PositionStrategy create(UnaryOperator<Double> open, UnaryOperator<Double> close) {
    return new PositionStrategy() {
      @Override
      public double open(double d) {
        return open.apply(d);
      }

      @Override
      public double close(double d) {
        return close.apply(d);
      }
    };
  }
}
