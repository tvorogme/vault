package com.goto_vault

/**
  * Created by acadabus on 6/20/17.
  */
package object bank {
  case class AccountType(val name:String, val credit_percent:Double, val deposit_percent:Double,
                         val giveaway_percent:Double, val contribution_percent:Double)


  class Transaction(val kind:String, val amount:Double){
    val datetime = Calendar.getInstance()
  }

  class Account(val accountType:AccountType) {
    var balance = 0.0
    val contributions: ListBuffer[Contribution] = ListBuffer()
    val credits: ListBuffer[Credit] = ListBuffer()
    val transactions:ListBuffer[Transaction] = ListBuffer()
    // Uid аккаунта
    val uid =  java.util.UUID.randomUUID.toString
    var blocked = false

    // Добавление денег на аккаунт
    def deposit(amount: Double) {
      if(blocked)
        println("Аккаунт заблокирован, невозможно произвести операцию")
      else if (amount <= 0)
        throw new IllegalArgumentException("У вас должно быть больше денег чем 0")
      else
      // Добавить денег
        balance += amount*accountType.deposit_percent
      transactions.append(new Transaction("deposit", amount*accountType.deposit_percent))
    }

    //вывод средств со счета
    def remove_money(amount: Double){
      if(blocked)
        println("Аккаунт заблокирован, невозможно произвести операцию")
      if(amount > balance)
        throw new IllegalArgumentException("Не достаточно денег на счете")
      else{
        balance -= amount
        transactions.append(new Transaction("windraw", -amount))
      }
    }

  }


}
