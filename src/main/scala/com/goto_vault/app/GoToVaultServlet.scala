package com.goto_vault.app

import org.scalatra._
import org.scalatra.auth.strategy.BasicAuthStrategy.BasicAuthRequest

class GoToVaultServlet extends ZvezdochkaStack {

  val s = Setup
  s.primary_setup_account()

  get("/profile") {
    contentType = "text/html"
    val user: Option[Account] = basicAuth()
    Setup.get_account(user.head.id)
  }
  get("/admin") {
    contentType = "text/html"

    val user: Option[Account] = basicAuth()

    if (user.head.admin) {
      Setup.all_accounts()
    } else {
      halt(404, "Not Found")
    }
  }
  get("/register") {
    contentType = "text/html"

    <form action='/register' method='post'>
      <input type='string' name='name'></input><br></br>
      <input type='string' name='email'></input><br></br>
      <input type='password' name='password'></input><br></br>
      <input type='submit'></input>
    </form>
  }
  post("/register") {
      Setup.add_account(response("name"), 0, response("password"), response("email"))
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

