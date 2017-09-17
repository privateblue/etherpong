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
    ballSize = 15
  )

  @JSExport
  def main(canvas: html.Canvas): Unit = {
    var state: State = Model.start(Player.Left, config)
    val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    dom.window.setInterval(() => {
      state = Model.next(state)
      draw(ctx, state)
    }, 10)
  }

  def draw(ctx: dom.CanvasRenderingContext2D, state: State): Unit = {
    import ctx._
    import state._
    import state.config._
    clearRect(0, 0, width, height)
    fillStyle = "rgb(0,0,0)"
    fillRect(0, leftPaddlePos, paddleWidth, paddleLength)
    fillRect(width - paddleWidth, rightPaddlePos, paddleWidth, paddleLength)
    fillRect(ballPos.x, ballPos.y, ballSize, ballSize)
  }
}