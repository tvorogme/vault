package com.goto_vault.app

import org.scalatra.auth.strategy.BasicAuthStrategy.BasicAuthRequest

class GoToVaultServlet extends ZvezdochkaStack with AuthenticationSupport {
  Setup.setup()

  protected def basicAuth(try_login: Boolean = true): Option[Account] = {
    val req = new BasicAuthRequest(request)
    var user: Option[Account] = None

    def notAuthenticated() {
      if (try_login) {
        response.setHeader("WWW-Authenticate", "Basic realm=\"%s\"" format "mc-nulty")
        redirect("register")
      }
    }

    if (!req.providesAuth) {
      notAuthenticated()
    }
    if (!req.isBasicAuth) {
      halt(400, "Bad Request")
    }

    var login: Boolean = false

    if (req.username.length > 0 && req.password.length > 0) {
      login = Setup.try_login(req.username, Setup.hash(req.password))
    }
    if (login) {
      user = Some(Setup.get_account_by_email(req.username))
      response.setHeader("REMOTE_USER", user.get.id.toString)
    }
    else {
      notAuthenticated()
    }

    user
  }

  get("/") {
    contentType = "text/html"
    ssp("/WEB-INF/templates/views/index.ssp")
  }

  get("/profile") {
    contentType = "text/html"

    val user: Option[Account] = basicAuth()

    if (user.isEmpty)
      redirect("register")

    ssp("/WEB-INF/templates/views/profile.ssp", "user" -> Setup.get_account(user.head.id))
  }

  get("/admin") {
    contentType = "text/html"
    val user: Option[Account] = basicAuth()

    if (user.head.admin) {
      Setup.all_accounts(mutable = true) +
        """
          |<form action='admin/add_good' method='post'>
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
      redirect("admin")
    } else {
      halt(404, "Not Found")
    }
  }
  post("/admin/add_money") {
    contentType = "text/html"

    val user: Option[Account] = basicAuth()

    if (user.head.admin) {
      Setup.money_operation_with_db(params("id").toInt, params("amount").toInt)
      redirect("vault/admin")
    } else {
      halt(404, "Not Found")
    }
  }

  get("/register") {
    contentType = "text/html"

    if (scentry.isAuthenticated)
      redirect("profile")

    ssp("/WEB-INF/templates/views/register.ssp")
  }
  post("/register") {
    Setup.add_account(params("name"), 0, params("password"), params("email"))
    redirect("profile")
  }

  get("/market") {
    contentType = "text/html"

    val user: Option[Account] = basicAuth()

    if (user.isEmpty)
      redirect("profile")

    ssp("/WEB-INF/templates/views/market.ssp", "items" -> Setup.all_cool_goods())
  }

  //FIXME add redirect
  get("/thank_you") {
    <p>Спасибо за покупку</p>
  }
  get("/not_enough_money") {
    <p>На Вашем счете недостаточно средств</p>
  }

  post("/market/buy") {
    contentType = "text/html"

    val user: Option[Account] = basicAuth()

    if (user.isEmpty) {
      redirect("profile")
    }

    if (user.get.balance < params("price").toDouble)
      redirect("not_enough_money")

    else {
      Setup.buy_good(user.head.id, params("id").toInt)
      redirect("thank_you")
    }
  }
}

