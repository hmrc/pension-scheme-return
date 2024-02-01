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

package utils

import uk.gov.hmrc.pensionschemereturn.models.etmp.SectionStatus.New
import uk.gov.hmrc.pensionschemereturn.models.etmp.sipp._
import uk.gov.hmrc.pensionschemereturn.models.requests.etmp.SippPsrSubmissionEtmpRequest

trait SippEtmpDummyTestValues extends SippEtmpTestValues {

  private val etmpSippLandConnectedPartyLong = EtmpSippLandConnectedParty(
    noOfTransactions = 1,
    transactionDetails = Some(List.fill(1)(etmpSippLandConnectedPartyTransactionDetail))
  )

  private val etmpSippOtherAssetsConnectedPartyLong = EtmpSippOtherAssetsConnectedParty(
    noOfTransactions = 1,
    transactionDetails = Some(List.fill(2)(etmpSippOtherAssetsConnectedPartyTransactionDetail))
  )

  private val etmpSippLandArmsLengthLong = EtmpSippLandArmsLength(
    noOfTransactions = 1,
    transactionDetails = Some(List.fill(2)(etmpSippLandArmsLengthTransactionDetail))
  )

  private val etmpSippTangiblePropertyLong = EtmpSippTangibleProperty(
    noOfTransactions = 1,
    transactionDetails = Some(List.fill(2)(etmpSippTangiblePropertyTransactionalDetail))
  )

  private val etmpSippLoanOutstandingLong = EtmpSippLoanOutstanding(
    noOfTransactions = 1,
    transactionDetails = Some(List.fill(2)(etmpSippLoanOutstandingTransactionalDetail))
  )

  private val etmpSippUnquotedSharesLong = EtmpSippUnquotedShares(
    noOfTransactions = 1,
    transactionDetails = Some(List.fill(2)(etmpSippUnquotedSharesTransactionalDetail))
  )

  private val etmpSippMemberAndTransactionsLongVersion: EtmpSippMemberAndTransactions = EtmpSippMemberAndTransactions(
    version = None,
    status = New,
    memberDetails = memberDetails,
    landConnectedParty = Some(etmpSippLandConnectedPartyLong),
    otherAssetsConnectedParty = None, //Some(etmpSippOtherAssetsConnectedPartyLong),
    landArmsLength = None, //Some(etmpSippLandArmsLengthLong),
    tangibleProperty = None, //Some(etmpSippTangiblePropertyLong),
    loanOutstanding = None, //Some(etmpSippLoanOutstandingLong),
    unquotedShares = None //Some(etmpSippUnquotedSharesLong)
  )

  private val etmpSippMemberAndTransactionsJustMemberDetailsVersion: EtmpSippMemberAndTransactions = EtmpSippMemberAndTransactions(
    version = None,
    status = New,
    memberDetails = memberDetails,
    landConnectedParty = None, //Some(etmpSippLandConnectedPartyLong),
    otherAssetsConnectedParty = None, //Some(etmpSippOtherAssetsConnectedPartyLong),
    landArmsLength = None, //Some(etmpSippLandArmsLengthLong),
    tangibleProperty = None, // Some(etmpSippTangiblePropertyLong),
    loanOutstanding = None, //Some(etmpSippLoanOutstandingLong),
    unquotedShares = None //Some(etmpSippUnquotedSharesLong)
  )

  private val membersAndTransactions: List[EtmpSippMemberAndTransactions] = List.fill(2500)(etmpSippMemberAndTransactionsLongVersion)
  private val membersAndTransactionsV2: List[EtmpSippMemberAndTransactions] = List.fill(57500)(etmpSippMemberAndTransactionsJustMemberDetailsVersion)

  // SIPP - ETMP
  val fullSippPsrSubmissionEtmpRequestLong: SippPsrSubmissionEtmpRequest = SippPsrSubmissionEtmpRequest(

    reportDetails = reportDetails,
    accountingPeriodDetails = Some(accountingPeriodDetails),
    memberAndTransactions = Some(membersAndTransactions ++ membersAndTransactionsV2),
    psrDeclaration = None
  )

}
