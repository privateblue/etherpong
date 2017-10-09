package etherpong

case class Point(x: Int, y: Int)

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

case class State(
  config: Config,
  restarted: Boolean,
  leftPoints: Int,
  rightPoints: Int,
  leftPaddlePos: Int,
  leftPaddleVel: Int,
  rightPaddlePos: Int,
  rightPaddleVel: Int,
  ballPos: Point,
  ballVel: Point
)
