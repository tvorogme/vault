package com.goto_vault.app

import org.scalatra._
//import com.goto_vault.app.Setup


class GoToVaultServlet extends ZvezdochkaStack {

  get("/") {
    //    <html>
    //      <body>
    //        <h1>Hello, world!</h1>
    //        Say <a href="hello-scalate">hello to Scalate</a>.
    //      </body>
    //    </html>

    val s = Setup
    s.primary_setup()

    <p>
      {s.get_last_account()}
    </p>
  }

}
