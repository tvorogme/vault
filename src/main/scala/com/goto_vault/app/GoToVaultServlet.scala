package com.goto_vault.app

import org.scalatra._
import org.scalatra.auth.strategy.BasicAuthStrategy.BasicAuthRequest


class GoToVaultServlet extends ZvezdochkaStack {
  Setup.primary_setup_account()

  get("/*") {
    basicAuth()
    <html>
      <body>
        <h1>Hello from Scalatra</h1>
        <p>You are authenticated.</p>
      </body>
    </html>
  }

  protected def basicAuth() = {
    val req = new BasicAuthRequest(request)

    def notAuthenticated() {
      response.setHeader("WWW-Authenticate", "Basic realm=\"%s\"" format "mc-nulty")
      halt(401, "Unauthenticated")
    }

    if (!req.providesAuth) {
      notAuthenticated()
    }
    if (!req.isBasicAuth) {
      halt(400, "Bad Request")
    }
    var user: Option[Account] = None

    val tryLogin: Boolean = Setup.try_login(req.username, Setup.hash(req.password))

    if (tryLogin) {
      user = Option(Setup.get_account_by_email(req.username))
      response.setHeader("REMOTE_USER", "user.id")
    }
    else {
      notAuthenticated()
    }

    user
  }

}