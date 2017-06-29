package com.goto_vault.app

import org.scalatra.auth.strategy.{BasicAuthStrategy, BasicAuthSupport}
import org.scalatra.auth.{ScentrySupport, ScentryConfig}
import org.scalatra.{ScalatraBase}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}


class OurBasicAuthStrategy(protected override val app: ScalatraBase, realm: String) extends BasicAuthStrategy[Account](app, realm) {

  protected def validate(userName: String, password: String)(implicit request: HttpServletRequest, response: HttpServletResponse): Option[Account] = {
    if (Setup.try_login(userName, Setup.hash(password))) Some(Setup.get_account_by_email(userName))
    else None
  }

  protected def getUserId(user: Account)(implicit request: HttpServletRequest, response: HttpServletResponse): String = user.id.toString
}

trait AuthenticationSupport extends ScentrySupport[Account] with BasicAuthSupport[Account] {
  self: ScalatraBase =>

  val realm = "Scalatra Basic Auth Example"

  protected def fromSession = {
    case id: String => Setup.get_account_by_id(id.toInt)
  }

  protected def toSession = {
    case usr: Account => usr.id.toString
  }

  protected val scentryConfig = new ScentryConfig {}.asInstanceOf[ScentryConfiguration]


  override protected def configureScentry() = {
    scentry.unauthenticated {
      scentry.strategies("Basic").unauthenticated()
    }
  }

  override protected def registerAuthStrategies() = {
    scentry.register("Basic", app => new OurBasicAuthStrategy(app, realm))
  }

}