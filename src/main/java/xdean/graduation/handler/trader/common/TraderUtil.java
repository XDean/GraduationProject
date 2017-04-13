package xdean.graduation.handler.trader.common;

import lombok.experimental.UtilityClass;
import xdean.graduation.index.base.DoubleIndex;
import xdean.graduation.model.Order;
import xdean.graduation.model.Repo;
import xdean.graduation.workspace.Context;
import xdean.jex.util.calc.MathUtil;

import com.google.common.base.Supplier;

@UtilityClass
public class TraderUtil {

  /**
   * 
   * @param index Index output histogram
   * @return index output now position
   */
  public DoubleIndex histogramToPosition(DoubleIndex index, Supplier<PositionPolicy> policy) {
    return new DoubleIndex() {
      double position = 0;
      Double oldHistogram;

      @Override
      public Double get() {
        return position;
      }

      @Override
      public void accept(Double t) {
        oldHistogram = index.get();
        index.accept(t);
        adjustPosition();
      }

      void adjustPosition() {
        position += adjustByHistogram(oldHistogram, index.get(), policy.get());
        position = MathUtil.toRange(position, -1d, 1d);
      }
    };
  }

  double adjustByHistogram(double oldHistogram, double histogram, PositionPolicy policy) {
    double delta = histogram - oldHistogram;
    if (histogram > 0) {
      if (delta > 0) {
        return policy.open(delta);
      } else {
        return -policy.close(-delta);
      }
    } else if (histogram < 0) {
      if (delta < 0) {
        return -policy.open(-delta);
      } else {
        return policy.close(delta);
      }
    } else {
      return 0;
    }
  }

  /**
   * @param repo
   * @param order
   * @param position the position adjust to, do nothing if NaN
   */
  public void tradeByPosition(Repo repo, Order order, double position) {
    if (Double.isNaN(position) == false) {
      if (position > repo.getPosition()) {
        sellPrice(repo, order);
      } else {
        buyPrice(repo, order);
      }
      repo.open(position);
    }
    currentPrice(repo, order);
  }

  public void buyPrice(Repo repo, Order order) {
    if (Context.TRADE_WITH_CURRENT_PRICE) {
      currentPrice(repo, order);
    } else {
      repo.price(order.getBuyPrice() == 0 ? order.getCurrentPrice() : order.getBuyPrice());
    }
  }

  public void sellPrice(Repo repo, Order order) {
    if (Context.TRADE_WITH_CURRENT_PRICE) {
      currentPrice(repo, order);
    } else {
      repo.price(order.getSellPrice() == 0 ? order.getCurrentPrice() : order.getSellPrice());
    }
  }

  public void currentPrice(Repo repo, Order order) {
    repo.price(order.getCurrentPrice());
  }
}
