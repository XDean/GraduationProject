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
public class VolumeOperator implements Operator<Order, Order> {

  int volume;

  @Override
  public Subscriber<? super Order> call(Subscriber<? super Order> s) {
    VolumeSubscriber ts = new VolumeSubscriber(s);
    s.add(ts);
    return ts;
  }

  private class VolumeSubscriber extends Subscriber<Order> {

    private final Subscriber<? super Order> actual;
    private Order old;

    public VolumeSubscriber(Subscriber<? super Order> actual) {
      this.actual = actual;
    }

    @Override
    public void onNext(Order next) {
      if (old == null) {
        old = next;
      } else {
        old = Order.merge(old, next);
      }
      while (old.getVolume() >= volume) {
        actual.onNext(old.toBuilder().volume(volume).build());
        old = old.toBuilder().volume(old.getVolume() - volume).build();
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
