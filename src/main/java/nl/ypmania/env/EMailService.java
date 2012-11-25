package nl.ypmania.env;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.net.smtp.AuthenticatingSMTPClient;
import org.apache.commons.net.smtp.SMTPClient;
import org.apache.commons.net.smtp.SMTPReply;
import org.apache.commons.net.smtp.SimpleSMTPHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EMailService {
  private static final Logger log = LoggerFactory.getLogger(EMailService.class);

  public void sendMail(String subject, String body) {
    try {
      final String username = System.getProperty("mail.username");
      final String password = System.getProperty("mail.password");
      final String recipients = System.getProperty("mail.recipients");

      if (recipients == null || password == null || username == null) {
        log.warn("Not sending mail, not configured");
        return;
      }

      String host = "smtp.gmail.com";
      int port = 587;

      String from = "deepthought@ypmania.nl";

      AuthenticatingSMTPClient client = new AuthenticatingSMTPClient(); 
      log.debug("connect");
      client.connect(host, port);
      checkReply(client);

      log.debug("login");
      client.login();
      checkReply(client);
      
      client.execTLS();

      log.debug("auth " + username);
      client.auth(AuthenticatingSMTPClient.AUTH_METHOD.PLAIN, username, password);
      checkReply(client);

      log.debug("sender");
      client.setSender(from);
      checkReply(client);

      for (String r : recipients.split(",")) {
        log.debug("recipient " + r);
        client.addRecipient(r);
        checkReply(client);
      }

      Writer writer = client.sendMessageData();
      if (writer == null)
        throw new IOException("Couldn't write message data");

      SimpleSMTPHeader header = new SimpleSMTPHeader(from, recipients, subject);
      writer.write(header.toString());
      writer.write(body);
      writer.close();
      log.debug("complete");
      client.completePendingCommand();
      checkReply(client);

      log.debug("logout");
      client.logout();
      client.disconnect();

    } catch (Exception e) {
      log.error("Could not send mail.", e);
    }
  }

  private void checkReply(SMTPClient sc) throws IOException {
    if (SMTPReply.isNegativeTransient(sc.getReplyCode())) {
      sc.disconnect();
      throw new IOException("Transient SMTP error " + sc.getReplyCode());
    } else if (SMTPReply.isNegativePermanent(sc.getReplyCode())) {
      sc.disconnect();
      throw new IOException("Permanent SMTP error " + sc.getReplyCode());
    }
  }
}
