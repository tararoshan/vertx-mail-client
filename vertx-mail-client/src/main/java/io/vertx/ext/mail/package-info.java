/*
 * Copyright (c) 2011-2014 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
/**
 * = Vert.x Mail client (SMTP client implementation)
 * 
 * Vert.x client for sending SMTP emails via a local mail server (e.g. postfix),
 * by external mail server (e.g. googlemail or aol) or by the vert.x event bus via
 * a mail service running on another machine on the local network.
 * 
 * The client supports a few additional auth methods like DIGEST-MD5 and has full
 * support for TLS and SSL and is completely asynchronous. The client supports
 * connection pooling to keep connections open for an specific time to be reused.
 * 
 * == Creating a client
 * 
 * There are two possibilities to send mails, either by creating a client that
 * opens SMTP connections from the local jvm or by creating a service that
 * communicates over the event bus with a service listener on another machine,
 * sending mails works the same in both cases.
 * 
 * === Local client
 * 
 * The client uses a configuration object, in Java this is called MailConfig, in
 * all other languages it is a Json Object. The default config is created as empty
 * object and will connect to localhost port 25, which should be ok in a standard
 * Linux environment where you have Postfix or similar mail server running on
 * localhost. For more examples of the config object, see below.
 * 
 * [source,$lang]
 * ----
 * {@link examples.Examples#createClient}
 * ----
 * 
 * === Service client
 * 
 * For documentation about the MailService interface, please take a look at the documentation
 * for the vertx-mail-service sub-project
 * 
 * == Sending mails
 * 
 * Once the client object is created, you can use it to send mails. Since the
 * sending of the mails works asynchronous in vert.x, the result handler will be
 * called when the mail operation finishes. You can start many mail send operations
 * in parallel, the connection pool will limit the number of concurrent operations
 * so that new operations will wait in a queue if no slots are available.
 * 
 * A mail message is constructed by the MailMessage object, this is either the
 * object in Java or a JSON Object in other languages. The MailMessage object has
 * properties from, to, cc, bcc, subject, text, html etc. that can be set by
 * setters in Java or as keys in JSON, depending on which values are set, the
 * format of the generated MIME message will vary. The recipient address properties
 * can either be a single address (String) or many addresses (List<String>).
 * 
 * The MIME encoder supports us-ascii (7bit) headers/messages and utf8 (usually
 * quoted-printable) headers/messages
 * 
 * [source,$lang]
 * ----
 * {@link examples.Examples#mailMessage}
 * ----
 * 
 * Attachments can be created by the MailAttachment object using data stored in a Buffer,
 * this supports base64 attachments.
 * 
 * [source,$lang]
 * ----
 * {@link examples.Examples#attachment}
 * ----
 * When sending the mail, you can provide a AsyncResult<MailResult> handler that will be called when
 * the send operation is finished or it failed.
 * 
 * A mail is sent as follows:
 * 
 * [source,$lang]
 * ----
 * {@link examples.Examples#sendMail}
 * ----
 *
 * == Mail-client data objects
 *
 * The objects used by the MailClient service are all @DataObject classes, which means that they have a few properties
 * and can be converted from and to JSON. In Java the regular getter and setter methods can be used for the properties
 * in Groovy you can use the property names directly. In JavaScript and Ruby, you can use a JSON object directly to
 * construct or read the properties. The objects can be used fluently. 
 *
 * === MailMessage properties
 * 
 * Email fields are Strings using the common formats for email with or without real
 * name
 * 
 * * `username@example.com`
 * * `username@example.com (Firstname Lastname)`
 * * `Firstname Lastname <username@example.com>`
 * 
 * The MailMessage object has the following properties
 * 
 * * `from` String representing the From address and the MAIL FROM field
 * * `to` String or List<String> representing the To addresses and the RCPT TO fields
 * * `cc` same as to
 * * `bcc` same as to
 * * `bounceAddress` String representing the error address (MAIL FROM), if not set from is used
 * * `text` String representing the text/plain part of the mail
 * * `html` String representing the text/html part of the mail
 * * `attachment` MailAttachment or List<MailAttachment> attachments of the message
 * * `headers` MultiMap representing headers to be added in addition to the headers necessary for the MIME Message
 * * `fixedHeaders` boolean if true, only the headers provided as headers property will be set in the generated message
 *
 * the last two properties allow for all kinds of trickery to generate messages with custom headers, e.g. providing
 * a message-id chosen by the calling program or setting different headers than would be generated by default. Unless you know
 * what you are doing, this may generate invalid messages.
 *
 * === MailAttachment properties
 * The MailAttachment object has the following properties
 * 
 * * `data` Buffer containing the binary data of the attachment
 * * `contentType` String of the Content-Type of the attachment (e.g. text/plain or text/plain charset="UTF8", default is application/octet-stream)
 * * `description` String describing the attachment (this is put in the description header of the attachment), optional
 * * `disposition` String describing the disposition of the attachment (this is either "inline" or "attachment", default is attachment)
 * * `name` String filename of the attachment (this is put into the disposition and in the contentType headers of the attachment), optional
 *
 * === MailConfig options
 * 
 * The configuration has the following properties
 * 
 * * `hostname` the hostname of the smtp server to connect to (default is localhost)
 * * `port` the port of the smtp server to connect to (default is 25)
 * * `startTLS` StartTLSOptions either DISABLED, OPTIONAL or REQUIRED, default is OPTIONAL
 * * `login` LoginOption either DISABLED, NONE or REQUIRED, default is NONE
 * * `username` String of the username to be used for login
 * * `password` String of the password to be used for login
 * * `ssl` boolean whether to use ssl on connect to the mail server (default is false), set this to use a port 465 ssl connection
 * * `ehloHostname` String to used in EHLO and for creating the message-id, if not set, the own hostname will be used, which may not be a good choice if it doesn't contain a FQDN or is localhost
 * * `authMethods` String space separated list of allowed auth methods, this can be used to disallow some auth methods or define one required auth method
 * * `keepAlive` boolean if connection pooling is enabled (default is true)
 * * `idleTimeout` int timeout in seconds that a connection is kept open after a mail has been sent (default is 300)
 * * `maxPoolSize` int max number of open connections kept in the pool or to be opened at one time (regardless if pooling is enabled or not), default is 10
 * * `trustAll` boolean whether to accept all certs from the server (default is false)
 * * `netClientOptions` NetClientOptions object to be used when connecting to the server port, this allows for example to set a custom keystore to use a self-defined certificate or a "custom" CA
 * 
 * === MailResult object
 * The MailResult object has the following members
 * 
 * * `messageID` the Message-ID of the generated mail
 * * `recipients` the list of recipients the mail was sent to
 *
 */
@Document(fileName = "index.adoc")
@GenModule(name = "vertx-mail")
package io.vertx.ext.mail;

import io.vertx.codegen.annotations.GenModule;
import io.vertx.docgen.Document;
