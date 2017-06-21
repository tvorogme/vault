package com.goto_vault.app

import org.scalatra.auth.strategy.{BasicAuthStrategy, BasicAuthSupport}
import org.scalatra.auth.{ScentrySupport, ScentryConfig}
import org.scalatra.ScalatraBase

class auth(protected override val app: ScalatraBase, realm: String)
  extends BasicAuthStrategy[Account](app, realm) {

  val s = Setup

  protected def validate(email: String, password: String): Option[Account] = {
    if (s.try_login(email, password)){
      s.get_account_by_email(email)
    } else{
      None
    }
  }

  protected def getUserId(user: Account): String = Account.id.toString()
}