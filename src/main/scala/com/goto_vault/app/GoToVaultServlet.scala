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
        redirect("https://goto.msk.ru/vault/register")
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
      user = Setup.get_account_by_email(req.username)
      response.setHeader("REMOTE_USER", user.get.id.toString)
    }
    else {
      notAuthenticated()
    }

    user
  }

  get("/") {
    contentType = "text/html"
    ssp("/WEB-INF/templates/views/index.ssp", "prefix" -> Setup.prefix)
  }

  get("/profile") {
    contentType = "text/html"

    val user: Option[Account] = basicAuth()

    if (user.isEmpty)
      redirect(s"${Setup.prefix}register")

    ssp("/WEB-INF/templates/views/profile.ssp", "user" -> Setup.get_account(user.head.id), "prefix" -> Setup.prefix)
  }

  get("/admin") {
    contentType = "text/html"
    val user: Option[Account] = basicAuth()

    if (user.head.admin) {
      Setup.all_accounts(mutable = true) +
        s"""<style>li{    width: 370px;
           |    font-size: 27px;
           |    font-weight: bold;
           |    height: 200px;
           |    float: left;
           |    padding: 25px;
           |    text-align: center;
           |    list-style-type: none;}</style>
           |<form action='${Setup.prefix}admin/add_good' method='post'>
           |<input type='text' name='good_name'> <br> <br>
           |<input type='text' name='good_price'> <br> <br>
           |<input type='submit'>
           |</form>
           |""".stripMargin +
        Setup.all_goods() +
        Setup.get_all_bought_goods()
    } else {
      halt(404, "Not Found")
    }
  }

  get("/admin/stats"){
    contentType = "text/html"
    val user: Option[Account] = basicAuth()

    if (user.head.admin) {
      Setup.all_accounts()
    }
  }

  post("/admin/add_good") {
    contentType = "text/html"

    val user: Option[Account] = basicAuth()

    if (user.head.admin) {
      Setup.add_good(params("good_name"), params("good_price").toInt)
      redirect(s"${Setup.prefix}admin")
    } else {
      halt(404, "Not Found")
    }
  }
  post("/admin/add_money") {
    contentType = "text/html"

    val user: Option[Account] = basicAuth()

    if (user.head.admin) {
      Setup.money_operation_with_db(params("id").toInt, params("amount").toInt)
      redirect(s"${Setup.prefix}admin")
    } else {
      halt(404, "Not Found")
    }
  }

  get("/register") {
    contentType = "text/html"

    if (scentry.isAuthenticated)
      redirect(s"${Setup.prefix}profile")

    ssp("/WEB-INF/templates/views/register.ssp", "prefix" -> Setup.prefix)
  }
  post("/register") {

    val name: String = params("name")
    val password: String = params("password")
    val email: String = params("email")

    if (checker.username(email) && checker.password(password) && checker.full_name(name)) {
      Setup.add_account(name, 0, password, email)
      redirect(s"${Setup.prefix}profile")
    }
    else {
      contentType = "text/html"
      "<h1 style='text-align: center;'>Something go wrong. Use other username, etc.</h1>" +
        "<h2 style='text-align: center;'>All values must be more than 5 symbols</h2> " +
        s"<script>setTimeout(function(){window.location='${Setup.prefix}register'}, 5000)</script>"
    }
  }

  get("/market") {
    contentType = "text/html"

    val user: Option[Account] = basicAuth()

    if (user.isEmpty)
      redirect(s"${Setup.prefix}profile")

    ssp("/WEB-INF/templates/views/market.ssp", "items" -> Setup.all_cool_goods(), "prefix" -> Setup.prefix)
  }

  get("/tasks") {
    contentType = "text/html"

    val user: Option[Account] = basicAuth()

    if (user.isEmpty)
      redirect(s"${Setup.prefix}profile")

    ssp("/WEB-INF/templates/views/tasks.ssp", "prefix" -> Setup.prefix)
  }
  get("/thank_you") {
    contentType = "text/html"
    "<h1>Спасибо за покупку</h1>" +
      s"<script>setTimeout(function(){window.location='${Setup.prefix}profile'}, 5000)</script>"
  }
  get("/not_enough_money") {
    contentType = "text/html"
    "<h1>На Вашем счете недостаточно средств</h1>" +
      s"<script>setTimeout(function(){window.location='${Setup.prefix}profile'}, 5000)</script>"
  }

  post("/market/buy") {
    contentType = "text/html"

    val user: Option[Account] = basicAuth()

    if (user.isEmpty) {
      redirect(s"${Setup.prefix}profile")
    } else {
      if (user.get.balance < Setup.get_good_price_by_id(params("id").toInt))
        redirect(s"${Setup.prefix}not_enough_money")

      else {
        val good_name: String = Setup.buy_good(user.get.id, params("id").toInt)

        Setup.add_bought_good(user.get.id, good_name)

        redirect(s"${Setup.prefix}thank_you")
      }
    }
  }

  def toInt(s: String): Option[Int] = {
    try {
      Some(s.toInt)
    } catch {
      case e: Exception => None
    }
  }

  get("/api/get_balance/:id") {
    contentType = "text/html"

    val intId: Option[Int] = toInt(params("id"))

    val account: Option[Account] = Setup.get_account_by_id(intId.getOrElse(-1))

    if (account.isDefined)
      account.get.balance
    else
      halt(404, "Not found")
  }
}

