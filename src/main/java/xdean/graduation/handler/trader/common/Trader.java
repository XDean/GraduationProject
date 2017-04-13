package xdean.graduation.handler.trader.common;

import xdean.graduation.model.Order;
import xdean.graduation.model.Repo;

public interface Trader<Param> {

  void trade(Order order);

  Repo getRepo();

  Trader<Param> setParam(Param p);
  
  Trader<Param> setPositionPolicy(PositionPolicy policy);
}
