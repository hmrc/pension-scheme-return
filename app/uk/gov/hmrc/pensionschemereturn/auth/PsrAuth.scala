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
import uk.gov.hmrc.auth.core.retrieve.{~, Retrieval}
import play.api.Logging
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.pensionschemereturn.connectors.SchemeDetailsConnector
import play.api.mvc.Results.BadRequest
import uk.gov.hmrc.pensionschemereturn.models.Srn
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals

import scala.concurrent.{ExecutionContext, Future}

final case class PsrAuthContext[A](
  externalId: String,
  psaPspId: String,
  credentialRole: String,
  request: Request[A]
)

trait PsrAuth extends AuthorisedFunctions with Logging {

  protected val schemeDetailsConnector: SchemeDetailsConnector
  private val AuthPredicate = Enrolment(psaEnrolmentKey).or(Enrolment(pspEnrolmentKey))
  private val PsrRetrievals: Retrieval[Option[String] ~ Enrolments] =
    Retrievals.externalId.and(Retrievals.allEnrolments)

  private type PsrAction[A] = PsrAuthContext[A] => Future[Result]

  def authorisedAsPsrUser(srnS: String)(
    body: PsrAction[Any]
  )(implicit ec: ExecutionContext, hc: HeaderCarrier, request: Request[_]): Future[Result] =
    authorisedUser(srnS)(body)

  private def authorisedUser[A](srnS: String)(
    block: PsrAction[A]
  )(implicit ec: ExecutionContext, hc: HeaderCarrier, request: Request[A]): Future[Result] =
    Srn(srnS) match {
      case Some(srn) =>
        authorised(AuthPredicate)
          .retrieve(PsrRetrievals) {
            case Some(externalId) ~ enrolments =>
              getPsaPspId(enrolments) match {
                case Some((psaPspId, credentialRole, idType)) =>
                  schemeDetailsConnector.checkAssociation(psaPspId, idType, srn).flatMap {
                    case true => block(PsrAuthContext(externalId, psaPspId, credentialRole, request))
                    case false =>
                      Future
                        .failed(new UnauthorizedException("Not Authorised - scheme is not associated with the user"))
                  }

                case psa => Future.failed(new BadRequestException(s"Bad Request without psaPspId/credentialRole $psa"))
              }
            case _ =>
              Future.failed(new UnauthorizedException("Not Authorised - Unable to retrieve credentials - externalId"))
          }
      case _ => Future.successful(BadRequest("Invalid scheme reference number"))
    }

  private def getPsaId(enrolments: Enrolments): Option[String] =
    enrolments
      .getEnrolment(psaEnrolmentKey)
      .flatMap(_.getIdentifier(psaId.toUpperCase))
      .map(_.value)

  private def getPspId(enrolments: Enrolments): Option[String] =
    enrolments
      .getEnrolment(pspEnrolmentKey)
      .flatMap(_.getIdentifier(pspId.toUpperCase))
      .map(_.value)

  private def getPsaPspId(enrolments: Enrolments): Option[(String, String, String)] =
    getPsaId(enrolments) match {
      case Some(id) => Some((id, PSA, psaId))
      case _ =>
        getPspId(enrolments) match {
          case Some(id) => Some((id, PSP, pspId))
          case _ => None
        }
    }
}
