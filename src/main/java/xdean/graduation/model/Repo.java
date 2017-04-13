package xdean.graduation.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import xdean.jex.util.calc.MathUtil;

/**
 * Single target repository with trade tax and can sell short 100%.
 * 
 * @author XDean
 *
 */
@Builder(toBuilder = true)
@Getter
@ToString
@EqualsAndHashCode
public class Repo {

  public static class RepoBuilder {
    private RepoBuilder() {
      this.perLot = 100;
    }
  }

  private final boolean canSellShort;
  private final int perLot;// share per lot, usually 100
  private final double taxRate;// 0 ~ 1, two-way, usually 0.0002 ~ 0.0005
  private int hold;// hold lot
  private double price;// price per share
  private double cost;
  private double funds;
  private double totalTax;
  private double totalTurnover;

  public Repo price(double price) {
    this.price = price;
    return this;
  }

  public int buy(int lot) {
    lot = MathUtil.toRange(lot, 0, (int) (funds / price / perLot / (1 + taxRate)));
    double preTax = price * lot * perLot;
    double tax = preTax * taxRate;
    double afterTax = preTax + tax;
    totalTax += tax;
    totalTurnover += afterTax;
    hold += lot;
    cost += afterTax;
    funds -= afterTax;
    return lot;
  }

  public int sell(int lot) {
    lot = MathUtil.toRange(lot, 0, (canSellShort ? getMaxHold() : 0) + hold);
    double preTax = price * lot * perLot;
    double tax = preTax * taxRate;
    double afterTax = preTax - tax;
    totalTax += tax;
    totalTurnover += afterTax;
    hold -= lot;
    cost -= afterTax;
    funds += afterTax;
    return lot;
  }

  public double getPosition() {
    return ((double) hold) / getMaxHold();
  }

  /**
   * Adjust position to assigned rate
   * 
   * @param position negative means short if can sell short
   * @return actual volume(lot), negative means sell
   */
  public int open(double position) {
    position = MathUtil.toRange(position, -1d, 1d);
    return buy((int) ((getMaxHold() * position)) - hold) -
        sell((int) (getMaxHold() * (-position)) + hold);
  }

  public int close() {
    return open(0d);
  }

  public int getMaxHold() {
    return Math.max((int) (getCurrentAsset() / price / perLot), Math.abs(hold));
  }

  public double getReturnRate() {
    return getCurrentAsset() / getInitialAsset() - 1;
  }

  public Repo copy() {
    return this.toBuilder().build();
  }

  public double getPayTaxRate() {
    return totalTax / getInitialAsset();
  }

  public double getTurnOverRate() {
    return totalTurnover / getInitialAsset();
  }

  public double getCurrentAsset() {
    return hold * price * perLot + funds;
  }

  public double getInitialAsset() {
    return cost + funds;
  }
}
