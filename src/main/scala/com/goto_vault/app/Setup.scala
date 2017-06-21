package com.goto_vault.app

import org.scalatra.{ScalatraBase, FutureSupport, ScalatraServlet}
import slick.jdbc.H2Profile.api._
import scala.concurrent.ExecutionContext.Implicits.global


object Setup {
  val db = Database.forConfig("h2mem1")
  val Account = TableQuery[Account]

  def primary_setup(): Unit = {
    val create_table = DBIO.seq(
      (Account.schema).create,
      Account += (1, "Andrew Tvorozhkov", 0),
      Account += (2, "Grisha Belogorov", 0)
    )
    db.run(create_table)
  }


  def get_last_account(): String = {
    val result = db.run(DBIO.seq(Account.length.result))

    println(result)
    "LOL"
  }
}

//
//val createSchemaAction = (Accounts.schema).create
//val dropSchemaAction = (Accounts.schema).drop
//val createDatabase = DBIO.seq(createSchemaAction)
//}
//
//class DBWrapper {
//  def register(): Unit ={
//    val id = Accounts[Tables.]
//  }
//}
