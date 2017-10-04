package etherpong

import scala.scalajs.js.annotation.JSExportTopLevel
import scala.scalajs.js.annotation.JSExport

import org.scalajs.dom
import org.scalajs.dom.html

import scala.util.Random

@JSExportTopLevel("etherpong.View")
object View {
  val config = Config(
    width = 797,
    height = 601,
    paddleLength = 100,
    paddleWidth = 13,
    ballSize = 7,
    minBallSpeed = 1,
    maxBallSpeed = 5
  )

  val background = Color(200,200,200)
  val foreground = Color(50,50,50)

  @JSExport
  def main(canvas: html.Canvas): Unit = {
    val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    var state: State = Model.init(config)
    redraw(ctx, null, state)
    dom.window.setInterval(() => {
      val updated = Model.update(state)
      redraw(ctx, state, updated)
      state = updated
    }, 10)
  }

  def redraw(ctx: dom.CanvasRenderingContext2D, previous: State, current: State): Unit = {
    if (current.restarted) clear(ctx)
    else {
      drawPaddles(ctx, previous, background)
      drawBall(ctx, previous, background)
    }
    drawPaddles(ctx, current, foreground)
    drawBall(ctx, current, foreground)
  }

  def clear(ctx: dom.CanvasRenderingContext2D): Unit = {
    import ctx._
    fillStyle = background.toString
    fillRect(0, 0, config.width, config.height)
  }

  def drawPaddles(ctx: dom.CanvasRenderingContext2D, state: State, color: Color): Unit = {
    import ctx._
    import state._
    import state.config._
    fillStyle = color.toString
    fillRect(0, leftPaddlePos, paddleWidth, paddleLength)
    fillRect(width - paddleWidth, rightPaddlePos, paddleWidth, paddleLength)
  }

  def drawBall(ctx: dom.CanvasRenderingContext2D, state: State, color: Color): Unit = {
    import ctx._
    import state._
    import state.config._
    fillStyle = color.toString
    fillRect(ballPos.x, ballPos.y, ballSize, ballSize)
  }

  case class Color(r: Int, g: Int, b: Int) {
    override def toString = s"rgb($r,$g,$b)"
  }
}
