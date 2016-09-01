package toguru.toggles

import akka.actor.Props
import akka.pattern.ask
import toguru.toggles.ToggleActor._

class ToggleActorSpec extends ActorSpec {

  trait ToggleActorSetup {
    def createActor(id: String, toggle: Option[Toggle] = None) = system.actorOf(Props(new ToggleActor(id, toggle)))
    val toggle        = Toggle("id", "name","description")
    val createCommand = CreateToggleCommand("name", "toggle description", Map("team" -> "Shared Services"))
    val createGlobalRolloutCommand = CreateGlobalRolloutConditionCommand(42)
  }

  "actor" should {
    "create toggle when receiving command in initial state" in new ToggleActorSetup {
      val actor = createActor("toggle-1")
      val success = await((actor ? createCommand).mapTo[CreateSucceeded])
      success.id mustBe "toggle-1"
    }

    "reject create command when toggle exists" in new ToggleActorSetup {
      val actor = createActor("toggle-2", Some(toggle))
      val alreadyExists = await((actor ? createCommand).mapTo[ToggleAlreadyExists])
      alreadyExists.id mustBe "toggle-2"
    }

    "create global rollout condition when receiving command" in new ToggleActorSetup {
      val actor = createActor("toggle-3", Some(toggle))
      val result = await(actor ? createGlobalRolloutCommand)
      result mustBe Success
    }

    "reject global rollout condition command when toggle does not exists" in new ToggleActorSetup {
      val actor = createActor("toggle-4")
      val doesNotExist = await((actor ? createGlobalRolloutCommand).mapTo[ToggleDoesNotExist])
      doesNotExist.id mustBe "toggle-4"
    }
  }
}
