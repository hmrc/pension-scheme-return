/*
 * Copyright 2023 HM Revenue & Customs
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

import uk.gov.hmrc.pensionschemereturn.services.PsrSubmissionService
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.pensionschemereturn.auth.PsrAuth
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.PsrSubmission
import uk.gov.hmrc.http.HttpErrorFunctions
import play.api.Logging
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext

import javax.inject.{Inject, Singleton}

@Singleton()
class PsrSubmitController @Inject()(
  cc: ControllerComponents,
  psrSubmissionService: PsrSubmissionService,
  val authConnector: AuthConnector
)(
  implicit ec: ExecutionContext
) extends BackendController(cc)
    with PsrBaseController
    with PsrAuth
    with HttpErrorFunctions
    with Results
    with Logging {

  def submitStandardPsr: Action[AnyContent] = Action.async { implicit request =>
    authorisedAsPsrUser { _ =>
      val psrSubmission = requiredBody.as[PsrSubmission]
      // TODO even when this is at info level and it is very useful for development, we'd need to take the body out before go-live:
      logger.info(message = s"Submitting standard PSR - Incoming payload: $psrSubmission")
      psrSubmissionService
        .submitStandardPsr(psrSubmission)
        .map(response => {
          // TODO even when this is at debug level and it is very useful for development, we'd need to take the body out before go-live:
          logger.debug(message = s"Submit standard PSR - response: ${response.status} , body: ${response.body}")
          NoContent
        })
    }
  }

  def getStandardPsr(
    pstr: String,
    optFbNumber: Option[String],
    optPeriodStartDate: Option[String],
    optPsrVersion: Option[String]
  ): Action[AnyContent] = Action.async { implicit request =>
    authorisedAsPsrUser { _ =>
      logger.debug(
        s"Retrieving standard PSR - with pstr: $pstr, fbNumber: $optFbNumber, periodStartDate: $optPeriodStartDate, psrVersion: $optPsrVersion"
      )
      psrSubmissionService.getStandardPsr(pstr, optFbNumber, optPeriodStartDate, optPsrVersion).map {
        case None => NotFound
        case Some(Right(psrSubmission)) =>
          val jsonResponse = Json.toJson(psrSubmission)
          // TODO even when this is at debug level and it is very useful for development, we'd need to take the body out before go-live:
          logger.debug(message = s"Retrieved data as JSON: $jsonResponse")
          Ok(jsonResponse)
        case Some(Left(error)) => InternalServerError(Json.toJson(error))
      }
    }
  }
}
