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
    */
  def dailyAmount: Transactions => DailyAmountReport =
    tnx => for ((day, dayTnx) <- tnx.groupBy(_.transactionDay))
      yield DailyAmount(day, sumAmount(dayTnx))

  /**
    * Calculates the average value of transactions per account for each type of transaction (there are seven in total).
    * The output contains one item per account, each item includes the account id and the average value
    * for each transaction type see [[AccountAverageValue]]
    */
  def accountAverageAmount: Transactions => AccountAverageValueReport =
    tnx => for ((accountId, accTnx) <- tnx.groupBy(_.accountId))
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
    */
  def rollingDailyAccountStat: (Transactions, Int, Boolean) => DailyAccountStatReport =
    (tnx, wnd, withPartial) => {
      val tnxByDay = tnx.groupBy(_.transactionDay)
      for {
        (day, previousDays) <- rollingCalendar(dayRange(tnx), wnd, withPartial)
        // collect + flatten instead of flatMap, like  (acc, accTnx) <- (previousDays flatMap tnxByDay).groupBy(_.accountId)
        // to handle potential whole day gaps in the transaction list. If it is garanteed that there is no gaps then it can be flatMap
        (acc, accTnx) <- (previousDays collect tnxByDay).flatten.groupBy(_.accountId)
      } yield DailyAccountStat(day
        , acc
        , accTnx.view.map(_.transactionAmount).max
        , avgAmount(accTnx)
        , accTnx.groupBy(_.category).mapValues(sumAmount))
    }

  private[core] def sumAmount: Transactions => Double = (tnx) => tnx.foldLeft(0.0)(_ + _.transactionAmount)

  private[core] def avgAmount: Transactions => Double = (tnx) => sumAmount(tnx) / tnx.size

  private[core] def dayRange: Transactions => DayRange =
    tnx => {
      // Find min, max days of all transactions
      import math.{min, max}
      val (maxDay, minDay) = tnx.view.map(_.transactionDay).foldLeft((Int.MinValue, Int.MaxValue)) {
        case ((mx, mn), d) => (max(mx, d), min(mn, d))
      }
      // Construct the full calendar filling out all missing days from min day to max day of the transactions
      minDay to maxDay
    }

  private[core] val rollingCalendar: (DayRange, Int, Boolean) => RollingCalendar =
    (dayRange, window, withPartial) => {
      val strict = dayRange.drop(window).zip(dayRange.iterator.sliding(window).withPartial(withPartial).toSeq)
      if (withPartial) {
        val partialDays = dayRange.take(window).toArray
        partialDays.zipWithIndex.map {
          case (day, idx) => (day, partialDays.take(idx).toList)
        } ++ strict
      }
      else
        strict
    }
}
