package xdean.graduation.handler.trader;

import xdean.graduation.handler.trader.common.AbstractTrader;
import xdean.graduation.handler.trader.common.Trader;
import xdean.graduation.handler.trader.common.TraderUtil;
import xdean.graduation.index.MACD;
import xdean.graduation.index.base.DoubleIndex;
import xdean.graduation.model.Order;
import xdean.graduation.model.Repo;

/**
 * 
 * PARAM: {f,s,a}
 * 
 * @author XDean
 *
 */
public class MacdTrader extends AbstractTrader<int[]> {

  MACD macd;
  DoubleIndex actual;

  public MacdTrader(Repo repo) {
    super(repo);
  }

  @Override
  public Trader<int[]> setParam(int[] p) {
    macd = new MACD(p[0], p[1], p[2]);
    actual = TraderUtil.histogramToPosition(
        DoubleIndex.create(d -> macd.accept(d), () -> macd.getHistogram()), () -> policy);
    return this;
  }

  @Override
  public void trade(Order order) {
    TraderUtil.tradeByPosition(repo, order, actual.get(order.getAveragePrice()));
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
