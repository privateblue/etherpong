package etherpong

case class Point(x: Int, y: Int) {
  def +(that: Point) = Point(x + that.x, y + that.y)
  override def toString = s"($x,$y)"
}

trait Side
object Side {
  case object Left extends Side
  case object Right extends Side
}

case class Config(
  width: Int,
  height: Int,
  paddleLength: Int,
  paddleWidth: Int,
  ballSize: Int,
  minBallSpeed: Int,
  maxBallSpeed: Int
)
