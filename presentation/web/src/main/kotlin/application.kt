import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.routes

val htmlPage = """
    <html>
        <body>
            <h1 style="text-align: center; font-size: 3em;">Hello functional World!</h1>
        </body>
    </html>
""".trimIndent()

val app: HttpHandler = routes(
    "/greetings" bind GET to ::greetings,
    "/data" bind POST to ::receiveData,
)

fun receiveData(request: Request): Response = Response(CREATED).body("Received: ${request.bodyString()}")

fun greetings(request: Request): Response = Response(OK).body(htmlPage)
