package cz.cuni.mff.d3s.deeco

import akka.actor.{ Actor, ActorRef, FSM }
import scala.concurrent.duration._
import akka.event.Logging
import scala.collection.script.Update

object Knowledge {
case class Request(val knowledge: List[String])
case class Response(val knowledge: Map[String, AnyVal])
case class Update(val knowledge: Map[String, AnyVal])
}

class KnowledgeRepository(var knowledge: Map[String, AnyVal]) extends Actor {
  val log = Logging(context.system, this)
  
  def receive = {
    case r: Knowledge.Request => 
      log.info("For Knowledge.Request for :" + r.knowledge.mkString)
      sender ! Knowledge.Response(knowledge.filterKeys(r.knowledge.contains))
      
    case u: Knowledge.Update => 
      log.info("For Knowledge.Update with :" + u.knowledge.mkString)
      knowledge = knowledge ++ u.knowledge;
  }
}