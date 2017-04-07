package xdean.graduation.index;

import xdean.graduation.index.base.DoubleIndex;
import xdean.graduation.index.base.Indexs;

public class MACD implements DoubleIndex {

  private DoubleIndex fast;
  private DoubleIndex slow;
  private DoubleIndex dif;// or macd
  private DoubleIndex macd;// or signal
  private DoubleIndex histogram;

  public MACD(int f, int s, int a) {
    fast = Indexs.expma(f);
    slow = Indexs.expma(s);
    dif = DoubleIndex.create(fast.andThen(slow), () -> fast.get() - slow.get());
    macd = Indexs.expma(a);
    histogram = DoubleIndex.create(d -> {
      dif.accept(d);
      macd.accept(dif.get());
    }, () -> dif.get() - macd.get());
  }

  @Override
  public void accept(Double t) {
    histogram.accept(t);
  }

  @Override
  public Double get() {
    return histogram.get();
  }

  public double getFast() {
    return fast.get();
  }

  public double getSlow() {
    return slow.get();
  }

  public double getDif() {
    return dif.get();
  }

  public double getMacd() {
    return macd.get();
  }

  public double getHistogram() {
    return histogram.get();
  }
}
