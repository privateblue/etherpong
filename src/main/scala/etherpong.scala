import scala.util.Random

case class Point(
  x: Int,
  y: Int
)

trait Player
object Player {
  case object Left extends Player
  case object Right extends Player
}

trait GameState

case class Start(
  ballYPos: Int,
  ballXVel: Int,
  ballYVel: Int
) extends GameState

case class Step(
  leftPaddlePos: Int,
  rightPaddlePos: Int,
  ballPos: Point,
  ballVel: Point
) extends GameState

case class End(
  leftPaddlePos: Int,
  rightPaddlePos: Int,
  winner: Player
) extends GameState

case class Screen(
  width: Int,
  height: Int,
  paddleLength: Int,
  paddleWidth: Int,
  ballSize: Int
)

case class Game(
  leftPoints: Int,
  rightPoints: Int,
  state: GameState
)

object Etherpong {
  def next(screen: Screen, game: Game): Game = game.state match {
    case Start(ballYPos, ballXVel, ballYVel) =>
      Game(
        leftPoints = 0,
        rightPoints = 0,
        state = Step(
          leftPaddlePos = (screen.height - screen.paddleLength) / 2,
          rightPaddlePos = (screen.height - screen.paddleLength) / 2,
          ballPos = Point(screen.paddleWidth + screen.ballSize, ballYPos),
          ballVel = Point(ballXVel, ballYVel)
        )
      )

    case Step(leftPaddlePos, rightPaddlePos, ballPos, ballVel) =>
      Game(
        leftPoints = game.leftPoints,
        rightPoints = game.rightPoints,
        state = {
          val (px, vx) = move(
            ballPos.x,
            ballVel.x,
            screen.width,
            screen.paddleWidth + screen.ballSize
          )
          val (py, vy) = move(
            ballPos.y,
            ballVel.y,
            screen.height,
            screen.ballSize
          )
          if (ballPos.x + ballVel.x < screen.paddleWidth + screen.ballSize &&
              !intersects(ballPos, ballVel, screen.ballSize, screen.paddleWidth, leftPaddlePos, leftPaddlePos + screen.paddleLength))
            End(leftPaddlePos, rightPaddlePos, Player.Right)
          else if (ballPos.x + ballVel.x > screen.width - screen.paddleWidth - screen.ballSize &&
                   !intersects(ballPos, ballVel, screen.ballSize, screen.width - screen.paddleWidth, leftPaddlePos, leftPaddlePos + screen.paddleLength))
            End(leftPaddlePos, rightPaddlePos, Player.Left)
          else
            Step(
              leftPaddlePos = leftPaddlePos,
              rightPaddlePos = rightPaddlePos,
              ballPos = Point(px, py),
              ballVel = Point(vx, vy)
            )
        }
      )

    case End(leftPaddlePos, rightPaddlePos, Player.Left) =>
      Game(
        leftPoints = game.leftPoints + 1,
        rightPoints = game.rightPoints,
        state = Step(
          leftPaddlePos = leftPaddlePos,
          rightPaddlePos = rightPaddlePos,
          ballPos = Point(
            screen.paddleWidth + screen.ballSize,
            Random.nextInt(screen.height)
          ),
          ballVel = Point(Random.nextInt(3) + 1, Random.nextInt(3) + 1)
        )
      )

    case End(leftPaddlePos, rightPaddlePos, Player.Right) =>
      Game(
        leftPoints = game.leftPoints,
        rightPoints = game.rightPoints + 1,
        state = Step(
          leftPaddlePos = leftPaddlePos,
          rightPaddlePos = rightPaddlePos,
          ballPos = Point(
            screen.width - screen.paddleWidth - screen.ballSize,
            Random.nextInt(screen.height)
          ),
          ballVel = Point(-1 * Random.nextInt(3) - 1, Random.nextInt(3) + 1)
        )
      )
  }

  def move(pos: Int, vel: Int, size: Int, offset: Int): (Int, Int) = {
    def bounce(y: Int) = math.abs(y - offset) + offset
    val sum = pos + vel
    if (sum < offset) (bounce(sum), -1 * vel)
    else if (sum > size - offset) (size - bounce(size - sum), -1 * vel)
    else (sum, vel)
  }

  def intersects(pos: Point, vel: Point, offset: Int, x: Int, top: Int, bottom: Int): Boolean = {
    val a = vel.y / vel.x.toDouble
    val y =
      if (math.signum(vel.x) < 0) (x - pos.x + offset) * a + pos.y
      else (x - pos.x - offset) * a + pos.y
    top - 1 <= y && y <= bottom + 1
  }
}
