package etherpong

import scala.util.Random

object Model {
  def start(player: Player, config: Config): State.Start = {
    import config._
    val (posX, velX) = player match {
      case Player.Left =>
        (paddleWidth + ballSize, Random.nextInt(3) + 1)
      case Player.Right =>
        (width - paddleWidth - ballSize, -1 * Random.nextInt(3) - 1)
    }
    State.Start(
      ballPos = Point(posX, Random.nextInt(height)),
      ballVel = Point(velX, Random.nextInt(7) - 3)
    )
  }

  def next(game: Game): Game = {
    import game.config._
    game.state match {
      case State.Start(ballPos, ballVel) =>
        Game(
          leftPoints = 0,
          rightPoints = 0,
          config = game.config,
          state = State.Step(
            leftPaddlePos = (height - paddleLength) / 2,
            rightPaddlePos = (height - paddleLength) / 2,
            ballPos = ballPos,
            ballVel = ballVel
          )
        )

      case State.Step(leftPaddlePos, rightPaddlePos, ballPos, ballVel) =>
        Game(
          leftPoints = game.leftPoints,
          rightPoints = game.rightPoints,
          config = game.config,
          state = {
            val (px, vx) = move(
              pos = ballPos.x,
              vel = ballVel.x,
              size = width,
              offset = paddleWidth + ballSize
            )
            val (py, vy) = move(
              pos = ballPos.y,
              vel = ballVel.y,
              size = height,
              offset = ballSize
            )
            if (ballPos.x + ballVel.x < paddleWidth + ballSize
                && ! touches(
                       pos = ballPos,
                       vel = ballVel,
                       ballSize = ballSize,
                       x = paddleWidth,
                       top = leftPaddlePos,
                       bottom = leftPaddlePos + paddleLength
                     )
               )
              State.End(leftPaddlePos, rightPaddlePos, Player.Right)
            else if (ballPos.x + ballVel.x > width - paddleWidth - ballSize
                     && ! touches(
                            pos = ballPos,
                            vel = ballVel,
                            ballSize = ballSize,
                            x = width - paddleWidth,
                            top = leftPaddlePos,
                            bottom = leftPaddlePos + paddleLength
                          )
                    )
              State.End(leftPaddlePos, rightPaddlePos, Player.Left)
            else
              State.Step(
                leftPaddlePos = leftPaddlePos,
                rightPaddlePos = rightPaddlePos,
                ballPos = Point(px, py),
                ballVel = Point(vx, vy)
              )
          }
        )

      case State.End(leftPaddlePos, rightPaddlePos, player) =>
        val st = start(player, game.config)
        val (leftWin, rightWin) = player match {
          case Player.Left => (1, 0)
          case Player.Right => (0, 1)
        }
        Game(
          leftPoints = game.leftPoints + leftWin,
          rightPoints = game.rightPoints + rightWin,
          config = game.config,
          state = State.Step(
            leftPaddlePos = leftPaddlePos,
            rightPaddlePos = rightPaddlePos,
            ballPos = st.ballPos,
            ballVel = st.ballVel
          )
        )
    }
  }

  def move(pos: Int, vel: Int, size: Int, offset: Int): (Int, Int) = {
    def bounce(y: Int) = math.abs(y - offset) + offset
    val sum = pos + vel
    if (sum < offset) (bounce(sum), -1 * vel)
    else if (sum > size - offset) (size - bounce(size - sum), -1 * vel)
    else (sum, vel)
  }

  def touches(pos: Point, vel: Point, ballSize: Int,
              x: Int, top: Int, bottom: Int): Boolean = {
    val a = vel.y / vel.x.toDouble
    val offset = if (math.signum(vel.x) < 0) ballSize else -1 * ballSize
    val y = (x - pos.x + offset) * a + pos.y
    top - 1 <= y && y <= bottom + 1
  }
}
