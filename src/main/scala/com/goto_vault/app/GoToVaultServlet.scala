package com.goto_vault.app

import org.scalatra._
import org.scalatra.auth.strategy.BasicAuthStrategy.BasicAuthRequest

class GoToVaultServlet extends ZvezdochkaStack {

  val s = Setup
  s.primary_setup_account()
  s.primary_setup_good()
  s.primary_setup_transaction()


  get("/profile") {
    contentType = "text/html"
    val user: Option[Account] = basicAuth()
    Setup.get_account(user.head.id)
  }
  get("/admin") {
    contentType = "text/html"
    Setup.add_good("Мороженое", 1000)
    Setup.add_transaction(1, 1, 1000.0)
    Setup.add_account("Lol", 0, "123123", "123123")
    val user: Option[Account] = basicAuth()

    if (user.head.admin) {
      Setup.all_accounts() +
        """
          |<form action='/admin/add_good' method='post'>
          |<input type='text' name='good_name'> <br> <br>
          |<input type='text' name='good_price'> <br> <br>
          |<input type='submit'>
          |</form>
          |""".stripMargin +
        Setup.all_goods() +
        Setup.all_transactions()
    } else {
      halt(404, "Not Found")
    }
  }

  post("/admin/add_good") {
    contentType = "text/html"

    val user: Option[Account] = basicAuth()

    if (user.head.admin) {
      Setup.add_good(params("good_name"), params("good_price").toInt)
      redirect("/admin")
    } else {
      halt(404, "Not Found")
    }
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

