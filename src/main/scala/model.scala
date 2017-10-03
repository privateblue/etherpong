package etherpong

import scala.util.Random

object Model {
  def init(player: Player, config: Config): State = {
    import config._
    def rnd(min: Int, max: Int) = Random.nextInt(max - min + 1) + min
    val (posX, velX) = player match {
      case Player.Left =>
        (paddleWidth, rnd(minBallSpeed, maxBallSpeed))
      case Player.Right =>
        (width - paddleWidth - ballSize, -1 * rnd(minBallSpeed, maxBallSpeed))
    }
    State(
      config = config,
      leftPoints = 0,
      rightPoints = 0,
      leftPaddlePos = (height - paddleLength) / 2,
      rightPaddlePos = (height - paddleLength) / 2,
      ballPos = Point(posX, Random.nextInt(height)),
      ballVel = Point(velX, Random.nextInt(2 * maxBallSpeed + 1) - maxBallSpeed)
    )
  }

  def update(state: State): State = {
    import state._
    import config._

    val leftPadRange =
      Range.inclusive(leftPaddlePos - 1, leftPaddlePos + paddleLength + 1)
    val rightPadRange =
      Range.inclusive(rightPaddlePos - 1, rightPaddlePos + paddleLength + 1)

    val atLeftEdge = ballPos.x + ballVel.x < paddleWidth
    val atRightEdge = ballPos.x + ballVel.x > width - paddleWidth - ballSize

    val leftMiss =
      atLeftEdge &&
      !leftPadRange.contains(
        bounceHeight(Player.Left, ballPos, ballVel, config)
      )
    val rightMiss =
      atRightEdge &&
      !rightPadRange.contains(
        bounceHeight(Player.Right, ballPos, ballVel, config)
      )

    if (leftMiss) {
      val st = init(Player.Right, config)
      State(
        config = config,
        leftPoints = leftPoints,
        rightPoints = rightPoints + 1,
        leftPaddlePos,
        rightPaddlePos,
        ballPos = st.ballPos,
        ballVel = st.ballVel
      )
    } else if (rightMiss) {
      val st = init(Player.Left, config)
      State(
        config = config,
        leftPoints = leftPoints + 1,
        rightPoints = rightPoints,
        leftPaddlePos,
        rightPaddlePos,
        ballPos = st.ballPos,
        ballVel = st.ballVel
      )
    } else {
      val (px, vx) = ballStep(
        pos = ballPos.x,
        vel = ballVel.x,
        size = width,
        edge = paddleWidth,
        ballSize = ballSize
      )
      val (py, vy) = ballStep(
        pos = ballPos.y,
        vel = ballVel.y,
        size = height,
        edge = 0,
        ballSize = ballSize
      )
      State(
        config = config,
        leftPoints = leftPoints,
        rightPoints = rightPoints,
        leftPaddlePos =
          paddleStep(Player.Left, leftPaddlePos, ballPos, ballVel, config),
        rightPaddlePos =
          paddleStep(Player.Right, rightPaddlePos, ballPos, ballVel, config),
        ballPos = Point(px, py),
        ballVel = Point(vx, vy)
      )
    }
  }

  def ballStep(pos: Int, vel: Int,
               size: Int, edge: Int, ballSize: Int): (Int, Int) = {
    def bounce(y: Int, offset: Int) = math.abs(y - offset) + offset
    val sum = pos + vel
    if (sum < edge)
      (bounce(sum, edge), -1 * vel)
    else if (sum > size - edge - ballSize)
      (size - bounce(size - sum, edge + ballSize), -1 * vel)
    else
      (sum, vel)
  }

  def paddleStep(player: Player, paddlePos: Int,
                 pos: Point, vel: Point, config: Config): Int = {
    import config._
    val y = bounceHeight(player, pos, vel, config)
    val targetPaddlePos =
      math.min(math.max(y - paddleLength / 2, 0), height - paddleLength)
    paddlePos + math.signum(targetPaddlePos - paddlePos) * paddleSpeed
  }

  def bounceHeight(player: Player, pos: Point, vel: Point,
                   config: Config): Int = {
    import config._
    val left = paddleWidth
    val top = 0
    val right = width - paddleWidth - ballSize
    val bottom = height - ballSize
    val horT =
      math.max(
        (left - pos.x) / vel.x.toFloat,
        (right - pos.x) / vel.x.toFloat
      )
    val verT =
      if (vel.y == 0) Float.PositiveInfinity
      else math.max(
        (top - pos.y) / vel.y.toFloat,
        (bottom - pos.y) / vel.y.toFloat
      )
    val t = math.min(horT, verT)
    val x = pos.x + t * vel.x
    val y = pos.y + t * vel.y
    player match {
      case Player.Left if x <= left => math.round(y)
      case Player.Right if x >= right => math.round(y)
      case _ =>
        val (px, vx) =
          ballStep(pos.x + t.toInt * vel.x, vel.x, width, paddleWidth, ballSize)
        val (py, vy) =
          ballStep(pos.y + t.toInt * vel.y, vel.y, height, 0, ballSize)
        bounceHeight(player, Point(px, py), Point(vx, vy), config)
    }
  }
}
