import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.server.Jetty
import org.http4k.server.asServer

val htmlPage = """
    <html>
        <body>
            <h1 style="text-align: center; font-size: 3em;">Hello functional World!</h1>
        </body>
    </html>
""".trimIndent()

val handler: HttpHandler = { Response(Status.OK).body(htmlPage) }

fun main() {
    handler.asServer(Jetty(8080)).start()
}
