package xdean.graduation.handler.param.selector;

import lombok.AllArgsConstructor;
import rx.Observable;
import rx.Subscriber;
import xdean.jex.extra.Pair;
import xdean.jex.extra.rx.SimpleOperator;

/**
 * It select the best param in an Observable. So usually you may nest Observable first.
 * 
 * <pre>
 * Observable&lt;Pair&lt;P, R&gt;&gt; ob;
 * ob.nest().lift(new SelectorOperator&lt;&gt;(ParamSelector));
 * </pre>
 * 
 * @author XDean
 *
 * @param <P>
 * @param <R>
 */
@AllArgsConstructor
public class SelectorOperator<P, R> extends SimpleOperator<Pair<P, R>, Observable<Pair<P, R>>> {
  ParamSelector<P, R> paramSelector;

  @Override
  public void onNext(Subscriber<? super Pair<P, R>> actual, Observable<Pair<P, R>> t) {
    actual.onNext(paramSelector.select(t));
  }
}