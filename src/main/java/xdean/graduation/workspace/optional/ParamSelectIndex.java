package xdean.graduation.workspace.optional;

import java.util.function.Function;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import xdean.graduation.index.base.Index;
import xdean.graduation.index.base.Indexs;
import xdean.graduation.model.Result;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ParamSelectIndex {
  RR("rr",
      acc -> Indexs.accumulateReturnRate(acc).newIn(r -> r.getRepo().getReturnRate())),
  RRMD("rr/md",
      acc -> Indexs.rrMaxDrawdown(acc)
          .newOut(d -> Double.isNaN(d) || !Double.isFinite(d) ? 0 : d)
          .newIn(r -> r.getRepo().getReturnRate()));

  @Getter
  String name;
  Function<Boolean, Index<? super Result<?>, Double>> index;

  public Index<? super Result<?>, Double> getIndex(boolean feedAccumulate) {
    return index.apply(feedAccumulate);
  }
}