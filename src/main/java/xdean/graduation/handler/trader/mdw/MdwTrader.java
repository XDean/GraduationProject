package xdean.graduation.handler.trader.mdw;

import lombok.Getter;
import xdean.graduation.handler.trader.Trader;
import xdean.graduation.handler.trader.TraderUtil;
import xdean.graduation.model.Order;
import xdean.graduation.model.Repo;

public class MdwTrader implements Trader<Integer[]> {

  @Getter
  Repo repo;
  int open;// Open
  int close;// Close
  boolean first = true;
  double openPrice;

  public MdwTrader(Repo repo) {
    this.repo = repo;
  }

  @Override
  public MdwTrader setParam(Integer[] p) {
    this.open = p[0];
    this.close = p[1];
    return this;
  }

  @Override
  public void trade(Order order) {
    if (first) {
      first = false;
      double delta = order.getCurrentPrice() - order.getLastClosePrice();
      if (delta > 0) {
        TraderUtil.sellPrice(repo, order);
      } else {
        TraderUtil.buyPrice(repo, order);
      }
      openPrice = order.getCurrentPrice();
      repo.open((int) (delta / open));

    } else {
      if (repo.getHold() != 0) {
        double profit = order.getCurrentPrice() - openPrice;
        if (profit * repo.getHold() < 0 && Math.abs(profit) > close) {
          if (repo.getHold() < 0) {
            TraderUtil.sellPrice(repo, order);
          } else {
            TraderUtil.buyPrice(repo, order);
          }
          repo.close();
        }
      }
    }
    repo.price(order.getCurrentPrice());
  }
}
