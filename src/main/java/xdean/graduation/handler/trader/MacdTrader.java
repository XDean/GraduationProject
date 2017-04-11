package xdean.graduation.handler.trader;

import java.util.function.UnaryOperator;

import lombok.Getter;
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
public class MacdTrader implements Trader<Integer[]> {

  @Getter
  Repo repo;
  MacdExtend macd;

  public MacdTrader(Repo repo) {
    this.repo = repo;
  }

  @Override
  public Trader<Integer[]> setParam(Integer[] p) {
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

  private static class MacdExtend extends MACD {

    MACD old;
    Double oldInput;

    double position = 0d;
    UnaryOperator<Double> openRate = d -> 1d;
    UnaryOperator<Double> closeRate = d -> d * d * 500;

    public MacdExtend(int f, int s, int a) {
      super(f, s, a);
      old = new MACD(f, s, a);
    }

    @Override
    public void accept(Double t) {
      if (oldInput != null) {
        old.accept(oldInput);
      }
      oldInput = t;
      super.accept(t);
    }

    double position() {
      double histogram = getHistogram();
      double delta = histogram - old.getHistogram();
      if (histogram > 0) {
        if (delta > 0) {
          position += openRate.apply(delta);
        } else {
          position -= closeRate.apply(-delta);
        }
      }
      if (histogram < 0) {
        if (delta < 0) {
          position -= openRate.apply(-delta);
        } else {
          position += closeRate.apply(delta);
        }
      }
      return position = MathUtil.toRange(position, -1d, 1d);
    }
  }
}
