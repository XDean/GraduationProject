package xdean.graduation.handler.trader;

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

  MacdExtend macd;

  public MacdTrader(Repo repo) {
    this.repo = repo;
  }

  @Override
  public Trader<int[]> setParam(int[] p) {
    macd = new MacdExtend(p[0], p[1], p[2]);
    return this;
  }

  @Override
  public void trade(Order order) {
    macd.accept(order.getAveragePrice());
    TraderUtil.tradeByPosition(repo, order, macd.position());
  }

  public MACD getMacd() {
    return macd;
  }

  private class MacdExtend extends MACD {

    Double oldHistogram;
    double position = 0d;

    public MacdExtend(int f, int s, int a) {
      super(f, s, a);
    }

    @Override
    public void accept(Double t) {
      oldHistogram = getHistogram();
      super.accept(t);
    }

    double position() {
      position += TraderUtil.adjustByHistogram(oldHistogram, getHistogram(), policy);
      return position = MathUtil.toRange(position, -1d, 1d);
    }
  }
}
