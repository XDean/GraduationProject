package xdean.graduation.handler.trader;

import xdean.graduation.handler.trader.common.PositionTrader;
import xdean.graduation.handler.trader.common.TraderUtil;
import xdean.graduation.index.KDJ.KDJ;
import xdean.graduation.model.Order;
import xdean.graduation.model.Repo;

/**
 * PARAM: {n, lm}
 * 
 * @author XDean
 *
 */
public class KdjTrader extends PositionTrader<int[]> {

  KDJ kdj;

  public KdjTrader(Repo repo) {
    super(repo);
  }

  @Override
  public KdjTrader setParam(int[] p) {
    this.kdj = new KDJ(p[0], p[1], p[1], KDJ.DEFAULT_S);
    return this;
  }

  @Override
  public double getPosition(double oldPosition, Order order) {
    oldK = getK();
    oldD = getD();
    kdj.accept(order.getAveragePrice());
    double position = adjustPositionByKD();
    TraderUtil.tradeByPosition(repo, order, position);
    return position;
  }

  /**
   * old way
   */
  double oldK, oldD;

  double adjustPositionByKD() {
    double k = getK();
    double d = getD();
    double position = Double.NaN;
    if (k < 30) {
      position = -1;
    }
    if (oldK < 30 && k > 30) {
      position = 1;
    }
    if (k > 70) {
      position = 1;
    }
    if (oldK > 70 && k < 70) {
      position = -1;
    }
    if (k < 70 && (oldK > oldD && k < d)) {
      position = -1;
    }
    if (k > 30 && (oldK < oldD && k > d)) {
      position = 1;
    }
    return position;
  }

  public double getRsv() {
    return kdj.getRsv().get();
  }

  public double getK() {
    return kdj.getK();
  }

  public double getD() {
    return kdj.getD();
  }

  public double getJ() {
    return kdj.getJ();
  }
}
