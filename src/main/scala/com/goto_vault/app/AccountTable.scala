package com.goto_vault.app

import slick.jdbc.H2Profile.api._

class AccountTable(tag: Tag) extends Table[(Int, String, Double, String, String, Boolean)](tag, "Accounts") {

  def id = column[Int]("ID", O.PrimaryKey)

  def name = column[String]("NAME")

  def balance = column[Double]("BALANCE")

  def password = column[String]("PASS")

  def email = column[String]("EMAIL")

  def admin = column[Boolean]("ADMIN")

  def * = (id, name, balance, password, email, admin)
}
