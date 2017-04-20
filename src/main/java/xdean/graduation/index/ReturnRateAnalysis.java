package xdean.graduation.index;

import xdean.graduation.index.base.Index;
import xdean.graduation.index.base.Indexs;

class ReturnRateAnalysis {
  double rr;
  MaxDrawdown md = Indexs.maxDrawdown(true);
  Index<Double, Double> max = Indexs.max();
  Index<Double, Double> min = Indexs.min();

  void accept(double rr) {
    this.rr = rr;
    Double d = rr;
    md.accept(d);
    max.accept(d);
    min.accept(d);
  }
}