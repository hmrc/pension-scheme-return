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

package uk.gov.hmrc.pensionschemereturn.connectors

import com.google.inject.ImplementedBy
import play.api.Logger
import play.api.http.Status.FORBIDDEN
import uk.gov.hmrc.http.UpstreamErrorResponse.WithStatusCode
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, NotFoundException}
import uk.gov.hmrc.pensionschemereturn.config.{AppConfig, Constants}
import uk.gov.hmrc.pensionschemereturn.connectors.MinimalDetailsError.{DelimitedAdmin, DetailsNotFound}
import uk.gov.hmrc.pensionschemereturn.models.MinimalDetails
import uk.gov.hmrc.pensionschemereturn.models.PensionSchemeId.{PsaId, PspId}
import uk.gov.hmrc.pensionschemereturn.utils.FutureUtils.FutureOps

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MinimalDetailsConnectorImpl @Inject()(appConfig: AppConfig, http: HttpClient) extends MinimalDetailsConnector {

  private val url = s"${appConfig.pensionsAdministrator}/pension-administrator/get-minimal-psa"

  override def fetch(psaId: PsaId)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[MinimalDetailsError, MinimalDetails]] =
    fetch(hc.withExtraHeaders("psaId" -> psaId.value))

  override def fetch(pspId: PspId)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[MinimalDetailsError, MinimalDetails]] =
    fetch(hc.withExtraHeaders("pspId" -> pspId.value))

  private def fetch(hc: HeaderCarrier)(implicit ec: ExecutionContext): Future[Either[MinimalDetailsError, MinimalDetails]] = {
    http.GET[MinimalDetails](url)(implicitly, hc, implicitly)
      .map(Right(_))
      .recover {
        case e: NotFoundException if e.message.contains(Constants.detailsNotFound) =>
          Left(DetailsNotFound)
        case e@WithStatusCode(FORBIDDEN) if e.message.contains(Constants.delimitedPSA) =>
          Left(DelimitedAdmin)
      }
      .tapError(t => Future.successful(logger.error(s"Failed to fetch minimal details with message ${t.getMessage}")))
  }
}

@ImplementedBy(classOf[MinimalDetailsConnectorImpl])
trait MinimalDetailsConnector {

  protected val logger: Logger = Logger(classOf[MinimalDetailsConnector])

  def fetch(psaId: PsaId)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[MinimalDetailsError, MinimalDetails]]

  def fetch(pspId: PspId)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[MinimalDetailsError, MinimalDetails]]
}

sealed trait MinimalDetailsError

object MinimalDetailsError {

  case object DelimitedAdmin extends MinimalDetailsError
  case object DetailsNotFound extends MinimalDetailsError
}