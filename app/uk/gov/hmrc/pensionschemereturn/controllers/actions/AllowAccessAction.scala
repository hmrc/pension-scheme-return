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

package uk.gov.hmrc.pensionschemereturn.controllers.actions

import play.api.mvc.Results.Unauthorized
import play.api.mvc.{ActionFunction, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pensionschemereturn.connectors.MinimalDetailsError.DelimitedAdmin
import uk.gov.hmrc.pensionschemereturn.connectors.{MinimalDetailsConnector, MinimalDetailsError, SchemeDetailsConnector}
import uk.gov.hmrc.pensionschemereturn.models.SchemeId.Srn
import uk.gov.hmrc.pensionschemereturn.models.{MinimalDetails, SchemeDetails, SchemeStatus}
import uk.gov.hmrc.pensionschemereturn.models.SchemeStatus.{Deregistered, Open, WoundUp}
import uk.gov.hmrc.pensionschemereturn.models.requests.{AllowedAccessRequest, IdentifierRequest}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AllowAccessAction @Inject()(
  schemeDetailsConnector: SchemeDetailsConnector,
  minimalDetailsConnector: MinimalDetailsConnector
)(implicit val ec: ExecutionContext) {

  val validStatuses: List[SchemeStatus] = List(Open, WoundUp, Deregistered)

  def apply(srn: Srn): ActionFunction[IdentifierRequest, AllowedAccessRequest] =
    new ActionFunction[IdentifierRequest, AllowedAccessRequest] {
      override def invokeBlock[A](request: IdentifierRequest[A], block: AllowedAccessRequest[A] => Future[Result]): Future[Result] = {

        implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

        (for {
          schemeDetails  <- fetchSchemeDetails(request, srn)
          isAssociated   <- fetchIsAssociated(request, srn)
          minimalDetails <- fetchMinimalDetails(request)
        } yield {
          if(
            isAssociated &&
            !minimalDetails.exists(_.rlsFlag) &&
            !minimalDetails.exists(_.deceasedFlag) &&
            !minimalDetails.left.exists(_ == DelimitedAdmin) &&
            validStatuses.contains(schemeDetails.schemeStatus)
          ) {
            block(AllowedAccessRequest(request, schemeDetails))
          } else {
            Future.successful(Unauthorized)
          }
        }).flatten

      }

      override protected def executionContext: ExecutionContext = ec
    }

  private def fetchSchemeDetails[A](request: IdentifierRequest[A], srn: Srn)(implicit hc: HeaderCarrier): Future[SchemeDetails] =
    request
      .fold(
        a => schemeDetailsConnector.details(a.psaId, srn)
      )(
        p => schemeDetailsConnector.details(p.pspId, srn)
      )

  private def fetchIsAssociated[A](request: IdentifierRequest[A], srn: Srn)(implicit hc: HeaderCarrier): Future[Boolean] =
    request
      .fold(
        a => schemeDetailsConnector.checkAssociation(a.psaId, srn)
      )(
        p => schemeDetailsConnector.checkAssociation(p.pspId, srn)
      )

  private def fetchMinimalDetails[A](request: IdentifierRequest[A])(implicit hc: HeaderCarrier)
    : Future[Either[MinimalDetailsError, MinimalDetails]] =
      request
        .fold(
          a => minimalDetailsConnector.fetch(a.psaId)
        )(
          p => minimalDetailsConnector.fetch(p.pspId)
        )
}

