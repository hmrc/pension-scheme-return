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
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.EtmpMemberPersonalDetails
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.MemberPersonalDetails
import uk.gov.hmrc.pensionschemereturn.transformations.ETMPTransformer

@Singleton()
class MemberPersonalDetailsTransformer @Inject()()
    extends ETMPTransformer[MemberPersonalDetails, EtmpMemberPersonalDetails] {

  override def toEtmp(memberPersonalDetails: MemberPersonalDetails): EtmpMemberPersonalDetails =
    EtmpMemberPersonalDetails(
      foreName = memberPersonalDetails.firstName,
      middleName = None,
      lastName = memberPersonalDetails.lastName,
      nino = memberPersonalDetails.ninoOrReason.toOption,
      reasonNoNino = memberPersonalDetails.ninoOrReason.swap.toOption,
      dateOfBirth = memberPersonalDetails.dateOfBirth
    )

  override def fromEtmp(out: EtmpMemberPersonalDetails): MemberPersonalDetails = ???
}