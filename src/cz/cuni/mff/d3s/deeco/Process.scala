package cz.cuni.mff.d3s.deeco

import akka.actor.{ Actor, ActorRef, FSM }
import scala.concurrent.duration._


object Process {	
	sealed trait State
	sealed trait Data
}


class Process(
    val kRepository: ActorRef, 
    val in: List[String], 
    val out: List[String], 
    val fn: Map[String, AnyVal] => Map[String, AnyVal]
    ) extends Actor with FSM[Process.State, Process.Data] {
  
  private case object Idle extends Process.State
  private case object WaitingForKnowledge extends Process.State

  private case object Empty extends Process.Data  
    
  startWith(Idle, Empty)

  when(Idle) {
    case Event(Scheduler.Tick, _) =>
      log.info("Got Scheduler.Tick")
      kRepository ! new Knowledge.Request(in)
      goto(WaitingForKnowledge)
  }
  
  when(WaitingForKnowledge) {
    case Event(Knowledge.Response(knowledge), _) =>
      log.info("Got Knowledge.Response")
      kRepository ! new Knowledge.Update(fn(knowledge))
      goto(Idle)
  }

  initialize()

}