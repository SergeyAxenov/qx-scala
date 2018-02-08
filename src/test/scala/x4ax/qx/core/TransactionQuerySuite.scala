package x4ax.qx.core

import org.scalatest.FunSuite
import org.scalatest.Matchers._
import x4ax.qx.core.model.{AccountAverageValue, DailyAccountStat, DailyAmount, Transaction}
import x4ax.qx.core.TransactionQuery._

class TransactionQuerySuite extends FunSuite {

  test("Daily amount report of an empty tnx list should be empty") {
    val tnx = List[Transaction]()
    dailyAmount(tnx).size should be(0)
  }

  test("Daily amount report for a single transaction should return a single line") {
    val tnx = List[Transaction](
      Transaction("", "", 1, "", 1.5)
    )
    dailyAmount(tnx).size should be(1)
  }

  test("Daily amount report for a single transaction should return the transaction amount") {
    val tnx = List[Transaction](
      Transaction("", "", 1, "", 1.5)
    )
    val expected = DailyAmount(1, 1.5)
    val actual = dailyAmount(tnx).head
    actual should be(expected)
  }

  test("Daily amount report - simple case") {
    val tnx = List[Transaction](
      Transaction("", "", 1, "", 1.5)
      , Transaction("", "", 1, "", 2.5)
      , Transaction("", "", 2, "", 0)
      , Transaction("", "", 12, "", 15.0)
      , Transaction("", "", 12, "", 25.0)
    )
    val expected = Array(
      DailyAmount(1, 4.0)
      , DailyAmount(2, 0.0)
      , DailyAmount(12, 40.0)
    )

    val actual = dailyAmount(tnx).toArray.sortBy(_.day)
    actual should be(expected)
  }

  test("Account Average Value report - simple case") {
    val tnx = List[Transaction](
      Transaction("", "acc1", 1, "AA", 2.0)
      , Transaction("", "acc1", 2, "AA", 4.0)
      , Transaction("", "acc1", 2, "BB", 4.5)
      , Transaction("", "acc2", 2, "CC", 10.0)
      , Transaction("", "acc2", 3, "CC", 20.0)
      , Transaction("", "acc2", 3, "UNKNOWN", 20.0)
      , Transaction("", "acc3", 4, "UNKNOWN", 20.0)
      , Transaction("", "acc1", 12, "GG", 5.0)
      , Transaction("", "acc1", 20, "GG", 10.0)
    )

    val expected = Array(
      AccountAverageValue("acc1", 3.0, 4.5, 0.0, 0.0, 0.0, 0.0, 7.5)
      , AccountAverageValue("acc2", 0.0, 0.0, 15.0, 0.0, 0.0, 0.0, 0.0)
      , AccountAverageValue("acc3", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    )

    val actual = accountAverageAmount(tnx).toArray.sortBy(_.accountId)
    actual should be(expected)
  }

  test("Calendar fills missing gaps in the list of transactions with missing days") {
    // Not sorted list of transactions with missing days starting day 2 ending day 10
    val tnx = List[Transaction](
      Transaction("", "", 10, "", 0.0)
      , Transaction("", "", 10, "", 0.0)
      , Transaction("", "", 2, "", 0.0)
      , Transaction("", "", 2, "", 0.0)
      , Transaction("", "", 6, "", 0.0)
      , Transaction("", "", 3, "", 0.0)
      , Transaction("", "", 4, "", 0.0)
      , Transaction("", "", 7, "", 0.0)
      , Transaction("", "", 2, "", 00.0)
    )

    val expected = Array(2, 3, 4, 5, 6, 7, 8, 9, 10)

    val actual = dayRange(tnx).toArray

    actual should be(expected)
  }

  test("Rolling 5 days calendar with NO partials starts at day 6") {
    val expected = Array(
      (6, List(1, 2, 3, 4, 5))
      , (7, List(2, 3, 4, 5, 6))
      , (8, List(3, 4, 5, 6, 7))
      , (9, List(4, 5, 6, 7, 8))
      , (10, List(5, 6, 7, 8, 9)))
    val actual = rollingCalendar(1 to 10, 5, false).toArray

    actual should be(expected)
  }

  test("Rolling 5 days calendar WITH partials starts at day 1") {
    val expected = Array(
      (1, List())
      , (2, List(1))
      , (3, List(1, 2))
      , (4, List(1, 2, 3))
      , (5, List(1, 2, 3, 4))
      , (6, List(1, 2, 3, 4, 5))
      , (7, List(2, 3, 4, 5, 6))
      , (8, List(3, 4, 5, 6, 7))
      , (9, List(4, 5, 6, 7, 8))
      , (10, List(5, 6, 7, 8, 9)))
    val actual = rollingCalendar(1 to 10, 5, true).toArray

    actual should be(expected)
  }

  test("Daily Account Stat report 5 days sliding - 9th day is missing in transactions - with partial 5 days at the beggining") {
    val tnx = List[Transaction](
      Transaction("", "acc1", 1, "AA", 1.0)
      , Transaction("", "acc2", 1, "AA", 2.0)
      , Transaction("", "acc2", 1, "BB", 3.0)
      , Transaction("", "acc1", 2, "AA", 5.0)
      , Transaction("", "acc2", 2, "AA", 2.0)
      , Transaction("", "acc1", 2, "AA", 3.0)
      , Transaction("", "acc2", 2, "CC", 10.0)
      , Transaction("", "acc2", 3, "CC", 11.0)
      , Transaction("", "acc1", 3, "CC", 20.0)
      , Transaction("", "acc2", 3, "AA", 20.0)
      , Transaction("", "acc2", 4, "FF", 10.0)
      , Transaction("", "acc1", 4, "AA", 20.0)
      , Transaction("", "acc2", 4, "CC", 20.0)
      , Transaction("", "acc1", 5, "FF", 11.0)
      , Transaction("", "acc2", 5, "AA", 20.0)
      , Transaction("", "acc2", 5, "CC", 20.0)
      , Transaction("", "acc1", 6, "AA", 10.0)
      , Transaction("", "acc1", 6, "CC", 22.0)
      , Transaction("", "acc2", 6, "FF", 22.0)
      , Transaction("", "acc2", 7, "CC", 10.0)
      , Transaction("", "acc2", 7, "AA", 20.0)
      , Transaction("", "acc1", 7, "FF", 25.0)
      , Transaction("", "acc1", 8, "FF", 10.0)
      , Transaction("", "acc2", 8, "AA", 20.0)
      , Transaction("", "acc1", 8, "CC", 21.0)
      , Transaction("", "acc2", 10, "FF", 10.0)
      , Transaction("", "acc1", 10, "AA", 20.0)
      , Transaction("", "acc2", 10, "CC", 20.0)
    )

    val expected = Array(
      DailyAccountStat(2, "acc1", 1.0, 1.0, 1.0, 0, 0)
      , DailyAccountStat(2, "acc2", 3.0, 2.5, 2.0, 0, 0)
      , DailyAccountStat(3, "acc1", 5.0, 3, 9.0, 0, 0)
      , DailyAccountStat(3, "acc2", 10.0, 4.25, 4.0, 10.0, 0.0)
      , DailyAccountStat(4, "acc1", 20.0, 7.25, 9.0, 20.0, 0.0)
      , DailyAccountStat(4, "acc2", 20.0, 8.0, 24.0, 21.0, 0.0)
      , DailyAccountStat(5, "acc1", 20.0, 9.8, 29.0, 20.0, 0.0)
      , DailyAccountStat(5, "acc2", 20.0, 9.75, 24.0, 41.0, 10.0)
      , DailyAccountStat(6, "acc1", 20.0, 10.0, 29.0, 20.0, 11.0)
      , DailyAccountStat(6, "acc2", 20.0, 11.8, 44.0, 61.0, 10.0)
      , DailyAccountStat(7, "acc1", 22.0, 13.0, 38.0, 42.0, 11.0)
      , DailyAccountStat(7, "acc2", 22.0, 15.0, 42.0, 61.0, 32.0)
      , DailyAccountStat(8, "acc1", 25.0, 18.0, 30.0, 42.0, 36.0)
      , DailyAccountStat(8, "acc2", 22.0, 17.0, 60.0, 61.0, 32.0)
      , DailyAccountStat(9, "acc1", 25.0, 17.0, 30.0, 43.0, 46.0)
      , DailyAccountStat(9, "acc2", 22.0, 17.75, 60.0, 50.0, 32.0)
      , DailyAccountStat(10, "acc1", 25.0, 16.5, 10.0, 43.0, 46.0)
      , DailyAccountStat(10, "acc2", 22.0, 112.0/6, 60.0, 30.0, 22.0)
    )

    val actual = rollingDailyAccountStat(tnx, 5, true).toArray.sortBy(r => (r.day, r.accountId))

    actual should be(expected)
  }

  test("Daily Account Stat report 5 days sliding - 9th day is missing in transactions - with NO partial 5 days at the beggining") {
    val tnx = List[Transaction](
      Transaction("", "acc1", 1, "AA", 1.0)
      , Transaction("", "acc2", 1, "AA", 2.0)
      , Transaction("", "acc2", 1, "BB", 3.0)
      , Transaction("", "acc1", 2, "AA", 5.0)
      , Transaction("", "acc2", 2, "AA", 2.0)
      , Transaction("", "acc1", 2, "AA", 3.0)
      , Transaction("", "acc2", 2, "CC", 10.0)
      , Transaction("", "acc2", 3, "CC", 11.0)
      , Transaction("", "acc1", 3, "CC", 20.0)
      , Transaction("", "acc2", 3, "AA", 20.0)
      , Transaction("", "acc2", 4, "FF", 10.0)
      , Transaction("", "acc1", 4, "AA", 20.0)
      , Transaction("", "acc2", 4, "CC", 20.0)
      , Transaction("", "acc1", 5, "FF", 11.0)
      , Transaction("", "acc2", 5, "AA", 20.0)
      , Transaction("", "acc2", 5, "CC", 20.0)
      , Transaction("", "acc1", 6, "AA", 10.0)
      , Transaction("", "acc1", 6, "CC", 22.0)
      , Transaction("", "acc2", 6, "FF", 22.0)
      , Transaction("", "acc2", 7, "CC", 10.0)
      , Transaction("", "acc2", 7, "AA", 20.0)
      , Transaction("", "acc1", 7, "FF", 25.0)
      , Transaction("", "acc1", 8, "FF", 10.0)
      , Transaction("", "acc2", 8, "AA", 20.0)
      , Transaction("", "acc1", 8, "CC", 21.0)
      , Transaction("", "acc2", 10, "FF", 10.0)
      , Transaction("", "acc1", 10, "AA", 20.0)
      , Transaction("", "acc2", 10, "CC", 20.0)
    )

    val expected = Array(
      DailyAccountStat(6, "acc1", 20.0, 10.0, 29.0, 20.0, 11.0)
      , DailyAccountStat(6, "acc2", 20.0, 11.8, 44.0, 61.0, 10.0)
      , DailyAccountStat(7, "acc1", 22.0, 13.0, 38.0, 42.0, 11.0)
      , DailyAccountStat(7, "acc2", 22.0, 15.0, 42.0, 61.0, 32.0)
      , DailyAccountStat(8, "acc1", 25.0, 18.0, 30.0, 42.0, 36.0)
      , DailyAccountStat(8, "acc2", 22.0, 17.0, 60.0, 61.0, 32.0)
      , DailyAccountStat(9, "acc1", 25.0, 17.0, 30.0, 43.0, 46.0)
      , DailyAccountStat(9, "acc2", 22.0, 17.75, 60.0, 50.0, 32.0)
      , DailyAccountStat(10, "acc1", 25.0, 16.5, 10.0, 43.0, 46.0)
      , DailyAccountStat(10, "acc2", 22.0, 112.0/6, 60.0, 30.0, 22.0)
    )

    val actual = rollingDailyAccountStat(tnx, 5, false).toArray.sortBy(r => (r.day, r.accountId))

    actual should be(expected)
  }



}
