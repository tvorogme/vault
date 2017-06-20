package com.goto_vault.app

import org.scalatra._

class GoToVaultServlet extends ZvezdochkaStack {

  get("/") {
    <html>
      <body>
        <h1>Hello, world!</h1>
        Say <a href="hello-scalate">hello to Scalate</a>.
      </body>
    </html>
  }

}
