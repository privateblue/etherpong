package etherpong.prototype

import etherpong._

import util.Random
import math._

class Player(side: Side, config: Config) {
  import config._

  var opponent: Option[Player] = None

  def introduce(player: Player): Unit =
    if (opponent.isEmpty) opponent = Some(player)

  var running: Boolean = false
  var lastUpdatedAt: Int = 0
  var ballPos: Point = Point(0, 0)
  var ballVel: Point = Point(0, 0)
  var paddlePos: Int = (height - paddleLength) / 2
  var score: Int = 0
  var startSide: Side = Side.Left

  def update(block: Int, restartPos: Option[Point] = None,
             restartVel: Option[Point] = None): Unit =

    // RUNNING
    if (running && block > lastUpdatedAt && opponent.isDefined) {
      val frame = block - lastUpdatedAt
      val sideBounces = extremas(frame, left, right, ballPos.x, ballVel.x)
      val miss = sideBounces.find { t =>
        val p = pos(t, ballPos, ballVel)
        val v = vel(t, ballPos, ballVel)
        val pp = side match {
          case Side.Left if p.x + v.x <= left => paddlePos
          case Side.Right if p.x + v.x >= right => paddlePos
          case _ => opponent.get.paddlePos
        }
        val paddle = Range.inclusive(
          max(top, pp - v.y / 2 - ballSize),
          min(bottom + ballSize, pp + paddleLength - v.y / 2)
        )
        !paddle.contains(p.y)
      }
      miss match {

        // MISSED BALL WITH PADDLE
        case Some(t) =>
          running = false
          val p = pos(t, ballPos, ballVel)
          val v = vel(t, ballPos, ballVel)
          side match {
            case Side.Left if p.x + v.x <= left =>
              startSide = Side.Right
            case Side.Right if p.x + v.x >= right =>
              startSide = Side.Left
            case _ =>
              startSide = side
              score = score + 1
          }
          ballPos = p + v
          ballVel = v
          lastUpdatedAt = lastUpdatedAt + t
          opponent.get.update(block)

        // REGULAR CASE
        case None =>
          val p = pos(frame, ballPos, ballVel)
          val v = vel(frame, ballPos, ballVel)
          ballPos = p
          ballVel = v
          lastUpdatedAt = block
          opponent.get.update(block)
          paddlePos = paddlePos + play(p, v)

      }

    // RESTART
    } else if (!running && block > lastUpdatedAt && opponent.isDefined) {
      running = true
      val (p, v) = restart
      ballPos = restartPos.getOrElse(p)
      ballVel = restartVel.getOrElse(v)
      lastUpdatedAt = block
      opponent.get.update(block, Some(ballPos), Some(ballVel))
    }

  def play(p: Point, v: Point): Int = {
    val t = side match {
      case Side.Left if v.x < 0 =>
        extrema(left, right, p.x, v.x)
      case Side.Right if v.x > 0 =>
        extrema(left, right, p.x, v.x)
      case _ =>
        extrema(left, right, p.x, v.x) +
          (right - left) / abs(v.x).toFloat
    }
    val y = value(t.toInt, bottom - top, paddleWidth, p.y, v.y)
    val target = y - paddleLength / 2 + ballSize / 2
    val targetPaddlePos = min(max(target, top), bottom - paddleLength)
    signum(targetPaddlePos - paddlePos)
  }

  private def restart: (Point, Point) = {
    val (posX, velX) = startSide match {
      case Side.Left =>
        (left, rnd(minBallSpeed, maxBallSpeed))
      case Side.Right =>
        (right, -1 * rnd(minBallSpeed, maxBallSpeed))
    }
    val p = Point(posX, rnd(top, bottom))
    val v = Point(
      velX,
      Random.shuffle(List(
        rnd(minBallSpeed, maxBallSpeed),
        -1 * rnd(minBallSpeed, maxBallSpeed)
      )).head
    )
    (p, v)
  }

  val left = paddleWidth
  val top = paddleWidth
  val right = width - paddleWidth - ballSize
  val bottom = height - paddleWidth - ballSize

  private def pos(t: Int, p: Point, v: Point): Point =
    Point(
      value(t, right - left, paddleWidth, p.x, v.x),
      value(t, bottom - top, paddleWidth, p.y, v.y)
    )

  private def vel(t: Int, p: Point, v: Point): Point =
    Point(
      slope(t, left, right, p.x, v.x),
      slope(t, top, bottom, p.y, v.y)
    )

  private def value(t: Int, period: Int, shift: Int, p: Int, v: Int): Int =
    abs(
      mod(v * t + p - shift - period, 2 * period) - period
    ) + shift

  private def slope(t: Int, lo: Int, hi: Int, p: Int, v: Int) =
    v * pow(-1, extremas(t, lo, hi, p, v).size).toInt

  private def extremas(t: Int, lo: Int, hi: Int, p: Int, v: Int): List[Int] = {
    extrema(lo, hi, p, v)
      .until(t.toFloat)
      .by((hi - lo).toFloat / abs(v).toFloat)
      .map(_.toInt)
      .toList
  }

  private def extrema(lo: Int, hi: Int, p: Int, v: Int): Float =
    max(
      (lo - p) / v.toFloat,
      (hi - p) / v.toFloat
    )

  private def mod(a: Int, b: Int): Int =
    if (a < 0) (a % b + b) % b else a % b

  private def rnd(min: Int, max: Int) =
    Random.nextInt(max - min + 1) + min
}