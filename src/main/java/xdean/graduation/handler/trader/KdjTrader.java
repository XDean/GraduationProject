package xdean.graduation.handler.trader;

import lombok.Getter;
import xdean.graduation.index.KDJ.KDJ;
import xdean.graduation.model.Order;
import xdean.graduation.model.Repo;

/**
 * PARAM: {n, lm}
 * 
 * @author XDean
 *
 */
public class KdjTrader implements Trader<Integer[]> {

  private static final int DEFAULT_S = 3;

  @Getter
  Repo repo;
  KdjExtend kdj;

  public KdjTrader(Repo repo) {
    this.repo = repo;
  }

  @Override
  public KdjTrader setParam(Integer[] p) {
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

  private static class KdjExtend extends KDJ {

    double oldK, oldD;

    public KdjExtend(int n, int m, int l, int s) {
      super(n, m, l, s);
    }

    @Override
    public void accept(Double d) {
      oldK = getK();
      oldD = getD();
      super.accept(d);
    }

    /**
     * 1 buy, -1 sell, 0 do nothing
     * 
     * @return
     */
    double position() {
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
  }
}
