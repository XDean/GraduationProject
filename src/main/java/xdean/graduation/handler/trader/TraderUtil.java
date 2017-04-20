package xdean.graduation.handler.trader;

import lombok.experimental.UtilityClass;
import rx.annotations.Beta;
import xdean.graduation.handler.trader.base.PositionHandler;
import xdean.graduation.model.Order;
import xdean.graduation.model.Repo;
import xdean.graduation.workspace.Context;
import xdean.jex.extra.Wrapper;

@UtilityClass
public class TraderUtil {

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

  public PositionHandler closeIfOverNight(Repo repo) {
    Wrapper<Order> old = Wrapper.empty();
    return (position, order) -> {
      Order oldOrder = old.get();
      old.set(order);
      if (oldOrder == null) {
        return position;
      }
      if (oldOrder.isNight() != order.isNight()) {
        tradeByPosition(repo, oldOrder, 0d);
        return 0d;
      }
      return position;
    };
  }

  /**
   * 
   * @param repo
   * @param lossThreshold negative double, max loss, -1.0 ~ 0.0
   * @return
   */
  public PositionHandler cutLoss(Repo repo, double lossThreshold) {
    return (position, order) -> {
      if (repo.getReturnRate() < lossThreshold) {
        return 0;
      }
      return position;
    };
  }

  @Beta
  public PositionHandler saveEnarning(Repo repo, double startThreshold, double saveThreshold) {
    return new PositionHandler() {

      boolean saved;
      double maxRR;

      @Override
      public double getPosition(double oldPosition, Order order) {
        if (saved) {
          return 0;
        }
        if (maxRR > startThreshold && repo.getReturnRate() / maxRR < saveThreshold) {
          saved = true;
          return 0;
        }
        if (repo.getReturnRate() > maxRR) {
          maxRR = repo.getReturnRate();
        }
        return oldPosition;
      }
    };
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
