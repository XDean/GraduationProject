package xdean.graduation.handler.trader.base;

import xdean.graduation.model.Order;

@FunctionalInterface
public interface PositionHandler {
  double getPosition(double oldPosition, Order order);
}