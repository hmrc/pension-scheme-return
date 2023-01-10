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
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.pensionschemereturn.config.AppConfig
import uk.gov.hmrc.pensionschemereturn.models.PensionSchemeId.{PsaId, PspId}
import uk.gov.hmrc.pensionschemereturn.models.SchemeId.Srn
import uk.gov.hmrc.pensionschemereturn.models.{SchemeDetails, SchemeId}
import uk.gov.hmrc.pensionschemereturn.utils.FutureUtils.FutureOps

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SchemeDetailsConnectorImpl @Inject()(appConfig: AppConfig, http: HttpClient) extends SchemeDetailsConnector {

  private def url(relativePath: String) = s"${appConfig.pensionsScheme}$relativePath"

  override def details(psaId: PsaId, schemeId: SchemeId)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SchemeDetails] = {

    val headers = List(
      "idNumber"     -> schemeId.value,
      "schemeIdType" -> schemeId.idType,
      "psaId"        -> psaId.value
    )

    http
      .GET[SchemeDetails](url("/pensions-scheme/scheme"))(implicitly, hc.withExtraHeaders(headers: _*), implicitly)
      .tapError { t =>
        Future.successful(logger.error(s"Failed to fetch scheme details $schemeId for psa $psaId with message ${t.getMessage}"))
      }
  }

  override def details(pspId: PspId, schemeId: Srn)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SchemeDetails] = {

    val headers = List(
      "pspId" -> pspId.value,
      "srn"   -> schemeId.value
    )

    http
      .GET[SchemeDetails](url("/pensions-scheme/psp-scheme"))(implicitly, hc.withExtraHeaders(headers: _*), implicitly)
      .tapError { t =>
        Future.successful(logger.error(s"Failed to fetch scheme details $schemeId for psp $pspId with message ${t.getMessage}"))
      }
  }

  override def checkAssociation(psaId: PsaId, schemeId: Srn)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] =
    checkAssociation(psaId.value, "psaId", schemeId)

  override def checkAssociation(pspId: PspId, schemeId: Srn)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] =
    checkAssociation(pspId.value, "pspId", schemeId)

  private def checkAssociation(idValue: String, idType: String, srn: Srn)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {

    val headers = List(
      idType                  -> idValue,
      "schemeReferenceNumber" -> srn.value,
      "Content-Type"          -> "application/json"
    )

    http
      .GET[Boolean](url("/pensions-scheme/is-psa-associated"))(implicitly, hc.withExtraHeaders(headers: _*), implicitly)
      .tapError { t =>
        Future.successful(logger.error(s"Failed check association for scheme $srn for $idType $idValue with message ${t.getMessage}"))
      }
  }

}

@ImplementedBy(classOf[SchemeDetailsConnectorImpl])
trait SchemeDetailsConnector {

  protected val logger: Logger = Logger(classOf[SchemeDetailsConnector])

  def details(psaId: PsaId, schemeId: SchemeId)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SchemeDetails]
  def details(pspId: PspId, schemeId: Srn)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SchemeDetails]
  def checkAssociation(psaId: PsaId, schemeId: Srn)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean]
  def checkAssociation(pspId: PspId, schemeId: Srn)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean]
}