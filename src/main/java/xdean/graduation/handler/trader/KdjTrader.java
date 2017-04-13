package xdean.graduation.handler.trader;

import xdean.graduation.index.KDJ.KDJ;
import xdean.graduation.model.Order;
import xdean.graduation.model.Repo;
import xdean.jex.util.calc.MathUtil;

/**
 * PARAM: {n, lm}
 * 
 * @author XDean
 *
 */
public class KdjTrader extends AbstractTrader<int[]> {

  private static final int DEFAULT_S = 3;

  KdjExtend kdj;

  public KdjTrader(Repo repo) {
    this.repo = repo;
  }

  @Override
  public KdjTrader setParam(int[] p) {
    this.kdj = new KdjExtend(p[0], p[1], p[1], DEFAULT_S);
    return this;
  }

  @Override
  public void trade(Order order) {
    kdj.accept(order.getAveragePrice());
    TraderUtil.tradeByPosition(repo, order, kdj.position());
  }

  public KDJ getKdj() {
    return kdj;
  }

  private class KdjExtend extends KDJ {

    double oldHistogram;
    double position = 0d;

    public KdjExtend(int n, int m, int l, int s) {
      super(n, m, l, s);
    }

    @Override
    public void accept(Double d) {
      oldHistogram = getK() - getD();
      super.accept(d);
    }

    double position() {
      position += TraderUtil.adjustByHistogram(oldHistogram, getK() - getD(), policy);
      return position = MathUtil.toRange(position, -1d, 1d);
    }
  }
}
