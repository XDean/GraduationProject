package xdean.graduation.index.base;

import lombok.experimental.UtilityClass;
import xdean.graduation.index.MaxDrawdown;
import xdean.graduation.index.KDJ.KDJ;
import xdean.graduation.index.KDJ.RSV;
import xdean.graduation.index.base.RecursiveIndex.DoubleRecursiveIndex;
import xdean.jex.extra.Wrapper;

@UtilityClass
public class Indexs {

  public DoubleIndex expma(int n) {
    return new DoubleRecursiveIndex(d -> d, (v, d) -> v / n * (n - 1) + d / n);
    // return new DoubleRecursiveIndex(d -> d, (v, d) -> v / (n + 1) * (n - 1) +
    // d / (n + 1) * 2);
  }

  public <T extends Comparable<T>> Index<T, T> max() {
    return new RecursiveIndex<>(t -> t, (v, d) -> d.compareTo(v) > 0 ? d : v);
  }

  public <T extends Comparable<T>> Index<T, T> min() {
    return new RecursiveIndex<>(t -> t, (v, d) -> d.compareTo(v) < 0 ? d : v);
  }

  public DoubleIndex sum() {
    return new DoubleRecursiveIndex(0d, (v, d) -> v + d);
  }

  public DoubleIndex product() {
    return new DoubleRecursiveIndex(1d, (v, d) -> v * d);
  }

  public DoubleIndex mean() {
    return new DoubleRecursiveIndex(0d, (v, c, d) -> (v * c + d) / (c + 1));
  }

  public DoubleIndex variance() {
    DoubleIndex mean = mean();
    return new DoubleRecursiveIndex(0d, (v, c, d) -> ((v + Math.pow(mean.get(), 2)) * c + d * d) / (c + 1) - Math.pow(mean.get(d), 2));
  }

  public DoubleIndex standardDeviation() {
    DoubleIndex var = variance();
    return new DoubleRecursiveIndex(0d, (v, d) -> Math.sqrt(var.get(d)));
  }

  /**
   * Feed daily return rate
   * 
   * @param base
   * @return
   */
  public DoubleIndex annualizedReturn() {
    return new DoubleIndex() {
      double rate = 1;
      int count = 0;

      @Override
      public Double get() {
        return Math.pow(rate, 250 / count) - 1;
      }

      @Override
      public void accept(Double t) {
        rate *= (1 + t);
        count++;
      }
    };
  }

  public DoubleIndex annualizedStandardDeviation() {
    DoubleIndex sd = standardDeviation();
    return new DoubleRecursiveIndex(d -> sd.get(d), (v, c, d) -> sd.get(d) * Math.sqrt(250 / (1 + c)));
  }

  /**
   * feed daily return rate
   * 
   * @param rf
   * @param feedAccumulatedReturnRate
   * @return
   */
  public DoubleIndex annualizedSharpRatio(double rf, boolean feedAccumulatedReturnRate) {
    DoubleIndex arr = annualizedReturn();
    DoubleIndex sd = annualizedStandardDeviation();
    Wrapper<Double> old = Wrapper.of(0d);
    return new DoubleRecursiveIndex(d -> {
      arr.accept(d);
      sd.accept(d);
      return Double.MAX_VALUE;
    }, (v, c, d) -> {
      if (feedAccumulatedReturnRate) {
        double oldValue = old.get();
        old.set(d);
        d = (1 + d) / (1 + oldValue) - 1;
      }
      double sdValue = sd.get(d);
      return sdValue == 0 ? Double.MAX_VALUE : (arr.get(d) - rf) / sdValue;
    });
  }

  public KDJ kdj(int n, int m, int l, int s) {
    return new KDJ(n, m, l, s);
  }

  public RSV rsv(int n) {
    return new RSV(n);
  }

  public DoubleIndex accumulateReturnRate(boolean feedAccumulate) {
    if (feedAccumulate) {
      return new DoubleRecursiveIndex(d -> d, (v, d) -> d);
    } else {
      return new DoubleRecursiveIndex(d -> d, (v, d) -> (1 + v) * (1 + d) - 1);
    }
  }

  public MaxDrawdown maxDrawdown(boolean feedAccumulate) {
    return new MaxDrawdown(feedAccumulate);
  }

  public DoubleIndex rrMaxDrawdown(boolean feedAccumulate) {
    MaxDrawdown md = maxDrawdown(feedAccumulate);
    if (feedAccumulate) {
      return new DoubleRecursiveIndex(0d, (v, d) -> d / md.get(d));
    } else {
      DoubleIndex accumuletRR = product();
      return new DoubleRecursiveIndex(0d, (v, d) -> (accumuletRR.get(d + 1) - 1)
          / md.get(d));
    }
  }
}
