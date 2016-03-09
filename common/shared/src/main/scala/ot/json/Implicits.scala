package ot.json

import prickle.JsConfig

object Implicits {

  implicit val pConfig = JsConfig(prefix = "", areSharedObjectsSupported = false)

}
