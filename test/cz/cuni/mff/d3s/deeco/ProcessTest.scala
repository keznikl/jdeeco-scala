package cz.cuni.mff.d3s.deeco

import scala.concurrent.duration._
import akka.actor.ActorDSL._
import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.BeforeAndAfterAll
import akka.testkit.ImplicitSender
import org.scalatest.WordSpec
import akka.testkit.TestFSMRef
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.BeforeAndAfter
import org.scalatest.BeforeAndAfterEach

class ProcessTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpec with ShouldMatchers with BeforeAndAfterAll  {

  val dummy = actor(new Act{})
  
  def this() = this(ActorSystem("ProcessTest"))

  "Process" should {

    "be in the Idle state when created" in {      
      val p = TestFSMRef(new Process(dummy, List(), List(), identity))
      p.stateName should be(Process.Idle)      
    }
    
    "be in the WaitingForKnowledge state after receiving the Scheduler.Tick message" in {
      val p = TestFSMRef(new Process(dummy, List(), List(), identity))
      p ! Scheduler.Tick
      p.stateName should be(Process.WaitingForKnowledge) 
    }
    
    "be in the Idle state after receiving the Knowledge.Response knowledge" in {
      val p = TestFSMRef(new Process(dummy, List(), List(), identity))
      p ! Knowledge.Response(Map())
      p.stateName should be(Process.Idle)      
    }
    
    "send a Knowledge.Request for the input knowledge when sent a Tick message" in {
      val p = TestFSMRef(new Process(testActor, List("1"), List(), identity))
      p ! Scheduler.Tick
      expectMsg(Knowledge.Request(List("1"))) 
    }
    
    "send a Knowledge.Update after receiving the Knowledge.Response in the WaitingForKnowledge state" in {
      val p = TestFSMRef(new Process(testActor, List("1"), List("1"), identity))
      p.setState(Process.WaitingForKnowledge)
      p ! Knowledge.Response(Map("1" -> 1))
      expectMsg(Knowledge.Update(Map("1" -> 1)))
    }
    
    "filter the Knowledge.Response and Knowledge.Update according to the input and output knowledge" in {
      val p = TestFSMRef(new Process(testActor, List("1", "2"), List("2"), identity))
      p.setState(Process.WaitingForKnowledge)
      p ! Knowledge.Response(Map("1" -> 1, "2" -> 2, "3" -> 3))
      expectMsg(Knowledge.Update(Map("2" -> 2)))
    }
    
    
  }
  
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
  
}
 
 