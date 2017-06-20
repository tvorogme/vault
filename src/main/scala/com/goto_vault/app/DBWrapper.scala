package com.goto_vault.app
import com.goto_vault.app.Accounts

import org.scalatra.{ScalatraBase, FutureSupport, ScalatraServlet}
import slick.jdbc.H2Profile.api._
import scala.concurrent.ExecutionContext.Implicits.global

val Accounts = TableQuery[Accounts]


val setup = DBIO.seq(Accounts.schema).

val insertSupplierAndCoffeeData = DBIO.seq(
Accounts += (1, "Andrew Tvorozhkov", 0),
Accounts += (2, "Grisha Belogorov", 0))


val createSchemaAction = (Accounts.schema).create
val dropSchemaAction = (Accounts.schema).drop
val createDatabase = DBIO.seq(createSchemaAction)
}

class DBWrapper {
  def register(): Unit ={
    val id = Accounts[Tables.]
  }
}
