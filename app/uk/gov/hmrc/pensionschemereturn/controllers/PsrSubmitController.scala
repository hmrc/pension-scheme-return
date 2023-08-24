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
import uk.gov.hmrc.pensionschemereturn.models.MinimalRequiredDetails
import uk.gov.hmrc.pensionschemereturn.service.PsrSubmissionService
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

  def submitMinimalRequiredDetails: Action[AnyContent] =
    Action.async { implicit request =>
      val minimalRequiredDetails = requiredBody.as[MinimalRequiredDetails]
      logger.debug(message = s"Submitting minimal required details - Incoming payload: $minimalRequiredDetails")
      psrSubmissionService
        .submitMinimalRequiredDetails(minimalRequiredDetails)
        .map(response => {
          logger.debug(message = s"Submit standard PSR - response: ${response.status} , body: ${response.body}")
          NoContent
        })
    }

  def submitStandardPsr: Action[AnyContent] = Action.async { implicit request =>
    val userAnswers = requiredBody
    logger.debug(message = s"Submit standard PSR - Incoming payload: $userAnswers")
    psrSubmissionService
      .submitStandardPsr(userAnswers)
      .map(response => {
        logger.debug(message = s"Submit standard PSR - response: ${response.status} , body: ${response.body}")
        NoContent
      })
  }

  private def requiredBody(implicit request: Request[AnyContent]) =
    request.body.asJson.getOrElse(throw new BadRequestException("Request does not contain Json body"))

}
