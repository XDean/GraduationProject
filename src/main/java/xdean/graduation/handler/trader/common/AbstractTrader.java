package xdean.graduation.handler.trader.common;

import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import xdean.graduation.model.Order;
import xdean.graduation.model.Repo;

@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class AbstractTrader<P> implements Trader<P> {
  @Getter
  Repo repo;
  PositionPolicy policy = PositionPolicy.ALL_OUT;
  Consumer<Order> overNight;

  public AbstractTrader(Repo repo) {
    this.repo = repo;
    overNight = TraderUtil.closeIfOverNight(repo);
  }

  @Override
  public void trade(Order order) {
    overNight.accept(order);
  }

  @Override
  public Trader<P> setPositionPolicy(PositionPolicy policy) {
    this.policy = policy;
    return this;
  }
}
