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
  val prefix: String = "https://goto.msk.ru/vault/"

  def hash(text: String): String = java.security.MessageDigest.getInstance("MD5").digest(text.getBytes()).map(0xFF & _).map {
    "%02x".format(_)
  }.foldLeft("") {
    _ + _
  }

  def setup(): Unit = {
    val create_table = DBIO.seq(
      Accounts.schema.create,
      Transactions.schema.create,
      Goods.schema.create
    )
    db.run(create_table)
  }

  def get_last_account(): Int = Await.result(db.run(Accounts.length.result), Duration.Inf)

  def get_last_transaction(): Int = Await.result(db.run(Transactions.length.result), Duration.Inf)

  def get_last_good(): Int = Await.result(db.run(Goods.length.result), Duration.Inf)

  def add_account(name: String, balance: Double, pass: String, email: String, admin: Boolean = false): Unit = {
    val last_id = this.get_last_account()

    if (last_id == 0) {
      db.run(DBIO.seq(Accounts += (last_id + 1, name, balance, hash(pass), email, true)))
    }
    else {
      db.run(DBIO.seq(Accounts += (last_id + 1, name, balance, hash(pass), email, admin)))
    }
  }

  def add_transaction(from: Int, to: Int, amount: Double): Unit = {
    val insertActions = DBIO.seq(Transactions += (this.get_last_transaction() + 1, from, to, amount))
    db.run(insertActions)
  }

  def add_good(name: String, price: Double): Unit = db.run(DBIO.seq(Goods += (this.get_last_good() + 1, name, price)))

  def buy_good(acc_id: Int, good_id: Int): Unit = {
    val query = Goods.filter(_.id === good_id)

    def price: Double = Await.result(db.run(query.map(_.price).result), Duration.Inf).head

    def balance = Await.result(db.run(Accounts.filter(_.id === acc_id).result), Duration.Inf).head._3

    if (balance >= price) {
      money_operation(acc_id, 1, price)
      db.run(query.delete)
    }
  }

  def get_good_price_by_id(good_id: Int): Double = {
    val query = Goods.filter(_.id === good_id)
    Await.result(db.run(query.map(_.price).result), Duration.Inf).head
  }

  def update_good_prize(id: Int, price: Double): Unit = db.run(Goods.filter(_.id === id).map(_.price).update(price))

  def money_operation(from: Int, to: Int, amount: Double): Unit = {
    money_operation_with_db(from, -amount)
    money_operation_with_db(to, amount)
    add_transaction(from, to, amount)
  }

  def money_operation_with_db(acc_id: Int, amount: Double): Unit = {
    val old_value_query = Accounts.filter(_.id === acc_id).map(_.balance).result

    def old_value: Double = Await.result(db.run(old_value_query), Duration.Inf).head

    val q2 = Accounts.filter(_.id === acc_id).map(_.balance).update(old_value + amount)
    db.run(q2)
  }

  def all_accounts(mutable: Boolean = false): String = {
    val all_accounts = Await.result(db.run(Accounts.sortBy(_.id).result), Duration.Inf)
    var html: String = "<ul>"
    for (account <- all_accounts) {
      if (mutable) {
        val buttonHtml: String =
          s"""
             |<form method='post' action='${this.prefix}admin/add_money'>
             |<input type='hidden' name='id' value='${account._1}'>
             |<input type='string' name='amount'>
             |<input value='Применить' type='submit'>
             |</form></li>""".stripMargin
        html += "<li>id: " + account._1 + "  name: " + account._2 + "   balance: " + account._3 + buttonHtml
      }
      else
        html += "<li>id: " + account._1 + "  name: " + account._2 + "   balance: " + account._3 + "</li>"
    }
    html + "</ul>"
  }

  def get_account(id: Int): (Int, String, Double, String, String, Boolean) = Await.result(db.run(Accounts.filter(_.id === id).result), Duration.Inf).head

  def all_transactions(): String = {
    val all_transactions = Await.result(db.run(Transactions.sortBy(_.id).result), Duration.Inf)

    var html: String = "<ul>"
    for (transaction <- all_transactions) {
      val user1 = this.get_account_by_id(transaction._2)
      val user2 = this.get_account_by_id(transaction._3)

      if (user1.isEmpty || user2.isEmpty)
      // Ignore
        html += ""
      else
        html += "<li>id: " + transaction._1 + "  from: " + user1.get.name + "   to: " + user2.get.name + " amount: " + transaction._4 + "</li>"
    }
    html += "</ul>"
    html
  }

  def all_cool_goods(): Seq[(Int, String, Double)] = Await.result(db.run(Goods.sortBy(_.id).result), Duration.Inf)

  def all_goods(buy: Boolean = false): String = {
    val all_goods = Await.result(db.run(Goods.sortBy(_.id).result), Duration.Inf)

    var html: String = "<ul>"
    for (good <- all_goods) {
      if (buy) {

        val buttonHtml: String =
          s"""
             |<form method='post' action='${this.prefix}market/buy'>
             |<input type='hidden' name='id' value='${good._1}'>
             |<input type='hidden' name='price' value='${good._3}'>
             |<input  value='Купить' type='submit'>
             |</form></li>""".stripMargin


        html += "<li> Name: " + good._2 + "   Price: " + good._3 + buttonHtml

      } else {
        html += "<li>id: " + good._1 + "  name: " + good._2 + "   price: " + good._3 + "</li>"
      }
    }
    html += "</ul>"
    html
  }

  def try_login(email: String, password: String): Boolean = {
    val query = Accounts.filter(_.email === email).map(_.password).result

    def res: Seq[String] = Await.result(db.run(query), Duration.Inf)

    if (res.nonEmpty) {
      password == res.head.toString
    }
    else
      false
  }

  def get_account_by_email(email: String): Option[Account] = {
    val query = Accounts.filter(_.email === email).result

    val result = Await.result(db.run(query), Duration.Inf)

    if (result.isEmpty)
      None
    else {
      def res: (Int, String, Double, String, String, Boolean) = result.head

      Some(Account(res._1, res._2, res._3, res._4, res._5, res._6))
    }
  }

  def get_account_by_id(id: Int): Option[Account] = {
    val query = Accounts.filter(_.id === id).result

    val result = Await.result(db.run(query), Duration.Inf)

    if (result.isEmpty)
      None
    else {
      def res: (Int, String, Double, String, String, Boolean) = result.head

      Some(Account(res._1, res._2, res._3, res._4, res._5, res._6))
    }
  }
}