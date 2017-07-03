package com.goto_vault.app

import slick.jdbc.H2Profile.api._

case class Bought_good(account_id: Int, good_name: String)

class Bought_goodTable(tag: Tag) extends Table[(Int, String)](tag, "bought_goods") {

  def account_id = column[Int]("Account")

  def good_name = column[String]("Good")

  def * = (account_id, good_name)
}
