package xdean.graduation.handler.param.selector;

import org.junit.Test;

import rx.Observable;
import xdean.graduation.handler.param.selector.ConvolutionSelector.WeightPolicy;
import xdean.jex.extra.Pair;

//TODO
public class TestConvolutionSelector {
  @Test
  public void test() {
    new ConvolutionSelector(WeightPolicy.CENTER, 1, sqrDistance -> 1d)
        .calcConvolution(Observable.just(
            Pair.of(new int[] { 0, 0 }, 1d),
            Pair.of(new int[] { 0, 1 }, 2d),
            Pair.of(new int[] { 0, 2 }, 3d),
            Pair.of(new int[] { 1, 0 }, 4d),
            Pair.of(new int[] { 1, 1 }, 5d),
            Pair.of(new int[] { 1, 2 }, 6d),
            Pair.of(new int[] { 2, 0 }, 7d),
            Pair.of(new int[] { 2, 1 }, 8d),
            Pair.of(new int[] { 2, 2 }, 9d)
            ))
        .map(Pair::getRight)
        .test()
        .assertResult(
            9d,
            11d,
            7d,
            7d,
            5d,
            3d,
            3d,
            -1d,
            1d
        );
  }
}
