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

import uk.gov.hmrc.pensionschemereturn.services.PsrVersionsService
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.pensionschemereturn.auth.PsrAuth
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HttpErrorFunctions
import play.api.Logging
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.{Inject, Singleton}

@Singleton()
class PsrVersionsController @Inject()(
  cc: ControllerComponents,
  psrVersionsService: PsrVersionsService,
  val authConnector: AuthConnector
)(
  implicit ec: ExecutionContext
) extends BackendController(cc)
    with PsrBaseController
    with PsrAuth
    with HttpErrorFunctions
    with Results
    with Logging {

  def getVersionsForYears(
    pstr: String,
    startDates: Seq[String]
  ): Action[AnyContent] = Action.async { implicit request =>
    authorisedAsPsrUser { _ =>
      logger.debug(s"Retrieving reporting versions for years- with pstr: $pstr, startDates: $startDates")
      Future
        .sequence(
          startDates.map(
            startDate => {
              psrVersionsService.getVersions(pstr, startDate).map { data =>
                val props: List[(String, JsValue)] = List(
                  Some("startDate" -> JsString(startDate)),
                  Some("data" -> data)
                ).flatten
                JsObject(props)
              }
            }
          )
        )
        .map(v => Ok(JsArray.apply(v)))
    }
  }

  def getVersions(
    pstr: String,
    startDate: String
  ): Action[AnyContent] = Action.async { implicit request =>
    authorisedAsPsrUser { _ =>
      logger.debug(
        s"Retrieving reporting versions - with pstr: $pstr, startDate: $startDate"
      )
      psrVersionsService.getVersions(pstr, startDate).map { data =>
        Ok(data)
      }
    }
  }
}
