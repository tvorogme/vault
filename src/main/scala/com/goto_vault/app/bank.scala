package com.goto_vault.app

import java.util.UUID
import java.util.Calendar

class Transaction(val from: UUID, val to: UUID, val amount: Double, val operation: Boolean) {
  val datetime = Calendar.getInstance()
}

class Account(var balance: Double = 0.0) {
  // Uid аккаунта
  val uid: UUID = java.util.UUID.randomUUID

  // Добавление денег на аккаунт
  def deposit(from: UUID, amount: Double) {
    // Добавить денег
//    val tmp_transaction: Transaction = new Transaction(from, this.uid, amount, 1)
    balance += amount
  }

  //вывод средств со счета
  def take_money(to: UUID, amount: Double): Boolean = {
    if (amount > balance)
      false
    else {
//      val tmp_transaction: Transaction = new Transaction(this.uid.to, amount, 0)
      balance -= amount

      true
    }
  }
}