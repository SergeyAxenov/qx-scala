package x4ax.qx.core

import java.io.{FileWriter, PrintWriter}

import scala.util.{Failure, Success}
import TransactionQuery._
import x4ax.qx.core.model.Transaction

import scala.io.Source

object QxApp extends App {

  private def run(argList: Array[String]) = {
    QxAppArgs.parse(argList.toList) match {
      case Success(params) => {
        try {

          //The lines of the CSV file (dropping the first to remove the header)
          val transactionslines = Source.fromFile(params.tnxFile).getLines().drop(1)
          //Here we split each line up by commas and construct Transactions
          val tnx = transactionslines.map { line =>
            val split = line.split(',')
            Transaction(split(0), split(1), split(2).toInt, split(3), split(4).toDouble)
          }.toList

          printToFile("dailyAmount.csv",
            (writer: PrintWriter) => {
              val report = dailyAmount(tnx).toSeq.sortBy(_.day)
              writer.println("Day,Total Value")
              report.foreach(r => writer.println(s"${r.day},${r.totalAmount}"))
            })

          printToFile("accountAverage.csv",
            (writer: PrintWriter) => {
              val report = accountAverageAmount(tnx).toSeq.sortBy(_.accountId)
              writer.println("Account ID,AA Average Value,BB Average Value,CC Average Value,DD Average Value,EE Average Value,FF Average Value,GG Average Value")
              report.foreach(r => {
                writer.println(s"${r.accountId},${r.aaValue},${r.bbValue},${r.ccValue},${r.ddValue},${r.eeValue},${r.ffValue},${r.ggValue}")
              })
            })

          printToFile(s"rollingStats${if (params.withPartial) "-withPartial" else ""}.csv",
            (writer: PrintWriter) => {
              val report = rollingDailyAccountStat(tnx, params.rollingWindow, params.withPartial).toSeq.sortBy(r => (r.day, r.accountId))
              writer.println("Day,Account ID,Maximum,Average,AA Total Value,CC Total Value,FF Total Value")
              report.foreach(r => {
                writer.println(s"${r.day},${r.accountId},${r.max},${r.avg},${r.aaValue},${r.ccValue},${r.ffValue}")
              })
            })

        } catch {
          case ex: Throwable =>
            System.err.println("An unhandled error occurred while running the app")
            ex.printStackTrace()
            System.exit(1)
        }
      }
      case Failure(t) => {
        System.err.println(s"Error parsing parameters: ${t.getMessage}")
        System.exit(1)
      }
    }
  }

  private def printToFile[A](fileName: String, print: PrintWriter => Unit) = {
    val printer = new PrintWriter(new FileWriter(fileName))
    try {
      print(printer)
      Console.println(s"Report ${fileName} has been generated")
    }
    finally
      printer.close()
  }

  run(args)
}
