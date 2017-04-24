package xdean.graduation.index;

import java.util.function.Function;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import xdean.graduation.index.base.DoubleIndex;
import xdean.jex.util.string.StringUtil;

/**
 * Accept stock price, output Z-value<br>
 * 0: negative runs<br>
 * 1: zero runs<br>
 * 2: positive runs<br>
 * 3: total runs<br>
 * 
 * @author XDean
 *
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RunsTest implements DoubleIndex {

  long N;
  long[] n = new long[3];
  long RUN;
  long[] run = new long[3];
  double oldValue;
  int oldSign;

  @Override
  public void accept(Double t) {
    double d = t;
    int sign = (int) Math.signum(d - oldValue);
    oldValue = d;
    N++;
    n[sign + 1]++;
    if (sign != oldSign) {
      oldSign = sign;
      RUN++;
      run[sign + 1]++;
    }
  }

  @Override
  public Double get() {
    return getKStatistic();
  }

  public double getKStatistic() {
    return (RUN + 0.5 - getMean()) / getStd();
  }

  public double getMean() {
    return (N * (N + 1) - sqrSum()) / N;
  }

  public double getStd() {
    double up = sqrSum() * (sqrSum() + N * (N + 1)) - 2 * N * cubeSum() - N * N * N;
    double down = N * N * (N - 1);
    double std = Math.sqrt(up / down);
    return std;
  }

  private double sqrSum() {
    return n[0] * n[0] + n[1] * n[1] + n[2] * n[2];
  }

  private double cubeSum() {
    return n[0] * n[0] * n[0] + n[1] * n[1] * n[1] + n[2] * n[2] * n[2];
  }

  public long getN(int i) {
    return i == 3 ? N : n[i];
  }

  public long getRun(int i) {
    return i == 3 ? RUN : run[i];
  }

  public double getExpectRun(int i) {
    return i == 3 ? getMean() : n[i] - n[i] * n[i] / (double) N;
  }

  public double getAvgLen(int i) {
    return getN(i) / (double) getRun(i);
  }

  public double getExpectAvgLen(int i) {
    return getN(i) / getExpectRun(i);
  }

  /**
   * <pre>
   *  n run run* len len* 
   * -
   * 0 
   * + 
   * t 
   * m σ K
   * </pre>
   */
  @Override
  public String toString() {
    String[] signs = { "-", "0", "+", "total" };
    Function<Integer, String> func = i -> String.format("|%8s|%8d|%8d|%8.0f|%8.2f|%8.2f|",
        signs[i], getN(i), getRun(i), getExpectRun(i), getAvgLen(i), getExpectAvgLen(i));
    return String.join("\n",
        StringUtil.repeat("-", 55),
        String.format("|%8s|%8s|%8s|%8s|%8s|%8s|", "", "n", "run", "run*", "len", "len*"),
        "|" + StringUtil.repeat("-", 53) + "|",
        func.apply(0),
        "|" + StringUtil.repeat("-", 53) + "|",
        func.apply(1),
        "|" + StringUtil.repeat("-", 53) + "|",
        func.apply(2),
        "|" + StringUtil.repeat("-", 53) + "|",
        func.apply(3),
        "|" + StringUtil.repeat("-", 53) + "|",
        String.format("|%8s|%8.0f|%8s|%8.2f|%8s|%8.2f|", "m", getMean(), "σ", getStd(), "K", getKStatistic()),
        StringUtil.repeat("-", 55)
        );
  }
}
