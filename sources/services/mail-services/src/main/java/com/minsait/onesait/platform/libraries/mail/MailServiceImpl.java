/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.libraries.mail;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MailServiceImpl implements MailService {

	@Autowired
	public JavaMailSender emailSender;

	@Override
	public void sendMail(String[] to, String subject, String text) {

		try {
			// emailSender.
			final SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(to);
			message.setSubject(subject);
			message.setText(text);
			emailSender.send(message);
		} catch (final MailException e) {
			log.error("Error sending mail", e);
			log.error("If you are using GMail check https://support.google.com/accounts/answer/6010255", e);
			throw e;
		}
	}
	
	@Override
	public void sendMail(String to, String subject, String text) {
	    sendMail(toArray(to), subject, text);	    
	}

	@Override
	public void sendMailWithTemplate(String[] to, String subject, SimpleMailMessage template, String... templateArgs) {
		final String text = String.format(template.getText(), templateArgs);
		sendMail(to, subject, text);
	}
	
	@Override
	public void sendMailWithTemplate(String to, String subject, SimpleMailMessage template, String... templateArgs) {
        sendMailWithTemplate(toArray(to), subject, template, templateArgs);
	}

	@Override
	public void sendHtmlMailWithFile(String[] to, String subject, String htmlText, String attachName, String attachment,
			boolean htmlenable) throws MessagingException {
		MimeMessageHelper helper = null;
		try {
			final MimeMessage message = emailSender.createMimeMessage();
			// pass 'true' to the constructor to create a multipart message
			if (attachment != null && attachName != null && attachName != "") {
				helper = new MimeMessageHelper(message, true);
				byte[] decode = Base64.decodeBase64(attachment);
				helper.addAttachment(attachName, new ByteArrayResource(decode));
			} else {
				helper = new MimeMessageHelper(message);
			}

			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(htmlText, htmlenable);

			emailSender.send(message);
		} catch (final MessagingException e) {
			log.error("Error sending mail", e);
			log.error("If you are using GMail check https://support.google.com/accounts/answer/6010255");
			throw e;
		}
	}
	
	@Override
	public void sendHtmlMailWithFile(String to, String subject, String text, String attachmentName, String attachment,
	        boolean htmlenable) throws MessagingException {
        sendHtmlMailWithFile(toArray(to), subject, text, attachmentName, attachment, htmlenable);	    
	}
	
	private String[] toArray(String address) {
	    String[] toArray = new String[1];
        toArray[0] = address;
        return toArray;
	}
}
