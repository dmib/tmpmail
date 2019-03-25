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
import javax.mail.Session
import jdk.nashorn.internal.codegen.OptimisticTypesPersistence.store
import javax.mail.Folder


fun main(args: Array<String>): Unit = io.ktor.server.tomcat.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(Application::class.java.classLoader, "templates")
    }


    routing {
        get("/") {
            val tempMail = generateTempMailAddress()
            call.respond(
                FreeMarkerContent(
                    "index.ftl",
                    mapOf("mails" to checkEmail(tempMail.mail_address, tempMail.password), "tempMail" to tempMail), "e"
                )
            )
        }
    }
}

class User(val userName: String, val email: String)

class Mail(val subject: String, val message: String, val sender: String, val date: String)

class MailAddress(val mail_address: String, val password: String)


fun generateTempMailAddress(): MailAddress = MailAddress("temp@abc.xyz", "password")

fun checkEmail(user: String, password: String): List<Mail> {

    val properties = Properties()

    properties["mail.pop3.host"] = "example.com"
    properties["mail.pop3.port"] = "995"
    properties["mail.pop3.starttls.enable"] = "true"
    properties["mail.pop3.socketFactory.fallback"] = "false"
    properties["mail.pop3.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
    properties["mail.pop3.socketFactory.port"] = "995"

    val emailSession = Session.getDefaultInstance(properties)
    val store = emailSession.getStore("pop3s")

    store.connect("example.com", user, password)
    val emailFolder = store.getFolder("INBOX")
    emailFolder.open(Folder.READ_ONLY)

    val messages = emailFolder.messages

    emailFolder.close(false)
    store.close()

    return messages.map { Mail(it.subject, it.content.toString(), it.from[0].toString(), it.sentDate.toString()) }
}
