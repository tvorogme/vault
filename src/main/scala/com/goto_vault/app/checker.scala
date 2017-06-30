package com.goto_vault.app

object checker {
  def username(user: String): Boolean = {
    if (user.length > 3) {
      if (Setup.get_account_by_email(user).isEmpty)
        true
      else
        false
    }
    else
      false
  }

  def password(passwd: String): Boolean = {
    if (passwd.length > 3)
      true
    else
      false
  }

  def full_name(name: String): Boolean = {
    if (name.length > 3)
      true
    else
      false
  }
}
