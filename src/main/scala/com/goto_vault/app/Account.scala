package com.goto_vault.app

import slick.jdbc.H2Profile.api._


case class Account(id: Int, name: String, balance: Double, password: String, email: String)

class AccountTable(tag: Tag) extends Table[(Int, String, Double, String, String)](tag, "Accounts") {

  def id = column[Int]("ID", O.PrimaryKey)

  def name = column[String]("NAME")

  def balance = column[Double]("BALANCE")

  def password = column[String]("PASS")

  def email = column[String]("EMAIL")

  def * = (id, name, balance, password, email)
}
