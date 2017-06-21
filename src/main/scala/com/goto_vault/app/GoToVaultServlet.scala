package com.goto_vault.app

import org.scalatra._


class GoToVaultServlet extends ZvezdochkaStack {
  val s = Setup
  s.primary_setup_account()

  get("/") {
    s.try_login("aaa@a.ru", "1234")
    <ul><li>id: 1  name: Andrew Tvorozhkov   balance: 0.0</li><li>id: 2  name: Grisha Belogorov   balance: 0.0</li></ul>
  }
  get("/stats"){
    {Setup.all_accounts()}
  }
}
