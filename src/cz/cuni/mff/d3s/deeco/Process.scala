package cz.cuni.mff.d3s.deeco

import akka.actor.{ Actor, ActorRef, FSM }
import scala.concurrent.duration._


object Process {	
	sealed trait State
	case object Idle extends Process.State
	case object WaitingForKnowledge extends Process.State
    
	sealed trait Data
	case object Empty extends Process.Data
}


class Process(
    val kRepository: ActorRef, 
    val in: List[String], 
    val out: List[String], 
    val fn: Map[String, AnyVal] => Map[String, AnyVal]
    ) extends Actor with FSM[Process.State, Process.Data] {
  
  startWith(Process.Idle, Process.Empty)

  when(Process.Idle) {
    case Event(Scheduler.Tick, _) =>
      log.info("Got Scheduler.Tick")
      kRepository ! new Knowledge.Request(in)
      goto(Process.WaitingForKnowledge)
  }
  
  when(Process.WaitingForKnowledge) {
    case Event(Knowledge.Response(knowledge), _) =>
      log.info("Got Knowledge.Response")
      kRepository ! new Knowledge.Update(fn(knowledge))
      goto(Process.Idle)
  }

  initialize()

}