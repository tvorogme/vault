package com.goto_vault.app

import slick.jdbc.H2Profile.api._

/**
  * Created by acadabus on 6/21/17.
  */


case class Account(id: Int, name: String, balance: Double, password: String, email: String)


