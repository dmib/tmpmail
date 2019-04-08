package by.example

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
import kotlin.collections.set


private const val STRING_MAIL_LENGTH = 7
private const val STRING_MAIL_PASSWORD = 20

private const val SETUP_PATH = "setup_path"
private const val DOMAIN = "domain"
private const val COOKIE_MAIL = "mail"

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
            call.respond(
                FreeMarkerContent(
                    "index.ftl",
                    mapOf("mails" to checkEmail(tempMail.mailAddress, tempMail.mailPassword), "tempMail" to tempMail),
                    "e"
                )
            )
            call.sessions.set(tempMail)
        }
        get("/mails") {
            val tempMail = call.sessions.get<MailAddress>() ?: error("No mail found or expired")
            call.respond(
                FreeMarkerContent(
                    "index.ftl",
                    mapOf("mails" to checkEmail(tempMail.mailAddress, tempMail.mailPassword), "tempMail" to tempMail),
                    "e"
                )
            )
        }
    }
}

class User(val userName: String, val email: String)

class Mail(val subject: String, val message: String, val sender: String, val date: String)

class MailAddress(val mailAddress: String, val mailPassword: String)


fun generateTempMailAddress(): MailAddress {
    val mail = "${RandomStringUtils.randomAlphabetic(STRING_MAIL_LENGTH)}@${System.getProperty(DOMAIN)}"
    val mailPassword = RandomStringUtils.randomAlphabetic(STRING_MAIL_PASSWORD)
    logger.info("User generate a new mail with login $mail and password $mailPassword")

    val output = Runtime.getRuntime()
        .exec(arrayOf("bash", "-c", "${System.getProperty(SETUP_PATH)} email add $mail $mailPassword")).waitFor()
    if (output == 0) {
        logger.info("Mail for user $mail generated")
    }
    return MailAddress(mail, mailPassword)
}

fun checkEmail(user: String, password: String): List<Mail> {

    val properties = Properties()

    properties["mail.pop3.port"] = "110"
    properties["mail.pop3.socketFactory.fallback"] = "false"
    properties["mail.pop3.socketFactory.port"] = "110"

    val store = Session.getInstance(properties).getStore("pop3")

    logger.info("Checking mails for user $user and password $password started")

    store.connect(System.getProperty(DOMAIN), user, password)
    val emailFolder = store.getFolder("Inbox")
    emailFolder.open(Folder.READ_ONLY)

    val messages = emailFolder.messages

    emailFolder.close(true)
    store.close()

    return messages.map { Mail(it.subject, it.content.toString(), it.from[0].toString(), it.sentDate.toString()) }
}
