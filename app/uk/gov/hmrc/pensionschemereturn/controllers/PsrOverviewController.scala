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
import uk.gov.hmrc.http.HttpErrorFunctions
import uk.gov.hmrc.pensionschemereturn.services.PsrOverviewService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class PsrOverviewController @Inject()(cc: ControllerComponents, psrOverviewService: PsrOverviewService)(
  implicit ec: ExecutionContext
) extends BackendController(cc)
    with PsrBaseController
    with HttpErrorFunctions
    with Results
    with Logging {

  def getOverview(
    pstr: String,
    fromDate: String,
    toDate: String
  ): Action[AnyContent] = Action.async { implicit request =>
    logger.debug(
      s"Retrieving overview - with pstr: $pstr, fromDate: $fromDate, toDate: $toDate"
    )
    psrOverviewService.getOverview(pstr, fromDate, toDate).map { data =>
      Ok(data)
    }
  }
}