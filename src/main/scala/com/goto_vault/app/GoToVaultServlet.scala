package com.goto_vault.app

import org.scalatra._


class GoToVaultServlet extends ZvezdochkaStack {
  val s = Setup
  s.primary_setup_account()

  get("/") {
    s.try_login("aaa@a.ru", "1234")
    <p>
      lol
    </p>
  }

}
