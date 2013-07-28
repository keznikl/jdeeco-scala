package cz.cuni.mff.d3s.deeco

import akka.actor.{ Actor, ActorRef, FSM }
import scala.concurrent.duration._


class ProcessInfo(val name: String, val ref: ActorRef, val period: Int)

object Scheduler {	
    sealed trait State
    case object Idle extends Scheduler.State
    case object Active extends Scheduler.State
  
	sealed trait Data

	case object Start
	case object Stop
	case object Tick
}

class Scheduler(var processes: List[ProcessInfo]) extends Actor with FSM[Scheduler.State, Scheduler.Data] {

  
  protected case object Empty extends Scheduler.Data  
  protected case class Notify(process: ActorRef)

  
  startWith(Scheduler.Idle, Empty)

  when(Scheduler.Idle) {
    case Event(Scheduler.Start, _) => goto(Scheduler.Active)
  }
  when(Scheduler.Active) {
    case Event(Scheduler.Stop, _) => 
      goto(Scheduler.Idle)
    case Event(Notify(process: ActorRef), _) => 
      process ! Scheduler.Tick 
      stay 
  }

  onTransition {
    case Scheduler.Idle -> Scheduler.Active => 
      processes.foreach(
          p => setTimer("tick" + p.name, Notify(p.ref), Duration(p.period, MILLISECONDS), true))
    case Scheduler.Active -> Scheduler.Idle => 
      processes.foreach(
          p => cancelTimer("tick" + p.name))
    case x -> Scheduler.Idle => 
      log.info("entering Idle from " + x)
  }
  initialize()

}