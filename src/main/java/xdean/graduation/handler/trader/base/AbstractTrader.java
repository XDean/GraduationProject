package xdean.graduation.handler.trader.base;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import xdean.graduation.model.Repo;

@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class AbstractTrader<P> implements Trader<P> {
  @Getter
  Repo repo;

  public AbstractTrader(Repo repo) {
    this.repo = repo;
  }
}
