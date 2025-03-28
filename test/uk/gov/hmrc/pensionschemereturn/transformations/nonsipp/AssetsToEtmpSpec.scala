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
import uk.gov.hmrc.pensionschemereturn.models.etmp.YesNo
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets.HowDisposed.{Other, Sold, Transferred}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.assets._
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer
import com.softwaremill.diffx.generic.auto.indicator
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.mockito.MockitoSugar

import java.time.LocalDate

class AssetsToEtmpSpec extends PlaySpec with MockitoSugar with Transformer with DiffShouldMatcher {

  private val transformation: AssetsToEtmp = new AssetsToEtmp()
  val today: LocalDate = LocalDate.now

  private val bondTransactions = Seq(
    BondTransactions(
      prePopulated = None,
      nameOfBonds = "nameOfBonds",
      methodOfHolding = SchemeHoldBond.Contribution,
      optDateOfAcqOrContrib = Some(today),
      costOfBonds = Double.MaxValue,
      optConnectedPartyStatus = Some(true),
      bondsUnregulated = false,
      optTotalIncomeOrReceipts = Some(Double.MaxValue),
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

  private val otherAssetTransactions = Seq(
    OtherAssetTransaction(
      prePopulated = None,
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
      optMovableSchedule29A = Some(true),
      optTotalIncomeOrReceipts = Some(Double.MaxValue),
      Some(
        Seq(
          OtherAssetDisposed(
            methodOfDisposal = Sold,
            optOtherMethod = None,
            optDateSold = Some(today),
            optPurchaserName = Some("PurchaserName"),
            optPropertyAcquiredFrom = Some(
              PropertyAcquiredFrom(
                IdentityType.Other,
                None,
                None,
                Some("otherDescription")
              )
            ),
            optTotalAmountReceived = Some(Double.MaxValue),
            optConnectedStatus = Some(false),
            optSupportedByIndepValuation = Some(false),
            anyPartAssetStillHeld = false
          ),
          OtherAssetDisposed(
            methodOfDisposal = Transferred,
            optOtherMethod = None,
            optDateSold = None,
            optPurchaserName = None,
            optPropertyAcquiredFrom = None,
            optTotalAmountReceived = None,
            optConnectedStatus = None,
            optSupportedByIndepValuation = None,
            anyPartAssetStillHeld = true
          )
        )
      )
    ),
    OtherAssetTransaction(
      prePopulated = None,
      assetDescription = "assetDescription",
      methodOfHolding = SchemeHoldAsset.Contribution,
      optDateOfAcqOrContrib = Some(today),
      costOfAsset = Double.MaxValue,
      optPropertyAcquiredFromName = None,
      optPropertyAcquiredFrom = None,
      optConnectedStatus = None,
      optIndepValuationSupport = Some(true),
      optMovableSchedule29A = Some(false),
      optTotalIncomeOrReceipts = Some(Double.MaxValue),
      optOtherAssetDisposed = None
    ),
    OtherAssetTransaction(
      prePopulated = None,
      assetDescription = "assetDescription",
      methodOfHolding = SchemeHoldAsset.Transfer,
      optDateOfAcqOrContrib = None,
      costOfAsset = Double.MaxValue,
      optPropertyAcquiredFromName = None,
      optPropertyAcquiredFrom = None,
      optConnectedStatus = None,
      optIndepValuationSupport = None,
      optMovableSchedule29A = Some(true),
      optTotalIncomeOrReceipts = Some(Double.MaxValue),
      optOtherAssetDisposed = Some(
        Seq(
          OtherAssetDisposed(
            methodOfDisposal = Other,
            optOtherMethod = Some("OtherMethod"),
            optDateSold = None,
            optPurchaserName = None,
            optPropertyAcquiredFrom = None,
            optTotalAmountReceived = None,
            optConnectedStatus = None,
            optSupportedByIndepValuation = None,
            anyPartAssetStillHeld = true
          )
        )
      )
    )
  )
  private def fullAssets(inUK: Boolean, address: Address): Assets = Assets(
    optLandOrProperty = Some(
      LandOrProperty(
        recordVersion = Some("001"),
        optLandOrPropertyHeld = Some(true),
        optDisposeAnyLandOrProperty = Some(true),
        landOrPropertyTransactions = List(
          LandOrPropertyTransactions(
            prePopulated = None,
            propertyDetails = PropertyDetails(
              landOrPropertyInUK = inUK,
              addressDetails = address,
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
              optIsLandOrPropertyResidential = Some(true),
              optLeaseDetails = Some(
                LeaseDetails(
                  optLesseeName = Some("lesseeName"),
                  optLeaseGrantDate = Some(today),
                  optAnnualLeaseAmount = Some(Double.MaxValue),
                  optConnectedPartyStatus = Some(false)
                )
              ),
              optLandOrPropertyLeased = Some(true),
              optTotalIncomeOrReceipts = Some(Double.MaxValue)
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
        recordVersion = Some("001"),
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
        recordVersion = Some("001"),
        optBondsWereAdded = Some(true),
        optBondsWereDisposed = Some(true),
        bondTransactions = bondTransactions
      )
    ),
    optOtherAssets = Some(
      OtherAssets(
        recordVersion = Some("001"),
        optOtherAssetsWereHeld = Some(true),
        optOtherAssetsWereDisposed = Some(false),
        otherAssetTransactions = otherAssetTransactions
      )
    )
  )

  private val etmpBondTransactions = Seq(
    EtmpBondTransactions(
      prePopulated = None,
      nameOfBonds = "nameOfBonds",
      methodOfHolding = "02",
      dateOfAcqOrContrib = Some(today),
      costOfBonds = Double.MaxValue,
      connectedPartyStatus = Some(Connected),
      bondsUnregulated = No,
      totalIncomeOrReceipts = Some(Double.MaxValue),
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

  private val etmpOtherAssetsTransaction = Seq(
    EtmpOtherAssetTransaction(
      prePopulated = None,
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
      movableSchedule29A = Some(Yes),
      totalIncomeOrReceipts = Some(Double.MaxValue),
      assetsDisposed = Some(
        Seq(
          EtmpAssetsDisposed(
            methodOfDisposal = "01",
            otherMethod = None,
            dateSold = Some(today),
            purchaserName = Some("PurchaserName"),
            purchaserType = Some(
              EtmpIdentityType(
                indivOrOrgType = "04",
                idNumber = None,
                reasonNoIdNumber = None,
                otherDescription = Some("otherDescription")
              )
            ),
            totalAmountReceived = Some(Double.MaxValue),
            connectedStatus = Some(Unconnected),
            supportedByIndepValuation = Some(No),
            fullyDisposedOf = "Yes"
          ),
          EtmpAssetsDisposed(
            methodOfDisposal = "02",
            otherMethod = None,
            dateSold = None,
            purchaserName = None,
            purchaserType = None,
            totalAmountReceived = None,
            connectedStatus = None,
            supportedByIndepValuation = None,
            fullyDisposedOf = "No"
          )
        )
      )
    ),
    EtmpOtherAssetTransaction(
      prePopulated = None,
      assetDescription = "assetDescription",
      methodOfHolding = "02",
      dateOfAcqOrContrib = Some(today),
      costOfAsset = Double.MaxValue,
      acquiredFromName = None,
      acquiredFromType = None,
      connectedStatus = None,
      supportedByIndepValuation = Some(Yes),
      movableSchedule29A = Some(No),
      totalIncomeOrReceipts = Some(Double.MaxValue),
      assetsDisposed = None
    ),
    EtmpOtherAssetTransaction(
      prePopulated = None,
      assetDescription = "assetDescription",
      methodOfHolding = "03",
      dateOfAcqOrContrib = None,
      costOfAsset = Double.MaxValue,
      acquiredFromName = None,
      acquiredFromType = None,
      connectedStatus = None,
      supportedByIndepValuation = None,
      movableSchedule29A = Some(Yes),
      totalIncomeOrReceipts = Some(Double.MaxValue),
      assetsDisposed = Some(
        Seq(
          EtmpAssetsDisposed(
            methodOfDisposal = "03",
            otherMethod = Some("OtherMethod"),
            dateSold = None,
            purchaserName = None,
            purchaserType = None,
            totalAmountReceived = None,
            connectedStatus = None,
            supportedByIndepValuation = None,
            fullyDisposedOf = "No"
          )
        )
      )
    )
  )

  private def fullExpectedAssets(inUK: String, etmpAddress: EtmpAddress): EtmpAssets = EtmpAssets(
    landOrProperty = Some(
      EtmpLandOrProperty(
        recordVersion = Some("001"),
        heldAnyLandOrProperty = Some(Yes),
        disposeAnyLandOrProperty = Some(Yes),
        noOfTransactions = Some(1),
        landOrPropertyTransactions = Some(
          Seq(
            EtmpLandOrPropertyTransactions(
              propertyDetails = EtmpPropertyDetails(
                prePopulated = None,
                landOrPropertyInUK = inUK,
                addressDetails = etmpAddress,
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
                residentialSchedule29A = Some(Yes),
                landOrPropertyLeased = Some(Yes),
                leaseDetails = Some(
                  EtmpLeaseDetails(
                    lesseeName = Some("lesseeName"),
                    connectedPartyStatus = Some(Unconnected),
                    leaseGrantDate = Some(today),
                    annualLeaseAmount = Some(Double.MaxValue)
                  )
                ),
                totalIncomeOrReceipts = Some(Double.MaxValue)
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
        recordVersion = Some("001"),
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
        recordVersion = Some("001"),
        bondsWereAdded = Some(Yes),
        bondsWereDisposed = Some(Yes),
        noOfTransactions = Some(1),
        bondTransactions = Some(etmpBondTransactions)
      )
    ),
    otherAssets = Some(
      EtmpOtherAssets(
        recordVersion = Some("001"),
        otherAssetsWereHeld = Some(Yes),
        otherAssetsWereDisposed = Some(No),
        noOfTransactions = Some(3),
        otherAssetTransactions = Some(etmpOtherAssetsTransaction)
      )
    )
  )

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
      transformation.transform(
        fullAssets(
          inUK = true,
          address = Address(
            "testAddressLine1",
            None,
            Some("testAddressLine3"),
            "town",
            Some("GB135HG"),
            "GB"
          )
        )
      ) shouldMatchTo fullExpectedAssets(
        inUK = Yes,
        etmpAddress = EtmpAddress(
          addressLine1 = "testAddressLine1",
          addressLine2 = "town",
          addressLine3 = Some("testAddressLine3"),
          addressLine4 = None,
          addressLine5 = None,
          ukPostCode = Some("GB135HG"),
          countryCode = "GB"
        )
      )
    }

    "when non UK address" in {
      transformation.transform(
        fullAssets(
          inUK = false,
          address = Address(
            "testAddressLine1",
            None,
            Some("testAddressLine3"),
            "town",
            Some("nonUkPostcode"),
            "GB"
          )
        )
      ) shouldMatchTo fullExpectedAssets(
        inUK = No,
        etmpAddress = EtmpAddress(
          addressLine1 = "testAddressLine1",
          addressLine2 = "town",
          addressLine3 = Some("testAddressLine3"),
          addressLine4 = None,
          addressLine5 = Some("nonUkPostcode"),
          ukPostCode = None,
          countryCode = "GB"
        )
      )
    }

    "when optional fields are non-None but with optDisposed sequences (bonds&landOrProperty&OtherAssets)" in {
      val assets = Assets(
        optLandOrProperty = Some(
          LandOrProperty(
            recordVersion = None,
            optLandOrPropertyHeld = Some(true),
            optDisposeAnyLandOrProperty = Some(false),
            landOrPropertyTransactions = List(
              LandOrPropertyTransactions(
                prePopulated = None,
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
                  optIsLandOrPropertyResidential = Some(false),
                  optLeaseDetails = None,
                  optLandOrPropertyLeased = Some(false),
                  optTotalIncomeOrReceipts = Some(Double.MaxValue)
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
            recordVersion = None,
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
            recordVersion = None,
            optBondsWereAdded = Some(true),
            optBondsWereDisposed = Some(false),
            bondTransactions = Seq(
              BondTransactions(
                prePopulated = None,
                nameOfBonds = "nameOfBonds",
                methodOfHolding = SchemeHoldBond.Acquisition,
                optDateOfAcqOrContrib = Some(today),
                costOfBonds = Double.MaxValue,
                optConnectedPartyStatus = Some(true),
                bondsUnregulated = false,
                optTotalIncomeOrReceipts = Some(Double.MaxValue),
                optBondsDisposed = Some(Seq.empty)
              )
            )
          )
        ),
        optOtherAssets = Some(
          OtherAssets(
            recordVersion = None,
            optOtherAssetsWereHeld = Some(true),
            optOtherAssetsWereDisposed = Some(false),
            otherAssetTransactions = Seq(
              OtherAssetTransaction(
                prePopulated = None,
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
                optMovableSchedule29A = Some(true),
                optTotalIncomeOrReceipts = Some(Double.MaxValue),
                optOtherAssetDisposed = Some(Seq.empty)
              ),
              OtherAssetTransaction(
                prePopulated = None,
                assetDescription = "assetDescription",
                methodOfHolding = SchemeHoldAsset.Contribution,
                optDateOfAcqOrContrib = Some(today),
                costOfAsset = Double.MaxValue,
                optPropertyAcquiredFromName = None,
                optPropertyAcquiredFrom = None,
                optConnectedStatus = None,
                optIndepValuationSupport = Some(true),
                optMovableSchedule29A = Some(false),
                optTotalIncomeOrReceipts = Some(Double.MaxValue),
                optOtherAssetDisposed = Some(Seq.empty)
              ),
              OtherAssetTransaction(
                prePopulated = None,
                assetDescription = "assetDescription",
                methodOfHolding = SchemeHoldAsset.Transfer,
                optDateOfAcqOrContrib = None,
                costOfAsset = Double.MaxValue,
                optPropertyAcquiredFromName = None,
                optPropertyAcquiredFrom = None,
                optConnectedStatus = None,
                optIndepValuationSupport = None,
                optMovableSchedule29A = Some(true),
                optTotalIncomeOrReceipts = Some(Double.MaxValue),
                optOtherAssetDisposed = Some(Seq.empty)
              )
            )
          )
        )
      )

      val expected = EtmpAssets(
        landOrProperty = Some(
          EtmpLandOrProperty(
            recordVersion = None,
            heldAnyLandOrProperty = Option(Yes),
            disposeAnyLandOrProperty = Option(No),
            noOfTransactions = Some(1),
            landOrPropertyTransactions = Some(
              Seq(
                EtmpLandOrPropertyTransactions(
                  propertyDetails = EtmpPropertyDetails(
                    prePopulated = None,
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
                    residentialSchedule29A = Some(No),
                    landOrPropertyLeased = Some(No),
                    leaseDetails = None,
                    totalIncomeOrReceipts = Some(Double.MaxValue)
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
            bondsWereAdded = Some(Yes),
            bondsWereDisposed = Some(No),
            noOfTransactions = Some(1),
            bondTransactions = Some(
              Seq(
                EtmpBondTransactions(
                  prePopulated = None,
                  nameOfBonds = "nameOfBonds",
                  methodOfHolding = "01",
                  dateOfAcqOrContrib = Some(today),
                  costOfBonds = Double.MaxValue,
                  connectedPartyStatus = Some(Connected),
                  bondsUnregulated = No,
                  totalIncomeOrReceipts = Some(Double.MaxValue),
                  bondsDisposed = None
                )
              )
            )
          )
        ),
        otherAssets = Some(
          EtmpOtherAssets(
            recordVersion = None,
            otherAssetsWereHeld = Some(Yes),
            otherAssetsWereDisposed = Some(No),
            noOfTransactions = Some(3),
            otherAssetTransactions = Some(
              Seq(
                EtmpOtherAssetTransaction(
                  prePopulated = None,
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
                  movableSchedule29A = Some(Yes),
                  totalIncomeOrReceipts = Some(Double.MaxValue),
                  assetsDisposed = None
                ),
                EtmpOtherAssetTransaction(
                  prePopulated = None,
                  assetDescription = "assetDescription",
                  methodOfHolding = "02",
                  dateOfAcqOrContrib = Some(today),
                  costOfAsset = Double.MaxValue,
                  acquiredFromName = None,
                  acquiredFromType = None,
                  connectedStatus = None,
                  supportedByIndepValuation = Some(Yes),
                  movableSchedule29A = Some(No),
                  totalIncomeOrReceipts = Some(Double.MaxValue),
                  assetsDisposed = None
                ),
                EtmpOtherAssetTransaction(
                  prePopulated = None,
                  assetDescription = "assetDescription",
                  methodOfHolding = "03",
                  dateOfAcqOrContrib = None,
                  costOfAsset = Double.MaxValue,
                  acquiredFromName = None,
                  acquiredFromType = None,
                  connectedStatus = None,
                  supportedByIndepValuation = None,
                  movableSchedule29A = Some(Yes),
                  totalIncomeOrReceipts = Some(Double.MaxValue),
                  assetsDisposed = None
                )
              )
            )
          )
        )
      )

      transformation.transform(assets) shouldMatchTo expected
    }
  }

  "Land or property with no lessee details" in {
    val landOrProperty = LandOrProperty(
      recordVersion = None,
      optLandOrPropertyHeld = Some(true),
      optDisposeAnyLandOrProperty = Some(false),
      landOrPropertyTransactions = List(
        LandOrPropertyTransactions(
          prePopulated = None,
          propertyDetails = PropertyDetails(
            true,
            Address("some", Some("address"), None, "London", Some("ZZ1 1ZZ"), "GB"),
            false,
            "some reason"
          ),
          heldPropertyTransaction = HeldPropertyTransaction(
            methodOfHolding = SchemeHoldLandProperty.Transfer,
            dateOfAcquisitionOrContribution = None,
            optLandOrPropertyLeased = Some(false),
            optIsLandOrPropertyResidential = Some(true),
            optLeaseDetails = None,
            optPropertyAcquiredFromName = None,
            optPropertyAcquiredFrom = None,
            optConnectedPartyStatus = None,
            totalCostOfLandOrProperty = 1000.0,
            optIndepValuationSupport = None,
            optTotalIncomeOrReceipts = None
          ),
          optDisposedPropertyTransaction = None
        )
      )
    )

    val etmpLandOrProperty = EtmpLandOrProperty(
      recordVersion = None,
      heldAnyLandOrProperty = Some("Yes"),
      disposeAnyLandOrProperty = Some("No"),
      noOfTransactions = Some(1),
      landOrPropertyTransactions = Some(
        List(
          EtmpLandOrPropertyTransactions(
            propertyDetails = EtmpPropertyDetails(
              prePopulated = None,
              landOrPropertyInUK = "Yes",
              addressDetails = EtmpAddress("some", "address", None, Some("London"), None, Some("ZZ1 1ZZ"), "GB"),
              landRegistryDetails = EtmpLandRegistryDetails(
                landRegistryReferenceExists = "No",
                landRegistryReference = None,
                reasonNoReference = Some("some reason")
              )
            ),
            disposedPropertyTransaction = None,
            heldPropertyTransaction = EtmpHeldPropertyTransaction(
              methodOfHolding = "03",
              landOrPropertyLeased = Some("No"),
              leaseDetails = None,
              residentialSchedule29A = Some("Yes"),
              dateOfAcquisitionOrContribution = None,
              propertyAcquiredFromName = None,
              propertyAcquiredFrom = None,
              connectedPartyStatus = None,
              totalCostOfLandOrProperty = 1000.0,
              indepValuationSupport = None,
              totalIncomeOrReceipts = None
            )
          )
        )
      )
    )

    transformation.transform(
      Assets(
        optLandOrProperty = Some(landOrProperty),
        optBorrowing = None,
        optOtherAssets = None,
        optBonds = None
      )
    ) shouldMatchTo EtmpAssets(
      landOrProperty = Some(etmpLandOrProperty),
      borrowing = None,
      bonds = None,
      otherAssets = None
    )
  }

  "Bonds in pre-population" in {
    val bonds = Bonds(
      recordVersion = None,
      optBondsWereAdded = None,
      optBondsWereDisposed = None,
      bondTransactions = bondTransactions.map(_.copy(prePopulated = Some(true)))
    )
    val etmpBonds = EtmpBonds(
      recordVersion = None,
      bondsWereAdded = None,
      bondsWereDisposed = None,
      bondTransactions = Some(etmpBondTransactions.map(_.copy(prePopulated = Some(YesNo.Yes)))),
      noOfTransactions = None
    )
    transformation.transform(
      Assets(
        optLandOrProperty = None,
        optBorrowing = None,
        optOtherAssets = None,
        optBonds = Some(bonds)
      )
    ) shouldMatchTo EtmpAssets(
      landOrProperty = None,
      borrowing = None,
      bonds = Some(etmpBonds),
      otherAssets = None
    )
  }
  "Other assets in pre-population" in {
    val otherAssets = OtherAssets(
      recordVersion = None,
      optOtherAssetsWereHeld = None,
      optOtherAssetsWereDisposed = None,
      otherAssetTransactions = otherAssetTransactions.map(_.copy(prePopulated = Some(true)))
    )
    val etmpOtherAssets = EtmpOtherAssets(
      recordVersion = None,
      otherAssetsWereHeld = None,
      otherAssetsWereDisposed = None,
      otherAssetTransactions = Some(etmpOtherAssetsTransaction.map(_.copy(prePopulated = Some(YesNo.Yes)))),
      noOfTransactions = None
    )
    transformation.transform(
      Assets(
        optLandOrProperty = None,
        optBorrowing = None,
        optOtherAssets = Some(otherAssets),
        optBonds = None
      )
    ) shouldMatchTo EtmpAssets(
      landOrProperty = None,
      borrowing = None,
      bonds = None,
      otherAssets = Some(etmpOtherAssets)
    )
  }
}
