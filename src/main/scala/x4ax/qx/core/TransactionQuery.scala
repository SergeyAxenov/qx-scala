package x4ax.qx.core

import x4ax.qx.core.model.{AccountAverageValue, DailyAccountStat, DailyAmount, Transaction}

/**
  * Implements the transaction queries
  */
object TransactionQuery {
  // Domain types
  type Transactions = Seq[Transaction]
  type DailyAmountReport = Iterable[DailyAmount]
  type AccountAverageValueReport = Iterable[AccountAverageValue]
  type DailyAccountStatReport = Iterable[DailyAccountStat]
  type Day = Int
  type DayRange = Range
  type RollingCalendar = Seq[(Day, Seq[Day])]

  /**
    * Calculates the total transaction value for all transactions for each day.
    * The output contains one item for each day and each item includes the day and the total value see [[DailyAmount]]
    *
    * @param tnx Transactions
    * @return Daily Amount Report
    */
  def dailyAmount(tnx: Transactions): DailyAmountReport =
    for ((day, dayTnx) <- tnx.groupBy(_.transactionDay))
      yield DailyAmount(day, sumAmount(dayTnx))

  /**
    * Calculates the average value of transactions per account for each type of transaction (there are seven in total).
    * The output contains one item per account, each item includes the account id and the average value
    * for each transaction type see [[AccountAverageValue]]
    *
    * @param tnx Transactions
    * @return Account Average Value Report
    */
  def accountAverageAmount(tnx: Transactions): AccountAverageValueReport =
    for ((accountId, accTnx) <- tnx.groupBy(_.accountId))
      yield AccountAverageValue(accountId, accTnx.groupBy(_.category).mapValues(avgAmount))

  /**
    * For each day, calculates statistics for each account number for given number of previous  days of transactions, not
    * including transactions from the day statistics are being calculated for.
    * For example, on day 10 you should consider only the transactions from days 5 to 9 given rolling window of 5 days.
    * The statistics include:
    * • The maximum transaction value in the rolling window of transactions per account
    * • The average transaction value of the rolling window of transactions per account
    * • The total transaction value of transactions types “AA”, “CC” and “FF” in the rolling window of transactions per account
    * The output contain one item per day per account id and each item contains each of the calculated statistic see [[DailyAccountStat]]
    *
    * @param tnx         Transactions
    * @param wnd         Size of the rolling window
    * @param withPartial True - to include initial partial rolling frames, False - only include the full frames
    * @return Daily Account Stat Report
    */
  def rollingDailyAccountStat(tnx: Transactions, wnd: Int, withPartial: Boolean): DailyAccountStatReport = {
    val tnxByDay = tnx.groupBy(_.transactionDay)
    for {
      (day, previousDays) <- rollingCalendar(dayRange(tnx), wnd, withPartial)
      (acc, accTnx) <- (previousDays collect tnxByDay).flatten.groupBy(_.accountId)
    } yield DailyAccountStat(
      day
      , acc
      , accTnx.view.map(_.transactionAmount).max
      , avgAmount(accTnx)
      , accTnx.groupBy(_.category).mapValues(sumAmount))
  }

  private[core] def sumAmount(tnx: Transactions): Double = tnx.foldLeft(0.0)(_ + _.transactionAmount)

  private[core] def avgAmount(tnx: Transactions): Double = sumAmount(tnx) / tnx.size

  /**
    * Returns the day range (min .. max) for the given transactions
    * Handle a case of not sorted list of transactions anb transactions with an entire day missing
    *
    * @param tnx Transactions
    * @return Day Range (min .. max) inclusive
    */
  private[core] def dayRange(tnx: Transactions): DayRange = {
    // Find min, max days of all transactions
    import math.{min, max}
    val (maxDay, minDay) = tnx.view.map(_.transactionDay).foldLeft((Int.MinValue, Int.MaxValue)) {
      case ((mx, mn), d) => (max(mx, d), min(mn, d))
    }
    // Construct the full calendar filling out all missing days from min day to max day of the transactions
    minDay to maxDay
  }

  /**
    * Constructs the rolling calendar (... (9, (4,5,6,7,8)),(10, (5,6,7,8,9)) ... ) over the given day range and the rolling window size
    * The calendar can be generated either with or without partial frames:
    * With partial frames: ((1, ()), (2, (1)), (3, (1,2)), (4,(1,2,3)), (5, (1,2,3,4)), (6, (1,2,3,4,5)) ...)
    * Without partial frames: ((6, (1,2,3,4,5)) ...)
    *
    * @param dayRange
    * @param wnd
    * @param withPartial
    * @return
    */
  private[core] def rollingCalendar(dayRange: DayRange, wnd: Int, withPartial: Boolean): RollingCalendar = {
    val strict = dayRange.drop(wnd).zip(dayRange.iterator.sliding(wnd).withPartial(withPartial).toSeq)
    if (withPartial) {
      val partialDays = dayRange.take(wnd).toArray
      partialDays.zipWithIndex.map {
        case (day, idx) => (day, partialDays.take(idx).toList)
      } ++ strict
    }
    else
      strict
  }
}
