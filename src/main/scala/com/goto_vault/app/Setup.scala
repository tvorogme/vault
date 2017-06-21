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
      Accounts += (1, "Andrew Tvorozhkov", 0, hash("admin"), "admin")
    )
    db.run(create_table)
  }

  def primary_setup_transaction(): Unit = {
    val create_table = DBIO.seq(
      Transactions.schema.create,
      Transactions += (1, 1, 2, 100),
      Transactions += (2, 2, 1, 100)
    )
    db.run(create_table)
  }

  def primary_setup_good(): Unit = {
    val create_table = DBIO.seq(
      Goods.schema.create,
      Goods += (1, "ice cream", 60),
      Goods += (2, "cookies", 50)
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

  def add_account(name: String, balance: Double, pass: String, email: String): Unit = {
    val insertActions = DBIO.seq(Accounts += (this.get_last_account() + 1, name, balance, hash(pass), email))
    db.run(insertActions)
  }

  def add_transaction(from: Int, to: Int, amount: Double): Unit = {
    val insertActions = DBIO.seq(Transactions += (this.get_last_transaction() + 1, from, to, amount))
    db.run(insertActions)
  }

  def add_good(name: String, price: Double): Unit = {
    val insertActions = DBIO.seq(Goods += (this.get_last_account() + 1, name, price))
    db.run(insertActions)
  }

  def buy_good(acc_id: Int, good_id: Int): Unit = {
    val query = Goods.filter(_.id === good_id)

    def price: Double = Await.result(db.run(query.map(_.price).result), Duration.Inf).head

    //ToDo to bank
    money_operation(acc_id, 1, price)
    db.run(query.delete)
  }

  def update_good_prize(id:Int, price:Double): Unit ={
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

  def all_accounts():String = {
    var q = Accounts.sortBy(_.id).result
    def res = Await.result(db.run(q), Duration.Inf)
    var html: String = "<ul>"
    for (i <- res) {
      html += "<li>id: " + i._1 + "  name: " + i._2 + "   balance: " + i._3 + "</li>"
    }
    html += "</ul>"
    html
  }

  def get_account(id:Int): Unit ={
    var q = Accounts.filter(_.id === id).result
    def res = Await.result(db.run(q), Duration.Inf).head
    var html: String = "<p>id: " + res._1 + "  name: " + res._2 + "   balance: " + res._3 + "  mail: " + res._4 +"</p"
    html
  }

  def all_transactions():String = {
    var q = Transactions.sortBy(_.id).result
    def res = Await.result(db.run(q), Duration.Inf)
    var html: String = "<ul>"
    for (i <- res) {
      html += "<li>id: " + i._1 + "  from: " + i._2 + "   to: " + i._3 + " amount: "+ i._4 + "</li>"
    }
    html += "</ul>"
    html
  }

  def all_goods():String = {
    var q = Transactions.sortBy(_.id).result
    def res = Await.result(db.run(q), Duration.Inf)
    var html: String = "<ul>"
    for (i <- res) {
      html += "<li>id: " + i._1 + "  name: " + i._2 + "   price: " + i._3 + "</li>"
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

    def res: (Int, String, Double, String, String) = Await.result(db.run(query), Duration.Inf).head

    Account(res._1, res._2, res._3, res._4, res._5)
  }


  def get_account_by_id(id: Int): Account = {
    val query = Accounts.filter(_.id === id).result

    def res: (Int, String, Double, String, String) = Await.result(db.run(query), Duration.Inf).head

    Account(res._1, res._2, res._3, res._4, res._5)
  }
}