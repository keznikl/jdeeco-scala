package cz.cuni.mff.d3s.deeco

import akka.actor.{ Actor, ActorRef, FSM }
import scala.concurrent.duration._
import akka.event.Logging
import scala.collection.script.Update

/** Associated KnowledgeRepository definitions. */
object Knowledge {
  // Messages used for interacting with the KnowledgeRepository actor
  case class Request(val knowledge: List[String])
  case class Response(val knowledge: Map[String, AnyVal])
  case class Update(val knowledge: Map[String, AnyVal])
}

/**
 * The actor representing the knowledge repository.
 * 
 * Receives: Knowledge.Request, Knowledge.Update
 * Sends:    Knowledge.Response
 * 
 * @author Jaroslav Keznikl <keznikl@d3s.mff.cuni.cz>
 */
class KnowledgeRepository(
    /** The contents of the knowledge repository. */
    var knowledge: Map[String, AnyVal]
  ) extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case r: Knowledge.Request =>
      log.info("Got Knowledge.Request for " + r.knowledge.mkString)

      val v = knowledge.filterKeys(r.knowledge.contains);
      sender ! Knowledge.Response(v)

      log.info("Sent Knowledge.Response with " + v.mkString)

    case u: Knowledge.Update =>
      log.info("Got Knowledge.Update with " + u.knowledge.mkString)
      knowledge = knowledge ++ u.knowledge;
  }
}