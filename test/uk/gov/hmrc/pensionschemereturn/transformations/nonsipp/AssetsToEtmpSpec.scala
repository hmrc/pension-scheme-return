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

import com.softwaremill.diffx.scalatest.DiffShouldMatcher
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets._
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.common.EtmpIdentityType
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets.HowDisposed.{Other, Sold, Transferred}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.assets._
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer
import com.softwaremill.diffx.generic.auto.diffForCaseClass
import org.scalatestplus.play.PlaySpec
import org.mockito.MockitoSugar

import java.time.LocalDate

class AssetsToEtmpSpec extends PlaySpec with MockitoSugar with Transformer with DiffShouldMatcher {

  private val transformation: AssetsToEtmp = new AssetsToEtmp()
  val today: LocalDate = LocalDate.now

  "AssetsToEtmp - PSR Assets should successfully transform to etmp format " should {

    "when optional fields are None" in {
      val assets = Assets(
        optLandOrProperty = None,
        optBorrowing = None,
        optBonds = None,
        optOtherAssets = None
      )

      val expected = EtmpAssets(
        landOrProperty = None,
        borrowing = None,
        bonds = None,
        otherAssets = None
      )

      transformation.transform(assets) shouldMatchTo expected
    }

    "when optional fields are non-None" in {
      val assets = Assets(
        optLandOrProperty = Some(
          LandOrProperty(
            landOrPropertyHeld = true,
            disposeAnyLandOrProperty = true,
            landOrPropertyTransactions = List(
              LandOrPropertyTransactions(
                propertyDetails = PropertyDetails(
                  landOrPropertyInUK = true,
                  addressDetails = Address(
                    "testAddressLine1",
                    None,
                    Some("testAddressLine3"),
                    "town",
                    Some("GB135HG"),
                    "GB"
                  ),
                  landRegistryTitleNumberKey = true,
                  landRegistryTitleNumberValue = "landRegistryTitleNumberValue"
                ),
                heldPropertyTransaction = HeldPropertyTransaction(
                  methodOfHolding = SchemeHoldLandProperty.Acquisition,
                  dateOfAcquisitionOrContribution = Some(today),
                  optPropertyAcquiredFromName = Some("propertyAcquiredFromName"),
                  optPropertyAcquiredFrom = Some(
                    PropertyAcquiredFrom(
                      IdentityType.Individual,
                      None,
                      Some("NoNinoReason"),
                      None
                    )
                  ),
                  optConnectedPartyStatus = Some(true),
                  totalCostOfLandOrProperty = Double.MaxValue,
                  optIndepValuationSupport = Some(true),
                  isLandOrPropertyResidential = true,
                  optLeaseDetails = Some(
                    LeaseDetails(
                      lesseeName = "lesseeName",
                      leaseGrantDate = today,
                      annualLeaseAmount = Double.MaxValue,
                      connectedPartyStatus = false
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
                      optDateOfSale = Some(today),
                      optNameOfPurchaser = Some("NameOfPurchaser"),
                      optPropertyAcquiredFrom = Some(
                        PropertyAcquiredFrom(
                          IdentityType.Other,
                          None,
                          None,
                          Some("OtherDescription")
                        )
                      ),
                      optSaleProceeds = Some(Double.MaxValue),
                      optConnectedPartyStatus = Some(true),
                      optIndepValuationSupport = Some(true),
                      portionStillHeld = false
                    ),
                    DisposedPropertyTransaction(
                      methodOfDisposal = Transferred,
                      optOtherMethod = None,
                      optDateOfSale = None,
                      optNameOfPurchaser = None,
                      optPropertyAcquiredFrom = None,
                      optSaleProceeds = None,
                      optConnectedPartyStatus = None,
                      optIndepValuationSupport = None,
                      portionStillHeld = false
                    ),
                    DisposedPropertyTransaction(
                      methodOfDisposal = Other,
                      optOtherMethod = Some("OtherMethod"),
                      optDateOfSale = None,
                      optNameOfPurchaser = None,
                      optPropertyAcquiredFrom = None,
                      optSaleProceeds = None,
                      optConnectedPartyStatus = None,
                      optIndepValuationSupport = None,
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
                dateOfBorrow = today,
                schemeAssetsValue = Double.MinPositiveValue,
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
            bondsWereDisposed = true,
            bondTransactions = Seq(
              BondTransactions(
                nameOfBonds = "nameOfBonds",
                methodOfHolding = SchemeHoldBond.Contribution,
                optDateOfAcqOrContrib = Some(today),
                costOfBonds = Double.MaxValue,
                optConnectedPartyStatus = Some(true),
                bondsUnregulated = false,
                totalIncomeOrReceipts = Double.MaxValue,
                optBondsDisposed = Some(
                  Seq(
                    BondDisposed(
                      methodOfDisposal = Sold,
                      optOtherMethod = None,
                      optDateSold = Some(today),
                      optAmountReceived = Some(Double.MaxValue),
                      optBondsPurchaserName = Some("BondsPurchaserName"),
                      optConnectedPartyStatus = Some(true),
                      totalNowHeld = Int.MaxValue
                    ),
                    BondDisposed(
                      methodOfDisposal = Transferred,
                      optOtherMethod = None,
                      optDateSold = None,
                      optAmountReceived = None,
                      optBondsPurchaserName = None,
                      optConnectedPartyStatus = None,
                      totalNowHeld = Int.MaxValue
                    ),
                    BondDisposed(
                      methodOfDisposal = Other,
                      optOtherMethod = Some("OtherMethod"),
                      optDateSold = None,
                      optAmountReceived = None,
                      optBondsPurchaserName = None,
                      optConnectedPartyStatus = None,
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
            otherAssetsWereDisposed = false,
            otherAssetTransactions = Seq(
              OtherAssetTransaction(
                assetDescription = "assetDescription",
                methodOfHolding = SchemeHoldAsset.Acquisition,
                optDateOfAcqOrContrib = Some(today),
                costOfAsset = Double.MaxValue,
                optPropertyAcquiredFromName = Some("PropertyAcquiredFromName"),
                optPropertyAcquiredFrom = Some(
                  PropertyAcquiredFrom(
                    identityType = IdentityType.Other,
                    idNumber = None,
                    reasonNoIdNumber = None,
                    otherDescription = Some("otherDescription")
                  )
                ),
                optConnectedStatus = Some(false),
                optIndepValuationSupport = Some(false),
                movableSchedule29A = true,
                totalIncomeOrReceipts = Double.MaxValue
              ),
              OtherAssetTransaction(
                assetDescription = "assetDescription",
                methodOfHolding = SchemeHoldAsset.Contribution,
                optDateOfAcqOrContrib = Some(today),
                costOfAsset = Double.MaxValue,
                optPropertyAcquiredFromName = None,
                optPropertyAcquiredFrom = None,
                optConnectedStatus = None,
                optIndepValuationSupport = Some(true),
                movableSchedule29A = false,
                totalIncomeOrReceipts = Double.MaxValue
              ),
              OtherAssetTransaction(
                assetDescription = "assetDescription",
                methodOfHolding = SchemeHoldAsset.Transfer,
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

      val expected = EtmpAssets(
        landOrProperty = Some(
          EtmpLandOrProperty(
            recordVersion = None,
            heldAnyLandOrProperty = Yes,
            disposeAnyLandOrProperty = Yes,
            noOfTransactions = Some(1),
            landOrPropertyTransactions = Some(
              Seq(
                EtmpLandOrPropertyTransactions(
                  propertyDetails = EtmpPropertyDetails(
                    landOrPropertyInUK = Yes,
                    addressDetails = EtmpAddress(
                      addressLine1 = "testAddressLine1",
                      addressLine2 = "town",
                      addressLine3 = Some("testAddressLine3"),
                      addressLine4 = None,
                      addressLine5 = None,
                      ukPostCode = Some("GB135HG"),
                      countryCode = "GB"
                    ),
                    landRegistryDetails = EtmpLandRegistryDetails(
                      landRegistryReferenceExists = Yes,
                      landRegistryReference = Some("landRegistryTitleNumberValue"),
                      reasonNoReference = None
                    )
                  ),
                  heldPropertyTransaction = EtmpHeldPropertyTransaction(
                    methodOfHolding = "01",
                    dateOfAcquisitionOrContribution = Some(today),
                    propertyAcquiredFromName = Some("propertyAcquiredFromName"),
                    propertyAcquiredFrom = Some(
                      EtmpIdentityType(
                        indivOrOrgType = "01",
                        idNumber = None,
                        reasonNoIdNumber = Some("NoNinoReason"),
                        otherDescription = None
                      )
                    ),
                    connectedPartyStatus = Some(Connected),
                    totalCostOfLandOrProperty = Double.MaxValue,
                    indepValuationSupport = Some(Yes),
                    residentialSchedule29A = Yes,
                    landOrPropertyLeased = Yes,
                    leaseDetails = Some(
                      EtmpLeaseDetails(
                        lesseeName = "lesseeName",
                        connectedPartyStatus = Unconnected,
                        leaseGrantDate = today,
                        annualLeaseAmount = Double.MaxValue
                      )
                    ),
                    totalIncomeOrReceipts = Double.MaxValue
                  ),
                  disposedPropertyTransaction = Some(
                    Seq(
                      EtmpDisposedPropertyTransaction(
                        methodOfDisposal = "01",
                        otherMethod = None,
                        dateOfSale = Some(today),
                        nameOfPurchaser = Some("NameOfPurchaser"),
                        purchaseOrgDetails = Some(
                          EtmpIdentityType(
                            indivOrOrgType = "04",
                            idNumber = None,
                            reasonNoIdNumber = None,
                            otherDescription = Some("OtherDescription")
                          )
                        ),
                        saleProceeds = Some(Double.MaxValue),
                        connectedPartyStatus = Some(Connected),
                        indepValuationSupport = Some(Yes),
                        portionStillHeld = No
                      ),
                      EtmpDisposedPropertyTransaction(
                        methodOfDisposal = "02",
                        otherMethod = None,
                        dateOfSale = None,
                        nameOfPurchaser = None,
                        purchaseOrgDetails = None,
                        saleProceeds = None,
                        connectedPartyStatus = None,
                        indepValuationSupport = None,
                        portionStillHeld = No
                      ),
                      EtmpDisposedPropertyTransaction(
                        methodOfDisposal = "03",
                        otherMethod = Some("OtherMethod"),
                        dateOfSale = None,
                        nameOfPurchaser = None,
                        purchaseOrgDetails = None,
                        saleProceeds = None,
                        connectedPartyStatus = None,
                        indepValuationSupport = None,
                        portionStillHeld = Yes
                      )
                    )
                  )
                )
              )
            )
          )
        ),
        borrowing = Some(
          EtmpBorrowing(
            recordVersion = None,
            moneyWasBorrowed = Yes,
            noOfBorrows = Some(1),
            moneyBorrowed = Some(
              Seq(
                EtmpMoneyBorrowed(
                  dateOfBorrow = today,
                  schemeAssetsValue = Double.MinPositiveValue,
                  amountBorrowed = Double.MaxValue,
                  interestRate = Double.MaxValue,
                  borrowingFromName = "borrowingFromName",
                  connectedPartyStatus = Connected,
                  reasonForBorrow = "reasonForBorrow"
                )
              )
            )
          )
        ),
        bonds = Some(
          EtmpBonds(
            recordVersion = None,
            bondsWereAdded = Yes,
            bondsWereDisposed = Yes,
            noOfTransactions = Some(1),
            bondTransactions = Some(
              Seq(
                EtmpBondTransactions(
                  nameOfBonds = "nameOfBonds",
                  methodOfHolding = "02",
                  dateOfAcqOrContrib = Some(today),
                  costOfBonds = Double.MaxValue,
                  connectedPartyStatus = Some(Connected),
                  bondsUnregulated = No,
                  totalIncomeOrReceipts = Double.MaxValue,
                  bondsDisposed = Some(
                    Seq(
                      EtmpBondsDisposed(
                        methodOfDisposal = "01",
                        otherMethod = None,
                        dateSold = Some(today),
                        amountReceived = Some(Double.MaxValue),
                        bondsPurchaserName = Some("BondsPurchaserName"),
                        connectedPartyStatus = Some(Connected),
                        totalNowHeld = Int.MaxValue
                      ),
                      EtmpBondsDisposed(
                        methodOfDisposal = "02",
                        otherMethod = None,
                        dateSold = None,
                        amountReceived = None,
                        bondsPurchaserName = None,
                        connectedPartyStatus = None,
                        totalNowHeld = Int.MaxValue
                      ),
                      EtmpBondsDisposed(
                        methodOfDisposal = "03",
                        otherMethod = Some("OtherMethod"),
                        dateSold = None,
                        amountReceived = None,
                        bondsPurchaserName = None,
                        connectedPartyStatus = None,
                        totalNowHeld = Int.MaxValue
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
            otherAssetsWereHeld = Yes,
            otherAssetsWereDisposed = No,
            noOfTransactions = Some(3),
            otherAssetTransactions = Some(
              Seq(
                EtmpOtherAssetTransaction(
                  assetDescription = "assetDescription",
                  methodOfHolding = "01",
                  dateOfAcqOrContrib = Some(today),
                  costOfAsset = Double.MaxValue,
                  acquiredFromName = Some("PropertyAcquiredFromName"),
                  acquiredFromType = Some(
                    EtmpIdentityType(
                      indivOrOrgType = "04",
                      idNumber = None,
                      reasonNoIdNumber = None,
                      otherDescription = Some("otherDescription")
                    )
                  ),
                  connectedStatus = Some(Unconnected),
                  supportedByIndepValuation = Some(No),
                  movableSchedule29A = Yes,
                  totalIncomeOrReceipts = Double.MaxValue
                ),
                EtmpOtherAssetTransaction(
                  assetDescription = "assetDescription",
                  methodOfHolding = "02",
                  dateOfAcqOrContrib = Some(today),
                  costOfAsset = Double.MaxValue,
                  acquiredFromName = None,
                  acquiredFromType = None,
                  connectedStatus = None,
                  supportedByIndepValuation = Some(Yes),
                  movableSchedule29A = No,
                  totalIncomeOrReceipts = Double.MaxValue
                ),
                EtmpOtherAssetTransaction(
                  assetDescription = "assetDescription",
                  methodOfHolding = "03",
                  dateOfAcqOrContrib = None,
                  costOfAsset = Double.MaxValue,
                  acquiredFromName = None,
                  acquiredFromType = None,
                  connectedStatus = None,
                  supportedByIndepValuation = None,
                  movableSchedule29A = Yes,
                  totalIncomeOrReceipts = Double.MaxValue
                )
              )
            )
          )
        )
      )

      transformation.transform(assets) shouldMatchTo expected
    }

    "when optional fields are non-None but with optDisposed sequences (bonds&landOrProperty)" in {
      val assets = Assets(
        optLandOrProperty = Some(
          LandOrProperty(
            landOrPropertyHeld = true,
            disposeAnyLandOrProperty = false,
            landOrPropertyTransactions = List(
              LandOrPropertyTransactions(
                propertyDetails = PropertyDetails(
                  landOrPropertyInUK = true,
                  addressDetails = Address(
                    "testAddressLine1",
                    None,
                    Some("testAddressLine3"),
                    "town",
                    Some("GB135HG"),
                    "GB"
                  ),
                  landRegistryTitleNumberKey = true,
                  landRegistryTitleNumberValue = "landRegistryTitleNumberValue"
                ),
                heldPropertyTransaction = HeldPropertyTransaction(
                  methodOfHolding = SchemeHoldLandProperty.Acquisition,
                  dateOfAcquisitionOrContribution = Some(today),
                  optPropertyAcquiredFromName = Some("propertyAcquiredFromName"),
                  optPropertyAcquiredFrom = Some(
                    PropertyAcquiredFrom(
                      IdentityType.Other,
                      None,
                      None,
                      Some("Other Desc")
                    )
                  ),
                  optConnectedPartyStatus = Some(false),
                  totalCostOfLandOrProperty = Double.MaxValue,
                  optIndepValuationSupport = Some(false),
                  isLandOrPropertyResidential = false,
                  optLeaseDetails = None,
                  landOrPropertyLeased = false,
                  totalIncomeOrReceipts = Double.MaxValue
                ),
                optDisposedPropertyTransaction = Some(
                  Seq.empty
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
                dateOfBorrow = today,
                schemeAssetsValue = Double.MinPositiveValue,
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
                methodOfHolding = SchemeHoldBond.Acquisition,
                optDateOfAcqOrContrib = Some(today),
                costOfBonds = Double.MaxValue,
                optConnectedPartyStatus = Some(true),
                bondsUnregulated = false,
                totalIncomeOrReceipts = Double.MaxValue,
                optBondsDisposed = Some(Seq.empty)
              )
            )
          )
        ),
        optOtherAssets = Some(
          OtherAssets(
            otherAssetsWereHeld = true,
            otherAssetsWereDisposed = false,
            otherAssetTransactions = Seq(
              OtherAssetTransaction(
                assetDescription = "assetDescription",
                methodOfHolding = SchemeHoldAsset.Acquisition,
                optDateOfAcqOrContrib = Some(today),
                costOfAsset = Double.MaxValue,
                optPropertyAcquiredFromName = Some("PropertyAcquiredFromName"),
                optPropertyAcquiredFrom = Some(
                  PropertyAcquiredFrom(
                    identityType = IdentityType.UKCompany,
                    idNumber = None,
                    reasonNoIdNumber = Some("Reason"),
                    otherDescription = None
                  )
                ),
                optConnectedStatus = Some(false),
                optIndepValuationSupport = Some(false),
                movableSchedule29A = true,
                totalIncomeOrReceipts = Double.MaxValue
              ),
              OtherAssetTransaction(
                assetDescription = "assetDescription",
                methodOfHolding = SchemeHoldAsset.Contribution,
                optDateOfAcqOrContrib = Some(today),
                costOfAsset = Double.MaxValue,
                optPropertyAcquiredFromName = None,
                optPropertyAcquiredFrom = None,
                optConnectedStatus = None,
                optIndepValuationSupport = Some(true),
                movableSchedule29A = false,
                totalIncomeOrReceipts = Double.MaxValue
              ),
              OtherAssetTransaction(
                assetDescription = "assetDescription",
                methodOfHolding = SchemeHoldAsset.Transfer,
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

      val expected = EtmpAssets(
        landOrProperty = Some(
          EtmpLandOrProperty(
            recordVersion = None,
            heldAnyLandOrProperty = Yes,
            disposeAnyLandOrProperty = No,
            noOfTransactions = Some(1),
            landOrPropertyTransactions = Some(
              Seq(
                EtmpLandOrPropertyTransactions(
                  propertyDetails = EtmpPropertyDetails(
                    landOrPropertyInUK = Yes,
                    addressDetails = EtmpAddress(
                      addressLine1 = "testAddressLine1",
                      addressLine2 = "town",
                      addressLine3 = Some("testAddressLine3"),
                      addressLine4 = None,
                      addressLine5 = None,
                      ukPostCode = Some("GB135HG"),
                      countryCode = "GB"
                    ),
                    landRegistryDetails = EtmpLandRegistryDetails(
                      landRegistryReferenceExists = Yes,
                      landRegistryReference = Some("landRegistryTitleNumberValue"),
                      reasonNoReference = None
                    )
                  ),
                  heldPropertyTransaction = EtmpHeldPropertyTransaction(
                    methodOfHolding = "01",
                    dateOfAcquisitionOrContribution = Some(today),
                    propertyAcquiredFromName = Some("propertyAcquiredFromName"),
                    propertyAcquiredFrom = Some(
                      EtmpIdentityType(
                        indivOrOrgType = "04",
                        idNumber = None,
                        reasonNoIdNumber = None,
                        otherDescription = Some("Other Desc")
                      )
                    ),
                    connectedPartyStatus = Some(Unconnected),
                    totalCostOfLandOrProperty = Double.MaxValue,
                    indepValuationSupport = Some(No),
                    residentialSchedule29A = No,
                    landOrPropertyLeased = No,
                    leaseDetails = None,
                    totalIncomeOrReceipts = Double.MaxValue
                  ),
                  disposedPropertyTransaction = None
                )
              )
            )
          )
        ),
        borrowing = Some(
          EtmpBorrowing(
            recordVersion = None,
            moneyWasBorrowed = Yes,
            noOfBorrows = Some(1),
            moneyBorrowed = Some(
              Seq(
                EtmpMoneyBorrowed(
                  dateOfBorrow = today,
                  schemeAssetsValue = Double.MinPositiveValue,
                  amountBorrowed = Double.MaxValue,
                  interestRate = Double.MaxValue,
                  borrowingFromName = "borrowingFromName",
                  connectedPartyStatus = Connected,
                  reasonForBorrow = "reasonForBorrow"
                )
              )
            )
          )
        ),
        bonds = Some(
          EtmpBonds(
            recordVersion = None,
            bondsWereAdded = Yes,
            bondsWereDisposed = No,
            noOfTransactions = Some(1),
            bondTransactions = Some(
              Seq(
                EtmpBondTransactions(
                  nameOfBonds = "nameOfBonds",
                  methodOfHolding = "01",
                  dateOfAcqOrContrib = Some(today),
                  costOfBonds = Double.MaxValue,
                  connectedPartyStatus = Some(Connected),
                  bondsUnregulated = No,
                  totalIncomeOrReceipts = Double.MaxValue,
                  bondsDisposed = None
                )
              )
            )
          )
        ),
        otherAssets = Some(
          EtmpOtherAssets(
            otherAssetsWereHeld = Yes,
            otherAssetsWereDisposed = No,
            noOfTransactions = Some(3),
            otherAssetTransactions = Some(
              Seq(
                EtmpOtherAssetTransaction(
                  assetDescription = "assetDescription",
                  methodOfHolding = "01",
                  dateOfAcqOrContrib = Some(today),
                  costOfAsset = Double.MaxValue,
                  acquiredFromName = Some("PropertyAcquiredFromName"),
                  acquiredFromType = Some(
                    EtmpIdentityType(
                      indivOrOrgType = "02",
                      idNumber = None,
                      reasonNoIdNumber = Some("Reason"),
                      otherDescription = None
                    )
                  ),
                  connectedStatus = Some(Unconnected),
                  supportedByIndepValuation = Some(No),
                  movableSchedule29A = Yes,
                  totalIncomeOrReceipts = Double.MaxValue
                ),
                EtmpOtherAssetTransaction(
                  assetDescription = "assetDescription",
                  methodOfHolding = "02",
                  dateOfAcqOrContrib = Some(today),
                  costOfAsset = Double.MaxValue,
                  acquiredFromName = None,
                  acquiredFromType = None,
                  connectedStatus = None,
                  supportedByIndepValuation = Some(Yes),
                  movableSchedule29A = No,
                  totalIncomeOrReceipts = Double.MaxValue
                ),
                EtmpOtherAssetTransaction(
                  assetDescription = "assetDescription",
                  methodOfHolding = "03",
                  dateOfAcqOrContrib = None,
                  costOfAsset = Double.MaxValue,
                  acquiredFromName = None,
                  acquiredFromType = None,
                  connectedStatus = None,
                  supportedByIndepValuation = None,
                  movableSchedule29A = Yes,
                  totalIncomeOrReceipts = Double.MaxValue
                )
              )
            )
          )
        )
      )

      transformation.transform(assets) shouldMatchTo expected
    }
  }
}
