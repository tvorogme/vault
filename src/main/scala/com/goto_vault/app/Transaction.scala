package com.goto_vault.app

import slick.jdbc.H2Profile.api._

case class Transaction(id: Int, from:Int, to:Int, amount:Double)


class TransactionTable(tag: Tag) extends Table[(Int, Int, Int, Double)](tag, "Transactions") {

  def id = column[Int]("ID", O.PrimaryKey)
  def from = column[Int]("FROM")
  def to = column[Int]("TO")
  def amount = column[Double]("AMOUNT")
  def * = (id, from, to, amount)
}