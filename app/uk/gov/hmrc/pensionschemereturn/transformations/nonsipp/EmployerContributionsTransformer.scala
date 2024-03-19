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

import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.memberpayments.{EmployerContributions, EmployerType}
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.memberpayments.{
  EmployerContributionsOrgType,
  EtmpEmployerContributions,
  OrganisationIdentity
}
import uk.gov.hmrc.pensionschemereturn.transformations.{ETMPTransformer, TransformerError}

@Singleton()
class EmployerContributionsTransformer @Inject()
    extends ETMPTransformer[EmployerContributions, EtmpEmployerContributions] {

  override def toEtmp(employerContributions: EmployerContributions): EtmpEmployerContributions =
    EtmpEmployerContributions(
      orgName = employerContributions.employerName,
      organisationIdentity = toOrgIdentity(employerContributions.employerType),
      totalContribution = employerContributions.totalTransferValue
    )

  override def fromEtmp(out: EtmpEmployerContributions): Either[TransformerError, EmployerContributions] =
    for {
      employerType <- toEmployerType(out.organisationIdentity)
    } yield EmployerContributions(
      employerName = out.orgName,
      employerType = employerType,
      totalTransferValue = out.totalContribution
    )

  private def toOrgIdentity(employerType: EmployerType): OrganisationIdentity = employerType match {
    case EmployerType.UKCompany(Left(reason)) =>
      OrganisationIdentity(
        orgType = EmployerContributionsOrgType.UKCompany,
        reasonNoIdNumber = Some(reason)
      )
    case EmployerType.UKCompany(Right(id)) =>
      OrganisationIdentity(
        orgType = EmployerContributionsOrgType.UKCompany,
        idNumber = Some(id)
      )
    case EmployerType.UKPartnership(Left(reason)) =>
      OrganisationIdentity(
        orgType = EmployerContributionsOrgType.UKPartnership,
        reasonNoIdNumber = Some(reason)
      )
    case EmployerType.UKPartnership(Right(id)) =>
      OrganisationIdentity(
        orgType = EmployerContributionsOrgType.UKPartnership,
        idNumber = Some(id)
      )
    case EmployerType.Other(description) =>
      OrganisationIdentity(
        orgType = EmployerContributionsOrgType.Other,
        otherDescription = Some(description)
      )
  }

  private def toEmployerType(o: OrganisationIdentity): Either[TransformerError, EmployerType] = o.orgType match {
    case EmployerContributionsOrgType.UKCompany =>
      (o.reasonNoIdNumber, o.idNumber)
        .toEither(TransformerError.NoIdOrReason)
        .map(EmployerType.UKCompany)
    case EmployerContributionsOrgType.UKPartnership =>
      (o.reasonNoIdNumber, o.idNumber)
        .toEither(TransformerError.NoIdOrReason)
        .map(EmployerType.UKPartnership)
    case EmployerContributionsOrgType.Other =>
      o.otherDescription
        .toRight(TransformerError.OtherNoDescription)
        .map(EmployerType.Other)
  }
}
