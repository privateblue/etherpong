package etherpong

import scala.scalajs.js.annotation.JSExportTopLevel
import scala.scalajs.js.annotation.JSExport

import org.scalajs.dom
import org.scalajs.dom.html
import scala.scalajs.js.timers._

import util.Random
import math._

@JSExportTopLevel("etherpong.Viewer")
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

  case class Color(r: Int, g: Int, b: Int) {
    override def toString = s"rgb($r,$g,$b)"
  }

  val background = Color(0,0,0)
  val foreground = Color(200,200,200)

  @JSExport
  def main(canvas: html.Canvas): Unit = {
    val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

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
        draw(ctx, left.paddlePos, right.paddlePos, leftScore, rightScore, ballPos)
      } else if (running) {
        val t = block - lastUpdatedAt
        val p = pos(t, ballPos, ballVel)
        val v = vel(t, ballPos, ballVel)
        val bt = nextBounceAt(L, R, p.x, v.x).toInt
        val leftMiss =
          bt == 0 &&
          p.x + v.x <= L &&
          !paddleRange(left.paddlePos, v.y).contains(p.y)
        val rightMiss =
          bt == 0 &&
          p.x + v.x >= R &&
          !paddleRange(right.paddlePos, v.y).contains(p.y)

        if (leftMiss) {
          rightScore = rightScore + 1
          running = false
          drawMiss(ctx, left.paddlePos, right.paddlePos, leftScore, rightScore, p, v)
        } else if (rightMiss) {
          leftScore = leftScore + 1
          running = false
          drawMiss(ctx, left.paddlePos, right.paddlePos, leftScore, rightScore, p, v)
        }

        draw(ctx, left.paddlePos, right.paddlePos, leftScore, rightScore, p)
      }

      block = block + 1
    }
  }

  def drawMiss(ctx: dom.CanvasRenderingContext2D,
               leftPaddlePos: Int, rightPaddlePos: Int,
               leftScore: Int, rightScore: Int,
               pos: Point, vel: Point): Unit = {
    var p = pos
    var anim: SetIntervalHandle = null
    anim = setInterval(10) {
      p = p + vel
      if (p.x >= 0 && p.y >= 0 && p.x + ballSize < width && p.y + ballSize < height) {
        draw(ctx, leftPaddlePos, rightPaddlePos, leftScore, rightScore, p)
      } else clearInterval(anim)
    }
  }

  def draw(ctx: dom.CanvasRenderingContext2D,
           leftPaddlePos: Int, rightPaddlePos: Int,
           leftScore: Int, rightScore: Int, ball: Point): Unit = {
    import ctx._

    // CLEAR
    fillStyle = background.toString
    fillRect(0, 0, width, height)

    fillStyle = foreground.toString

    // TOP, BOTTOM LINE
    fillRect(0, 0, width, paddleWidth)
    fillRect(0, height - paddleWidth, width, paddleWidth)

    // MIDLINE
    T until height by 3 * ballSize foreach { y =>
      fillRect(width / 2 - ballSize / 2, y, ballSize, min(2 * ballSize, height - y))
    }

    // TODO SCORE

    // PADDLES
    fillRect(0, leftPaddlePos, paddleWidth, paddleLength)
    fillRect(width - paddleWidth, rightPaddlePos, paddleWidth, paddleLength)

    // BALL
    fillRect(ball.x, ball.y, ballSize, ballSize)
  }

  val L = paddleWidth
  val T = paddleWidth
  val R = width - paddleWidth - ballSize
  val B = height - paddleWidth - ballSize

  private def pos(t: Int, p: Point, v: Point): Point =
    Point(
      position(t, R - L, paddleWidth, p.x, v.x),
      position(t, B - T, paddleWidth, p.y, v.y)
    )

  private def vel(t: Int, p: Point, v: Point): Point =
    Point(
      velocity(t, L, R, p.x, v.x),
      velocity(t, T, B, p.y, v.y)
    )

  private def position(t: Int, period: Int, shift: Int, p: Int, v: Int): Int =
    abs(
      mod(v * t + p - shift - period, 2 * period) - period
    ) + shift

  private def velocity(t: Int, lo: Int, hi: Int, p: Int, v: Int) =
    v * pow(-1, bounces(t, lo, hi, p, v).size).toInt

  private def bounces(t: Int, lo: Int, hi: Int, p: Int, v: Int): List[Int] = {
    nextBounceAt(lo, hi, p, v)
      .until(t.toFloat)
      .by((hi - lo).toFloat / abs(v).toFloat)
      .map(_.toInt)
      .toList
  }

  private def nextBounceAt(lo: Int, hi: Int, p: Int, v: Int): Float =
    max(
      (lo - p) / v.toFloat,
      (hi - p) / v.toFloat
    )

  private def paddleRange(pp: Int, vy: Int) =
    Range.inclusive(
      max(T, pp - vy / 2 - ballSize),
      min(B + ballSize, pp + paddleLength - vy / 2)
    )

  private def mod(a: Int, b: Int): Int =
    if (a < 0) (a % b + b) % b else a % b
}
