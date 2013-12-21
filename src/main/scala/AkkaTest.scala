import akka.actor.{ActorRef, Props, Actor, ActorSystem}
import akka.pattern.ask
import akka.pattern.{ after, ask, pipe }
import scala.concurrent.Future

case class RenderedContent(mimeType:String, content:String)

sealed class Response

case class RedirectResponse(repository:Repository, path:String) extends Response

case class ContentResponse[T](kind:String, content:T) extends Response

trait Repository {

  // TODO: if already complete future is not will be dispatched to another thread call but runned immedeatellty
  // TODO: than we can use Future in other case needs to use Actor
  def get(path:String):Future[Response]
}

case class Location(repository:Repository, path:String)

class OctopusRepository extends Repository {

  def get(path:String):Future[Response] = {

  }
}

class RemoteRepository extends Repository {

  def get(path: String): Future[Response] = {

  }
}


case class PresentContent(location:Location)

class Presenter(presenterDispatcher:ActorRef) extends Actor {

  def receive: Actor.Receive = {

    case PresentContent(location) =>

      // Capture sender val
      // val sender = this.sender

      def handlerFuture(label:String)(response:Response) = {

        case RedirectResponse(repository, url) => location.repository.get(url) map handlerFuture("")

        case ContentResponse(kind, content) =>

          label match {

            case "" =>
              // This is "/"
              // Compound content. Needs to ask presenters to provide another parts.

              val pFuture1 = presenterDispatcher ? PresentContent(Location(location.repository, "/top"))
              val pFuture2 = presenterDispatcher ? PresentContent(Location(location.repository, "/content"))

              // TODO: This is how to implement soft timeout for rendering using "after"
              // For every operation we needs to have single future ith will group another two together
              // first is the rendering future and second is timeout fallback future
              // THINK ABOUT SETUP ANOTHER MAPPING FOR RENDERING FUTURE IN THE FALLBACK TIMEOUT FUTURE.
              // WE CAN ADD SOME CODE WHICH WILL HANDLE RESULT AND SEND IT ASYNC ( OVER WEBSOCKET ) TO BROWSER
              // AFTER PAGE IS RENDERED.
              // val searchFuture = search(worktime)
              // val fallback = after(timeout, context.system.scheduler) {
              //   Future successful s"$worktime ms > $timeout"
              // }
              // Future firstCompletedOf Seq(searchFuture, fallback)


              val futures: Seq[Future[Double]] = //...

              Future.fold(futures)(0.0) {_ max _} map {maxRel =>
                println(s"Max relevance: $maxRel")
              }

            case "top" =>
              // Simple static content

              // TODO: Needs to create and return future here ??

              RenderedContent("text", content.asInstanceOf[String])

            case "remoteContent" =>
              // This content is loaded async from remote repository

              // TODO: Needs to create and return future here ??

              RenderedContent("text", content.asInstanceOf[String])


          }
      }

      val getFuture = location.repository.get(location.path)
      getFuture map handlerFuture("") pipeTo sender
  }
}


/**
 * This is test prototype for Repository/Cursor/Presenter architecture in Octopus
 *
 */

object AkkaTest extends App {

  val system = ActorSystem("akkaTest")



}
