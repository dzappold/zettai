import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes


val app: HttpHandler = routes(
    "/todo/{user}/{list}" bind GET to ::showList,
)

fun showList(request: Request): Response {
    val user = request.path("user")
    val list = request.path("list")
    val htmlPage = """
    <html>
        <body>
            <h1>Zettai</h1>
            <p>Here is the list <b>$list</b> of user <b>$user</b></p>
        </body>
    </html>
    """.trimIndent()
    return Response(OK).body(htmlPage)
}
