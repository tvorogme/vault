package com.goto_vault.app

import org.scalatra._
//import org.scalatra.auth

class GoToVaultServlet extends ZvezdochkaStack {

  val s = Setup
  s.primary_setup_account()

  get("/profile") {
    s.try_login("aaa@a.ru", "1234")
    <ul><li>id: 1  name: Andrew Tvorozhkov   balance: 0.0</li><li>id: 2  name: Grisha Belogorov   balance: 0.0</li></ul>
  }
  get("/stats"){
    {Setup.all_accounts()}
  }
//  protected def basicAuth() = {
//    val req = new BasicAuthRequest(request)
//
//    def notAuthenticated() {
//      response.setHeader("WWW-Authenticate", "Basic realm=\"%s\"" format "mc-nulty")
//      halt(401, "Unauthenticated")
//    }
//
//    if (!req.providesAuth) {
//      notAuthenticated()
//    }
//    if (!req.isBasicAuth) {
//      halt(400, "Bad Request")
//    }
//    var user: Option[Account] = None
//
//    val tryLogin: Boolean = Setup.try_login(req.username, Setup.hash(req.password))
//
//    if (tryLogin) {
//      user = Option(Setup.get_account_by_email(req.username))
//      response.setHeader("REMOTE_USER", "user.id")
//    }
//    else {
//      notAuthenticated()
//    }
//
//    user
//  }
}

