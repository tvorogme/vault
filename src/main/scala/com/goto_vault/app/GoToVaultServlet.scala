package com.goto_vault.app

import org.scalatra._
import org.scalatra.auth.strategy.BasicAuthStrategy.BasicAuthRequest

class GoToVaultServlet extends ZvezdochkaStack {

  val s = Setup
  s.primary_setup_account()
  s.primary_setup_good()
  s.primary_setup_transaction()

  get("/") {
    contentType = "text/html"
    ssp("/WEB-INF/templates/views/index.ssp")
  }

  get("/profile") {
    contentType = "text/html"
    val user: Option[Account] = basicAuth()

    if (user.isEmpty)
      redirect("https://goto.msk.ru/vault/admin/register")

    ssp("/WEB-INF/templates/views/profile.ssp", "user" -> Setup.get_account(user.head.id))
  }
  get("/admin") {
    contentType = "text/html"
    val user: Option[Account] = basicAuth()

    if (user.head.admin) {
      Setup.all_accounts(mutable = true) +
        """
          |<form action='https://goto.msk.ru/vault/admin/add_good' method='post'>
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
      redirect("https://goto.msk.ru/vault/admin")
    } else {
      halt(404, "Not Found")
    }
  }
  post("/admin/add_money") {
    contentType = "text/html"

    val user: Option[Account] = basicAuth()

    if (user.head.admin) {
      Setup.money_operation_with_db(params("id").toInt, params("amount").toInt)
      redirect("https://goto.msk.ru/vault/admin")
    } else {
      halt(404, "Not Found")
    }
  }
  get("/register") {
    contentType = "text/html"

    ssp("/WEB-INF/templates/views/register.ssp")

  }
  post("/register") {
    Setup.add_account(params("name"), 15, params("password"), params("email"))
    redirect("https://goto.msk.ru/vault/profile")
  }

  get("/market") {
    contentType = "text/html"

    val user: Option[Account] = basicAuth()

    if (user.isEmpty) {
      redirect("https://goto.msk.ru/vault/profile")
    }
    ssp("/WEB-INF/templates/views/market.ssp", "items" -> Setup.all_cool_goods())
  }
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
      redirect("https://goto.msk.ru/vault/profile")
    }
    if (user.get.balance < params("price").toDouble)
      redirect("https://goto.msk.ru/vault/not_enough_money")
    else {
      Setup.buy_good(user.head.id, params("id").toInt)
      redirect("https://goto.msk.ru/vault/thank_you")
    }
    get("/thank_you") {
      <p>Спасибо за покупку</p>
    }
    get("/not_enough_money") {
      <p>На Вашем счете недостаточно средств</p>
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

    var tryLogin: Boolean = false
    
    if (req.username.length > 0 && req.password.length > 0) {
      tryLogin = Setup.try_login(req.username, Setup.hash(req.password))
    }

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

