package xdean.graduation.handler.param.selector;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.function.Function;

import lombok.AllArgsConstructor;
import rx.Observable;
import rx.Single;
import xdean.jex.extra.Pair;
import xdean.jex.util.calc.MathUtil;
import xdean.jex.util.lang.ArrayUtil;

import com.google.common.collect.TreeMultimap;
import com.google.common.util.concurrent.AtomicDouble;

/**
 * 
 * 
 * Basic assumption:<br>
 * 1. every P[] have same length<br>
 * 2. don't consider different spacing, every adjoin items have same spacing<br>
 * 3. finite elements<br>
 * 4. no items have same param
 * 
 * @author XDean
 */
@SuppressWarnings("unchecked")
@AllArgsConstructor
public class ConvolutionSelector implements ParamSelector<int[], Double> {

  /**
   * How to make weight sum equals 1
   */
  public static enum WeightStrategy {
    /* only add center value */
    CENTER,

    /* average all value */
    AVG;
  }

  WeightStrategy weightStrategy;
  int maxDistance;
  Function<Integer, Double> sqrDistanceToWeight;

  @Override
  public Single<Pair<int[], Double>> select(Observable<Pair<int[], Double>> ob) {
    return calcConvolution(ob)
        .reduce((a, b) -> b.getRight().compareTo(a.getRight()) > 0 ? b : a)
        .map(Pair::getLeft)
        .toSingle();
  }

  public Observable<Pair<Pair<int[], Double>, Double>> calcConvolution(
      Observable<Pair<int[], Double>> ob) {
    List<Pair<int[], Double>> list = ob.toList().toSingle().toBlocking().value();

    int dimension = list.get(0).getLeft().length;

    TreeMultimap<Integer, Pair<int[], Double>>[] views = new TreeMultimap[dimension];
    for (int i = 0; i < dimension; i++) {
      TreeMultimap<Integer, Pair<int[], Double>> view = TreeMultimap.create(Comparator.naturalOrder(),
          (p1, p2) -> ArrayUtil.compare(p1.getLeft(), p2.getLeft()));
      views[i] = view;
      for (Pair<int[], Double> pair : list) {
        view.put(pair.getLeft()[i], pair);
      }
    }

    return Observable.from(list)
        .<Pair<Pair<int[], Double>, Double>> flatMap(pair -> {
          int[] param = pair.getLeft();
          AtomicDouble weightSum = new AtomicDouble();
          return Observable.just(Observable.range(-maxDistance, maxDistance * 2 + 1))
              .repeat(dimension)
              .compose(MathUtil::cartesianProduct)
              .map(point -> {
                Set<Pair<int[], Double>> targetSet = new HashSet<>();
                for (int i = 0; i < dimension; i++) {
                  Integer d = get(views[i].keySet(), param[i], point[i]);
                  if (d == null) {
                    return 0d;
                  }
                  NavigableSet<Pair<int[], Double>> section = views[i].get(d);
                  if (i == 0) {
                    targetSet.addAll(section);
                  } else {
                    targetSet.retainAll(section);
                    if (targetSet.isEmpty()) {
                      return 0d;
                    }
                  }
                }
                Pair<int[], Double> theParam = targetSet.stream().findFirst().get();
                double weight = sqrDistanceToWeight.apply(MathUtil.squareSum(point));
                weightSum.addAndGet(weight);
                return weight * theParam.getRight().doubleValue();
              })
              .reduce((v, n) -> v + n)
              .map(convolution -> {
                if (weightStrategy == WeightStrategy.CENTER) {
                  return convolution += (1 - weightSum.get()) * pair.getRight().doubleValue();
                } else if (weightStrategy == WeightStrategy.AVG) {
                  return convolution / weightSum.get();
                } else {
                  throw new UnsupportedOperationException();
                }
              })
              // .doOnNext(d -> System.out.println(Arrays.toString(pair.getLeft()) + ", " + weightSum.get() + ", " +
              // pair.getRight() + ", " + d))
              .map(c -> Pair.of(pair, c));
        });
  }

  private static <T> T get(NavigableSet<T> set, T center, int relativePos) {
    T answer = center;
    while (true) {
      if (relativePos == 0 || answer == null) {
        return answer;
      } else if (relativePos > 0) {
        relativePos--;
        answer = set.higher(answer);
      } else {
        relativePos++;
        answer = set.lower(answer);
      }
    }
  }
}
