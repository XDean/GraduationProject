package xdean.graduation.handler.trader;

import lombok.experimental.UtilityClass;
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
  
  public PositionHandler cutLoss(){
    return null;
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
