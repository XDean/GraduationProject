package org.surus.math;

import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import rx.Observable;
import xdean.graduation.index.MACD_Stable;
import xdean.graduation.model.Order;
import xdean.graduation.workspace.Context;
import xdean.graduation.workspace.Util;
import xdean.jex.extra.Pair;
import xdean.jex.extra.rx.op.ContinuousGroupOperator;

public class TestMacd {
  @Test
  public void test() {
    Util.getReader().read(Paths.get("data", "IF17S1.jbh.bsv"))
        .lift(new ContinuousGroupOperator<>(Order::getDate))
        .map(this::skipAndOp)
        .forEach(p -> {
          MACD_Stable macd = new MACD_Stable(12, 26, 9);
          List<Double> priceList = new LinkedList<>();
          List<Double> macdList = new LinkedList<>();
          List<Double> stableMacdList = new LinkedList<>();
          p.getRight()
              .doOnNext(o -> macd.accept(o.getAveragePrice()))
              .doOnNext(o -> priceList.add(o.getCurrentPrice()))
              .doOnNext(o -> macdList.add(macd.getHistogram()))
              .doOnNext(o -> stableMacdList.add(macd.getStableMacd()))
              .subscribe();
          double[] priceArray = toArray(priceList);
          double[] macdArray = toArray(macdList);
          double[] stableArray = toArray(stableMacdList);
          System.out.println("price: " + new AugmentedDickeyFuller(priceArray).isNeedsDiff());
          System.out.println("macd: " + new AugmentedDickeyFuller(macdArray).isNeedsDiff());
          System.out.println("stable: " + new AugmentedDickeyFuller(stableArray).isNeedsDiff());
        });
    ;
  }

  private double[] toArray(List<Double> list) {
    double[] array = new double[list.size()];
    int i = 0;
    for (Double d : list) {
      array[i] = d;
      i++;
    }
    return array;
  }

  private Pair<String, Observable<Order>> skipAndOp(Pair<String, List<Order>> pair) {
    return pair.right(
        Observable.from(pair.getRight())
            .skip(1)
            .lift(Context.DEFAULT_OPERATER));
  }
}
