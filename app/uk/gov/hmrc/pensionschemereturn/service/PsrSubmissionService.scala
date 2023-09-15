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

package uk.gov.hmrc.pensionschemereturn.service

import com.google.inject.{Inject, Singleton}
import play.api.Logging
import play.api.libs.json._
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pensionschemereturn.connectors.PsrConnector
import uk.gov.hmrc.pensionschemereturn.models._
import uk.gov.hmrc.pensionschemereturn.service.PsrSubmissionService._

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class PsrSubmissionService @Inject()(psrConnector: PsrConnector) extends Logging {

  def submitMinimalRequiredDetails(
    minimalRequiredDetails: MinimalRequiredDetails
  )(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext, request: RequestHeader): Future[HttpResponse] = {
    val payload = ETMPMinimalRequiredDetails(
      ETMPReportDetails(
        pstr = minimalRequiredDetails.reportDetails.pstr,
        psrStatus = Compiled,
        periodStart = minimalRequiredDetails.reportDetails.periodStart,
        periodEnd = minimalRequiredDetails.reportDetails.periodEnd
      ),
      ETMPAccountingPeriodDetails(
        recordVersion = 1, // TODO hardcoded for now
        accountingPeriods = minimalRequiredDetails.accountingPeriods.map {
          case (start, end) =>
            ETMPAccountingPeriod(
              accPeriodStart = start,
              accPeriodEnd = end
            )
        }
      ),
      ETMPSchemeDesignatory(
        recordVersion = 1, // TODO hardcoded for now
        openBankAccount = minimalRequiredDetails.schemeDesignatory.openBankAccount,
        reasonNoOpenAccount = minimalRequiredDetails.schemeDesignatory.reasonForNoBankAccount,
        noOfActiveMembers = minimalRequiredDetails.schemeDesignatory.activeMembers,
        noOfDeferredMembers = minimalRequiredDetails.schemeDesignatory.deferredMembers,
        noOfPensionerMembers = minimalRequiredDetails.schemeDesignatory.pensionerMembers,
        totalPayments = minimalRequiredDetails.schemeDesignatory.totalPayments
      )
    )
    psrConnector.submitStandardPsr(Json.toJson(payload))
  }

  def submitStandardPsr(
    userAnswersJson: JsValue
  )(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext, request: RequestHeader): Future[HttpResponse] =
    // TODO transform from userDetails into ETMP format
    psrConnector.submitStandardPsr(userAnswersJson)
}

object PsrSubmissionService {
  private implicit val psrStatusWrites: OWrites[PSRStatus] = status => Json.obj("status" -> status.name)
  private implicit val reportDetailsWrites: OWrites[ETMPReportDetails] = Json.writes[ETMPReportDetails]
  private implicit val accountingPeriodWrites: OWrites[ETMPAccountingPeriod] = Json.writes[ETMPAccountingPeriod]
  private implicit val accountingPeriodDetailsWrites: OWrites[ETMPAccountingPeriodDetails] =
    Json.writes[ETMPAccountingPeriodDetails]
  private implicit val schemeDesignatoryWrites: OWrites[ETMPSchemeDesignatory] = Json.writes[ETMPSchemeDesignatory]
  implicit val reads: OWrites[ETMPMinimalRequiredDetails] = Json.writes[ETMPMinimalRequiredDetails]
}
