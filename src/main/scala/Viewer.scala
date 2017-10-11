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
    width = 797,
    height = 601,
    paddleLength = 80,
    paddleWidth = 13,
    ballSize = 13,
    minBallSpeed = 1,
    maxBallSpeed = 3
  )
  import config._

  case class Color(r: Int, g: Int, b: Int) {
    override def toString = s"rgb($r,$g,$b)"
  }

  val background = Color(200,200,200)
  val foreground = Color(50,50,50)

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
      if (Random.nextInt(10) < 1)
        Random.shuffle(List(left, right)).head.update(block)

      if (block == left.lastUpdatedAt && block == right.lastUpdatedAt) {
        lastUpdatedAt = block
        running = left.running
        ballPos = left.ballPos
        ballVel = left.ballVel
        leftScore = left.score
        rightScore = right.score
        if (running) draw(ctx, left.paddlePos, right.paddlePos, ballPos)
        else () // TODO render waiting screen
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
          drawMiss(ctx, left.paddlePos, right.paddlePos, p, v)
        } else if (rightMiss) {
          leftScore = leftScore + 1
          running = false
          drawMiss(ctx, left.paddlePos, right.paddlePos, p, v)
        }

        draw(ctx, left.paddlePos, right.paddlePos, p)
      } else {
        // TODO render waiting screen
      }

      block = block + 1
    }
  }

  def drawMiss(ctx: dom.CanvasRenderingContext2D,
               leftPaddlePos: Int, rightPaddlePos: Int,
               pos: Point, vel: Point): Unit = {
    var p = pos
    var anim: SetIntervalHandle = null
    anim = setInterval(10) {
      p = p + vel
      if (p.x >= 0 && p.y >= 0 && p.x + ballSize < width && p.y + ballSize < height) {
        draw(ctx, leftPaddlePos, rightPaddlePos, p)
      } else clearInterval(anim)
    }
  }

  def draw(ctx: dom.CanvasRenderingContext2D,
           leftPaddlePos: Int, rightPaddlePos: Int, ball: Point): Unit = {
    import ctx._
    fillStyle = background.toString
    fillRect(0, 0, width, height)
    fillStyle = foreground.toString
    fillRect(0, leftPaddlePos, paddleWidth, paddleLength)
    fillRect(width - paddleWidth, rightPaddlePos, paddleWidth, paddleLength)
    fillRect(ball.x, ball.y, ballSize, ballSize)
    // TODO render score
  }

  val L = paddleWidth
  val T = 0
  val R = width - paddleWidth - ballSize
  val B = height - ballSize

  private def pos(t: Int, p: Point, v: Point): Point =
    Point(
      position(t, R - L, paddleWidth, p.x, v.x),
      position(t, B - T, 0, p.y, v.y)
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
      max(0, pp - vy / 2 - ballSize),
      min(height, pp + paddleLength - vy / 2)
    )

  private def mod(a: Int, b: Int): Int =
    if (a < 0) (a % b + b) % b else a % b
}
