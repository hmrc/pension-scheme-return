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

import com.networknt.schema.{CustomErrorMessageType, ValidationMessage}
import uk.gov.hmrc.pensionschemereturn.models.etmp.YesNo.{No, Yes}
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.etmp.sipp.EtmpSippReportDetails
import uk.gov.hmrc.pensionschemereturn.models.etmp.{Compiled, ReportStatusCompiled, SectionStatus, Standard}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.HowDisposed.Sold
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.IdentityType.{Individual, UKCompany}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.SchemeHoldLandProperty.Acquisition
import uk.gov.hmrc.pensionschemereturn.models.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.requests.etmp.{PsrSubmissionEtmpRequest, SippPsrSubmissionEtmpRequest}
import uk.gov.hmrc.pensionschemereturn.models.response._
import uk.gov.hmrc.pensionschemereturn.models.sipp.{SippPsrSubmission, SippReportDetailsSubmission}

import java.text.MessageFormat
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

trait TestValues {

  val pstr = "testPstr"
  val sampleToday: LocalDate = LocalDate.of(2023, 10, 19)

  // Standard - PSR

  val sampleMinimalRequiredSubmission: MinimalRequiredSubmission = MinimalRequiredSubmission(
    reportDetails = ReportDetails(
      pstr = pstr,
      periodStart = sampleToday,
      periodEnd = sampleToday
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
    membersPayments = None
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
    landOrProperty = LandOrProperty(
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
    ),
    borrowing = Borrowing(
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
  )

  val sampleEmployerContribution1 = EmployerContributions(
    employerName = "test employer one",
    employerType = EmployerType.UKCompany(Right("test company id")),
    totalTransferValue = 12.34
  )

  val sampleEmployerContribution2 = EmployerContributions(
    employerName = "test employer two",
    employerType = EmployerType.UKCompany(Left("test reason")),
    totalTransferValue = 34.56
  )

  val sampleEmployerContribution3 = EmployerContributions(
    employerName = "test employer three",
    employerType = EmployerType.Other("test description"),
    totalTransferValue = 56.78
  )

  val sampleEmployerContributions4 = EmployerContributions(
    employerName = "test employer four",
    employerType = EmployerType.UKPartnership(Right("test partnership id")),
    totalTransferValue = 78.99
  )

  val sampleMemberDetails1 = MemberDetails(
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
    )
  )

  val sampleMemberDetails2 = MemberDetails(
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
    )
  )

  val sampleMemberPayments: MemberPayments = MemberPayments(
    memberDetails = List(
      sampleMemberDetails1,
      sampleMemberDetails2
    ),
    employerContributionsCompleted = true
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
    noOfTransactions = 1,
    landOrPropertyTransactions = List(
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

  val sampleEtmpLoans: EtmpLoans = EtmpLoans(
    recordVersion = Some("001"),
    schemeHadLoans = "Yes",
    noOfLoans = 1,
    loanTransactions = List(
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

  val sampleEtmpAssets: EtmpAssets = EtmpAssets(
    landOrProperty = sampleEtmpLandOrProperty,
    borrowing = EtmpBorrowing(
      recordVersion = None,
      moneyWasBorrowed = "moneyWasBorrowed",
      noOfBorrows = None,
      moneyBorrowed = Seq.empty
    ),
    bonds = EtmpBonds(bondsWereAdded = "bondsWereAdded", bondsWereDisposed = "bondsWereDisposed"),
    otherAssets =
      EtmpOtherAssets(otherAssetsWereHeld = "otherAssetsWereHeld", otherAssetsWereDisposed = "otherAssetsWereDisposed")
  )

  val sampleEtmpMemberPayments: EtmpMemberPayments = EtmpMemberPayments(
    recordVersion = None,
    employerContributionMade = Yes,
    unallocatedContribsMade = No,
    unallocatedContribAmount = None,
    memberContributionMade = No,
    schemeReceivedTransferIn = No,
    schemeMadeTransferOut = No,
    lumpSumReceived = No,
    pensionReceived = No,
    surrenderMade = No,
    memberDetails = List(
      EtmpMemberDetails(
        memberStatus = SectionStatus.New,
        memberPSRVersion = "0",
        noOfContributions = Some(2),
        totalContributions = 0,
        noOfTransfersIn = 0,
        noOfTransfersOut = 0,
        pensionAmountReceived = None,
        personalDetails = EtmpMemberPersonalDetails(
          foreName = sampleMemberDetails1.personalDetails.firstName,
          middleName = None,
          lastName = sampleMemberDetails1.personalDetails.lastName,
          nino = sampleMemberDetails1.personalDetails.nino,
          reasonNoNINO = None,
          dateOfBirth = sampleToday
        ),
        memberEmpContribution = List(
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
      EtmpMemberDetails(
        memberStatus = SectionStatus.New,
        memberPSRVersion = "0",
        noOfContributions = Some(2),
        totalContributions = 0,
        noOfTransfersIn = 0,
        noOfTransfersOut = 0,
        pensionAmountReceived = None,
        personalDetails = EtmpMemberPersonalDetails(
          foreName = "test first two",
          middleName = None,
          lastName = "test last two",
          nino = None,
          reasonNoNINO = Some("no nino reason"),
          dateOfBirth = sampleToday
        ),
        memberEmpContribution = List(
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
      )
    )
  )

  val samplePsrSubmissionEtmpResponse: PsrSubmissionEtmpResponse = PsrSubmissionEtmpResponse(
    EtmpSchemeDetails(pstr = "12345678AA", schemeName = "My Golden Egg scheme"),
    EtmpPsrDetails(
      fbVersion = "001",
      fbstatus = Compiled,
      periodStart = LocalDate.parse("2023-04-06"),
      periodEnd = LocalDate.parse("2024-04-05"),
      compilationOrSubmissionDate = LocalDateTime.parse("2023-12-17T09:30:47Z", DateTimeFormatter.ISO_DATE_TIME)
    ),
    sampleEtmpAccountingPeriodDetails,
    EtmpSchemeDesignatory(
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
    Some(
      EtmpLoans(
        recordVersion = Some("003"),
        schemeHadLoans = "Yes",
        noOfLoans = 1,
        loanTransactions = Seq(
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
    ),
    Some(
      EtmpAssets(
        landOrProperty = sampleEtmpLandOrProperty,
        borrowing = EtmpBorrowing(
          recordVersion = Some("164"),
          moneyWasBorrowed = "Yes",
          noOfBorrows = Some(1),
          moneyBorrowed = Seq(
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
        ),
        bonds = EtmpBonds(bondsWereAdded = "No", bondsWereDisposed = "No"),
        otherAssets = EtmpOtherAssets(
          otherAssetsWereHeld = "No",
          otherAssetsWereDisposed = "No"
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
            totalContributions = 30000.0,
            noOfTransfersIn = 2,
            noOfTransfersOut = 2,
            pensionAmountReceived = Some(12000.0),
            personalDetails = EtmpMemberPersonalDetails(
              foreName = "Ferdinand",
              middleName = Some("Felix"),
              lastName = "Bull",
              nino = Some("EB103145A"),
              reasonNoNINO = None,
              dateOfBirth = LocalDate.of(1960, 5, 31)
            ),
            memberEmpContribution = List(
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
          EtmpMemberDetails(
            memberStatus = SectionStatus.Changed,
            memberPSRVersion = "001",
            noOfContributions = Some(2),
            totalContributions = 20000.0,
            noOfTransfersIn = 2,
            noOfTransfersOut = 2,
            pensionAmountReceived = None,
            personalDetails = EtmpMemberPersonalDetails(
              foreName = "Johnny",
              middleName = Some("Be"),
              lastName = "Quicke",
              nino = None,
              reasonNoNINO = Some("Could not find it on record."),
              dateOfBirth = LocalDate.of(1940, 10, 31)
            ),
            memberEmpContribution = List(
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
          )
        )
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
    memberPayments = None
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

  val validationMessage: ValidationMessage = ValidationMessage.ofWithCustom(
    "type",
    CustomErrorMessageType.of("CustomErrorMessageType"),
    new MessageFormat("MessageFormat"),
    "customMessage",
    "at",
    "schemaPath"
  )

  val sampleOverviewResponse = Seq(
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

  val sampleVersionsResponse = Seq(
    PsrVersionsEtmpResponse(
      reportFormBundleNumber = "123456785012",
      reportVersion = 1,
      reportStatus = ReportStatusCompiled,
      compilationOrSubmissionDate = LocalDateTime.parse("2023-04-02T09:30:47"),
      reportSubmitterDetails = ReportSubmitterDetails(
        reportSubmittedBy = "PSP",
        organisationOrPartnershipDetails = Some(
          OrganisationOrPartnershipDetails(
            organisationOrPartnershipName = "ABC Limited"
          )
        ),
        individualDetails = None
      ),
      psaDetails = PsaDetails(
        psaOrganisationOrPartnershipDetails = Some(
          PsaOrganisationOrPartnershipDetails(
            organisationOrPartnershipName = "XYZ Limited"
          )
        ),
        psaIndividualDetails = None
      )
    )
  )
}
