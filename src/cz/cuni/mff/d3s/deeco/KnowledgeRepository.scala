package cz.cuni.mff.d3s.deeco

import akka.actor.{ Actor, ActorRef, FSM }
import scala.concurrent.duration._
import akka.event.Logging

object Knowledge {
case class Request(val knowledge: List[String])
case class Response(val knowledge: Map[String, AnyVal])
case class Update(val knowledge: Map[String, AnyVal])
}

class KnowledgeRepository(var knowledge: Map[String, AnyVal]) extends Actor {
  val log = Logging(context.system, this)
  
  def receive = {
    case r: Knowledge.Request => log.info("For KnowledgeReques for :" + r.knowledge.mkString)
  }
}