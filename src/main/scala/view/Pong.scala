package etherpong.view

import etherpong._

import math._

class Pong(config: Config) {
  import config._

  val L = paddleWidth
  val T = paddleWidth
  val R = width - paddleWidth - ballSize
  val B = height - paddleWidth - ballSize

  def misses(leftPaddlePos: Int, rightPaddlePos: Int,
             p: Point, v: Point): (Boolean, Boolean) = {
    def hitrange(paddlePos: Int) = Range.inclusive(
      max(T, paddlePos - v.y / 2 - ballSize),
      min(B + ballSize, paddlePos + paddleLength - v.y / 2)
    )
    val bt = extrema(L, R, p.x, v.x).toInt
    val leftMiss =
      bt == 0 &&
      p.x + v.x <= L &&
      !hitrange(leftPaddlePos).contains(p.y)
    val rightMiss =
      bt == 0 &&
      p.x + v.x >= R &&
      !hitrange(rightPaddlePos).contains(p.y)
    (leftMiss, rightMiss)
  }

  def pos(t: Int, p: Point, v: Point): Point =
    Point(
      value(t, R - L, paddleWidth, p.x, v.x),
      value(t, B - T, paddleWidth, p.y, v.y)
    )

  def vel(t: Int, p: Point, v: Point): Point =
    Point(
      slope(t, L, R, p.x, v.x),
      slope(t, T, B, p.y, v.y)
    )

  def value(t: Int, period: Int, shift: Int, p: Int, v: Int): Int =
    abs(
      mod(v * t + p - shift - period, 2 * period) - period
    ) + shift

  def slope(t: Int, lo: Int, hi: Int, p: Int, v: Int) =
    v * pow(-1, extremas(t, lo, hi, p, v).size).toInt

  def extremas(t: Int, lo: Int, hi: Int, p: Int, v: Int): List[Int] = {
    extrema(lo, hi, p, v)
      .until(t.toFloat)
      .by((hi - lo).toFloat / abs(v).toFloat)
      .map(_.toInt)
      .toList
  }

  def extrema(lo: Int, hi: Int, p: Int, v: Int): Float =
    max(
      (lo - p) / v.toFloat,
      (hi - p) / v.toFloat
    )

  def mod(a: Int, b: Int): Int =
    if (a < 0) (a % b + b) % b else a % b
}
