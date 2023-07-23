import org.http4k.cloudnative.asK8sServer
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Undertow

val routes = routes(
    "/" bind GET to { Response(OK) },
    "/404" bind GET to {Response(NOT_FOUND)}
)

fun main() {
    val server = ServerFilters
        .CatchAll()
        .then(ServerFilters.RequestTracing())
        .then(routes)
        .asK8sServer(::Undertow, 0)
        .start()

    println("http://localhost:${server.port()}")
}
