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
import cz.cuni.mff.d3s.deeco.api.KnowledgeRepository

class ExecutorActorImpl(
  val krActor: ActorRef,
  val actorSystem: ActorSystem,
  val processActors: Map[String, ActorRef]) extends Executor {

  def this(kr: KnowledgeRepository, actorSystem: ActorSystem) = this(
      // TODO: make the KR actor use the Java KR interface internally
      actorSystem.actorOf(Props(
        classOf[cz.cuni.mff.d3s.deeco.KnowledgeRepository],
        Map()), "KnowledgeRepository"),
      actorSystem,
      Map())

  def execute(pi: ProcessInfo) {
    // create a new actor for a process not yet executed
    if (!processActors.contains(pi.name)) {
      val a = actorSystem.actorOf(Props(
        classOf[Process],
        krActor,
        List(),
        List(),
        identity _), "executor-" + pi.name)
      processActors(pi.name) = a;
    }
    processActors(pi.name) ! Scheduler.Tick;
  }
}