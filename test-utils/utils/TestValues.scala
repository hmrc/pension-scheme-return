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

package utils

import uk.gov.hmrc.pensionschemereturn.models.nonsipp.IdentityType.{Individual, UKCompany}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets._
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.common.EtmpIdentityType
import uk.gov.hmrc.pensionschemereturn.config.Constants.{psaEnrolmentKey, psaIdKey}
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.sipp.{SippPsrSubmission, SippReportDetailsSubmission}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets.SchemeHoldBond.Contribution
import uk.gov.hmrc.pensionschemereturn.models.requests.{PsrSubmissionEtmpRequest, SippPsrSubmissionEtmpRequest}
import com.networknt.schema.ValidationMessage
import uk.gov.hmrc.pensionschemereturn.models.response._
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets.SchemeHoldAsset.Transfer
import uk.gov.hmrc.pensionschemereturn.models.etmp.YesNo.{No, Yes}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.shares.Shares
import uk.gov.hmrc.pensionschemereturn.models.etmp._
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets.HowDisposed.Sold
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.memberpayments._
import uk.gov.hmrc.pensionschemereturn.models.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.assets._
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.memberpayments._
import uk.gov.hmrc.pensionschemereturn.models.etmp.sipp.EtmpSippReportDetails
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets.SchemeHoldLandProperty.Acquisition

import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter

trait TestValues {

  val externalId: String = "externalId"
  val enrolments: Enrolments = Enrolments(
    Set(
      Enrolment(
        psaEnrolmentKey,
        Seq(
          EnrolmentIdentifier(psaIdKey, "A0000000")
        ),
        "Activated",
        None
      )
    )
  )
  val pstr = "testPstr"
  val sampleToday: LocalDate = LocalDate.of(2023, 10, 19)

  // Standard - PSR

  val sampleMinimalRequiredSubmission: MinimalRequiredSubmission = MinimalRequiredSubmission(
    reportDetails = ReportDetails(
      fbVersion = Some("001"),
      fbstatus = Some(Compiled.name),
      pstr = pstr,
      periodStart = sampleToday,
      periodEnd = sampleToday,
      compilationOrSubmissionDate = Some(LocalDateTime.parse("2023-04-02T09:30:47"))
    ),
    accountingPeriods = List(sampleToday -> sampleToday),
    schemeDesignatory = SchemeDesignatory(
      reasonForNoBankAccount = None,
      openBankAccount = true,
      activeMembers = 1,
      deferredMembers = 2,
      pensionerMembers = 3,
      totalAssetValueStart = Some(12.34),
      totalAssetValueEnd = None,
      totalCashStart = Some(34.56),
      totalCashEnd = None,
      totalPayments = Some(56.78)
    )
  )

  val samplePsrSubmission: PsrSubmission = PsrSubmission(
    minimalRequiredSubmission = sampleMinimalRequiredSubmission,
    checkReturnDates = true,
    loans = None,
    assets = None,
    membersPayments = None,
    shares = None,
    psrDeclaration = None
  )

  val sampleLoans: Loans = Loans(
    schemeHadLoans = true,
    loanTransactions = List(
      LoanTransactions(
        recipientIdentityType = RecipientIdentityType(
          IdentityType.Individual,
          None,
          Some("NoNinoReason"),
          None
        ),
        loanRecipientName = "IndividualName",
        connectedPartyStatus = true,
        optRecipientSponsoringEmployer = None,
        datePeriodLoanDetails = LoanPeriod(sampleToday, Double.MaxValue, Int.MaxValue),
        loanAmountDetails = LoanAmountDetails(Double.MaxValue, Double.MaxValue, Double.MaxValue),
        equalInstallments = true,
        loanInterestDetails = LoanInterestDetails(Double.MaxValue, Double.MaxValue, Double.MaxValue),
        optSecurityGivenDetails = None,
        optOutstandingArrearsOnLoan = Some(Double.MaxValue)
      )
    )
  )

  val sampleAddress: Address = Address(
    "testAddressLine1",
    Some("testAddressLine2"),
    Some("testAddressLine3"),
    "town",
    Some("GB135HG"),
    "GB"
  )

  val sampleAssets: Assets = Assets(
    optLandOrProperty = Some(
      LandOrProperty(
        landOrPropertyHeld = true,
        disposeAnyLandOrProperty = true,
        landOrPropertyTransactions = Seq(
          LandOrPropertyTransactions(
            propertyDetails = PropertyDetails(
              landOrPropertyInUK = true,
              addressDetails = sampleAddress,
              landRegistryTitleNumberKey = true,
              landRegistryTitleNumberValue = "landRegistryTitleNumberValue"
            ),
            heldPropertyTransaction = HeldPropertyTransaction(
              methodOfHolding = Acquisition,
              dateOfAcquisitionOrContribution = Some(sampleToday),
              optPropertyAcquiredFromName = Some("PropertyAcquiredFromName"),
              optPropertyAcquiredFrom = Some(
                PropertyAcquiredFrom(
                  identityType = UKCompany,
                  idNumber = Some("idNumber"),
                  reasonNoIdNumber = None,
                  otherDescription = None
                )
              ),
              optConnectedPartyStatus = Some(true),
              totalCostOfLandOrProperty = Double.MaxValue,
              optIndepValuationSupport = Some(true),
              isLandOrPropertyResidential = true,
              optLeaseDetails = Some(
                LeaseDetails(
                  lesseeName = "lesseeName",
                  leaseGrantDate = sampleToday,
                  annualLeaseAmount = Double.MaxValue,
                  connectedPartyStatus = true
                )
              ),
              landOrPropertyLeased = true,
              totalIncomeOrReceipts = Double.MaxValue
            ),
            optDisposedPropertyTransaction = Some(
              Seq(
                DisposedPropertyTransaction(
                  methodOfDisposal = Sold,
                  optOtherMethod = None,
                  optDateOfSale = Some(sampleToday),
                  optNameOfPurchaser = Some("NameOfPurchaser"),
                  optPropertyAcquiredFrom = Some(
                    PropertyAcquiredFrom(
                      identityType = Individual,
                      idNumber = Some("idNumber"),
                      reasonNoIdNumber = None,
                      otherDescription = None
                    )
                  ),
                  optSaleProceeds = Some(Double.MaxValue),
                  optConnectedPartyStatus = Some(true),
                  optIndepValuationSupport = Some(false),
                  portionStillHeld = true
                )
              )
            )
          )
        )
      )
    ),
    optBorrowing = Some(
      Borrowing(
        moneyWasBorrowed = true,
        moneyBorrowed = Seq(
          MoneyBorrowed(
            dateOfBorrow = sampleToday,
            schemeAssetsValue = Double.MaxValue,
            amountBorrowed = Double.MaxValue,
            interestRate = Double.MaxValue,
            borrowingFromName = "borrowingFromName",
            connectedPartyStatus = true,
            reasonForBorrow = "reasonForBorrow"
          )
        )
      )
    ),
    optBonds = Some(
      Bonds(
        bondsWereAdded = true,
        bondsWereDisposed = false,
        bondTransactions = Seq(
          BondTransactions(
            nameOfBonds = "nameOfBonds",
            methodOfHolding = Contribution,
            optDateOfAcqOrContrib = Some(sampleToday),
            costOfBonds = Double.MaxValue,
            optConnectedPartyStatus = Some(false),
            bondsUnregulated = false,
            totalIncomeOrReceipts = Double.MaxValue,
            optBondsDisposed = Some(
              Seq(
                BondDisposed(
                  methodOfDisposal = Sold,
                  optOtherMethod = None,
                  optDateSold = Some(sampleToday),
                  optAmountReceived = Some(Double.MaxValue),
                  optBondsPurchaserName = Some("BondsPurchaserName"),
                  optConnectedPartyStatus = Some(true),
                  totalNowHeld = Int.MaxValue
                )
              )
            )
          )
        )
      )
    ),
    optOtherAssets = Some(
      OtherAssets(
        otherAssetsWereHeld = true,
        otherAssetsWereDisposed = true,
        otherAssetTransactions = Seq(
          OtherAssetTransaction(
            assetDescription = "assetDescription",
            methodOfHolding = Transfer,
            optDateOfAcqOrContrib = None,
            costOfAsset = Double.MaxValue,
            optPropertyAcquiredFromName = None,
            optPropertyAcquiredFrom = None,
            optConnectedStatus = None,
            optIndepValuationSupport = None,
            movableSchedule29A = true,
            totalIncomeOrReceipts = Double.MaxValue
          )
        )
      )
    )
  )

  val sampleEmployerContribution1: EmployerContributions = EmployerContributions(
    employerName = "test employer one",
    employerType = EmployerType.UKCompany(Right("test company id")),
    totalTransferValue = 12.34
  )

  val sampleEmployerContribution2: EmployerContributions = EmployerContributions(
    employerName = "test employer two",
    employerType = EmployerType.UKCompany(Left("test reason")),
    totalTransferValue = 34.56
  )

  val sampleEmployerContribution3: EmployerContributions = EmployerContributions(
    employerName = "test employer three",
    employerType = EmployerType.Other("test description"),
    totalTransferValue = 56.78
  )

  val sampleEmployerContributions4: EmployerContributions = EmployerContributions(
    employerName = "test employer four",
    employerType = EmployerType.UKPartnership(Right("test partnership id")),
    totalTransferValue = 78.99
  )

  val sampleTransfersIn1: TransfersIn = TransfersIn(
    schemeName = "test scheme name one",
    dateOfTransfer = LocalDate.of(2010, 10, 10),
    transferSchemeType = PensionSchemeType.RegisteredPS("some pension scheme"),
    transferValue = 12.34,
    transferIncludedAsset = true
  )

  val sampleTransfersIn2: TransfersIn = TransfersIn(
    schemeName = "test scheme name two",
    dateOfTransfer = LocalDate.of(2013, 4, 10),
    transferSchemeType = PensionSchemeType.QualifyingRecognisedOverseasPS("some overseas scheme"),
    transferValue = 34.56,
    transferIncludedAsset = false
  )

  val sampleTransfersOut1: TransfersOut = TransfersOut(
    schemeName = "test scheme name one out",
    dateOfTransfer = LocalDate.of(2010, 10, 10),
    transferSchemeType = PensionSchemeType.RegisteredPS("some pension scheme")
  )

  val sampleTransfersOut2: TransfersOut = TransfersOut(
    schemeName = "test scheme name two out",
    dateOfTransfer = LocalDate.of(2013, 4, 10),
    transferSchemeType = PensionSchemeType.QualifyingRecognisedOverseasPS("some overseas scheme")
  )

  val sampleMemberDetails1: MemberDetails = MemberDetails(
    MemberPersonalDetails(
      firstName = "test first one",
      lastName = "test last one",
      nino = Some("nino"),
      reasonNoNINO = None,
      dateOfBirth = sampleToday
    ),
    employerContributions = List(
      sampleEmployerContribution1,
      sampleEmployerContribution2
    ),
    totalContributions = Some(Double.MaxValue),
    transfersIn = List(
      sampleTransfersIn1
    ),
    memberLumpSumReceived = Some(MemberLumpSumReceived(Double.MaxValue, Double.MaxValue)),
    transfersOut = List(
      sampleTransfersOut1
    ),
    benefitsSurrendered = Some(
      PensionSurrender(
        totalSurrendered = 12.34,
        dateOfSurrender = LocalDate.of(2022, 12, 12),
        surrenderReason = "some reason"
      )
    ),
    pensionAmountReceived = Some(12.34)
  )

  val sampleMemberDetails2: MemberDetails = MemberDetails(
    MemberPersonalDetails(
      firstName = "test first two",
      lastName = "test last two",
      nino = None,
      reasonNoNINO = Some("no nino reason"),
      dateOfBirth = sampleToday
    ),
    employerContributions = List(
      sampleEmployerContribution3,
      sampleEmployerContributions4
    ),
    totalContributions = None,
    transfersIn = List(
      sampleTransfersIn2
    ),
    memberLumpSumReceived = None,
    transfersOut = List(
      sampleTransfersOut2
    ),
    benefitsSurrendered = Some(
      PensionSurrender(
        totalSurrendered = 12.34,
        dateOfSurrender = LocalDate.of(2022, 12, 12),
        surrenderReason = "some reason"
      )
    ),
    pensionAmountReceived = Some(12.34)
  )

  val sampleUnallocatedContribAmount: Double = 201.34

  val sampleMemberPayments: MemberPayments = MemberPayments(
    unallocatedContribsMade = true,
    unallocatedContribAmount = Some(sampleUnallocatedContribAmount),
    memberDetails = List(
      sampleMemberDetails1,
      sampleMemberDetails2
    ),
    employerContributionsDetails = SectionDetails(made = true, completed = true),
    memberContributionMade = true,
    transfersInCompleted = true,
    transfersOutCompleted = true,
    lumpSumReceived = true,
    pensionReceived = true,
    benefitsSurrenderedDetails = SectionDetails(made = true, completed = true)
  )

  val sampleShares: Shares = Shares(optShareTransactions = None, optTotalValueQuotedShares = None)

  val samplePsrDeclaration: PsrDeclaration = PsrDeclaration(
    submittedBy = PSA,
    submitterId = "A0000000",
    optAuthorisingPSAID = None,
    declaration1 = true,
    declaration2 = true
  )

  // Standard - ETMP

  val sampleEtmpMinimalRequiredSubmission: EtmpMinimalRequiredSubmission = EtmpMinimalRequiredSubmission(
    EtmpReportDetails(
      pstr = None,
      psrStatus = Compiled,
      periodStart = sampleToday,
      periodEnd = sampleToday
    ),
    EtmpAccountingPeriodDetails(
      recordVersion = None,
      accountingPeriods = List(
        EtmpAccountingPeriod(
          accPeriodStart = sampleToday,
          accPeriodEnd = sampleToday
        )
      )
    ),
    EtmpSchemeDesignatory(
      recordVersion = None,
      openBankAccount = "Yes",
      reasonNoOpenAccount = None,
      noOfActiveMembers = 1,
      noOfDeferredMembers = 2,
      noOfPensionerMembers = 3,
      totalAssetValueStart = Some(12.34),
      totalAssetValueEnd = None,
      totalCashStart = Some(34.56),
      totalCashEnd = None,
      totalPayments = Some(56.78)
    )
  )

  private val sampleEtmpAccountingPeriodDetails: EtmpAccountingPeriodDetails = EtmpAccountingPeriodDetails(
    recordVersion = Some("002"),
    accountingPeriods = List(
      EtmpAccountingPeriod(
        accPeriodStart = LocalDate.parse("2022-04-06"),
        accPeriodEnd = LocalDate.parse("2022-12-31")
      ),
      EtmpAccountingPeriod(
        accPeriodStart = LocalDate.parse("2023-01-01"),
        accPeriodEnd = LocalDate.parse("2023-04-05")
      )
    )
  )

  private val etmpAddress: EtmpAddress = EtmpAddress(
    addressLine1 = "testAddressLine1",
    addressLine2 = "testAddressLine2",
    addressLine3 = Some("testAddressLine3"),
    addressLine4 = None,
    addressLine5 = None,
    ukPostCode = Some("GB135HG"),
    countryCode = "GB"
  )

  private val sampleEtmpLandOrProperty: EtmpLandOrProperty = EtmpLandOrProperty(
    recordVersion = Some("001"),
    heldAnyLandOrProperty = "Yes",
    disposeAnyLandOrProperty = "Yes",
    noOfTransactions = Some(1),
    landOrPropertyTransactions = Some(
      List(
        EtmpLandOrPropertyTransactions(
          propertyDetails = EtmpPropertyDetails(
            landOrPropertyInUK = "Yes",
            addressDetails = etmpAddress,
            landRegistryDetails = EtmpLandRegistryDetails(
              landRegistryReferenceExists = "Yes",
              landRegistryReference = Some("landRegistryTitleNumberValue"),
              reasonNoReference = None
            )
          ),
          heldPropertyTransaction = EtmpHeldPropertyTransaction(
            methodOfHolding = "01",
            dateOfAcquisitionOrContribution = Some(sampleToday),
            propertyAcquiredFromName = Some("PropertyAcquiredFromName"),
            propertyAcquiredFrom = Some(
              EtmpIdentityType(
                indivOrOrgType = "02",
                idNumber = Some("idNumber"),
                reasonNoIdNumber = None,
                otherDescription = None
              )
            ),
            connectedPartyStatus = Some("01"),
            totalCostOfLandOrProperty = Double.MaxValue,
            indepValuationSupport = Some("Yes"),
            residentialSchedule29A = "Yes",
            landOrPropertyLeased = "Yes",
            leaseDetails = Some(
              EtmpLeaseDetails(
                lesseeName = "lesseeName",
                connectedPartyStatus = "01",
                leaseGrantDate = sampleToday,
                annualLeaseAmount = Double.MaxValue
              )
            ),
            totalIncomeOrReceipts = Double.MaxValue
          ),
          disposedPropertyTransaction = Some(
            List(
              EtmpDisposedPropertyTransaction(
                methodOfDisposal = "01",
                otherMethod = None,
                dateOfSale = Some(sampleToday),
                nameOfPurchaser = Some("NameOfPurchaser"),
                purchaseOrgDetails = Some(
                  EtmpIdentityType(
                    indivOrOrgType = "01",
                    idNumber = Some("idNumber"),
                    reasonNoIdNumber = None,
                    otherDescription = None
                  )
                ),
                saleProceeds = Some(Double.MaxValue),
                connectedPartyStatus = Some("01"),
                indepValuationSupport = Some("No"),
                portionStillHeld = "Yes"
              )
            )
          )
        )
      )
    )
  )

  val sampleEtmpLoans: EtmpLoans = EtmpLoans(
    recordVersion = Some("001"),
    schemeHadLoans = "Yes",
    noOfLoans = Some(1),
    loanTransactions = Some(
      List(
        EtmpLoanTransactions(
          dateOfLoan = sampleToday,
          loanRecipientName = "UKPartnershipName",
          recipientIdentityType = EtmpIdentityType(
            indivOrOrgType = "03",
            idNumber = Some("1234567890"),
            reasonNoIdNumber = None,
            otherDescription = None
          ),
          recipientSponsoringEmployer = "Yes",
          connectedPartyStatus = "02",
          loanAmount = Double.MaxValue,
          loanInterestAmount = Double.MaxValue,
          loanTotalSchemeAssets = Double.MaxValue,
          loanPeriodInMonths = Int.MaxValue,
          equalInstallments = "No",
          loanInterestRate = Double.MaxValue,
          securityGiven = "Yes",
          securityDetails = Some("SecurityGivenDetails"),
          capRepaymentCY = Double.MaxValue,
          intReceivedCY = Double.MaxValue,
          arrearsPrevYears = "No",
          amountOfArrears = None,
          amountOutstanding = Double.MaxValue
        )
      )
    )
  )

  val sampleEtmpAssets: EtmpAssets = EtmpAssets(
    landOrProperty = Some(sampleEtmpLandOrProperty),
    borrowing = Some(
      EtmpBorrowing(
        recordVersion = None,
        moneyWasBorrowed = "moneyWasBorrowed",
        noOfBorrows = None,
        moneyBorrowed = None
      )
    ),
    bonds = Some(
      EtmpBonds(
        recordVersion = None,
        bondsWereAdded = "bondsWereAdded",
        bondsWereDisposed = "bondsWereDisposed",
        noOfTransactions = None,
        bondTransactions = None
      )
    ),
    otherAssets = Some(
      EtmpOtherAssets(
        otherAssetsWereHeld = "otherAssetsWereHeld",
        otherAssetsWereDisposed = "otherAssetsWereDisposed",
        noOfTransactions = None,
        otherAssetTransactions = None
      )
    )
  )

  val sampleEtmpMemberPayments: EtmpMemberPayments = EtmpMemberPayments(
    recordVersion = None,
    employerContributionMade = Yes,
    unallocatedContribsMade = Yes,
    unallocatedContribAmount = Some(sampleUnallocatedContribAmount),
    memberContributionMade = Yes,
    schemeReceivedTransferIn = Yes,
    schemeMadeTransferOut = Yes,
    lumpSumReceived = Yes,
    pensionReceived = Yes,
    surrenderMade = Yes,
    memberDetails = List(
      EtmpMemberDetails(
        memberStatus = SectionStatus.New,
        memberPSRVersion = "001",
        noOfContributions = Some(2),
        totalContributions = Some(Double.MaxValue),
        noOfTransfersIn = Some(1),
        noOfTransfersOut = Some(1),
        pensionAmountReceived = Some(12.34),
        personalDetails = EtmpMemberPersonalDetails(
          foreName = sampleMemberDetails1.personalDetails.firstName,
          middleName = None,
          lastName = sampleMemberDetails1.personalDetails.lastName,
          nino = sampleMemberDetails1.personalDetails.nino,
          reasonNoNINO = None,
          dateOfBirth = sampleToday
        ),
        memberEmpContribution = Some(
          List(
            EtmpEmployerContributions(
              orgName = sampleEmployerContribution1.employerName,
              organisationIdentity = OrganisationIdentity(
                orgType = EmployerContributionsOrgType.UKCompany,
                idNumber = Some("test company id"),
                reasonNoIdNumber = None,
                otherDescription = None
              ),
              totalContribution = 12.34
            ),
            EtmpEmployerContributions(
              orgName = sampleEmployerContribution2.employerName,
              organisationIdentity = OrganisationIdentity(
                orgType = EmployerContributionsOrgType.UKCompany,
                idNumber = None,
                reasonNoIdNumber = Some("test reason"),
                otherDescription = None
              ),
              totalContribution = 34.56
            )
          )
        ),
        memberTransfersIn = Some(
          List(
            EtmpTransfersIn(
              schemeName = sampleTransfersIn1.schemeName,
              dateOfTransfer = sampleTransfersIn1.dateOfTransfer,
              transferSchemeType = TransferSchemeType.registeredScheme("some pension scheme"),
              transferValue = 12.34,
              transferIncludedAsset = Yes
            )
          )
        ),
        memberTransfersOut = Some(
          List(
            EtmpTransfersOut(
              schemeName = sampleTransfersOut1.schemeName,
              dateOfTransfer = sampleTransfersOut1.dateOfTransfer,
              transferSchemeType = TransferSchemeType.registeredScheme("some pension scheme")
            )
          )
        ),
        memberLumpSumReceived = Some(List(EtmpMemberLumpSumReceived(Double.MaxValue, Double.MaxValue))),
        memberPensionSurrender = Some(
          List(
            EtmpPensionSurrender(
              totalSurrendered = 12.34,
              dateOfSurrender = LocalDate.of(2022, 12, 12),
              surrenderReason = "some reason"
            )
          )
        )
      ),
      EtmpMemberDetails(
        memberStatus = SectionStatus.New,
        memberPSRVersion = "001",
        noOfContributions = Some(2),
        totalContributions = None,
        noOfTransfersIn = Some(1),
        noOfTransfersOut = Some(1),
        pensionAmountReceived = Some(12.34),
        personalDetails = EtmpMemberPersonalDetails(
          foreName = "test first two",
          middleName = None,
          lastName = "test last two",
          nino = None,
          reasonNoNINO = Some("no nino reason"),
          dateOfBirth = sampleToday
        ),
        memberEmpContribution = Some(
          List(
            EtmpEmployerContributions(
              orgName = sampleEmployerContribution3.employerName,
              organisationIdentity = OrganisationIdentity(
                orgType = EmployerContributionsOrgType.Other,
                idNumber = None,
                reasonNoIdNumber = None,
                otherDescription = Some("test description")
              ),
              totalContribution = 56.78
            ),
            EtmpEmployerContributions(
              orgName = sampleEmployerContributions4.employerName,
              organisationIdentity = OrganisationIdentity(
                orgType = EmployerContributionsOrgType.UKPartnership,
                idNumber = Some("test partnership id"),
                reasonNoIdNumber = None,
                otherDescription = None
              ),
              totalContribution = 78.99
            )
          )
        ),
        memberTransfersIn = Some(
          List(
            EtmpTransfersIn(
              schemeName = sampleTransfersIn2.schemeName,
              dateOfTransfer = sampleTransfersIn2.dateOfTransfer,
              transferSchemeType = TransferSchemeType.qrops("some overseas scheme"),
              transferValue = 34.56,
              transferIncludedAsset = No
            )
          )
        ),
        memberTransfersOut = Some(
          List(
            EtmpTransfersOut(
              schemeName = sampleTransfersOut2.schemeName,
              dateOfTransfer = sampleTransfersOut2.dateOfTransfer,
              transferSchemeType = TransferSchemeType.qrops("some overseas scheme")
            )
          )
        ),
        memberLumpSumReceived = None,
        memberPensionSurrender = Some(
          List(
            EtmpPensionSurrender(
              totalSurrendered = 12.34,
              dateOfSurrender = LocalDate.of(2022, 12, 12),
              surrenderReason = "some reason"
            )
          )
        )
      )
    )
  )

  val sampleEtmpShares: EtmpShares = EtmpShares(
    recordVersion = Some("001"),
    sponsorEmployerSharesWereHeld = Yes,
    noOfSponsEmplyrShareTransactions = Some(1),
    unquotedSharesWereHeld = Yes,
    noOfUnquotedShareTransactions = Some(1),
    connectedPartySharesWereHeld = Yes,
    noOfConnPartyTransactions = Some(1),
    sponsorEmployerSharesWereDisposed = No,
    unquotedSharesWereDisposed = No,
    connectedPartySharesWereDisposed = No,
    shareTransactions = Some(
      List(
        EtmpShareTransaction(
          typeOfSharesHeld = "01",
          shareIdentification = EtmpShareIdentification(
            nameOfSharesCompany = "AppleSauce Inc.",
            crnNumber = None,
            reasonNoCRN = Some("Not able to locate Company on Companies House"),
            classOfShares = Some("Ordinary Shares")
          ),
          heldSharesTransaction = EtmpHeldSharesTransaction(
            methodOfHolding = "01",
            dateOfAcqOrContrib = Some(sampleToday),
            totalShares = 200,
            acquiredFromName = "Fredd Bloggs",
            acquiredFromType = EtmpIdentityType(
              indivOrOrgType = "01",
              idNumber = Some("JE123176A"),
              reasonNoIdNumber = None,
              otherDescription = None
            ),
            connectedPartyStatus = Some("02"),
            costOfShares = 10000,
            supportedByIndepValuation = Yes,
            totalAssetValue = Some(2000),
            totalDividendsOrReceipts = 500
          ),
          disposedSharesTransaction = Some(
            List(
              EtmpDisposedSharesTransaction(
                methodOfDisposal = "04",
                otherMethod = Some("otherMethod"),
                salesQuestions = None,
                redemptionQuestions = None,
                totalSharesNowHeld = 2
              )
            )
          )
        )
      )
    ),
    totalValueQuotedShares = 0.00
  )

  val sampleEtmpPsrDeclaration: EtmpPsrDeclaration =
    EtmpPsrDeclaration(
      submittedBy = PSA,
      submitterId = "A2100005",
      psaId = None,
      psaDeclaration = Some(EtmpPsaDeclaration(psaDeclaration1 = true, psaDeclaration2 = true)),
      pspDeclaration = None
    )

  val samplePsrSubmissionEtmpResponse: PsrSubmissionEtmpResponse = PsrSubmissionEtmpResponse(
    schemeDetails = EtmpSchemeDetails(pstr = "12345678AA", schemeName = "My Golden Egg scheme"),
    psrDetails = EtmpPsrDetails(
      fbVersion = "001",
      fbstatus = Compiled,
      periodStart = LocalDate.parse("2023-04-06"),
      periodEnd = LocalDate.parse("2024-04-05"),
      compilationOrSubmissionDate = LocalDateTime.parse("2023-12-17T09:30:47Z", DateTimeFormatter.ISO_DATE_TIME)
    ),
    accountingPeriodDetails = sampleEtmpAccountingPeriodDetails,
    schemeDesignatory = EtmpSchemeDesignatory(
      recordVersion = Some("002"),
      openBankAccount = "Yes",
      reasonNoOpenAccount = None,
      noOfActiveMembers = 5,
      noOfDeferredMembers = 2,
      noOfPensionerMembers = 10,
      totalAssetValueStart = Some(10000000),
      totalAssetValueEnd = Some(11000000),
      totalCashStart = Some(2500000),
      totalCashEnd = Some(2800000),
      totalPayments = Some(2000000)
    ),
    loans = Some(
      EtmpLoans(
        recordVersion = Some("003"),
        schemeHadLoans = "Yes",
        noOfLoans = Some(1),
        loanTransactions = Some(
          Seq(
            EtmpLoanTransactions(
              dateOfLoan = LocalDate.parse("2023-03-30"),
              loanRecipientName = "Electric Car Co.",
              recipientIdentityType = EtmpIdentityType(
                indivOrOrgType = "01",
                idNumber = None,
                reasonNoIdNumber = None,
                otherDescription = Some("Identification not on record.")
              ),
              recipientSponsoringEmployer = "No",
              connectedPartyStatus = "01",
              loanAmount = 10000,
              loanInterestAmount = 2000,
              loanTotalSchemeAssets = 2000,
              loanPeriodInMonths = 24,
              equalInstallments = "Yes",
              loanInterestRate = 5.55,
              securityGiven = "Yes",
              securityDetails = Some("Japanese ming vase #344343444."),
              capRepaymentCY = 5000,
              intReceivedCY = 555,
              arrearsPrevYears = "No",
              amountOfArrears = None,
              amountOutstanding = 5000
            )
          )
        )
      )
    ),
    assets = Some(
      EtmpAssets(
        landOrProperty = Some(sampleEtmpLandOrProperty),
        borrowing = Some(
          EtmpBorrowing(
            recordVersion = Some("164"),
            moneyWasBorrowed = "Yes",
            noOfBorrows = Some(1),
            moneyBorrowed = Some(
              Seq(
                EtmpMoneyBorrowed(
                  dateOfBorrow = sampleToday,
                  schemeAssetsValue = Double.MaxValue,
                  amountBorrowed = Double.MaxValue,
                  interestRate = Double.MaxValue,
                  borrowingFromName = "borrowingFromName",
                  connectedPartyStatus = "01",
                  reasonForBorrow = "reasonForBorrow"
                )
              )
            )
          )
        ),
        bonds = Some(
          EtmpBonds(
            recordVersion = Some("528"),
            bondsWereAdded = "Yes",
            bondsWereDisposed = "Yes",
            noOfTransactions = Some(2),
            bondTransactions = Some(
              Seq(
                EtmpBondTransactions(
                  nameOfBonds = "Xenex Bonds",
                  methodOfHolding = "01",
                  dateOfAcqOrContrib = Some(sampleToday),
                  costOfBonds = 10234.56,
                  connectedPartyStatus = Some("02"),
                  bondsUnregulated = "No",
                  totalIncomeOrReceipts = 50.0,
                  bondsDisposed = Some(
                    Seq(
                      EtmpBondsDisposed(
                        methodOfDisposal = "01",
                        otherMethod = None,
                        dateSold = Some(sampleToday),
                        amountReceived = Some(12333.59),
                        bondsPurchaserName = Some("Happy Bond Buyers Inc."),
                        connectedPartyStatus = Some("02"),
                        totalNowHeld = 120
                      )
                    )
                  )
                ),
                EtmpBondTransactions(
                  nameOfBonds = "Really Goods Bonds ABC",
                  methodOfHolding = "03",
                  dateOfAcqOrContrib = Some(sampleToday),
                  costOfBonds = 2000.5,
                  connectedPartyStatus = Some("02"),
                  bondsUnregulated = "No",
                  totalIncomeOrReceipts = 300,
                  bondsDisposed = Some(
                    Seq(
                      EtmpBondsDisposed(
                        methodOfDisposal = "01",
                        otherMethod = None,
                        dateSold = Some(sampleToday),
                        amountReceived = Some(3333.33),
                        bondsPurchaserName = Some("Bonds Buyers (PTY) Ltd"),
                        connectedPartyStatus = Some("01"),
                        totalNowHeld = 50
                      )
                    )
                  )
                )
              )
            )
          )
        ),
        otherAssets = Some(
          EtmpOtherAssets(
            otherAssetsWereHeld = "Yes",
            otherAssetsWereDisposed = "No",
            noOfTransactions = Some(1),
            otherAssetTransactions = Some(
              Seq(
                EtmpOtherAssetTransaction(
                  assetDescription = "Box of matches",
                  methodOfHolding = "01",
                  dateOfAcqOrContrib = Some(sampleToday),
                  costOfAsset = Double.MaxValue,
                  acquiredFromName = Some("Dodgy Den Match Co."),
                  acquiredFromType = Some(
                    EtmpIdentityType(
                      indivOrOrgType = "01",
                      idNumber = None,
                      reasonNoIdNumber = Some("reasonNoId"),
                      otherDescription = None
                    )
                  ),
                  connectedStatus = Some("01"),
                  supportedByIndepValuation = Some("No"),
                  movableSchedule29A = "No",
                  totalIncomeOrReceipts = Double.MaxValue
                )
              )
            )
          )
        )
      )
    ),
    membersPayments = Some(
      EtmpMemberPayments(
        recordVersion = Some("002"),
        employerContributionMade = Yes,
        unallocatedContribsMade = No,
        unallocatedContribAmount = None,
        memberContributionMade = Yes,
        schemeReceivedTransferIn = Yes,
        schemeMadeTransferOut = Yes,
        lumpSumReceived = Yes,
        pensionReceived = Yes,
        surrenderMade = Yes,
        memberDetails = List(
          EtmpMemberDetails(
            memberStatus = SectionStatus.Changed,
            memberPSRVersion = "001",
            noOfContributions = Some(2),
            totalContributions = Some(30000.0),
            noOfTransfersIn = Some(2),
            noOfTransfersOut = Some(2),
            pensionAmountReceived = Some(12000.0),
            personalDetails = EtmpMemberPersonalDetails(
              foreName = "Ferdinand",
              middleName = Some("Felix"),
              lastName = "Bull",
              nino = Some("EB103145A"),
              reasonNoNINO = None,
              dateOfBirth = LocalDate.of(1960, 5, 31)
            ),
            memberEmpContribution = Some(
              List(
                EtmpEmployerContributions(
                  orgName = "Acme Ltd",
                  organisationIdentity = OrganisationIdentity(
                    orgType = EmployerContributionsOrgType.UKCompany,
                    idNumber = Some("AC123456"),
                    reasonNoIdNumber = None,
                    otherDescription = None
                  ),
                  totalContribution = 20000.0
                ),
                EtmpEmployerContributions(
                  orgName = "UK Company Ltd",
                  organisationIdentity = OrganisationIdentity(
                    orgType = EmployerContributionsOrgType.UKCompany,
                    idNumber = Some("AC123456"),
                    reasonNoIdNumber = None,
                    otherDescription = None
                  ),
                  totalContribution = 10000.0
                )
              )
            ),
            memberTransfersIn = Some(
              List(
                EtmpTransfersIn(
                  schemeName = "The Happy Retirement Scheme",
                  dateOfTransfer = LocalDate.of(2022, 8, 8),
                  transferSchemeType = TransferSchemeType.qrops("Q123456"),
                  transferValue = 10000.0,
                  transferIncludedAsset = No
                ),
                EtmpTransfersIn(
                  schemeName = "The Happy Retirement Scheme",
                  dateOfTransfer = LocalDate.of(2022, 11, 27),
                  transferSchemeType = TransferSchemeType.qrops("Q123456"),
                  transferValue = 8000.0,
                  transferIncludedAsset = No
                )
              )
            ),
            memberTransfersOut = Some(
              List(
                EtmpTransfersOut(
                  schemeName = "The Golden Egg Scheme",
                  dateOfTransfer = LocalDate.of(2022, 9, 30),
                  transferSchemeType = TransferSchemeType.registeredScheme("76509173AA")
                ),
                EtmpTransfersOut(
                  schemeName = "The Golden Egg Scheme",
                  dateOfTransfer = LocalDate.of(2022, 12, 20),
                  transferSchemeType = TransferSchemeType.registeredScheme("76509173AB")
                )
              )
            ),
            memberLumpSumReceived = Some(List(EtmpMemberLumpSumReceived(30000.0, 20000.00))),
            memberPensionSurrender = Some(
              List(
                EtmpPensionSurrender(
                  totalSurrendered = 1000.0,
                  dateOfSurrender = LocalDate.of(2022, 12, 19),
                  surrenderReason = "ABC"
                ),
                EtmpPensionSurrender(
                  totalSurrendered = 2000.0,
                  dateOfSurrender = LocalDate.of(2023, 2, 8),
                  surrenderReason = "I felt like giving money away..."
                )
              )
            )
          ),
          EtmpMemberDetails(
            memberStatus = SectionStatus.Changed,
            memberPSRVersion = "001",
            noOfContributions = Some(2),
            totalContributions = Some(20000.0),
            noOfTransfersIn = Some(2),
            noOfTransfersOut = Some(2),
            pensionAmountReceived = None,
            personalDetails = EtmpMemberPersonalDetails(
              foreName = "Johnny",
              middleName = Some("Be"),
              lastName = "Quicke",
              nino = None,
              reasonNoNINO = Some("Could not find it on record."),
              dateOfBirth = LocalDate.of(1940, 10, 31)
            ),
            memberEmpContribution = Some(
              List(
                EtmpEmployerContributions(
                  orgName = "Sofa Inc.",
                  organisationIdentity = OrganisationIdentity(
                    orgType = EmployerContributionsOrgType.Other,
                    idNumber = None,
                    reasonNoIdNumber = None,
                    otherDescription = Some("Found it down back of my sofa")
                  ),
                  totalContribution = 10000.0
                ),
                EtmpEmployerContributions(
                  orgName = "UK Company XYZ Ltd.",
                  organisationIdentity = OrganisationIdentity(
                    orgType = EmployerContributionsOrgType.UKCompany,
                    idNumber = Some("CC123456"),
                    reasonNoIdNumber = None,
                    otherDescription = None
                  ),
                  totalContribution = 10000.0
                )
              )
            ),
            memberTransfersIn = Some(
              List(
                EtmpTransfersIn(
                  schemeName = "Golden Years Pension Scheme",
                  dateOfTransfer = LocalDate.of(2022, 12, 2),
                  transferSchemeType = TransferSchemeType.registeredScheme("88390774ZZ"),
                  transferValue = 50000.0,
                  transferIncludedAsset = Yes
                ),
                EtmpTransfersIn(
                  schemeName = "Golden Goose Egg Laying Scheme",
                  dateOfTransfer = LocalDate.of(2022, 10, 30),
                  transferSchemeType = TransferSchemeType.qrops("Q654321"),
                  transferValue = 2000.0,
                  transferIncludedAsset = No
                )
              )
            ),
            memberTransfersOut = Some(
              List(
                EtmpTransfersOut(
                  schemeName = "Dodgy Pensions Ltd",
                  dateOfTransfer = LocalDate.of(2022, 5, 30),
                  transferSchemeType = TransferSchemeType.other("Unknown identifier")
                ),
                EtmpTransfersOut(
                  schemeName = "My back pocket Pension Scheme",
                  dateOfTransfer = LocalDate.of(2022, 7, 31),
                  transferSchemeType = TransferSchemeType.qrops("Q000002")
                )
              )
            ),
            memberLumpSumReceived = None,
            memberPensionSurrender = None
          )
        )
      )
    ),
    shares = Some(
      EtmpShares(
        recordVersion = Some("001"),
        sponsorEmployerSharesWereHeld = Yes,
        noOfSponsEmplyrShareTransactions = Some(1),
        unquotedSharesWereHeld = Yes,
        noOfUnquotedShareTransactions = Some(1),
        connectedPartySharesWereHeld = Yes,
        noOfConnPartyTransactions = Some(1),
        sponsorEmployerSharesWereDisposed = No,
        unquotedSharesWereDisposed = No,
        connectedPartySharesWereDisposed = No,
        shareTransactions = Some(
          List(
            EtmpShareTransaction(
              typeOfSharesHeld = "01",
              shareIdentification = EtmpShareIdentification(
                nameOfSharesCompany = "AppleSauce Inc.",
                crnNumber = None,
                reasonNoCRN = Some("Not able to locate Company on Companies House"),
                classOfShares = Some("Ordinary Shares")
              ),
              heldSharesTransaction = EtmpHeldSharesTransaction(
                methodOfHolding = "01",
                dateOfAcqOrContrib = Some(sampleToday),
                totalShares = 200,
                acquiredFromName = "Fredd Bloggs",
                acquiredFromType = EtmpIdentityType(
                  indivOrOrgType = "01",
                  idNumber = Some("JE123176A"),
                  reasonNoIdNumber = None,
                  otherDescription = None
                ),
                connectedPartyStatus = Some("02"),
                costOfShares = 10000,
                supportedByIndepValuation = Yes,
                totalAssetValue = Some(2000),
                totalDividendsOrReceipts = 500
              ),
              disposedSharesTransaction = Some(
                List(
                  EtmpDisposedSharesTransaction(
                    methodOfDisposal = "01",
                    otherMethod = None,
                    salesQuestions = Some(
                      EtmpSalesQuestions(
                        dateOfSale = LocalDate.of(2023, 2, 16),
                        noOfSharesSold = 50,
                        amountReceived = 8000.0,
                        nameOfPurchaser = "Sharebuyers Inc.",
                        purchaserType = EtmpIdentityType(
                          indivOrOrgType = "01",
                          idNumber = Some("0008503350"),
                          reasonNoIdNumber = None,
                          otherDescription = None
                        ),
                        connectedPartyStatus = "02",
                        supportedByIndepValuation = Yes
                      )
                    ),
                    redemptionQuestions = None,
                    totalSharesNowHeld = 150
                  ),
                  EtmpDisposedSharesTransaction(
                    methodOfDisposal = "02",
                    otherMethod = None,
                    salesQuestions = None,
                    redemptionQuestions = Some(
                      EtmpRedemptionQuestions(
                        dateOfRedemption = LocalDate.of(2023, 3, 6),
                        noOfSharesRedeemed = 50,
                        amountReceived = 7600.0
                      )
                    ),
                    totalSharesNowHeld = 100
                  )
                )
              )
            ),
            EtmpShareTransaction(
              typeOfSharesHeld = "03",
              shareIdentification = EtmpShareIdentification(
                nameOfSharesCompany = "Pear Computers Inc.",
                crnNumber = Some("LP289157"),
                reasonNoCRN = None,
                classOfShares = Some("Preferred Shares")
              ),
              heldSharesTransaction = EtmpHeldSharesTransaction(
                methodOfHolding = "01",
                dateOfAcqOrContrib = Some(sampleToday),
                totalShares = 10000,
                acquiredFromName = "Golden Investments Ltd.",
                acquiredFromType = EtmpIdentityType(
                  indivOrOrgType = "03",
                  idNumber = Some("28130262"),
                  reasonNoIdNumber = None,
                  otherDescription = None
                ),
                connectedPartyStatus = Some("02"),
                costOfShares = 50000,
                supportedByIndepValuation = Yes,
                totalAssetValue = Some(40000),
                totalDividendsOrReceipts = 200
              ),
              disposedSharesTransaction = Some(
                List(
                  EtmpDisposedSharesTransaction(
                    methodOfDisposal = "01",
                    otherMethod = None,
                    salesQuestions = Some(
                      EtmpSalesQuestions(
                        dateOfSale = LocalDate.of(2022, 10, 31),
                        noOfSharesSold = 1100,
                        amountReceived = 30000,
                        nameOfPurchaser = "Share Acquisitions Inc.",
                        purchaserType = EtmpIdentityType(
                          indivOrOrgType = "01",
                          idNumber = Some("JJ507888A"),
                          reasonNoIdNumber = None,
                          otherDescription = None
                        ),
                        connectedPartyStatus = "02",
                        supportedByIndepValuation = Yes
                      )
                    ),
                    redemptionQuestions = None,
                    totalSharesNowHeld = 8000
                  ),
                  EtmpDisposedSharesTransaction(
                    methodOfDisposal = "02",
                    otherMethod = None,
                    salesQuestions = None,
                    redemptionQuestions = Some(
                      EtmpRedemptionQuestions(
                        dateOfRedemption = LocalDate.of(2022, 12, 20),
                        noOfSharesRedeemed = 900,
                        amountReceived = 27005.78
                      )
                    ),
                    totalSharesNowHeld = 8000
                  )
                )
              )
            ),
            EtmpShareTransaction(
              typeOfSharesHeld = "03",
              shareIdentification = EtmpShareIdentification(
                nameOfSharesCompany = "Connected Party Inc.",
                crnNumber = Some("LP289157"),
                reasonNoCRN = None,
                classOfShares = Some("Convertible Preference Shares")
              ),
              heldSharesTransaction = EtmpHeldSharesTransaction(
                methodOfHolding = "02",
                dateOfAcqOrContrib = Some(sampleToday),
                totalShares = 1000,
                acquiredFromName = "Investec Inc.",
                acquiredFromType = EtmpIdentityType(
                  indivOrOrgType = "02",
                  idNumber = Some("0000123456"),
                  reasonNoIdNumber = None,
                  otherDescription = None
                ),
                connectedPartyStatus = Some("02"),
                costOfShares = 120220.34,
                supportedByIndepValuation = Yes,
                totalAssetValue = Some(10000),
                totalDividendsOrReceipts = 599.99
              ),
              disposedSharesTransaction = Some(
                List(
                  EtmpDisposedSharesTransaction(
                    methodOfDisposal = "02",
                    otherMethod = None,
                    salesQuestions = None,
                    redemptionQuestions = Some(
                      EtmpRedemptionQuestions(
                        dateOfRedemption = LocalDate.of(2022, 11, 3),
                        noOfSharesRedeemed = 200,
                        amountReceived = 50000
                      )
                    ),
                    totalSharesNowHeld = 800
                  ),
                  EtmpDisposedSharesTransaction(
                    methodOfDisposal = "01",
                    otherMethod = None,
                    salesQuestions = Some(
                      EtmpSalesQuestions(
                        dateOfSale = LocalDate.of(2022, 12, 31),
                        noOfSharesSold = 200,
                        amountReceived = 52000,
                        nameOfPurchaser = "Sam Smithsonian",
                        purchaserType = EtmpIdentityType(
                          indivOrOrgType = "01",
                          idNumber = Some("JE443364A"),
                          reasonNoIdNumber = None,
                          otherDescription = None
                        ),
                        connectedPartyStatus = "01",
                        supportedByIndepValuation = Yes
                      )
                    ),
                    redemptionQuestions = None,
                    totalSharesNowHeld = 400
                  )
                )
              )
            )
          )
        ),
        totalValueQuotedShares = 0.00
      )
    ),
    psrDeclaration = Some(
      EtmpPsrDeclaration(
        submittedBy = PSP,
        submitterId = "21000005",
        psaId = Some("A2100005"),
        psaDeclaration = None,
        pspDeclaration = Some(EtmpPspDeclaration(pspDeclaration1 = true, pspDeclaration2 = true))
      )
    )
  )

  val samplePsrSubmissionEtmpRequest: PsrSubmissionEtmpRequest = PsrSubmissionEtmpRequest(
    EtmpReportDetails(None, Compiled, sampleToday, sampleToday),
    EtmpAccountingPeriodDetails(None, List(EtmpAccountingPeriod(sampleToday, sampleToday))),
    EtmpSchemeDesignatory(
      Some("001"),
      "openBankAccount",
      None,
      1,
      2,
      3,
      None,
      None,
      None,
      None,
      None
    ),
    None,
    None,
    membersPayments = None,
    shares = None,
    psrDeclaration = None
  )

  // SIPP - PSR
  val sampleSippReportDetailsSubmission: SippReportDetailsSubmission = SippReportDetailsSubmission(
    "17836742CF",
    periodStart = LocalDate.of(2020, 12, 12),
    periodEnd = LocalDate.of(2021, 12, 12),
    memberTransactions = "Yes"
  )

  val sampleSippPsrSubmission: SippPsrSubmission = SippPsrSubmission(
    sampleSippReportDetailsSubmission
  )

  // SIPP - ETMP

  val sampleSippPsrSubmissionEtmpRequest: SippPsrSubmissionEtmpRequest = SippPsrSubmissionEtmpRequest(
    EtmpSippReportDetails(None, Compiled, sampleToday, sampleToday, "Yes", None, None)
  )

  val sampleSippPsrSubmissionEtmpResponse: SippPsrSubmissionEtmpResponse = SippPsrSubmissionEtmpResponse(
    reportDetails = EtmpSippReportDetails(
      pstr = Some("12345678AA"),
      status = Compiled,
      periodStart = LocalDate.parse("2022-04-06"),
      periodEnd = LocalDate.parse("2023-04-05"),
      memberTransactions = "Yes",
      schemeName = Some("PSR Scheme"),
      psrVersion = Some("001")
    ),
    accountingPeriodDetails = sampleEtmpAccountingPeriodDetails
  )

  val validationMessage: ValidationMessage = ValidationMessage
    .builder()
    .message("customMessage")
    .build()

  val sampleOverviewResponse: Seq[PsrOverviewEtmpResponse] = Seq(
    PsrOverviewEtmpResponse(
      periodStartDate = LocalDate.parse("2022-04-06"),
      periodEndDate = LocalDate.parse("2023-04-05"),
      numberOfVersions = 1,
      submittedVersionAvailable = "No",
      compiledVersionAvailable = "Yes",
      ntfDateOfIssue = LocalDate.parse("2022-12-06"),
      psrDueDate = LocalDate.parse("2023-03-31"),
      psrReportType = Standard
    ),
    PsrOverviewEtmpResponse(
      periodStartDate = LocalDate.parse("2021-04-06"),
      periodEndDate = LocalDate.parse("2022-04-05"),
      numberOfVersions = 2,
      submittedVersionAvailable = "Yes",
      compiledVersionAvailable = "Yes",
      ntfDateOfIssue = LocalDate.parse("2021-12-06"),
      psrDueDate = LocalDate.parse("2022-03-31"),
      psrReportType = Standard
    )
  )

  val sampleVersionsResponse: Seq[PsrVersionsEtmpResponse] = Seq(
    PsrVersionsEtmpResponse(
      reportFormBundleNumber = "123456785012",
      reportVersion = 1,
      reportStatus = ReportStatusCompiled,
      compilationOrSubmissionDate = LocalDateTime.parse("2023-04-02T09:30:47"),
      reportSubmitterDetails = Some(
        ReportSubmitterDetails(
          reportSubmittedBy = "PSP",
          organisationOrPartnershipDetails = Some(
            OrganisationOrPartnershipDetails(
              organisationOrPartnershipName = "ABC Limited"
            )
          ),
          individualDetails = None
        )
      ),
      psaDetails = Some(
        PsaDetails(
          psaOrganisationOrPartnershipDetails = Some(
            PsaOrganisationOrPartnershipDetails(
              organisationOrPartnershipName = "XYZ Limited"
            )
          ),
          psaIndividualDetails = None
        )
      )
    )
  )
}
