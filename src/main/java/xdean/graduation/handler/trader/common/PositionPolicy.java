package xdean.graduation.handler.trader.common;

import java.util.function.UnaryOperator;

public interface PositionPolicy {

  /**
   * @param d Instantaneous Volatility
   * @return position adjustment
   */
  double open(double d);

  double close(double d);

  public static PositionPolicy ALL_OUT = create(d -> 2d, d -> 2d);

  public static PositionPolicy create(UnaryOperator<Double> open, UnaryOperator<Double> close) {
    return new PositionPolicy() {
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
