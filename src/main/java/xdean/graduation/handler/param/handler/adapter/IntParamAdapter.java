package xdean.graduation.handler.param.handler.adapter;

import lombok.AllArgsConstructor;
import rx.Observable;
import xdean.jex.extra.Pair;
import xdean.jex.extra.rx.RxUtil;

@AllArgsConstructor
public class IntParamAdapter implements ParamAdapter<Integer, Integer> {

  int from, to, step, scale;

  @Override
  public Pair<Observable<Integer>, Integer> getParams() {
    return Pair.of(RxUtil.range(from, to, step), step / scale);
  }

  @Override
  public Pair<Observable<Integer>, Integer> getParams(Integer paramResult, Integer precision) {
    if (precision == 0) {
      return Pair.of(Observable.just(paramResult), precision);
    }
    return Pair.of(
        RxUtil.range(
            Math.max(from, paramResult - precision * scale),
            Math.min(to, paramResult + precision * scale),
            precision
            ),
        precision / scale);
  }

}
