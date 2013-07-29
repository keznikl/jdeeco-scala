package demo

import akka.actor.ActorSystem
import akka.actor.Props
import cz.cuni.mff.d3s.deeco.KnowledgeRepository
import cz.cuni.mff.d3s.deeco.Process
import cz.cuni.mff.d3s.deeco.Scheduler
import cz.cuni.mff.d3s.deeco.ProcessInfo

object LeaderFollower extends App {
  def leaderMoveFn(in: Map[String, AnyVal]): Map[String, AnyVal] = {
    if (in("leader.position") == in("leader.destination")) {
      return Map() // do nothing
  	} else {
      val newPos = (in("leader.position").asInstanceOf[Int] + 1)
      println("Leader: " + newPos);
      return Map("leader.position" -> newPos)
    }
  }

  def followerMoveFn(in: Map[String, AnyVal]): Map[String, AnyVal] = {
    if (in("follower.position").asInstanceOf[Int] == in("follower.leaderPosition").asInstanceOf[Int] - 1) {
      val newPos = in("follower.leaderPosition")
      println("Follower: " + newPos);
      return Map("follower.position" -> newPos)
    } else {
      return Map() // do nothing
    }
  }

  def ensembleFn(in: Map[String, AnyVal]): Map[String, AnyVal] = {
    if ((in("follower.position").asInstanceOf[Int] - in("leader.position").asInstanceOf[Int]).abs <= 1)
      return Map("follower.leaderPosition" -> in("leader.position"))
    else
      return Map(); // do nothing
  }

  val system = ActorSystem("LeaderFollower")

  val kr = system.actorOf(Props(
    classOf[KnowledgeRepository],
    Map(
      "leader.position" -> 1,
      "leader.destination" -> 6,
      "follower.position" -> 3,
      "follower.leaderPosition" -> 0)), "KnowledgeRepository")

  val leaderMove = system.actorOf(Props(
    classOf[Process],
    kr,
    List("leader.position", "leader.destination"),
    List("leader.position"),
    leaderMoveFn _), "Leader")

  val followerMove = system.actorOf(Props(
    classOf[Process],
    kr,
    List("follower.position", "follower.leaderPosition"),
    List("follower.position"),
    followerMoveFn _), "Follower")

  val knowledgeExchange = system.actorOf(Props(
    classOf[Process],
    kr,
    List("leader.position", "follower.position"),
    List("follower.leaderPosition"),
    ensembleFn _), "Ensemble")

  val scheduler = system.actorOf(Props(
    classOf[Scheduler],
    List(
      new ProcessInfo("leaderMove", leaderMove, 1000),
      new ProcessInfo("followerMove", followerMove, 1000),
      new ProcessInfo("knowledgeExchange", knowledgeExchange, 600)
      )), "Scheduler")

  scheduler ! Scheduler.Start

}