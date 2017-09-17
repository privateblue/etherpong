package etherpong

import scala.scalajs.js.annotation.JSExportTopLevel
import scala.scalajs.js.annotation.JSExport

import org.scalajs.dom
import org.scalajs.dom.html

import scala.util.Random

@JSExportTopLevel("etherpong.View")
object View {
  val config = Config(
    width = 800,
    height = 600,
    paddleLength = 100,
    paddleWidth = 20,
    ballSize = 10
  )

  val init = Game(
    leftPoints = 0,
    rightPoints = 0,
    config = config,
    state = Model.start(Player.Left, config)
  )

  def draw(ctx: dom.CanvasRenderingContext2D, game: Game): Unit =
    game.state match {
      case State.Start(ballPos, ballVel) =>
        ()

      case State.Step(leftPaddlePos, rightPaddlePos, ballPos, ballVel) =>
        import game.config._
        ctx.clearRect(0, 0, width, height)
        ctx.fillStyle = "rgb(0,0,0)"
        ctx.fillRect(0, leftPaddlePos, paddleWidth, paddleLength)
        ctx.fillRect(width - paddleWidth, rightPaddlePos, paddleWidth, paddleLength)
        ctx.beginPath()
        ctx.arc(ballPos.x, ballPos.y, ballSize, 0, 2 * math.Pi)
        ctx.closePath()
        ctx.fill()

      case State.End(leftPaddlePos, rightPaddlePos, Player.Left) =>
        ()
    }

  @JSExport
  def main(canvas: html.Canvas): Unit = {
    var state: Game = init
    val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    dom.window.setInterval(() => {
      state = Model.next(state)
      draw(ctx, state)
    }, 10)
  }
}
