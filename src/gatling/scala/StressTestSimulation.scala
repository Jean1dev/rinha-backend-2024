import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class StressTestSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:9999")
    .header("X-Tenant", "tenant2")
    .header("Cookie", "JSESSIONID=20D45B27B5A11F727EC88EE9D8D41CDA")

  val scn = scenario("Stress Test")
    .exec(http("request")
      .get("/clientes/2/extrato"))

  setUp(
    scn.inject(
      rampUsersPerSec(10) to 100 during (1 minute), // Aumenta o número de usuários gradualmente de 10 para 100 durante 1 minuto
      constantUsersPerSec(100) during (2 minutes) // Mantém 100 usuários por segundo durante 5 minutos
    )
  ).protocols(httpProtocol)
}