package xdean.graduation.handler.trader.base;

import java.util.LinkedList;
import java.util.List;

import xdean.graduation.handler.trader.TraderUtil;
import xdean.graduation.model.Order;
import xdean.graduation.model.Repo;

public abstract class PositionTrader<P> extends AbstractTrader<P> implements PositionHandler {

  double position = 0;
  List<PositionHandler> handlers;

  public PositionTrader(Repo repo) {
    super(repo);
    handlers = new LinkedList<>();
  }

  @Override
  public final void trade(Order order) {
    position = getPosition(position, order);
    handlers.forEach(b -> position = b.getPosition(position, order));
    TraderUtil.tradeByPosition(repo, order, position);
  }

  public void addAdditionalPositionHandler(PositionHandler handler) {
    handlers.add(handler);
  }

}
