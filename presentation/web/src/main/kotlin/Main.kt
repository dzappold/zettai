import org.http4k.server.Jetty
import org.http4k.server.asServer

fun main() {
    Zettai().asServer(Jetty(8080)).start()
}
