package xdean.graduation.index;

import xdean.graduation.index.base.DoubleIndex;
import xdean.graduation.index.base.Index;
import xdean.graduation.index.base.Indexs;

public class MACD implements DoubleIndex {

  private DoubleIndex fast;
  private DoubleIndex slow;
  private DoubleIndex dif;// or macd
  private DoubleIndex macd;// or signal
  private DoubleIndex histogram;
  private Index<Boolean, Integer> count;
  private int f;
  private final Double zero = 0d;

  public MACD(int f, int s, int a) {
    this.f = f;
    count = Indexs.count();
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
    count.accept(Boolean.TRUE);
    histogram.accept(t);
  }

  private boolean notEnough() {
    return count.get() < f;
  }

  @Override
  public Double get() {
    if (notEnough()) {
      return zero;
    }
    return histogram.get();
  }

  public double getFast() {
    if (notEnough()) {
      return zero;
    }
    return fast.get();
  }

  public double getSlow() {
    if (notEnough()) {
      return zero;
    }
    return slow.get();
  }

  public double getDif() {
    if (notEnough()) {
      return zero;
    }
    return dif.get();
  }

  public double getMacd() {
    if (notEnough()) {
      return zero;
    }
    return macd.get();
  }

  public double getHistogram() {
    if (notEnough()) {
      return zero;
    }
    return histogram.get();
  }
}
