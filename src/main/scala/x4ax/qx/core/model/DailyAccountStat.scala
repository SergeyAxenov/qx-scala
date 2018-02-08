package x4ax.qx.core.model

case class DailyAccountStat(
                             day: Int
                             , accountId: String
                             , max: Double
                             , avg: Double
                             , aaValue: Double
                             , ccValue: Double
                             , ffValue: Double
                           ) {

  def this(
            day: Int
            , accountId: String
            , max: Double
            , avg: Double
            , categorizedValues: Map[String, Double]
          ) =
    this(
      day, accountId, max, avg
      , categorizedValues.getOrElse("AA", 0.0)
      , categorizedValues.getOrElse("CC", 0.0)
      , categorizedValues.getOrElse("FF", 0.0)
    )
}

object DailyAccountStat {
  def apply(
             day: Int
             , accountId: String
             , max: Double
             , avg: Double
             , categorizedValues: Map[String, Double]
           ) =
    new DailyAccountStat(day, accountId, max, avg, categorizedValues)
}
