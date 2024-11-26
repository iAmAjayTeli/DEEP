package com.example.deep.util

import android.util.Log
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailSender {
    fun sendEmail(toEmail: String, subject: String, body: String) {
        val properties = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")  // Ensure TLS is enabled
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "465")  // Use 587 for TLS, 465 for SSL
            put("mail.debug", "true")
        }


        val session = Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication("yourdad4400@gmail.com", "9821@jaYgup")
            }
        })

        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress("yourdad4400@gmail.com"))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                setSubject(subject)
                setText(body)
            }
            Transport.send(message)
           Log.e("EmailSend", "Email sent successfully")
        } catch (e: Exception) {
            Log.e("EmailSend", "Error sending email: ${e.message}")
            e.printStackTrace()
        }
    }
}
