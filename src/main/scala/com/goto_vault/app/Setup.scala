package com.goto_vault.app

import org.scalatra.{ScalatraBase, FutureSupport, ScalatraServlet}
import slick.jdbc.H2Profile.api._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.Duration


object Setup {
  var answer: Int = 0
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
    val query = Account.length.result


    def res = Await.result(db.run(query), Duration.Inf)
    // 3 HOURS for this sheat ^^^^^^^^^^^^^

    
    res.toString()
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
