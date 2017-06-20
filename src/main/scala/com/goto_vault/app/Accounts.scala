package com.goto_vault.app
import slick.jdbc.H2Profile.api._

/**
  * Created by acadabus on 6/21/17.
  */
class Accounts(tag: Tag) extends Table[(Int, String, Int)](tag, "Accounts") {

    def id = column[Int]("ID", O.PrimaryKey)
    def name = column[String]("NAME")
    def balance = column[Int]("BALANCE")

    def * = (id, name, balance)
}
