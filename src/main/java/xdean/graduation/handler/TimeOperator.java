package xdean.graduation.handler;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import rx.Observable.Operator;
import rx.Subscriber;
import xdean.graduation.model.Order;

/**
 * Orders with less than baseGap will be merged, and every two adjoin orders'
 * gap equals baseGap or more than ignoreGap
 * 
 * @author XDean
 *
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class TimeOperator implements Operator<Order, Order> {
  private static final double ERROR_RANGE = 0.1;
  int baseGap;
  int ignoreGap;

  @Override
  public Subscriber<? super Order> call(Subscriber<? super Order> s) {
    TimeSubscriber ts = new TimeSubscriber(s);
    s.add(ts);
    return ts;
  }

  private class TimeSubscriber extends Subscriber<Order> {

    private final Subscriber<? super Order> actual;

    private Order old;

    public TimeSubscriber(Subscriber<? super Order> actual) {
      this.actual = actual;
    }

    @Override
    public void onNext(Order next) {
      if (old == null) {
        old = next;
        return;
      }

      long gap = next.getTime() - old.getTime();
      // fit or ignore
      if (Math.abs(gap - baseGap) < baseGap * ERROR_RANGE || gap > ignoreGap) {
        actual.onNext(old);
        old = next;
      } else if (gap < baseGap) {// merge
        old = Order.merge(old, next).toBuilder().time(old.getTime()).build();
      } else {// split(zero fill)
        actual.onNext(old);
        old = Order.builder()
            .time(old.getTime() + baseGap)
            .volume(0)
            .currentPrice(old.getCurrentPrice())
            .averagePrice(old.getCurrentPrice())
            .build();
        onNext(next);
      }
    }

    @Override
    public void onCompleted() {
      if (old != null) {
        actual.onNext(old);
        old = null;
      }
      actual.onCompleted();
    }

    @Override
    public void onError(Throwable e) {
      actual.onError(e);
    }
  }
}
