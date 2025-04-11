package gitinsp

import org.scalajs.dom

object GitInspectorFrontend:
  def main(args: Array[String]): Unit =
    dom.document.querySelector("#app").innerHTML = """
      <div>
        <h1>Hello from Git Inspector Frrrontend!</h1>
        <div class="card">
          <button id="counter" type="button">Count: 0</button>
        </div>
      </div>
    """

    val button = dom.document.getElementById("counter")
    var count  = 0

    button.addEventListener(
      "click",
      {
        (_: dom.Event) =>
          count += 1
          button.textContent = s"Count: $count"
      },
    )
