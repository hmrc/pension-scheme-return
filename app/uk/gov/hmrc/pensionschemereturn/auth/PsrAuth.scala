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

package uk.gov.hmrc.pensionschemereturn.auth

import play.api.mvc.{Request, Result}
import uk.gov.hmrc.pensionschemereturn.config.Constants._
import uk.gov.hmrc.auth.core.{AuthorisedFunctions, Enrolment, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.{~, Name}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import play.api.Logging
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, UnauthorizedException}

import scala.concurrent.{ExecutionContext, Future}

final case class PsrAuthContext[A](
  externalId: String,
  psaPspId: String,
  name: Option[Name],
  request: Request[A]
)

trait PsrAuth extends AuthorisedFunctions with Logging {

  private val AuthPredicate = Enrolment(psaEnrolmentKey).or(Enrolment(pspEnrolmentKey))
  private val PsrRetrievals = Retrievals.externalId.and(Retrievals.allEnrolments).and(Retrievals.name)

  private type PsrAction[A] = PsrAuthContext[A] => Future[Result]

  def authorisedAsPsrUser(
    body: PsrAction[Any]
  )(implicit ec: ExecutionContext, hc: HeaderCarrier, request: Request[_]): Future[Result] =
    authorisedUser(body)

  private def authorisedUser[A](
    block: PsrAction[A]
  )(implicit ec: ExecutionContext, hc: HeaderCarrier, request: Request[A]): Future[Result] =
    authorised(AuthPredicate)
      .retrieve(PsrRetrievals) {
        case Some(externalId) ~ enrolments ~ name =>
          getPsaPspId(enrolments) match {
            case Some(psaPspId) => block(PsrAuthContext(externalId, psaPspId, name, request))
            case psa => Future.failed(new BadRequestException(s"Bad Request without psaPspId $psa"))
          }
        case _ =>
          Future.failed(new UnauthorizedException("Not Authorised - Unable to retrieve credentials - externalId"))
      }

  private def getPsaId(enrolments: Enrolments): Option[String] =
    enrolments
      .getEnrolment(psaEnrolmentKey)
      .flatMap(_.getIdentifier(psaIdKey))
      .map(_.value)

  private def getPspId(enrolments: Enrolments): Option[String] =
    enrolments
      .getEnrolment(pspEnrolmentKey)
      .flatMap(_.getIdentifier(pspIdKey))
      .map(_.value)

  private def getPsaPspId(enrolments: Enrolments): Option[String] =
    getPsaId(enrolments) match {
      case id @ Some(_) => id
      case _ =>
        getPspId(enrolments) match {
          case id @ Some(_) => id
          case _ => None
        }
    }
}
