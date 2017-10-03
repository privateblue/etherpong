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
    paddleWidth = 15,
    ballSize = 15,
    minBallSpeed = 5,
    maxBallSpeed = 5
  )

  @JSExport
  def main(canvas: html.Canvas): Unit = {
    var state: State = Model.init(Player.Left, config)
    val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    dom.window.setInterval(() => {
      state = Model.update(state)
      draw(ctx, state)
    }, 10)
  }

  def draw(ctx: dom.CanvasRenderingContext2D, state: State): Unit = {
    import ctx._
    import state._
    import state.config._
    fillStyle = "rgb(200,200,200)"
    fillRect(0, 0, width, height)
    fillStyle = "rgb(0,0,0)"
    fillRect(0, leftPaddlePos, paddleWidth, paddleLength)
    fillRect(width - paddleWidth, rightPaddlePos, paddleWidth, paddleLength)
    fillRect(ballPos.x, ballPos.y, ballSize, ballSize)
  }
}
