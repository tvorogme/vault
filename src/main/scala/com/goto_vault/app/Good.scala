package com.goto_vault.app

import slick.jdbc.H2Profile.api._
case class Good(id:Int, name:String, price:Double)

class GoodTable(tag: Tag) extends Table[(Int, String, Double)](tag, "Goods") {

  def id = column[Int]("ID", O.PrimaryKey)
  def name = column[String]("NAME")
  def price = column[Double]("PRICE")

  def * = (id, name, price)
}