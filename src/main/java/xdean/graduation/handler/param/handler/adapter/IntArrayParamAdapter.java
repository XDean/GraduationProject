package xdean.graduation.handler.param.handler.adapter;

import java.util.List;

import rx.Observable;
import xdean.jex.extra.Pair;
import xdean.jex.util.calc.MathUtil;
import xdean.jex.util.collection.ListUtil;

public class IntArrayParamAdapter implements ParamAdapter<int[], int[]> {

  ParamAdapter<Integer, Integer>[] intParamAdapters;

  @SafeVarargs
  public IntArrayParamAdapter(ParamAdapter<Integer, Integer>... intParamAdapters) {
    this.intParamAdapters = intParamAdapters;
  }

  @Override
  public Pair<Observable<int[]>, int[]> getParams() {
    return merge(Observable.from(intParamAdapters).map(ParamAdapter::getParams));
  }

  @Override
  public Pair<Observable<int[]>, int[]> getParams(int[] paramResult, int[] precision) {
    boolean breaked = false;
    for (int i : precision) {
      if (i != 0) {
        breaked = true;
        break;
      }
    }
    if (breaked == false) {
      return Pair.of(Observable.just(paramResult), precision);
    }
    return merge(Observable.<Pair<Observable<Integer>, Integer>> unsafeCreate(s -> {
      for (int i = 0; i < intParamAdapters.length; i++) {
        s.onNext(intParamAdapters[i].getParams(paramResult[i], precision[i]));
      }
      s.onCompleted();
    }));
  }

  private int[] toArray(Observable<Integer> ob) {
    List<Integer> list = ob.toList().toBlocking().last();
    int[] array = new int[list.size()];
    ListUtil.forEach(list, (t, n) -> array[n] = t);
    return array;
  }

  private Pair<Observable<int[]>, int[]> merge(Observable<Pair<Observable<Integer>, Integer>> ob) {
    return ob.toList()
        .map(list -> Pair.of(
            MathUtil.cartesianProduct(Observable.from(list).map(Pair::getLeft)),
            toArray(Observable.from(list).map(Pair::getRight))))
        .toBlocking()
        .last();
  }
}
