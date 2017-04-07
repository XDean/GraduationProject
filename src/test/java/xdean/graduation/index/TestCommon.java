package xdean.graduation.index;

import org.junit.Assert;
import org.junit.Test;

import xdean.graduation.index.KDJ.RSV;
import xdean.graduation.index.base.DoubleIndex;
import xdean.graduation.index.base.Index;
import xdean.graduation.index.base.Indexs;

public class TestCommon {
  @Test
  public void testMa() {
    DoubleIndex ma = Indexs.expma(2);
    Double[] data = { 1d, 2d, 3d, 4d, 5d };
    Double[] answer = { 1d, 1.5, 2.25, 3.125, 4.0625 };
    testIndex(ma, data, answer);
  }

  @Test
  public void testRSV() {
    RSV rsv = new RSV(5);
    Double[] data = { 20d, 15d, 17.5, 17d, 10d, 11.5, 16d, 18d, 15d };
    Double[] answer = { 100d, 0d, 50d, 40d, 0d, 20d, 80d, 100d, 62.5 };
    testIndex(rsv, data, answer);
//     for (int i = 0; i < data.length; i++) {
//     rsv.accept(data[i]);
//     Assert.assertTrue(answer[i] == rsv.get());
//     }
  }

  private <I, O> void testIndex(Index<I, O> index, I[] in, O[] out) {
    for (int i = 0; i < in.length; i++) {
      index.accept(in[i]);
      Assert.assertEquals(out[i], index.get());
    }
  }
}
