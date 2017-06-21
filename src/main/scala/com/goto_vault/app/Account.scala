package com.goto_vault.app
import slick.jdbc.H2Profile.api._

/**
  * Created by acadabus on 6/21/17.
  */
class Account(tag: Tag) extends Table[(Int, String, Double,String, String)](tag, "Accounts") {

    def id = column[Int]("ID", O.PrimaryKey)
    def name = column[String]("NAME")
    def balance = column[Double]("BALANCE")
    def password = column[String]("PASS")
    def email = column[String]("EMAIL")
    def * = (id, name, balance, password, email)
}
