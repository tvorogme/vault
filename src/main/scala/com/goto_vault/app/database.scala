package com.goto_vault.app


import org.scalatra.{ScalatraBase, FutureSupport, ScalatraServlet}
import slick.jdbc.H2Profile.api._
import scala.concurrent.ExecutionContext.Implicits.global

object Tables {

  // Definition of the Accounts table
  class Accounts(tag: Tag) extends Table[(Int, String, Int)](tag, "Accounts") {

    def id = column[Int]("ID", O.PrimaryKey)
    def name = column[String]("NAME")
    def balance = column[Int]("BALANCE")

    def * = (id, name, balance)
  }

  val Accounts = TableQuery[Accounts]


  val insertSupplierAndCoffeeData = DBIO.seq(
    Tables.Accounts += (1, "Andrew Tvorozhkov", 0),
    Tables.Accounts += (1, "Grisha Belogorov", 0)
  )

  val createSchemaAction = (Accounts.schema).create
  val dropSchemaAction = (Accounts.schema).drop
  val createDatabase = DBIO.seq(createSchemaAction)
}

class SlickApp(val db: Database) extends ScalatraServlet with FutureSupport {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global

}