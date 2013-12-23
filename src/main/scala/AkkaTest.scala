
import scala._
import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, Promise, Future}

import akka.actor._
import akka.pattern.{ after, ask, pipe }
import akka.routing.RoundRobinRouter
import akka.util.Timeout


/****************************************************************************************************/

/**
 * Helper object for tracing execution times and logging
 *
 */
object Tracer {

  var logging = false

  var now:Long = 0

  def start(logging:Boolean) = {
    this.now = System.nanoTime
    this.logging = logging
  }

  def log(message:String) = {

    if (logging) {

      val micros = (System.nanoTime - now) / 1000
      val millis = micros / 1000
      // println("%d microseconds".format(micros))
      // println("%d milliseconds".format(millis))

      println("%d: %s".format(micros, message))
    }
  }

  def finish() = {

    val micros = (System.nanoTime - now) / 1000
    val millis = micros / 1000
    println("%d microseconds".format(micros))
    println("%d milliseconds".format(millis))
  }
}


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

  def get(path:String)(implicit akkaSystem:ActorSystem):Either[Response, Future[Response]]
}

case class Location(repository:Repository, path:String)

/****************************************************************************************************/

/**
 * This is modeled async repository which is manage async content loading ( GDrive for example )
 *
 */
class RemoteRepository extends Repository {

  def get(path: String)(implicit akkaSystem:ActorSystem): Either[Response, Future[Response]] = {

    path match {

      case "id:00000000" =>

        import akkaSystem.dispatcher

        Right(after(FiniteDuration(1, "seconds"), akkaSystem.scheduler) {
          Future successful ContentResponse("",
            Content(
              Map[String, String](),
              "Remote content"
            )
          )
        })

      case "id:00000002" =>
        // This content will lead to timeout

        import akkaSystem.dispatcher

        Right(after(FiniteDuration(10, "seconds"), akkaSystem.scheduler) {
          Future successful ContentResponse("",
            Content(
              Map[String, String](),
              "Remote content"
            )
          )
        })

    }
  }
}

/**
 * This is modeled simple sync repository
 *
 */
class OctopusRepository extends Repository {

  def get(path: String)(implicit akkaSystem: ActorSystem): Either[Response, Future[Response]] = {

    path match {

      case "/" =>

        Tracer.log(s"Repository: Send content for $path")

        Left(
          ContentResponse("",
            Content(
              Map[String, String](
                "top" -> "/top",
                "content" -> "/content",
                "content2" -> "/content2"
              ),
              "Compound content"
            )
          ))

      case "/top" =>

        Tracer.log(s"Repository: Send content for $path")

        Left(
          ContentResponse("",
            Content(
              Map[String, String](),
              "Top content"
            )
          ))

      case "/content" =>

        Tracer.log(s"Repository: Send content for $path")

        Left(RedirectResponse(new RemoteRepository(), "id:00000000"))

      case "/content2" =>

        Tracer.log(s"Repository: Send content for $path")

        Left(RedirectResponse(new RemoteRepository(), "id:00000002"))

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

      Tracer.log(s"$label: Present content")

      // Capture sender val
      // val sender = this.sender

      import context.system
      import context.dispatcher

      renderContent(label, location.repository, location.path) match {
        case Left(renderedContent) => sender ! renderedContent
        case Right(future) => future pipeTo sender
      }
  }

  private def renderContent(label: String, repository:Repository, path:String):Either[RenderedContent, Future[RenderedContent]] = {

    import context.system
    import context.dispatcher

    getContent(label, repository, path) match {

      case Left(contentResponse) => render(repository, label, "", contentResponse.content.asInstanceOf[Content])

      case Right(getFuture) => Right(getFuture flatMap {
        contentResponse=>

        render(repository, label, "", contentResponse.content.asInstanceOf[Content]) match {
          case Left(renderedContent) => Future.successful[RenderedContent](renderedContent)
          case Right(renderFuture) => renderFuture
        }
      })
    }
  }

  private def getContent(label: String, repository:Repository, path:String): Either[ContentResponse[_], Future[ContentResponse[_]]] = {

    import context.system
    import context.dispatcher

    def handleAnswer(answer: Either[Response, Future[Response]]):Either[ContentResponse[_], Future[ContentResponse[_]]] = {

      answer match {

        case Left(response) =>

          response match {
            case redirectResponse:RedirectResponse => handleAnswer(redirectResponse.repository.get(redirectResponse.path))
            case contentResponse:ContentResponse[_] => Left(contentResponse)
          }

        case Right(future) =>

          // TODO: There will be problem if content repository will return Future[RedirectResponse]]

          Right(future.asInstanceOf[Future[ContentResponse[_]]])
      }
    }

    handleAnswer(repository.get(path))
  }

  /****************************************************************************************************************/


  /*
  private def responseHandler(label: String, repository:Repository)(response: Response): Future[RenderResponse] = {

    import context.system
    import context.dispatcher

    response match {

      case RedirectResponse(redirectRepository, url) =>

        Tracer.log(s"$label: Redirect response received: $url")

        redirectRepository.get(url) match {

          case Left(redirectResponse) =>
            // We not have support several redirects yet, so expect to got ContentResponse here.

            val contentResponse = redirectResponse.asInstanceOf[ContentResponse[Content]]

            render(repository, label, contentResponse.kind, contentResponse.content) match {
              case Left(renderedContent) => Future.successful(renderedContent)
              case Right(future) => future
            }

          case Right(future) => future flatMap responseHandler(label, redirectRepository)
        }

      case ContentResponse(kind, content: Content) =>

        Tracer.log(s"$label: Content response received")

        render(repository, label, kind, content) match {
          case Left(renderedContent) => Future.successful(renderedContent)
          case Right(future) => future
        }
    }
  }
  */

  private def render(repository: Repository, label: String, kind: String, content: Content): Either[RenderedContent, Future[RenderedContent]] = {

    // TODO: We needs to move this part into content rendered.
    // Content renderer will be determined by content kind ( and may be content class )
    // Renderer will return future.

    if (content.subContent.size>0) {

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
          implicit val timeout = Timeout(60 seconds)

          val subContentLabel = entry._1
          val subContentUrl = entry._2

          Tracer.log(s"Request subcontent:$subContentLabel url:$subContentUrl")

          list :+ (Future firstCompletedOf Seq(
            DI.presenterDispatcher ? PresentContent(subContentLabel, Location(repository, subContentUrl)),
            after(FiniteDuration(3, "seconds"), context.system.scheduler) {
              Future successful RenderTimeout(subContentLabel)
            }
          )).mapTo[RenderResponse]
      }

      import context.system
      import context.dispatcher

      // And finally we will put rendered subcontent into cells inside parent layout
      Right(Future.fold(futures)(Map[String, String]()) {
        (contentMap, response: RenderResponse) =>

          Tracer.log(s"$label: Got response for subcontent $response")

          response match {
            case RenderedContent(subContentLabel, mimeType, data) => contentMap ++ Map((subContentLabel, data))
            case RenderTimeout(subContentLabel) => contentMap ++ Map((subContentLabel, "Content rendering timeout"))
          }

      } map {
        contentMap =>
        // Render all gathered content ( will render template is the real system here )
          renderFlat(label, content, contentMap)
      })

    } else {

      Left(renderFlat(label: String, content:Content, Map[String,String]()))
    }
  }

  private def renderFlat(label: String, content:Content, subContentMap:Map[String,String]):RenderedContent = {

    Tracer.log(s"$label: Final rendering")

    RenderedContent(label, "text", subContentMap.foldLeft(content.content) {
      (data, entry) =>

        val contentLabel = entry._1
        val contentData = entry._2

        data.concat(s"<div id='$contentLabel'>$contentData</div>")
    })
  }


}


/**
 * This is test prototype for Repository/Cursor/Presenter architecture in Octopus
 *
 */

object AkkaTest extends App {

  def time[A](a: => A) = {
    val now = System.nanoTime
    val result = a
    val micros = (System.nanoTime - now) / 1000
    val millis = micros / 1000
    println("%d microseconds".format(micros))
    println("%d milliseconds".format(millis))
    result
  }

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

  for(i <- 1 to 10) {

    Tracer.start(true)

    val result = Await.result(presentersRouter ? PresentContent("root", Location(new OctopusRepository(), "/")), 300 seconds)

    Tracer.finish()

    println(s"Got final result $result ")
  }

  // presentersRouter ? PresentContent("root", Location(new OctopusRepository(), "/")) onComplete(result=> {
  //  result
  // })

}
