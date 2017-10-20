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
@JSImport("./client.js", JSImport.Namespace)
object Client extends js.Object {
  def config(leftAddress: String, rightAddress: String): Config = js.native
  def onBlock(leftAddress: String, rightAddress: String)(
    callback: (Int, Boolean, Boolean, Int, Int, js.Array[Int], js.Array[Int], js.Array[Int], js.Array[Int], Int, Int, Int, Int) => Unit
  ): Unit = js.native
}
