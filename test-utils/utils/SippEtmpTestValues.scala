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

import uk.gov.hmrc.pensionschemereturn.models.etmp.Compiled
import uk.gov.hmrc.pensionschemereturn.models.etmp.SectionStatus.New
import uk.gov.hmrc.pensionschemereturn.models.etmp.YesNo.Yes
import uk.gov.hmrc.pensionschemereturn.models.etmp.sipp.common.EtmpSippConnectedOrUnconnectedType.{Connected, Unconnected}
import uk.gov.hmrc.pensionschemereturn.models.etmp.sipp.common.EtmpSippCostOrMarketType.Cost
import uk.gov.hmrc.pensionschemereturn.models.etmp.sipp.common.EtmpSippDispossalDetail.PurchaserDetail
import uk.gov.hmrc.pensionschemereturn.models.etmp.sipp.common.EtmpSippIndOrOrgType.Individual
import uk.gov.hmrc.pensionschemereturn.models.etmp.sipp.common.{EtmpIndividualOrOrgIdentityDetails, EtmpSippAddress, EtmpSippDispossalDetail, EtmpSippJointPropertyDetail, EtmpSippLesseeDetail, EtmpSippRegistryDetails, EtmpSippSharesCompanyDetail, EtmpSippSharesDisposalDetails}
import uk.gov.hmrc.pensionschemereturn.models.etmp.sipp.{EtmpSippAccountingPeriodDetails, EtmpSippLandArmsLength, EtmpSippLandConnectedParty, EtmpSippLoanOutstanding, EtmpSippMemberAndTransactions, EtmpSippMemberDetails, EtmpSippOtherAssetsConnectedParty, EtmpSippReportDetails, EtmpSippTangibleProperty, EtmpSippUnquotedShares, EtmsSippAccountingPeriod}
import uk.gov.hmrc.pensionschemereturn.models.requests.etmp.SippPsrSubmissionEtmpRequest

import java.time.LocalDate

trait SippEtmpTestValues {
  val sampleDate: LocalDate = LocalDate.of(2023, 10, 19)

  private val sippAddress: EtmpSippAddress = EtmpSippAddress(
    addressLine1 = "Flat 9, 76 Bla bla Street",
    addressLine2 = "Another long and Second one",
    addressLine3 = Some("Third Long Line"),
    addressLine4 = Some("Forth Long Line"),
    addressLine5 = Some("Fifth Long Line"),
    ukPostCode = Some("DA18 4XY"),
    countryCode = "GB"
  )

  private val sippRegistryDetails: EtmpSippRegistryDetails = EtmpSippRegistryDetails(
    registryRefExist = Yes,
    registryReference = Some("RegistryReference"),
    noRegistryRefReason = Some("I have a registry and I have entered my registry reference")
  )

  private val sippIndividualOrOrgIdentityDetails: EtmpIndividualOrOrgIdentityDetails =
    EtmpIndividualOrOrgIdentityDetails(
      indivOrOrgType = Individual,
      idNumber = Some("A12345678"),
      reasonNoIdNumber = Some("I have an id number and I have entered my id number"),
      otherDescription = Some("Some other description!")
    )

  private val sippJointPropertyDetail1: EtmpSippJointPropertyDetail = EtmpSippJointPropertyDetail(
    personName = "AnotherLongName Surname",
    nino = Some("QQ123456A"),
    reasonNoNINO = Some("I have a Nino!")
  )

  private val sippJointPropertyDetail2: EtmpSippJointPropertyDetail = EtmpSippJointPropertyDetail(
    personName = "Another AgainLongName Surname",
    nino = Some("QQ123457A"),
    reasonNoNINO = Some("I have a Nino !!!!")
  )

  private val sippLesseeDetail1: EtmpSippLesseeDetail = EtmpSippLesseeDetail(
    lesseeName = "A long lessee name surname",
    lesseeConnectedParty = Connected,
    leaseGrantedDate = sampleDate,
    annualLeaseAmount = 99999.9
  )

  private val sippLesseeDetail2: EtmpSippLesseeDetail = EtmpSippLesseeDetail(
    lesseeName = "A long lessee name surname",
    lesseeConnectedParty = Unconnected,
    leaseGrantedDate = sampleDate,
    annualLeaseAmount = 777777.10
  )

  private val sippDispossalDetail: EtmpSippDispossalDetail = EtmpSippDispossalDetail(
    disposedPropertyProceedsAmt = 89999.99,
    independentValutionDisposal = Yes,
    propertyFullyDisposed = Yes,
    purchaserDetails = List(
      PurchaserDetail(purchaserConnectedParty = Connected, purchaserName = "Another long purchaser name"),
      PurchaserDetail(purchaserConnectedParty = Unconnected, purchaserName = "Wow long purchaser name")
    )
  )

  protected val reportDetails: EtmpSippReportDetails = EtmpSippReportDetails(
    pstr = None,
    status = Compiled,
    periodStart = sampleDate,
    periodEnd = sampleDate,
    memberTransactions = "Yes",
    schemeName = None,
    psrVersion = None
  )

  private val period: EtmsSippAccountingPeriod = EtmsSippAccountingPeriod(
    accPeriodStart = sampleDate,
    accPeriodEnd = sampleDate
  )

  protected val accountingPeriodDetails: EtmpSippAccountingPeriodDetails = EtmpSippAccountingPeriodDetails(
    version = None,
    accountingPeriods = Some(
      List(
        period
      )
    )
  )

  protected val memberDetails = EtmpSippMemberDetails(
    personalDetails = EtmpSippMemberDetails.PersonalDetails(
      firstName = "TestLongFirstName",
      middleName = Some("TestLongMiddleName"),
      lastName = "TestLongLastName",
      nino = Some("QQ123456A"),
      reasonNoNINO = Some("I have a Nino!"),
      dateOfBirth = sampleDate
    ),
    isUKAddress = Yes,
    addressDetails = sippAddress
  )

  private val etmpSippSharesCompanyDetail: EtmpSippSharesCompanyDetail = EtmpSippSharesCompanyDetail(
    companySharesName = "A Long Company Name",
    companySharesCRN = Some("A1231233"),
    reasonNoCRN = Some("I have a CRN NUMBER"),
    sharesClass = "A class",
    noOfShares = 1
  )

  private val individualOrOrgIdentityDetails: EtmpIndividualOrOrgIdentityDetails = EtmpIndividualOrOrgIdentityDetails(
    indivOrOrgType = Individual,
    idNumber = Some("A1231233"),
    reasonNoIdNumber = Some("I have an id number"),
    otherDescription = Some("Other description")
  )

  protected val etmpSippSharesDisposalDetails: EtmpSippSharesDisposalDetails = EtmpSippSharesDisposalDetails(
    disposedShareAmount = 2123.22, disposalConnectedParty = Connected, purchaserName = "Some Long Purchaser Name", independentValutionDisposal = Yes
  )

  protected val etmpSippLandConnectedPartyTransactionDetail: EtmpSippLandConnectedParty.TransactionDetail =
    EtmpSippLandConnectedParty.TransactionDetail(
      acquisitionDate = sampleDate,
      landOrPropertyinUK = Yes,
      addressDetails = sippAddress,
      registryDetails = sippRegistryDetails,
      acquiredFromName = "TestLongName TestLongSurname",
      acquiredFromType = sippIndividualOrOrgIdentityDetails,
      totalCost = 9999999.99,
      independentValution = Yes,
      jointlyHeld = Yes,
      noOfPersons = Some(2),
      jointPropertyPersonDetails = Some(List(sippJointPropertyDetail1, sippJointPropertyDetail2)),
      residentialSchedule29A = Yes,
      isLeased = Yes,
      lesseeDetails = Some(List(sippLesseeDetail1, sippLesseeDetail2)),
      totalIncomeOrReceipts = 99999.00,
      isPropertyDisposed = Yes,
      disposalDetails = Some(sippDispossalDetail)
    )

  protected val etmpSippOtherAssetsConnectedPartyTransactionDetail: EtmpSippOtherAssetsConnectedParty.TransactionDetail =
    EtmpSippOtherAssetsConnectedParty.TransactionDetail(
      acquisitionDate = sampleDate,
      assetDescription = "Some Asset Description",
      acquisitionOfShares = Yes,
      sharesCompanyDetails = Some(etmpSippSharesCompanyDetail),
      acquiredFromName = "Acquired From Name",
      acquiredFromType = individualOrOrgIdentityDetails,
      totalCost = 999999.99,
      independentValution = Yes,
      tangibleSchedule29A = Yes,
      totalIncomeOrReceipts = 999.99,
      isPropertyDisposed = Yes,
      disposalDetails = Some(sippDispossalDetail),
      disposalOfShares = Yes,
      noOfSharesHeld = Some(2)
    )

  protected val etmpSippLandArmsLengthTransactionDetail = EtmpSippLandArmsLength.TransactionDetail(
    acquisitionDate = sampleDate,
    landOrPropertyinUK = Yes,
    addressDetails = sippAddress,
    registryDetails = sippRegistryDetails,
    acquiredFromName = "TestLongName TestLongSurname",
    acquiredFromType = sippIndividualOrOrgIdentityDetails,
    totalCost = 9999999.99,
    independentValution = Yes,
    jointlyHeld = Yes,
    noOfPersons = Some(2),
    jointPropertyPersonDetails = Some(List(sippJointPropertyDetail1, sippJointPropertyDetail2)),
    residentialSchedule29A = Yes,
    isLeased = Yes,
    lesseeDetails = Some(List(sippLesseeDetail1, sippLesseeDetail2)),
    totalIncomeOrReceipts = 99999.00,
    isPropertyDisposed = Yes,
    disposalDetails = Some(sippDispossalDetail)
  )

  protected val etmpSippTangiblePropertyTransactionalDetail = EtmpSippTangibleProperty.TransactionDetail(
    assetDescription = "Some long asset description",
    acquisitionDate = sampleDate,
    totalCost = 9999999.99,
    acquiredFromName = "TestLongName TestLongSurname",
    acquiredFromType = sippIndividualOrOrgIdentityDetails,
    independentValution = Yes,
    totalIncomeOrReceipts = 9999999.99,
    costOrMarket = Cost,
    costMarketValue = 9999999.99,
    isPropertyDisposed = Yes,
    disposalDetails = Some(sippDispossalDetail)
  )

  protected val etmpSippLoanOutstandingTransactionalDetail = EtmpSippLoanOutstanding.TransactionDetail(
   loanRecipientName = "Long Loan Recipient Name",
    indivOrOrgIdentityDetails = individualOrOrgIdentityDetails,
    dateOfLoan = sampleDate,
    amountOfLoan = 9999999.99,
    loanConnectedParty = Connected,
    repayDate = sampleDate,
    interestRate = 11.1,
    loanSecurity = Yes,
    capitalRepayments = 123123.12,
    interestPayments = 11.1,
    arrearsOutstandingPrYears = Yes,
    arrearsOutstandingPrYearsAmt = Some(1231.22),
    outstandingYearEndAmount = 12312.12
  )

  protected val etmpSippUnquotedSharesTransactionalDetail = EtmpSippUnquotedShares.TransactionDetail(
    sharesCompanyDetails = etmpSippSharesCompanyDetail,
    acquiredFromName = "TestLongName TestLongSurname",
    acquiredFromType = sippIndividualOrOrgIdentityDetails,
    totalCost = 9999999.99,
    independentValution = Yes,
    noOfSharesSold = Some(3),
    totalDividendsIncome = 2000.2,
    sharesDisposed = Yes,
    sharesDisposalDetails = etmpSippSharesDisposalDetails,
    noOfSharesHeld = Some(2)
  )

  private val etmpSippLandConnectedParty = EtmpSippLandConnectedParty(
    noOfTransactions = 1,
    transactionDetails = Some(List(etmpSippLandConnectedPartyTransactionDetail))
  )

  private val etmpSippOtherAssetsConnectedParty = EtmpSippOtherAssetsConnectedParty(
    noOfTransactions = 1,
    transactionDetails = Some(List(etmpSippOtherAssetsConnectedPartyTransactionDetail))
  )

  private val etmpSippLandArmsLength = EtmpSippLandArmsLength(
    noOfTransactions = 1,
    transactionDetails = Some(List(etmpSippLandArmsLengthTransactionDetail))
  )

  private val etmpSippTangibleProperty = EtmpSippTangibleProperty(
    noOfTransactions = 1,
    transactionDetails = Some(List(etmpSippTangiblePropertyTransactionalDetail))
  )

  private val etmpSippLoanOutstanding = EtmpSippLoanOutstanding(
    noOfTransactions = 1,
    transactionDetails = Some(List(etmpSippLoanOutstandingTransactionalDetail))
  )

  private val etmpSippUnquotedShares = EtmpSippUnquotedShares(
    noOfTransactions = 1,
    transactionDetails = Some(List(etmpSippUnquotedSharesTransactionalDetail))
  )

  private val etmpSippMemberAndTransactions: EtmpSippMemberAndTransactions = EtmpSippMemberAndTransactions(
    version = None,
    status = New,
    memberDetails = memberDetails,
    landConnectedParty = Some(etmpSippLandConnectedParty),
    otherAssetsConnectedParty = Some(etmpSippOtherAssetsConnectedParty),
    landArmsLength = Some(etmpSippLandArmsLength),
    tangibleProperty = Some(etmpSippTangibleProperty),
    loanOutstanding = Some(etmpSippLoanOutstanding),
    unquotedShares = Some(etmpSippUnquotedShares)
  )

  // SIPP - ETMP
  val fullSippPsrSubmissionEtmpRequest: SippPsrSubmissionEtmpRequest = SippPsrSubmissionEtmpRequest(
    reportDetails = reportDetails,
    accountingPeriodDetails = Some(accountingPeriodDetails),
    memberAndTransactions = Some(List(etmpSippMemberAndTransactions)),
    psrDeclaration = None
  )

}
