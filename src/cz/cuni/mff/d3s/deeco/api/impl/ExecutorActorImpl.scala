package cz.cuni.mff.d3s.deeco.api.impl

import cz.cuni.mff.d3s.deeco.api.Executor
import cz.cuni.mff.d3s.deeco.ProcessInfo
import cz.cuni.mff.d3s.deeco.Process
import akka.actor.ActorDSL._
import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.actor.Props
import scala.collection.mutable.Map
import cz.cuni.mff.d3s.deeco.Scheduler

class ExecutorActorImpl(
    val krActor: ActorRef,
    val actorSystem: ActorSystem,
    val processActors: Map[String, ActorRef]
    ) extends Executor {
	
  def execute(pi: ProcessInfo) {
	  if (!processActors.contains(pi.name)) {
	    val a = actorSystem.actorOf(Props(
	    		classOf[Process],
	    		krActor,
	    		List(),
	    		List(),
	    		identity _))
	    processActors(pi.name) = a;
	  }
	  processActors(pi.name) ! Scheduler.Tick;
	}
}