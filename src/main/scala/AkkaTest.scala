import akka.actor.{Props, Actor, ActorSystem}
import scala.concurrent.Future

class Cursor {

}

class OctopusRepository {

  def get(path:String):Future[Cursor] = {

  }
}

case class PresentContent(path:String)

class Presenter extends Actor {

  def receive: Actor.Receive = {

    case PresentContent(path) =>

      path match {
        case "/" =>

          val presenter1 = context.system.actorOf(Props[Presenter], "presenter")
          presenter1 ? PresentContent("/top")

          val presenter2 = context.system.actorOf(Props[Presenter], "presenter")



          // Compound content. Needs to ask presenters to provide another parts.
          val greeter = Akka.actorOf(Props[Greeter], "greeter")

        case "/top" =>
          // Static top content
          val repo = new OctopusRepository()


        case "/content" =>
          // Dynamic, async loaded content

      }
  }
}


/**
 * This is test prototype for Repository/Cursor/Presenter architecture in Octopus
 *
 */

object AkkaTest extends App {

  val system = ActorSystem("akkaTest")



}
