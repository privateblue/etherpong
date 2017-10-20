package etherpong.view

import scalajs.js
import scalajs.js.annotation.JSImport

@js.native
trait Config extends js.Object {
  val width: Int = js.native
  val height: Int = js.native
  val paddleLength: Int = js.native
  val paddleWidth: Int = js.native
  val ballSize: Int = js.native
  val minBallSpeed: Int = js.native
  val maxBallSpeed: Int = js.native
}

@js.native
trait State extends js.Object {
  val block: Int = js.native
  val running: Boolean = js.native
  val lastUpdatedAt: Int = js.native
  val ballPos: js.Array[Int] = js.native
  val ballVel: js.Array[Int] = js.native
  val score: Int = js.native
  val paddlePos: Int = js.native
}

@js.native
@JSImport("./client.js", JSImport.Namespace)
object Client extends js.Object {
  def getConfig(address: String): Config = js.native
  def onBlock(leftAddress: String, rightAddress: String,
              callback: js.Function2[State, State, Unit]): Unit = js.native
}
