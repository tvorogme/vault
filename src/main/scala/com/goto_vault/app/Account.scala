package com.goto_vault.app

import slick.jdbc.H2Profile.api._


case class Account(id: Int, name: String, balance: Double, password: String, email: String)


