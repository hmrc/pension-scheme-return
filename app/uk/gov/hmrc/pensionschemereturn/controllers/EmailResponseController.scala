/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.pensionschemereturn.controllers

import uk.gov.hmrc.pensionschemereturn.audit.EmailAuditEvent
import uk.gov.hmrc.pensionschemereturn.services.AuditService
import play.api.mvc._
import com.google.inject.Inject
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.pensionschemereturn.models.{EmailEvents, Opened}
import uk.gov.hmrc.http.{Request => _}
import uk.gov.hmrc.crypto.{ApplicationCrypto, Crypted}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import play.api.Logger
import play.api.libs.json.JsValue
import uk.gov.hmrc.pensionschemereturn.config.Constants.emailRegex

import scala.concurrent.ExecutionContext

class EmailResponseController @Inject()(
  auditService: AuditService,
  cc: ControllerComponents,
  crypto: ApplicationCrypto,
  parser: PlayBodyParsers,
  val authConnector: AuthConnector
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with AuthorisedFunctions {
  private val logger = Logger(classOf[EmailResponseController])

  def sendAuditEvents(
    submittedBy: String,
    requestId: String,
    email: String,
    encryptedPsaOrPspId: String,
    encryptedPstr: String,
    reportVersion: String
  ): Action[JsValue] = Action(parser.tolerantJson) { implicit request =>
    decryptPsaOrPspIdAndEmail(encryptedPsaOrPspId, encryptedPstr, email) match {
      case Right(Tuple3(psaOrPspId, pstr, emailAddress)) =>
        request.body
          .validate[EmailEvents]
          .fold(
            _ => BadRequest("Bad request received for email call back event"),
            valid => {
              valid.events
                .filterNot(
                  _.event == Opened
                )
                .foreach { event =>
                  logger.debug(s"Email Audit event is $event")
                  auditService.sendEvent(
                    EmailAuditEvent(psaOrPspId, pstr, submittedBy, emailAddress, event.event, requestId, reportVersion)
                  )(request, implicitly)
                }
              Ok
            }
          )

      case Left(result) => result
    }
  }

  private def decryptPsaOrPspIdAndEmail(
    encryptedPsaOrPspId: String,
    encryptedPstr: String,
    email: String
  ): Either[Result, (String, String, String)] = {
    val psaOrPspId = crypto.QueryParameterCrypto.decrypt(Crypted(encryptedPsaOrPspId)).value
    val pstr = crypto.QueryParameterCrypto.decrypt(Crypted(encryptedPstr)).value
    val emailAddress = crypto.QueryParameterCrypto.decrypt(Crypted(email)).value

    try {
      require(emailAddress.matches(emailRegex))
      Right(Tuple3(psaOrPspId, pstr, emailAddress))
    } catch {
      case _: IllegalArgumentException => Left(Forbidden(s"Malformed email address : $emailAddress"))
    }
  }
}
