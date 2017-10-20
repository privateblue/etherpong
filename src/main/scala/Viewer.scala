package etherpong.view

import scalajs.js
import scalajs.js.annotation.JSExportTopLevel
import scalajs.js.annotation.JSExport
import scalajs.js.timers._
import org.scalajs.dom
import org.scalajs.dom.html

import util.Random
import math._

@JSExportTopLevel("etherpong.view.Viewer")
object Viewer {
  trait Side
  object Side {
    case object Left extends Side
    case object Right extends Side
  }

  case class Color(r: Int, g: Int, b: Int) {
    override def toString = s"rgb($r,$g,$b)"
  }

  def toPoint(arr: js.Array[Int]): Point = Point(arr(0), arr(1))

  val leftAddress = "0x14e97af287fa89596cd7b5e21be3756165d8a068"
  val rightAddress = "0xc439b75412511feb80589363d989cd1268dea5f3"

  val background = Color(0,0,0)
  val foreground = Color(200,200,200)

  @JSExport
  def main(canvas: html.Canvas): Unit = {
    implicit val ctx =
      canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    implicit val config =
      Client.config(leftAddress, rightAddress)

    implicit val digits = new Digits

    val pong = new Pong

    var running: Boolean = false
    var lastUpdatedAt: Int = 0
    var ballPos: Point = Point(0, 0)
    var ballVel: Point = Point(0, 0)
    var scoreLeft: Int = 0
    var scoreRight: Int = 0

    Client.onBlock(leftAddress, rightAddress) {
      (
        block: Int,
        leftRunning: Boolean,
        rightRunning: Boolean,
        leftLastUpdatedAt: Int,
        rightLastUpdatedAt: Int,
        leftBallPos: js.Array[Int],
        rightBallPos: js.Array[Int],
        leftBallVel: js.Array[Int],
        rightBallVel: js.Array[Int],
        leftScore: Int,
        rightScore: Int,
        leftPaddlePos: Int,
        rightPaddlePos: Int
      ) =>
        if (block == leftLastUpdatedAt && block == rightLastUpdatedAt) {
          lastUpdatedAt = block
          running = leftRunning
          ballPos = toPoint(leftBallPos)
          ballVel = toPoint(leftBallVel)
          scoreLeft = leftScore
          scoreRight = rightScore
          draw(leftPaddlePos, rightPaddlePos, scoreLeft, scoreRight, ballPos)
        } else if (running) {
          val t = block - lastUpdatedAt
          val p = pong.pos(t, ballPos, ballVel)
          val v = pong.vel(t, ballPos, ballVel)
          val (leftMiss, rightMiss) =
            pong.misses(leftPaddlePos, rightPaddlePos, p, v)
          if (leftMiss) {
            scoreRight = scoreRight + 1
            running = false
          } else if (rightMiss) {
            scoreLeft = scoreLeft + 1
            running = false
          }
          draw(leftPaddlePos, rightPaddlePos, scoreLeft, scoreRight, p)
        }
    }
  }

  def draw(leftPaddlePos: Int, rightPaddlePos: Int,
           leftScore: Int, rightScore: Int, ballPos: Point)
          (implicit ctx: dom.CanvasRenderingContext2D, config: Config, digits: Digits): Unit = {
    import ctx._
    import config._

    // CLEAR
    fillStyle = background.toString
    fillRect(0, 0, width, height)

    fillStyle = foreground.toString

    // TOP, BOTTOM LINE
    fillRect(0, 0, width, paddleWidth)
    fillRect(0, height - paddleWidth, width, paddleWidth)

    // MIDLINE
    paddleWidth until height by 3 * ballSize foreach { y =>
      val x = width / 2 - ballSize / 2
      val w = ballSize
      val h = min(2 * ballSize, height - y)
      fillRect(x, y, w, h)
    }

    // SCORE
    digits.drawScore(leftScore, rightScore)

    // PADDLES
    fillRect(0, leftPaddlePos, paddleWidth, paddleLength)
    fillRect(width - paddleWidth, rightPaddlePos, paddleWidth, paddleLength)

    // BALL
    fillRect(ballPos.x, ballPos.y, ballSize, ballSize)
  }
}
