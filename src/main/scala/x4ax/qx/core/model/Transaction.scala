package x4ax.qx.core.model

case class Transaction(
                        transactionId: String,
                        accountId: String,
                        transactionDay: Int,
                        category: String,
                        transactionAmount: Double)
