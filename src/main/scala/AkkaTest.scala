
import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, Promise, Future}

import akka.actor._
import akka.pattern.{ after, ask, pipe }
import akka.routing.RoundRobinRouter
import akka.util.Timeout

/****************************************************************************************************/

sealed class RenderResponse()

case class RenderedContent(label:String, mimeType:String, content:String) extends RenderResponse

case class RenderTimeout(label:String) extends RenderResponse

/****************************************************************************************************/

/**
 * For simplicity we assume tha all repositories will return content of the same type.
 *
 * @param subContent      References to the subcontent
 * @param content         Text content
 */
case class Content(subContent:Map[String, String], content:String)

/****************************************************************************************************/

sealed class Response

case class RedirectResponse(repository:Repository, path:String) extends Response

case class ContentResponse[T](kind:String, content:T) extends Response

/****************************************************************************************************/

trait Repository {

  // TODO: if already complete future is not will be dispatched to another thread call but runned immedeatellty
  // TODO: than we can use Future in other case needs to use Actor
  def get(path:String)(implicit akkaSystem:ActorSystem):Future[Response]
}

case class Location(repository:Repository, path:String)

/****************************************************************************************************/

/**
 * This is modeled async repository which is manage async content loading ( GDrive for example )
 *
 */
class RemoteRepository extends Repository {

  // TODO: We may needs to define implicit "dispatcher" here.
  // TODO: This will be usefull because this repository will needs to create future
  def get(path: String)(implicit akkaSystem:ActorSystem): Future[Response] = {

    path match {

      case "id:00000000" =>

        import akkaSystem.dispatcher

        after(FiniteDuration(1, "seconds"), akkaSystem.scheduler) {
          Future successful ContentResponse("",
            Content(
              Map[String, String](),
              "Remote content"
            )
          )
        }

    }
  }
}

/**
 * This is modeled simple sync repository
 *
 */
class OctopusRepository extends Repository {

  def get(path:String)(implicit akkaSystem:ActorSystem):Future[Response] = {

    path match {

      case "/" => Future.successful(
        ContentResponse("",
          Content(
            Map[String, String](
              "top"->"/top",
              "content"->"/content"
            ),
            "Compound content"
          )
        ))

      case "/top" => Future.successful(
        ContentResponse("",
          Content(
            Map[String, String](),
            "Top content"
          )
        ))

      case "/content" => Future.successful(RedirectResponse(new RemoteRepository(), "id:00000000"))

    }
  }
}

/****************************************************************************************************/

/**
 * Simple DI holder for testing purposes
 */
object DI {
  var presenterDispatcher:ActorRef = null
}


/****************************************************************************************************/

/**
 * Ask presenter to present ( render ) content
 *
 * @param label       Content name in the parent container. Useful for identifying subcontent.
 * @param location    Content location
 */
case class PresentContent(label: String, location:Location)

/****************************************************************************************************/

class Presenter extends Actor {

  def receive: Actor.Receive = {

    case PresentContent(label, location) =>

      println(s"$label: Present content")

      // Capture sender val
      // val sender = this.sender

      import context.system
      import context.dispatcher

      location.repository.get(location.path) flatMap responseHandler(label, location.repository) pipeTo sender
  }

  private def responseHandler(label: String, repository:Repository)(response: Response): Future[RenderResponse] = {

    import context.system
    import context.dispatcher

    response match {

      case RedirectResponse(redirectRepository, url) =>

        println(s"$label: Redirect response received: $url")

        redirectRepository.get(url) flatMap responseHandler(label, redirectRepository)

      case ContentResponse(kind, content: Content) =>

        println(s"$label: Content response received")

        // TODO: We needs to move this part into content rendered.
        // Content renderer will be determined by content kind ( and may be content class )
        // Renderer will return future.

        val futures = content.subContent.foldLeft(List[Future[RenderResponse]]()) {
          (list, entry) =>

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

          // Another possible implementation - always send rendering results to the special actor, also send
          // timeout message. If timeout message will arrive before content message - content will be scheduled
          // for delivery to the client over websocket.

            import context.dispatcher
            implicit val timeout = Timeout(4 seconds)

            val subContentLabel = entry._1
            val subContentUrl = entry._2

            println(s"Request subcontent:$subContentLabel url:$subContentUrl")

            list :+ (Future firstCompletedOf Seq(
              DI.presenterDispatcher ? PresentContent(subContentLabel, Location(repository, subContentUrl)),
              after(FiniteDuration(3, "seconds"), context.system.scheduler) {
                Future successful RenderTimeout
              }
            )).mapTo[RenderResponse]
        }

        // And finally we will put rendered subcontent into cells inside parent layout
        Future.fold(futures)(Map[String, String]()) {
          (contentMap, response: RenderResponse) =>

            println(s"$label: Got response for subcontent $response")

            response match {
              case RenderedContent(subContentLabel, mimeType, data) => contentMap ++ Map((subContentLabel, data))
              case RenderTimeout(subContentLabel) => contentMap ++ Map((subContentLabel, "Content rendering timeout"))
            }

        } map {
          contentMap =>
          // Render all gathered content ( will render template is the real system here )

            println(s"$label: Final rendering")

            RenderedContent(label, "text", contentMap.foldLeft(content.content) {
              (data, entry) =>

                val contentLabel = entry._1
                val contentData = entry._2

                data.concat(s"<div id='$contentLabel'>$contentData</div>")
            })
        }
    }
  }
}


/**
 * This is test prototype for Repository/Cursor/Presenter architecture in Octopus
 *
 */

object AkkaTest extends App {

  val system = ActorSystem("akkaTest")

  val presentersRouter:ActorRef = system.actorOf(Props.empty.withRouter(RoundRobinRouter(routees = List[ActorRef](
    system.actorOf(Props(classOf[Presenter])),
    system.actorOf(Props(classOf[Presenter])),
    system.actorOf(Props(classOf[Presenter])),
    system.actorOf(Props(classOf[Presenter]))
  ))))

  DI.presenterDispatcher = presentersRouter

  implicit val timeout = Timeout(300 seconds)
  import system.dispatcher

  val result = Await.result(presentersRouter ? PresentContent("root", Location(new OctopusRepository(), "/")), 300 seconds)

  println(s"Got final result $result ")


  // presentersRouter ? PresentContent("root", Location(new OctopusRepository(), "/")) onComplete(result=> {
  //  result
  // })

}
