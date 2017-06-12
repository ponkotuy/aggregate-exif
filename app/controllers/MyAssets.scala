package controllers

import play.api.mvc.Controller

class MyAssets extends Controller {
  def at(path: String, file: String, aggressiveCaching: Boolean = false) = {
    if (file.endsWith("/")) Assets.at(path, file + "index.html")
    else Assets.at(path, file, aggressiveCaching)
  }
}
