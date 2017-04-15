package xdean.graduation.handler.trader;

import xdean.graduation.handler.trader.common.AbstractTrader;
import xdean.graduation.handler.trader.common.PositionPolicy;
import xdean.graduation.handler.trader.common.Trader;
import xdean.graduation.handler.trader.common.TraderUtil;
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
public class MacdTrader extends AbstractTrader<int[]> {

  MACD macd;
  double position = 0;
  Double oldHistogram;

  public MacdTrader(Repo repo) {
    super(repo);
  }

  @Override
  public Trader<int[]> setParam(int[] p) {
    macd = new MACD(p[0], p[1], p[2]);
    return this;
  }

  @Override
  public void trade(Order order) {
    super.trade(order);
    oldHistogram = macd.get();
    macd.accept(order.getAveragePrice());
    position += adjustByHistogram(oldHistogram, macd.get(), policy);
    position = MathUtil.toRange(position, -1d, 1d);
    TraderUtil.tradeByPosition(repo, order, position);
  }

  static double adjustByHistogram(double oldHistogram, double histogram, PositionPolicy policy) {
    double delta = histogram - oldHistogram;
    if (histogram > 0) {
      if (delta >= 0) {
        return policy.open(delta);
      } else {
        return -policy.close(-delta);
      }
    } else if (histogram < 0) {
      if (delta <= 0) {
        return -policy.open(-delta);
      } else {
        return policy.close(delta);
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
