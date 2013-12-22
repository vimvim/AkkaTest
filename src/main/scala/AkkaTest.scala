import akka.actor.{ActorRef, Props, Actor, ActorSystem}
import akka.pattern.ask
import akka.pattern.{ after, ask, pipe }
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.Future

/****************************************************************************************************/

sealed class RenderResponse()

case class RenderedContent(label:String, mimeType:String, content:String) extends RenderResponse

case class RenderTimeout(label:String) extends RenderResponse

/****************************************************************************************************/

class Content {




}

/****************************************************************************************************/

sealed class Response

case class RedirectResponse(repository:Repository, path:String) extends Response

case class ContentResponse[T](kind:String, content:T) extends Response

/****************************************************************************************************/

trait Repository {

  // TODO: if already complete future is not will be dispatched to another thread call but runned immedeatellty
  // TODO: than we can use Future in other case needs to use Actor
  def get(path:String):Future[Response]
}

case class Location(repository:Repository, path:String)

/****************************************************************************************************/

class RemoteRepository extends Repository {

  // TODO: We may needs to define implicit "dispatcher" here.
  // TODO: This will be usefull because this repository will needs to create future
  def get(path: String): Future[Response] = {

    path match {
      case "id:00000000" =>
    }
  }
}

class OctopusRepository extends Repository {

  def get(path:String):Future[Response] = {

    path match {
      case "/" =>
    }
  }
}

/**
 *
 * @param label       Content name in the parent container. Useful for identifying subcontent.
 * @param location
 */
case class PresentContent(label: String, location:Location)

class Presenter(presenterDispatcher:ActorRef) extends Actor {

  def receive: Actor.Receive = {

    case PresentContent(label, location) =>

      // Capture sender val
      // val sender = this.sender

      def handlerFuture(label:String)(response:Response):Future[RenderResponse] = {

        case RedirectResponse(repository, url) => location.repository.get(url) flatMap handlerFuture(label)

        case ContentResponse(kind, content) =>

          // TODO: We needs to move this part into content rendered.
          // Content renderer will be determined by content kind ( and may be content class )
          // Renderer will return future.

          kind match {

            case "compound" =>
              // This is "/"
              // Compound content. Needs to ask presenters to provide another parts.

              val subContent = Map[String, String](
                "top"->"/top",
                "content"->"/content"
              )

              val futures = subContent.foldLeft(List[Future[RenderResponse]]()) {(list, entry)=>

                // For every operation we needs to have single future ith will group another two together
                // first is the rendering future and second is timeout fallback future
                // THINK ABOUT SETUP ANOTHER MAPPING FOR RENDERING FUTURE IN THE FALLBACK TIMEOUT FUTURE.
                // WE CAN ADD SOME CODE WHICH WILL HANDLE RESULT AND SEND IT ASYNC ( OVER WEBSOCKET ) TO BROWSER
                // AFTER PAGE IS RENDERED.

                // TODO: How we can grab result of the late future ??

                // https://github.com/twitter/util/blob/master/util-core/src/main/scala/com/twitter/util/Future.scala#L331-336
                // https://gist.github.com/viktorklang/4488970

                // Think about create Promise for answer from presenter future. Handler for answer will setup value for promise.
                // In the timeout we can check status of the Promise or associated future and if it is not completed than
                // setup handler for it which will grab result and send it to the AsyncRenderingCollector actor which will
                // handle rendered content ( store or send to the websocket ).

                list :+ (Future firstCompletedOf Seq(
                  presenterDispatcher ? PresentContent(entry._1, Location(location.repository, entry._2)),
                  after(FiniteDuration(3, "seconds"), context.system.scheduler) {
                    Future successful RenderTimeout
                  }
                )).mapTo[RenderResponse]
              }

              // And finally we will put rendered subcontent into cells inside parent layout
              Future.fold(futures)(Map[String, String]()) {(contentMap, response:RenderResponse)=>

                response match {
                  case RenderedContent(subContentLabel, mimeType, data) => contentMap ++ Map((subContentLabel, data))
                  case RenderTimeout(subContentLabel) => contentMap ++ Map((subContentLabel, "Content rendering timeout"))
                }

              } map {contentMap =>
                // Render all gathered content

                RenderedContent(label, "text", contentMap.foldLeft(""){(data, entry)=>

                  val contentLabel = entry._1
                  val contentData = entry._2

                  data.concat(s"<div id='$contentLabel'>$contentData</div>")
                })
              }

            case "simple" =>
              // Simple static content

              Future successful RenderedContent(label, "text", content.asInstanceOf[String])

          }
      }

      val getFuture = location.repository.get(location.path)
      getFuture map handlerFuture(label) pipeTo sender
  }
}


/**
 * This is test prototype for Repository/Cursor/Presenter architecture in Octopus
 *
 */

object AkkaTest extends App {

  val system = ActorSystem("akkaTest")



}
