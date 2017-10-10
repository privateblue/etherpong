package etherpong

import scala.scalajs.js.annotation.JSExportTopLevel
import scala.scalajs.js.annotation.JSExport

import org.scalajs.dom
import org.scalajs.dom.html

import scala.util.Random

@JSExportTopLevel("etherpong.Viewer")
object Viewer {
  val config = Config(
    width = 797,
    height = 601,
    paddleLength = 80,
    paddleWidth = 13,
    ballSize = 13,
    minBallSpeed = 4,
    maxBallSpeed = 4
  )

  val background = Color(200,200,200)
  val foreground = Color(50,50,50)

  @JSExport
  def main(canvas: html.Canvas): Unit = {
    import config._

    val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    import ctx._

    val left = new Player(Side.Left, config)
    val right = new Player(Side.Right, config)
    left.introduce(right)
    right.introduce(left)

    var block: Int = 0
    dom.window.setInterval(() => {
      val either = Random.shuffle(List(left, right)).head

      either.update(block)

      fillStyle = background.toString
      fillRect(0, 0, width, height)
      fillStyle = foreground.toString
      fillRect(0, left.paddlePos, paddleWidth, paddleLength)
      fillRect(width - paddleWidth, right.paddlePos, paddleWidth, paddleLength)
      fillRect(either.ballPos.x, either.ballPos.y, ballSize, ballSize)

      block = block + 1
    }, 10)
  }

case class Color(r: Int, g: Int, b: Int) {
    override def toString = s"rgb($r,$g,$b)"
  }
}
