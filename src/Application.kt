package by.example

import freemarker.cache.ClassTemplateLoader
import io.ktor.application.*
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.html.*
import kotlinx.html.*
import kotlinx.css.*
import java.util.*

fun main(args: Array<String>): Unit = io.ktor.server.tomcat.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(Application::class.java.classLoader, "templates")
    }


    routing {
        get("/") {
            call.respond(
                FreeMarkerContent(
                    "index.ftl",
                    mapOf("mails" to generateFakeMails(), "tempMail" to generateTempMailAddress()), "e"
                )
            )
        }
    }
}

class User(val userName: String, val email: String)

class Mail(val subject: String, val message: String, val sender: String, val date: String)

class MailAddress(val mail_address: String)

fun generateFakeMails(): List<Mail> =
    listOf(
        Mail("subject1", "message1", "sender@gfwefr.few", Calendar.getInstance().time.toString()),
        Mail("subject2", "message2", "sender@gfwefr.few", Calendar.getInstance().time.toString()),
        Mail("subject3", "message3", "sender@gfwefr.few", Calendar.getInstance().time.toString())
    )

fun generateTempMailAddress(): MailAddress = MailAddress("temp@abc.xyz")
