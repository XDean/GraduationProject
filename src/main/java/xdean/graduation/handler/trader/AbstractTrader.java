package xdean.graduation.handler.trader;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import xdean.graduation.model.Repo;

@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class AbstractTrader<P> implements Trader<P> {
  @Getter
  Repo repo;
  PositionPolicy policy = PositionPolicy.ALL_OUT;

  @Override
  public Trader<P> setPositionPolicy(PositionPolicy policy) {
    this.policy = policy;
    return this;
  }
}