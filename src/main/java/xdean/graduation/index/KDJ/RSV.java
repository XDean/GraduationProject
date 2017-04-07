package xdean.graduation.index.KDJ;

import java.util.Comparator;

import xdean.graduation.index.base.DoubleIndex;
import xdean.jex.extra.Pair;
import xdean.jex.extra.collection.LinkedMonotoneList;
import xdean.jex.extra.collection.LinkedMonotoneList.MonoType;

public class RSV implements DoubleIndex {

  private final int n;
  private double current;
  private int count;
  private LinkedMonotoneList<Pair<Integer, Double>> max, min;

  public RSV(int n) {
    this.n = n;
    max = new LinkedMonotoneList<>(Comparator.<Pair<Integer, Double>> comparingDouble(p -> p.getRight()).reversed(), MonoType.OVERWRITE);
    min = new LinkedMonotoneList<>(Comparator.comparingDouble(p -> p.getRight()), MonoType.OVERWRITE);
  }

  @Override
  public void accept(Double d) {
    Pair<Integer, Double> pair = Pair.of(count, d);
    addAndRemove(max, pair);
    addAndRemove(min, pair);

    current = d;
    count++;
  }

  private void addAndRemove(LinkedMonotoneList<Pair<Integer, Double>> list, Pair<Integer, Double> pair) {
    list.add(pair);
    Pair<Integer, Double> first = list.get(0);
    if (count - first.getLeft() > n - 1) {
      list.remove(0);
    }
  }

  public Pair<Integer, Double> getMax() {
    return max.get(0);
  }

  public Pair<Integer, Double> getMin() {
    return min.get(0);
  }

  @Override
  public Double get() {
    if (count == 0) {
      return 100d;
    }
    double high = max.get(0).getRight();
    double low = min.get(0).getRight();
    if (low == high) {
      return 100d;
    } else {
      return (current - low) / (high - low) * 100;
    }
  }
}
