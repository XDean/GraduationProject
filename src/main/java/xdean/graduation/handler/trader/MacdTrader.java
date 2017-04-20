package xdean.graduation.handler.trader;

import xdean.graduation.handler.trader.base.PositionStrategy;
import xdean.graduation.handler.trader.base.PositionTrader;
import xdean.graduation.index.MACD;
import xdean.graduation.model.Order;
import xdean.graduation.model.Repo;
import xdean.jex.util.calc.MathUtil;

/**
 * 
 * PARAM: {f,s,a}
 * 
 * @author XDean
 *
 */
public class MacdTrader extends PositionTrader<int[]> {

  MACD macd;
  Double oldHistogram;

  public MacdTrader(Repo repo) {
    super(repo);
  }

  @Override
  public MacdTrader setParam(int[] p) {
    macd = new MACD(p[0], p[1], p[2]);
    return this;
  }

  @Override
  public double getPosition(double oldPosition, Order order) {
    oldHistogram = macd.get();
    macd.accept(order.getAveragePrice());
    double position = oldPosition + adjustByHistogram(oldHistogram, macd.get(), strategy);
    position = MathUtil.toRange(position, -1d, 1d);
    return position;
  }

  static double adjustByHistogram(double oldHistogram, double histogram, PositionStrategy strategy) {
    double delta = histogram - oldHistogram;
    if (histogram > 0) {
      if (delta >= 0) {
        return strategy.open(delta);
      } else {
        return -strategy.close(-delta);
      }
    } else if (histogram < 0) {
      if (delta <= 0) {
        return -strategy.open(-delta);
      } else {
        return strategy.close(delta);
      }
    } else {
      return 0;
    }
  }

  public double getFast() {
    return macd.getFast();
  }

  public double getSlow() {
    return macd.getSlow();
  }

  public double getDif() {
    return macd.getDif();
  }

  public double getMacd() {
    return macd.getMacd();
  }

  public double getHistogram() {
    return macd.getHistogram();
  }
}
