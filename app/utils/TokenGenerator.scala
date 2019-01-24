package utils

import java.security.SecureRandom

import scala.util.Random

class TokenGenerator(tokenTable: TokenTable) {
  val DefaultSize = 64
  private[this] val random = new Random(new SecureRandom())

  def table = tokenTable.value

  def generate(size: Int = DefaultSize): String =
    Iterator.continually(random.nextInt(table.length)).map(table).take(size).mkString
}

sealed abstract class TokenTable(val value: String)

object TokenTable {
  case object LegacyTable extends TokenTable("abcdefghijklmnopqrstuvwxyz1234567890_.~*'()")
  case object AlphabetTable extends TokenTable("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890")
}
