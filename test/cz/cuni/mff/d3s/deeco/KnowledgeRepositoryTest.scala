package cz.cuni.mff.d3s.deeco

import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.BeforeAndAfterAll
import akka.testkit.ImplicitSender
import org.scalatest.WordSpec
import akka.testkit.TestFSMRef
import org.scalatest.matchers.ShouldMatchers
import akka.testkit.TestActorRef

class KnowledgeRepositoryTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpec with ShouldMatchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("KnowledgeRepositoryTest"))

  "KnowledgeRepository" should {

    "return Knowledge.Response when sent Knowledge.Request" in {
      val s = TestActorRef(new KnowledgeRepository(Map()))
      s ! Knowledge.Request(List())
      expectMsg(Knowledge.Response(Map()))
    }
    
    "return values of the requested knowledge when sent Knowledge.Request" in {
      val s = TestActorRef(new KnowledgeRepository(Map("1" -> 1, "2" -> 2)))
      s ! Knowledge.Request(List("1"))
      expectMsg(Knowledge.Response(Map("1" -> 1)))
    }
    
    "update its knowledge if sent a Knowledge.Update" in {
      val s = TestActorRef(new KnowledgeRepository(Map("1" -> 1, "2" -> 2)))
      s ! Knowledge.Update(Map("1" -> 3, "4" -> 4))
      s.underlyingActor.knowledge should be (Map("1" -> 3, "2" -> 2, "4" -> 4))
    }
  }
  
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
}
 
 