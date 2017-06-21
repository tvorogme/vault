package com.goto_vault.app

import org.scalatra.{FutureSupport, ScalatraBase, ScalatraServlet}
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.Duration

object Setup {

  val db = Database.forConfig("h2mem1")
  val Accounts = TableQuery[AccountTable]
  val Transactions = TableQuery[TransactionTable]
  val Goods = TableQuery[GoodTable]


  def hash(text: String): String = java.security.MessageDigest.getInstance("MD5").digest(text.getBytes()).map(0xFF & _).map {
    "%02x".format(_)
  }.foldLeft("") {
    _ + _
  }


  def primary_setup_account(): Unit = {
    val create_table = DBIO.seq(
      Accounts.schema.create,
      Accounts += (1, "Andrew Tvorozhkov", 0, hash("admin"), "admin", true)
    )
    db.run(create_table)
  }

  def primary_setup_transaction(): Unit = {
    val create_table = DBIO.seq(
      Transactions.schema.create
    )
    db.run(create_table)
  }

  def primary_setup_good(): Unit = {
    val create_table = DBIO.seq(
      Goods.schema.create
    )
    db.run(create_table)
  }


  def get_last_account(): Int = {
    val query = Accounts.length.result
    println(query)

    def res = Await.result(db.run(query), Duration.Inf)

    res
  }

  def get_last_transaction(): Int = {
    val query = Transactions.length.result

    def res = Await.result(db.run(query), Duration.Inf)

    res
  }

  def get_last_good(): Int = {
    val query = Goods.length.result

    def res = Await.result(db.run(query), Duration.Inf)

    res
  }

  def add_account(name: String, balance: Double, pass: String, email: String, admin: Boolean = false): Unit = {
    val insertActions = DBIO.seq(Accounts += (this.get_last_account() + 1, name, balance, hash(pass), email, admin))
    db.run(insertActions)
  }

  def add_transaction(from: Int, to: Int, amount: Double): Unit = {
    val insertActions = DBIO.seq(Transactions += (this.get_last_transaction() + 1, from, to, amount))
    db.run(insertActions)
  }

  def add_good(name: String, price: Double): Unit = {
    val tmp = (this.get_last_good() + 1, name, price)
    println(tmp)

    val insertActions = DBIO.seq(Goods += tmp)

    println(Goods)
    db.run(insertActions)
  }

  def buy_good(acc_id: Int, good_id: Int): Unit = {
    val query = Goods.filter(_.id === good_id)

    def price: Double = Await.result(db.run(query.map(_.price).result), Duration.Inf).head

    var query2 = Accounts.filter(_.id === acc_id).result

    def balance = Await.result(db.run(query2), Duration.Inf).head._3

    //ToDo to bank
    if (balance >= price) {
      money_operation(acc_id, 1, price)
      db.run(query.delete)
    }
  }

  def update_good_prize(id: Int, price: Double): Unit = {
    val query = Goods.filter(_.id === id).map(_.price).update(price)
    db.run(query)
  }

  def money_operation(from: Int, to: Int, amount: Double): Unit = {
    money_operation_with_db(from, -amount)
    money_operation_with_db(to, amount)
    add_transaction(from, to, amount)
  }

  def money_operation_with_db(acc_id: Int, amount: Double): Unit = {
    val query = Accounts.filter(_.id === acc_id).map(_.balance).result

    def res: Double = Await.result(db.run(query), Duration.Inf).head

    val q2 = Accounts.filter(_.id === acc_id).map(_.balance).update(res + amount)
    db.run(q2)
  }

  def all_accounts(mutable: Boolean = false): String = {
    var q = Accounts.sortBy(_.id).result

    def res = Await.result(db.run(q), Duration.Inf)

    var html: String = "<ul>"
    for (i <- res) {
      if (mutable) {
        val buttonHtml: String =
          s"""
             |<form method='post' action='/admin/add_money'>
             |<input type='hidden' name='id' value='${i._1}'>
             |<input type='string' name='amount'>
             |<input value='Применить' type='submit'>
             |</form></li>""".stripMargin
        html += "<li>id: " + i._1 + "  name: " + i._2 + "   balance: " + i._3 + buttonHtml
      }
      else
        html += "<li>id: " + i._1 + "  name: " + i._2 + "   balance: " + i._3 + "</li>"
    }
    html += "</ul>"
    html
  }

  def get_account(id: Int): (Int, String, Double, String, String, Boolean) = {
    var q = Accounts.filter(_.id === id).result

    def res: (Int, String, Double, String, String, Boolean) = Await.result(db.run(q), Duration.Inf).head

    res
  }

  def all_transactions(): String = {
    var q = Transactions.sortBy(_.id).result

    def res = Await.result(db.run(q), Duration.Inf)

    var html: String = "<ul>"
    for (i <- res) {
      html += "<li>id: " + i._1 + "  from: " + i._2 + "   to: " + i._3 + " amount: " + i._4 + "</li>"
    }
    html += "</ul>"
    html
  }

  def all_cool_goods(): Seq[(Int, String, Double)] = {
    val q = Goods.sortBy(_.id).result

    def res = Await.result(db.run(q), Duration.Inf)

    res
  }

  def all_goods(buy: Boolean = false): String = {
    val q = Goods.sortBy(_.id).result

    def res = Await.result(db.run(q), Duration.Inf)

    var html: String = "<ul>"
    for (i <- res) {
      if (buy) {

        val buttonHtml: String =
          s"""
             |<form method='post' action='/market/buy'>
             |<input type='hidden' name='id' value='${i._1}'>
             |<input type='hidden' name='price' value='${i._3}'>
             |<input  value='Купить' type='submit'>
             |</form></li>""".stripMargin


        html += "<li> Name: " + i._2 + "   Price: " + i._3 + buttonHtml

      } else {
        html += "<li>id: " + i._1 + "  name: " + i._2 + "   price: " + i._3 + "</li>"
      }
    }
    html += "</ul>"
    html
  }

  def try_login(email: String, password: String): Boolean = {
    val query = Accounts.filter(_.email === email).map(_.password).result

    def res = Await.result(db.run(query), Duration.Inf)

    password == res.head.toString
  }

  def get_account_by_email(email: String): Account = {
    val query = Accounts.filter(_.email === email).result

    def res: (Int, String, Double, String, String, Boolean) = Await.result(db.run(query), Duration.Inf).head

    Account(res._1, res._2, res._3, res._4, res._5, res._6)
  }


  def get_account_by_id(id: Int): Account = {
    val query = Accounts.filter(_.id === id).result

    def res: (Int, String, Double, String, String, Boolean) = Await.result(db.run(query), Duration.Inf).head

    Account(res._1, res._2, res._3, res._4, res._5, res._6)
  }
}