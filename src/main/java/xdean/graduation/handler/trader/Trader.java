package xdean.graduation.handler.trader;

import xdean.graduation.model.Order;
import xdean.graduation.model.Repo;

public interface Trader<Param> {
  Repo getRepo();

  void trade(Order order);

  Trader<Param> setParam(Param p);
}
