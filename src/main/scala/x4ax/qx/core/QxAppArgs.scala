package x4ax.qx.core

import scala.util.{Failure, Success}

case class QxAppArgs(
                      tnxFile: String
                      , rollingWindow: Int
                      , withPartial: Boolean)

object QxAppArgs {
  type OptionMap = Map[Symbol, Any]

  def nextOption(map: OptionMap, list: List[String]): OptionMap = {
    def isSwitch(s: String) = s(0) == '-'

    def asInputFile(s: String): Map[Symbol, String] = {
      Map('tnxFile -> s.trim())
    }

    list match {
      case Nil => map
      case "-p" :: tail => nextOption(map ++ Map('withPartial -> true), tail)
      case string :: opt2 :: tail if isSwitch(opt2) => nextOption(map ++ asInputFile(string), list.tail)
      case string :: Nil => nextOption(map ++ asInputFile(string), list.tail)
      case option :: tail => throw new IllegalArgumentException(s"Unknown option ${option}")
    }
  }

  def parse(arglist: List[String]) = {
    try {
      val options = nextOption(Map(), arglist)
      val tnxFile = options.get('tnxFile).asInstanceOf[Option[String]].getOrElse("transactions.txt")
      val withPartial = options.get('withPartial).asInstanceOf[Option[Boolean]].getOrElse(false)
      val rollingWindow = options.get('rollingWindow).asInstanceOf[Option[Int]].getOrElse(5)
      Success(QxAppArgs(tnxFile, rollingWindow, withPartial))
    }
    catch {
      case t: Throwable => {
        Failure(t)
      }
    }
  }
}
