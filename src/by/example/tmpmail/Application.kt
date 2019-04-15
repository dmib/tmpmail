package by.example

import by.example.tmpmail.data.Mail
import by.example.tmpmail.data.MailAddress
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.sessions.*
import org.apache.commons.lang3.RandomStringUtils
import org.slf4j.LoggerFactory
import java.util.*
import javax.mail.Folder
import javax.mail.Session


private const val STRING_MAIL_LENGTH = 7
private const val STRING_MAIL_PASSWORD = 20

private const val DOMAIN = "domain"
private const val PORT = "port"
private const val COOKIE_MAIL = "mail"

private const val LOGIN = "login"
private const val PASSWORD = "password"

val logger = LoggerFactory.getLogger("ten-minutes-mail")

fun main(args: Array<String>): Unit = io.ktor.server.tomcat.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(Application::class.java.classLoader, "templates")
    }

    install(Sessions) {
        cookie<MailAddress>(COOKIE_MAIL, storage = SessionStorageMemory())
    }


    routing {
        get("/") {
            val tempMail = call.sessions.get<MailAddress>() ?: generateTempMailAddress()
            call.sessions.set(tempMail)
            call.respond(
                FreeMarkerContent(
                    "index.ftl",
                    mapOf("mails" to checkEmail(tempMail.email, tempMail.passwordPlaintext), "tempMail" to tempMail),
                    "e"
                )
            )
        }
        get("/mails") {
            val tempMail = call.sessions.get<MailAddress>() ?: error("No mail found or expired")
            call.respond(
                FreeMarkerContent(
                    "index.ftl",
                    mapOf("mails" to checkEmail(tempMail.email, tempMail.passwordPlaintext), "tempMail" to tempMail),
                    "e"
                )
            )
        }
    }
}

fun generateTempMailAddress(): MailAddress {
    val mail = "${RandomStringUtils.randomAlphabetic(STRING_MAIL_LENGTH).toLowerCase()}@${System.getProperty(DOMAIN)}"
    val mailPassword = RandomStringUtils.randomAlphabetic(STRING_MAIL_PASSWORD)


    Fuel.post("http://${System.getProperty(DOMAIN)}:${System.getProperty(PORT)}/admin/api/v1/boxes")
        .header(Pair("Content-Type", "application/json"))
        .body(Gson().toJson(MailAddress(mail, mailPassword)).toString())
        .authenticate("${System.getProperty(LOGIN)}@${System.getProperty(DOMAIN)}", "${System.getProperty(PASSWORD)}")
        .response()

    logger.info("User generate a new mail with login $mail and password $mailPassword")

    return MailAddress(mail, mailPassword)
}

fun checkEmail(user: String, password: String): List<Mail> {

    val properties = Properties()

    properties.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
    properties.setProperty("mail.pop3.socketFactory.fallback", "false")
    properties.setProperty("mail.pop3.port", "995")
    properties.setProperty("mail.pop3.socketFactory.port", "995")


    val store = Session.getInstance(properties).getStore("pop3")

    logger.info("Checking mails for user $user and password $password started")

    store.connect(System.getProperty(DOMAIN), user, password)
    val emailFolder = store.getFolder("Inbox")
    emailFolder.open(Folder.READ_ONLY)

    val messages = emailFolder.messages.map {
        Mail(
            it.subject,
            it.content.toString(),
            it.from[0].toString(),
            it.sentDate.toString()
        )
    }


    emailFolder.close(true)
    store.close()

    return messages
}
