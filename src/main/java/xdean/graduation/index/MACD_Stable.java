package xdean.graduation.index;

import java.util.List;

import xdean.jex.extra.collection.FixedLengthList;

public class MACD_Stable extends MACD {

  List<Double> list;

  public MACD_Stable(int f, int s, int a) {
    super(f, s, a);
    list = new FixedLengthList<>(s + a);
  }

  @Override
  public void accept(Double t) {
    super.accept(t);
    list.add(t);
  }

  @Override
  public Double get() {
    if (list.size() == 0) {
      return 0d;
    }
    return super.get() / list.get(0);

  }
  
  public double getStableMacd() {
    return get();
  }
}
