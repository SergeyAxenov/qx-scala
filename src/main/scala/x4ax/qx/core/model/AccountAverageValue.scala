package x4ax.qx.core.model

case class AccountAverageValue(
                                accountId: String,
                                aaValue: Double,
                                bbValue: Double,
                                ccValue: Double,
                                ddValue: Double,
                                eeValue: Double,
                                ffValue: Double,
                                ggValue: Double
                              ) {

  def this(accountId: String, categorizedValues: Map[String, Double]) =
    this(accountId
      , categorizedValues.getOrElse("AA", 0.0)
      , categorizedValues.getOrElse("BB", 0.0)
      , categorizedValues.getOrElse("CC", 0.0)
      , categorizedValues.getOrElse("DD", 0.0)
      , categorizedValues.getOrElse("EE", 0.0)
      , categorizedValues.getOrElse("FF", 0.0)
      , categorizedValues.getOrElse("GG", 0.0)
    )
}

object AccountAverageValue {
  def apply(accountId: String, categorizedValues: Map[String, Double]) =
    new AccountAverageValue(accountId, categorizedValues)
}

