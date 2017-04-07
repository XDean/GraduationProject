package xdean.graduation.index.KDJ;

import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import xdean.graduation.index.base.DoubleIndex;
import xdean.graduation.index.base.Index;
import xdean.graduation.index.base.Indexs;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KDJ implements DoubleIndex {
  
  RSV rsv;
  Index<Double, Double> K, D;
  Supplier<Double> J;

  public KDJ(int n, int m, int l, int s) {
    rsv = new RSV(n);
    K = Indexs.expma(m);
    D = Indexs.expma(l);
    J = () -> s * K.get() - (s - 1) * D.get();
  }

  @Override
  public void accept(Double d) {
    rsv.accept(d);
    K.accept(rsv.get());
    D.accept(K.get());
  }

  @Override
  public Double get() {
    return getJ();
  }
  
  public RSV getRsv() {
    return rsv;
  }

  public double getK() {
    return K.get();
  }

  public double getD() {
    return D.get();
  }

  public double getJ() {
    return J.get();
  }
}
