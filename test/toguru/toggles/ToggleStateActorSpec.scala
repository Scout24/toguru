package toguru.toggles

import akka.pattern.ask
import akka.actor.{ActorRef, Props}
import toguru.toggles.ToggleStateActor.GetState
import toguru.events.toggles._

class ToggleStateActorSpec extends ActorSpec {

  def createActor(toggles: Map[String, ToggleState] = Map.empty): ActorRef =
    system.actorOf(Props(new ToggleStateActor((_,_) => (), toggles)))

  val toggles = Map(
    "toggle-1" -> ToggleState("toggle-1", Map("team" -> "Toguru team")),
    "toggle-2" -> ToggleState("toggle-2", rolloutPercentage = Some(20))
  )

  "toggle state actor" should {
    "return current toggle state" in {
      val actor = createActor(toggles)

      val response = await(actor ? GetState)

      response mustBe toggles
    }

    "build toggle state from events" in {
      val actor = createActor()

      val id1 = "toggle-1"
      val id2 = "toggle-2"
      val id3 = "toggle-3"

      actor ! (id1, ToggleCreated("name", "description", Map("team" -> "Toguru team")))
      actor ! (id1, GlobalRolloutCreated(10))
      actor ! (id1, GlobalRolloutDeleted())

      actor ! (id2, ToggleCreated("name", "", Map.empty))
      actor ! (id2, ToggleUpdated("name", "description", Map.empty))
      actor ! (id2, GlobalRolloutCreated(10))
      actor ! (id2, GlobalRolloutUpdated(20))

      actor ! (id3, ToggleCreated("name", "description", Map.empty))
      actor ! (id3, ToggleDeleted())

      val response = await(actor ? GetState)

      response mustBe toggles
    }
  }
}
