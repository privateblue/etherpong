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
  ballSize: Int
)

case class Game(
  leftPoints: Int,
  rightPoints: Int,
  config: Config,
  state: State
)

trait State
object State {
  case class Start(
    ballYPos: Int,
    ballXVel: Int,
    ballYVel: Int
  ) extends State

  case class Step(
    leftPaddlePos: Int,
    rightPaddlePos: Int,
    ballPos: Point,
    ballVel: Point
  ) extends State

  case class End(
    leftPaddlePos: Int,
    rightPaddlePos: Int,
    winner: Player
  ) extends State
}
