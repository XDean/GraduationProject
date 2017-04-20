package org.surus.math;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

public class TestAugmentedDickeyFuller {

  @Test
  public void testLinearTrend() {
    double[] x = getAR1(0.5, 100);
    assertTrue(new AugmentedDickeyFuller(x).isNeedsDiff());
  }

  @Test
  public void testLinearTrendWithOutlier() {
    double[] x = getAR1(0.5, 100);
    x[50] = 10000;
    assertTrue(new AugmentedDickeyFuller(x).isNeedsDiff());
  }

  private double[] getAR1(double p, int len) {
    Random rand = new Random();
    double[] x = new double[len];
    x[0] = rand.nextDouble();
    for (int i = 1; i < x.length; i++) {
      x[i] = p * x[i - 1] + rand.nextDouble();
    }
    return x;
  }
}