package com.goto_vault.app

import org.scalatra.{ScalatraBase, FutureSupport, ScalatraServlet}
import slick.jdbc.H2Profile.api._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.Duration
import com.goto_vault.app.Transaction
import java.util.UUID

object Setup {

  val db = Database.forConfig("h2mem1")
  val Account = TableQuery[Account]
  val Transaction = TableQuery[Transaction]

  def primary_setup_account(): Unit = {
    val create_table = DBIO.seq(
      Account.schema.create,
      Account += (1, "Andrew Tvorozhkov", 0, "1234", "aaa@a.ru"),
      Account += (2, "Grisha Belogorov", 0, "1212", "bbb@bb.ru")
    )
    db.run(create_table)
  }

  def primary_setup_transaction(): Unit = {
    val create_table = DBIO.seq(
      Transaction.schema.create,
      Transaction += (1, 1, 2, 100, true),
      Transaction += (2, 2, 1, 100, true)
    )
    db.run(create_table)
  }


  def get_last_account(): Int = {
    val query = Account.length.result
    def res = Await.result(db.run(query), Duration.Inf)
    
    res
  }
  def get_last_transaction(): Int = {
    val query = Transaction.length.result
    def res = Await.result(db.run(query), Duration.Inf)
    // 3 HOURS for this sheet ^^^^^^^^^^^^^
    res
  }

  def add_account( name:String, balance:Double, pass:String,  email:String): Unit ={
    val insertActions = DBIO.seq(Account += (this.get_last_account()+1, name, balance, pass, email))
    db.run(insertActions)
  }
  def add_transaction(from:Int, to:Int, amount:Double): Unit ={
    val insertActions = DBIO.seq(Transaction += (this.get_last_transaction()+1, from, to, amount))
    db.run(insertActions)
  }

  def money_operation(from:Int, to:Int, amount:Double): Unit ={
    money_operation_with_db(from, amount)
    money_operation_with_db(to, -amount)
    add_transaction(from, to, amount)
  }
  def money_operation_with_db(acc_id:Int, amount:Double): Unit ={
    val query = Account.filter(_.id === acc_id).map(_.balance).result
    def res:Double = Await.result(db.run(query), Duration.Inf).head

    val q2 = Account.filter(_.id === acc_id).map(_.balance).update(res+amount)
    db.run(q2)
  }
}