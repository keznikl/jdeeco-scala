package cz.cuni.mff.d3s.deeco.api.impl

import cz.cuni.mff.d3s.deeco.api.Scheduler
import akka.actor.ActorRef
import cz.cuni.mff.d3s.deeco.ProcessInfo
import akka.actor.ActorSystem
import akka.actor.Props


class SchedulerActorImpl(val scheduler: ActorRef) extends Scheduler {
  
  def this(system: ActorSystem) = this(
      system.actorOf(Props(
        classOf[cz.cuni.mff.d3s.deeco.Scheduler],
        List()
        )))
  
  def start() = {
    scheduler ! cz.cuni.mff.d3s.deeco.Scheduler.Start;
  }
  
  def stop() = {
    scheduler ! cz.cuni.mff.d3s.deeco.Scheduler.Stop;
  }
  
  def schedule(pi: ProcessInfo) = {
    scheduler ! cz.cuni.mff.d3s.deeco.Scheduler.Schedule(pi)
  }

}