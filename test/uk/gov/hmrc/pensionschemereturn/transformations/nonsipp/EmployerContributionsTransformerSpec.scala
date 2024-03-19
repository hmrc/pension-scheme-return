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

package uk.gov.hmrc.pensionschemereturn.transformations.nonsipp

import uk.gov.hmrc.pensionschemereturn.models.nonsipp.memberpayments.{EmployerContributions, EmployerType}
import uk.gov.hmrc.pensionschemereturn.base.EtmpTransformerSpec
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.memberpayments.{
  EmployerContributionsOrgType,
  EtmpEmployerContributions,
  OrganisationIdentity
}
import uk.gov.hmrc.pensionschemereturn.transformations.TransformerError

class EmployerContributionsTransformerSpec extends EtmpTransformerSpec {

  private val employerContribution = EmployerContributions(
    employerName = "test employer one",
    employerType = EmployerType.UKCompany(Right("test company id")),
    totalTransferValue = 12.34
  )

  private val etmpEmployerContribution = EtmpEmployerContributions(
    orgName = "test employer one",
    organisationIdentity = OrganisationIdentity(
      orgType = EmployerContributionsOrgType.UKCompany,
      idNumber = Some("test company id")
    ),
    totalContribution = 12.34
  )

  "EmployerContributionsTransformer" should {
    "successfully transform to ETMP format" in {
      val result = employerContributionsTransformer.toEtmp(employerContribution)

      result shouldMatchTo etmpEmployerContribution
    }

    "successfully transform from ETMP format" in {
      val result = employerContributionsTransformer.fromEtmp(etmpEmployerContribution)

      result shouldMatchTo Right(employerContribution)
    }

    "fail to transform from ETMP format" should {
      "when org identity is UK company but missing both idNumber and reasonNoIdNumber" in {
        val incorrectEtmpEmployerContribution = EtmpEmployerContributions(
          orgName = "test employer one",
          organisationIdentity = OrganisationIdentity(
            orgType = EmployerContributionsOrgType.UKCompany
          ),
          totalContribution = 12.34
        )
        val result = employerContributionsTransformer.fromEtmp(incorrectEtmpEmployerContribution)

        result shouldMatchTo Left(TransformerError.NoIdOrReason)
      }

      "when org identity is UK partnership but missing both idNumber and reasonNoIdNumber" in {
        val incorrectEtmpEmployerContribution = EtmpEmployerContributions(
          orgName = "test employer one",
          organisationIdentity = OrganisationIdentity(
            orgType = EmployerContributionsOrgType.UKPartnership
          ),
          totalContribution = 12.34
        )
        val result = employerContributionsTransformer.fromEtmp(incorrectEtmpEmployerContribution)

        result shouldMatchTo Left(TransformerError.NoIdOrReason)
      }

      "when org identity is Other but missing description" in {
        val incorrectEtmpEmployerContribution = EtmpEmployerContributions(
          orgName = "test employer one",
          organisationIdentity = OrganisationIdentity(
            orgType = EmployerContributionsOrgType.Other
          ),
          totalContribution = 12.34
        )
        val result = employerContributionsTransformer.fromEtmp(incorrectEtmpEmployerContribution)

        result shouldMatchTo Left(TransformerError.OtherNoDescription)
      }
    }
  }
}
