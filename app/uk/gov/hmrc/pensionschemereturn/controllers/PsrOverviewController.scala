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

import uk.gov.hmrc.pensionschemereturn.services.PsrOverviewService
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.pensionschemereturn.auth.PsrAuth
import uk.gov.hmrc.auth.core.AuthConnector
import play.api.Logging
import uk.gov.hmrc.http.HttpErrorFunctions
import uk.gov.hmrc.pensionschemereturn.connectors.SchemeDetailsConnector

import scala.concurrent.ExecutionContext

import javax.inject.{Inject, Singleton}

@Singleton()
class PsrOverviewController @Inject()(
  cc: ControllerComponents,
  psrOverviewService: PsrOverviewService,
  override val authConnector: AuthConnector,
  override protected val schemeDetailsConnector: SchemeDetailsConnector
)(
  implicit ec: ExecutionContext
) extends BackendController(cc)
    with PsrBaseController
    with PsrAuth
    with HttpErrorFunctions
    with Results
    with Logging {

  def getOverview(
    pstr: String,
    fromDate: String,
    toDate: String
  ): Action[AnyContent] = Action.async { implicit request =>
    val Seq(srnS) = requiredHeaders("srn")
    authorisedAsPsrUser(srnS) { _ =>
      logger.debug(
        s"Retrieving overview - with pstr: $pstr, fromDate: $fromDate, toDate: $toDate"
      )
      psrOverviewService.getOverview(pstr, fromDate, toDate).map { data =>
        Ok(data)
      }
    }
  }
}
