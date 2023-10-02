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

import play.api.Logging
import play.api.mvc._
import uk.gov.hmrc.http.{BadRequestException, HttpErrorFunctions}
import uk.gov.hmrc.pensionschemereturn.controllers.PsrSubmitController.requiredBody
import uk.gov.hmrc.pensionschemereturn.models.PsrSubmission
import uk.gov.hmrc.pensionschemereturn.services.PsrSubmissionService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class PsrSubmitController @Inject()(cc: ControllerComponents, psrSubmissionService: PsrSubmissionService)(
  implicit ec: ExecutionContext
) extends BackendController(cc)
    with HttpErrorFunctions
    with Results
    with Logging {

  def submitStandardPsr: Action[AnyContent] = Action.async { implicit request =>
    val psrSubmission = requiredBody.as[PsrSubmission]
    logger.debug(message = s"Submitting standard PSR - Incoming payload: $psrSubmission")
    psrSubmissionService
      .submitStandardPsr(psrSubmission)
      .map(response => {
        logger.debug(message = s"Submit standard PSR - response: ${response.status} , body: ${response.body}")
        NoContent
      })
  }

  def getStandardPsr(
    pstr: String,
    optFbNumber: Option[String],
    optPeriodStartDate: Option[String],
    optPsrVersion: Option[String]
  ): Action[AnyContent] = Action.async { implicit request =>
    logger.debug(
      s"Retrieving standard PSR - with pstr: $pstr, fbNumber: $optFbNumber, periodStartDate: $optPeriodStartDate, psrVersion: $optPsrVersion"
    )
    psrSubmissionService.getStandardPsr(pstr, optFbNumber, optPeriodStartDate, optPsrVersion).map {
      case None => NotFound
      case Some(jsObj) => Ok(jsObj)
    }
  }
}

object PsrSubmitController extends Logging {

  private def requiredBody(implicit request: Request[AnyContent]) =
    request.body.asJson.getOrElse(throw new BadRequestException("Request does not contain Json body"))
}
