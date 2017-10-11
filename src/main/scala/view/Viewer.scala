package etherpong.view

import etherpong._
import prototype.Player

import scalajs.js.annotation.JSExportTopLevel
import scalajs.js.annotation.JSExport
import scalajs.js.timers._
import org.scalajs.dom
import org.scalajs.dom.html

import util.Random
import math._

@JSExportTopLevel("etherpong.view.Viewer")
object Viewer {
  val config = Config(
    width = 800,
    height = 600,
    paddleLength = 80,
    paddleWidth = 15,
    ballSize = 15,
    minBallSpeed = 1,
    maxBallSpeed = 3
  )
  import config._

  val pong = new Pong(config)
  val digits = new Digits(config)

  case class Color(r: Int, g: Int, b: Int) {
    override def toString = s"rgb($r,$g,$b)"
  }

  val background = Color(0,0,0)
  val foreground = Color(200,200,200)

  @JSExport
  def main(canvas: html.Canvas): Unit = {
    implicit val ctx =
      canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    val left = new Player(Side.Left, config)
    val right = new Player(Side.Right, config)
    left.introduce(right)
    right.introduce(left)

    var block: Int = 0
    var running: Boolean = false
    var lastUpdatedAt: Int = 0
    var ballPos: Point = Point(0, 0)
    var ballVel: Point = Point(0, 0)
    var leftScore: Int = 0
    var rightScore: Int = 0

    setInterval(10) {
      if (Random.nextInt(10) < 1) {
        Random.shuffle(List(left, right)).head.update(block)
      }

      if (block == left.lastUpdatedAt && block == right.lastUpdatedAt) {
        lastUpdatedAt = block
        running = left.running
        ballPos = left.ballPos
        ballVel = left.ballVel
        leftScore = left.score
        rightScore = right.score
        draw(left.paddlePos, right.paddlePos, leftScore, rightScore, ballPos)
      } else if (running) {
        val t = block - lastUpdatedAt
        val p = pong.pos(t, ballPos, ballVel)
        val v = pong.vel(t, ballPos, ballVel)
        val (leftMiss, rightMiss) =
          pong.misses(left.paddlePos, right.paddlePos, p, v)
        if (leftMiss) {
          rightScore = rightScore + 1
          running = false
          ballout(left.paddlePos, right.paddlePos, leftScore, rightScore, p, v)
        } else if (rightMiss) {
          leftScore = leftScore + 1
          running = false
          ballout(left.paddlePos, right.paddlePos, leftScore, rightScore, p, v)
        }
        draw(left.paddlePos, right.paddlePos, leftScore, rightScore, p)
      }

      block = block + 1
    }
  }

  def ballout(leftPaddlePos: Int, rightPaddlePos: Int,
              leftScore: Int, rightScore: Int,
              pos: Point, vel: Point)
             (implicit ctx: dom.CanvasRenderingContext2D): Unit = {
    var p = pos
    var anim: SetIntervalHandle = null
    anim = setInterval(10) {
      p = p + vel
      val more =
        p.x >= 0 && p.y >= 0 &&
        p.x + ballSize < width && p.y + ballSize < height
      if (more) draw(leftPaddlePos, rightPaddlePos, leftScore, rightScore, p)
      else clearInterval(anim)
    }
  }

  def draw(leftPaddlePos: Int, rightPaddlePos: Int,
           leftScore: Int, rightScore: Int, ballPos: Point)
          (implicit ctx: dom.CanvasRenderingContext2D): Unit = {
    import ctx._

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
