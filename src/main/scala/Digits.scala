package etherpong.view

import etherpong._

import scalajs.js.Array
import org.scalajs.dom

class Digits(implicit config: Config) {
  import config._

  val lw = paddleWidth
  val lh = paddleWidth
  val dw = 3 * lw
  val dh = 5 * lh
  def nw(n: Int) = n.toString.size * (dw + lw) - lw

  def drawScore(leftScore: Int, rightScore: Int)
               (implicit ctx: dom.CanvasRenderingContext2D): Unit = {

    val lx = width / 2 - paddleWidth * 3/2 - nw(leftScore)
    val rx = width / 2 + paddleWidth * 3/2
    val y = 2 * paddleWidth
    drawNumber(leftScore, lx, y)
    drawNumber(rightScore, rx, y)
  }

  def drawNumber(n: Int, left: Int, top: Int)
                (implicit ctx: dom.CanvasRenderingContext2D): Unit = {
    n.toString.foldLeft(left) { (x, d) =>
      drawDigit(d.toString.toInt, x, top)
      x + dw + lw
    }
  }

  def drawDigit(d: Int, x: Int, y: Int)
               (implicit ctx: dom.CanvasRenderingContext2D): Unit = {
    if (digit(d)(0) > 0) ctx.fillRect(x, y, dw, lh)
    if (digit(d)(1) > 0) ctx.fillRect(x, y, lw, dh / 2)
    if (digit(d)(2) > 0) ctx.fillRect(x + dw - lw, y, lw, dh / 2)
    if (digit(d)(3) > 0) ctx.fillRect(x, y + dh / 2 - lh / 2, dw, lh)
    if (digit(d)(4) > 0) ctx.fillRect(x, y + dh / 2, lw, dh / 2)
    if (digit(d)(5) > 0) ctx.fillRect(x + dw - lw, y + dh / 2, lw, dh / 2)
    if (digit(d)(6) > 0) ctx.fillRect(x, y + dh - lh, dw, lh)
  }

  val digit: Array[Array[Int]] =
    Array(
      Array(1, 1, 1, 0, 1, 1, 1), // 0
      Array(0, 0, 1, 0, 0, 1, 0), // 1
      Array(1, 0, 1, 1, 1, 0, 1), // 2
      Array(1, 0, 1, 1, 0, 1, 1), // 3
      Array(0, 1, 1, 1, 0, 1, 0), // 4
      Array(1, 1, 0, 1, 0, 1, 1), // 5
      Array(1, 1, 0, 1, 1, 1, 1), // 6
      Array(1, 0, 1, 0, 0, 1, 0), // 7
      Array(1, 1, 1, 1, 1, 1, 1), // 8
      Array(1, 1, 1, 1, 0, 1, 0)  // 9
    )
}
