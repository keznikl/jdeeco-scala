package cz.cuni.mff.d3s.deeco

import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.BeforeAndAfterAll
import akka.testkit.ImplicitSender
import org.scalatest.WordSpec
import akka.testkit.TestFSMRef
import org.scalatest.matchers.ShouldMatchers

class SchedulerTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpec with ShouldMatchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("SchedulerTest"))

  "Scheduler" should {

    "start when started and stop when stopped" in {
      val s = TestFSMRef(new Scheduler(List()))
      s ! Scheduler.Start
      s.stateName should be(Scheduler.Active)
      s ! Scheduler.Stop
      s.stateName should be(Scheduler.Idle)
    }

    "activate a timer for each process after started and deactivate afte it's stopped" in {
      val s = TestFSMRef(new Scheduler(List(new ProcessInfo("p", testActor, 100))))
      s ! Scheduler.Start
      assert(s.isTimerActive("tickp"))
      s ! Scheduler.Stop
      assert(!s.isTimerActive("tickp"))
    }

    "send a Tick message to the process Actor within 2x the process's period when started" in {
      val s = TestFSMRef(new Scheduler(List(new ProcessInfo("p", testActor, 100))))
      within(200 millis) {
        s ! Scheduler.Start
        expectMsg(Scheduler.Tick)
      }
      within(200 millis) {
        s ! Scheduler.Stop
        expectNoMsg
      }
    }

  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
}
 
 