package cz.cuni.mff.d3s.deeco

import akka.actor.{ Actor, ActorRef, FSM }
import scala.concurrent.duration._

/**
 * A class representing the information about a process as required by the scheduler.
 */
class ProcessInfo(
    /** Name of the process. */
    val name: String, 
    /** Ref. to the process's actor. */
    val ref: ActorRef, 
    /** Period of the process in miliseconds. */
    val period: Int)

/** Associated Scheduler definitions. */
object Scheduler {
  // The states of the Scheduler FSM
  sealed trait State
  case object Idle extends State
  case object Active extends State

  sealed trait Data
  
  // Messages used for interacting with the Scheduler actor
  case object Start
  case object Stop
  case object Tick
  case class Schedule(val pi: ProcessInfo)
}

/**
 * The actor representing the process scheduler.
 * 
 * Receives: Scheduler.Start, Scheduler.Stop
 * Sends:    Scheduler.Tick
 * 
 * FSM:
 * - Idle: 
 *   Inactive, waits for Scheduler.Start, after that sets a timer for each registered process. 
 *   Then goto Active
 * - Active: 
 *   Processes time notifications and sends Scheduler.Tick to the corresponding process actors.
 *   After receiving Scheduler.Stop, stop all timers and goto Idle 
 *   
 * @author Jaroslav Keznikl <keznikl@d3s.mff.cuni.cz>
 */
class Scheduler(
    /** The processes to be scheduled */
    var processes: List[ProcessInfo]
  ) extends Actor with FSM[Scheduler.State, Scheduler.Data] {

  
  protected case object Empty extends Scheduler.Data  
  protected case class Notify(process: ActorRef)

  
  startWith(Scheduler.Idle, Empty)

  when(Scheduler.Idle) {
    case Event(Scheduler.Start, _) => 
      goto(Scheduler.Active)
    case Event(Scheduler.Schedule(pi: ProcessInfo), _) =>
      processes ::= pi    
      stay
  }
  when(Scheduler.Active) {
    case Event(Scheduler.Stop, _) => 
      goto(Scheduler.Idle)
    case Event(Notify(process: ActorRef), _) => 
      process ! Scheduler.Tick 
      stay 
    case Event(Scheduler.Schedule(pi: ProcessInfo), _) =>
      processes ::= pi      
      startTimerForProcess(pi)
      stay
  }
  
  onTransition {
    case Scheduler.Idle -> Scheduler.Active => 
      processes.foreach(startTimerForProcess)
    case Scheduler.Active -> Scheduler.Idle => 
      processes.foreach(cancelTimerForProcess)
    case x -> Scheduler.Idle => 
      log.info("entering Idle from " + x)
  }
  
  initialize()
  
  def startTimerForProcess(p: ProcessInfo) = {
    setTimer("tick" + p.name, Notify(p.ref), Duration(p.period, MILLISECONDS), true)
  }
  
  def cancelTimerForProcess(p: ProcessInfo) = {
    cancelTimer("tick" + p.name)
  }

}