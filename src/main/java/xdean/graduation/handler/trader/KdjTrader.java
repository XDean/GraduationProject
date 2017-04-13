package xdean.graduation.handler.trader;

import xdean.graduation.handler.trader.common.AbstractTrader;
import xdean.graduation.handler.trader.common.TraderUtil;
import xdean.graduation.index.KDJ.KDJ;
import xdean.graduation.index.base.DoubleIndex;
import xdean.graduation.model.Order;
import xdean.graduation.model.Repo;

/**
 * PARAM: {n, lm}
 * 
 * @author XDean
 *
 */
public class KdjTrader extends AbstractTrader<int[]> {

  KDJ kdj;
  DoubleIndex actual;

  public KdjTrader(Repo repo) {
    super(repo);
  }

  @Override
  public KdjTrader setParam(int[] p) {
    this.kdj = new KDJ(p[0], p[1], p[1], KDJ.DEFAULT_S);
    this.actual = TraderUtil.histogramToPosition(
        DoubleIndex.create(d -> kdj.accept(d), () -> kdj.getK() - kdj.getD()),
        () -> policy);
    return this;
  }

  @Override
  public void trade(Order order) {
    TraderUtil.tradeByPosition(repo, order, actual.get(order.getAveragePrice()));
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
