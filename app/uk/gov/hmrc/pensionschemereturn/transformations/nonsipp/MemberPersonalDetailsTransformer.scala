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

package uk.gov.hmrc.pensionschemereturn.transformations.nonsipp

import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.memberpayments.MemberPersonalDetails
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.memberpayments.EtmpMemberPersonalDetails
import uk.gov.hmrc.pensionschemereturn.transformations.{ETMPTransformer, TransformerError}

@Singleton()
class MemberPersonalDetailsTransformer @Inject()
    extends ETMPTransformer[MemberPersonalDetails, EtmpMemberPersonalDetails] {

  override def toEtmp(memberPersonalDetails: MemberPersonalDetails): EtmpMemberPersonalDetails =
    EtmpMemberPersonalDetails(
      foreName = memberPersonalDetails.firstName,
      middleName = None,
      lastName = memberPersonalDetails.lastName,
      nino = memberPersonalDetails.nino,
      reasonNoNINO = memberPersonalDetails.reasonNoNINO,
      dateOfBirth = memberPersonalDetails.dateOfBirth
    )

  override def fromEtmp(out: EtmpMemberPersonalDetails): Either[TransformerError, MemberPersonalDetails] =
    Right(
      MemberPersonalDetails(
        firstName = out.foreName,
        lastName = out.lastName,
        nino = out.nino,
        reasonNoNINO = out.reasonNoNINO,
        dateOfBirth = out.dateOfBirth
      )
    )
}
