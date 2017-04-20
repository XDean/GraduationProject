package xdean.graduation.handler.trader;

import xdean.graduation.handler.trader.base.PositionTrader;
import xdean.graduation.index.MACD_Stable;
import xdean.graduation.model.Order;
import xdean.graduation.model.Repo;

/**
 * {up, low, multi}
 * 
 * @author XDean
 *
 */
public class BaoSiTrader extends PositionTrader<double[]> {

  MACD_Stable macd;
  double up, low;
  double upup, lowlow;

  public BaoSiTrader(Repo repo) {
    super(repo);
    macd = new MACD_Stable(12, 26, 9);
  }

  @Override
  public double getPosition(double oldPosition, Order order) {
    double old = macd.get();
    macd.accept(order.getAveragePrice());
    double now = macd.get();
    if (oldPosition == 0) {
      if (old <= low && now > low) {
        return 1;
      }
      if (old >= up && now < up) {
        return -1;
      }
    } else if (oldPosition > 0) {
      if (old <= 0 && now > 0 || old >= lowlow && now < lowlow) {
        return 0;
      }
    } else {
      if (old >= 0 && now < 0 || old <= upup && now > upup) {
        return 0;
      }
    }
    return oldPosition;
  }

  @Override
  public BaoSiTrader setParam(double[] p) {
    up = p[0];
    upup = p[0] * p[2];
    low = p[1];
    lowlow = p[1] * p[2];
    return this;
  }

  public double getHistogram() {
    return macd.getHistogram();
  }

  public double getStableMacd() {
    return macd.getStableMacd();
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
}
