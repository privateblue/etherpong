package etherpong

case class Point(x: Int, y: Int)

trait Player
object Player {
  case object Left extends Player
  case object Right extends Player
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
  leftPoints: Int,
  rightPoints: Int,
  leftPaddlePos: Int,
  leftPaddleVel: Int,
  rightPaddlePos: Int,
  rightPaddleVel: Int,
  ballPos: Point,
  ballVel: Point
)
