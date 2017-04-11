package xdean.graduation.handler.param.handler.adapter;

import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.observables.SyncOnSubscribe;
import xdean.jex.extra.Pair;
import xdean.jex.util.collection.ListUtil;

public class IntArrayParamAdapter implements ParamAdapter<Integer[], Integer[]> {

  // public static void main(String[] args) {
  // new IntArrayParamAdapter(
  // new IntParamAdapter(1, 100, 10, 10),
  // new IntParamAdapter(1, 100, 10, 10),
  // new IntParamAdapter(1, 100, 10, 10))
  // .select(i -> {
  // System.out.println(Arrays.toString(i));
  // return i[0] / i[1] + i[1] / i[0];
  // })
  // .doOnSuccess(s -> System.out.println(s))
  // .subscribe();
  // }

  ParamAdapter<Integer, Integer>[] intParamAdapters;

  @SafeVarargs
  public IntArrayParamAdapter(ParamAdapter<Integer, Integer>... intParamAdapters) {
    this.intParamAdapters = intParamAdapters;
  }

  @Override
  public Pair<Observable<Integer[]>, Integer[]> getParams() {
    return merge(Observable.from(intParamAdapters).map(ParamAdapter::getParams));
  }

  @Override
  public Pair<Observable<Integer[]>, Integer[]> getParams(Integer[] paramResult, Integer[] precision) {
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

  private Observable<Integer[]> toObservableArray(Observable<Observable<Integer>> obs) {
    List<Integer[]> list = obs.map(ob -> toArray(ob)).toList().toBlocking().last();
    return Observable.create(new SyncOnSubscribe<Integer[], Integer[]>() {
      @Override
      protected Integer[] generateState() {
        Integer[] array = new Integer[list.size()];
        Arrays.fill(array, 0);
        return array;
      }

      @Override
      protected Integer[] next(Integer[] state, Observer<? super Integer[]> observer) {
        Integer[] next = new Integer[list.size()];
        for (int i = 0; i < next.length; i++) {
          next[i] = list.get(i)[state[i]];
        }
        observer.onNext(next);
        state[state.length - 1]++;
        for (int i = state.length - 1; i >= 0; i--) {
          int delta = list.get(i).length - state[i];
          if (delta > 0) {
            break;
          } else if (delta == 0) {
            state[i] = 0;
            if (i == 0) {
              observer.onCompleted();
              break;
            }
            state[i - 1]++;
          }
        }
        return state;
      }
    });
  }

  private Integer[] toArray(Observable<Integer> ob) {
    List<Integer> list = ob.toList().toBlocking().last();
    Integer[] array = new Integer[list.size()];
    ListUtil.forEach(list, (t, n) -> array[n] = t);
    return array;
  }

  private Pair<Observable<Integer[]>, Integer[]> merge(Observable<Pair<Observable<Integer>, Integer>> ob) {
    return ob.toList()
        .map(list -> Pair.of(
            toObservableArray(Observable.from(list).map(Pair::getLeft)),
            toArray(Observable.from(list).map(Pair::getRight))))
        .toBlocking()
        .last();
  }
}
