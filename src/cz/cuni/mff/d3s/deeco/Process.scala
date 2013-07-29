package cz.cuni.mff.d3s.deeco

import akka.actor.{ Actor, ActorRef, FSM }
import scala.concurrent.duration._

/** Associated Process definitions. */
object Process {	
    // The states of the Process FSM
	sealed trait State
	case object Idle extends State
	case object WaitingForKnowledge extends State
    
	sealed trait Data
}

/**
 * The actor representing a process.
 * 
 * Receives: Scheduler.Tick, Knowledge.Response
 * Sends:    Knowledge.Request, Knowledge.Update
 * 
 * FSM:
 * - Idle: 
 *   Inactive, waits for Scheduler.Tick, after that sends Knowledge.Request for the input knowledge, 
 *   Then goto WaitingForKnowledge
 * - WaitingForKnowledge: 
 *   Waits for the knowledge repository to send the corresponding input knowledge.
 *   After getting the Knowledge.Response with the input knowledge, invokes the 
 *   process's body and sends the output as Knowledge.Update
 *   Then goto Idle 
 *   
 * @author Jaroslav Keznikl <keznikl@d3s.mff.cuni.cz>
 */
class Process(
    /** An actor representing the knowledge repository */
    val kRepository: ActorRef, 
    /** IDs of the input knowledge */
    val in: List[String], 
    /** IDs of the output knowledge */
    val out: List[String],
    /** The process's body */
    val fn: Map[String, AnyVal] => Map[String, AnyVal]
  ) extends Actor with FSM[Process.State, Process.Data] {
  
  case object Empty extends Process.Data
  
  startWith(Process.Idle, Empty)

  when(Process.Idle) {
    case Event(Scheduler.Tick, _) =>
      log.info("Got Scheduler.Tick")
      
      kRepository ! new Knowledge.Request(in)
      
      log.info("Sent Knowledge.Request requesting " + in.mkString)
      
      goto(Process.WaitingForKnowledge)
  }
  
  when(Process.WaitingForKnowledge) {
    case Event(Knowledge.Response(knowledge), _) =>
      log.info("Got Knowledge.Response with " + knowledge.mkString)
      
      val result = fn(knowledge.filterKeys(in.contains)).filterKeys(out.contains);      
      kRepository ! new Knowledge.Update(result)
      
      log.info("Sent Knowledge.Update with " + result.mkString)
      
      goto(Process.Idle)
  }

  initialize()

}