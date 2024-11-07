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

import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets.SchemeHoldAsset.{Acquisition, Contribution}
import com.softwaremill.diffx.scalatest.DiffShouldMatcher
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets._
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.common.EtmpIdentityType
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets.HowDisposed.{Other, Sold, Transferred}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.assets._
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets.SchemeHoldBond.Transfer
import com.softwaremill.diffx.generic.auto.diffForCaseClass
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.mockito.MockitoSugar

import java.time.LocalDate

class AssetsFromEtmpSpec extends PlaySpec with MockitoSugar with Transformer with DiffShouldMatcher {

  private val transformation: AssetsFromEtmp = new AssetsFromEtmp()
  private val today: LocalDate = LocalDate.now
  private def assets(landOrPropertyInUK: String, etmpAddress: EtmpAddress) = EtmpAssets(
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
                landOrPropertyInUK = landOrPropertyInUK,
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
                        indivOrOrgType = "02",
                        idNumber = None,
                        reasonNoIdNumber = Some("NoCrnReason"),
                        otherDescription = None
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
              schemeAssetsValue = Double.MaxValue,
              amountBorrowed = Double.MaxValue,
              interestRate = Double.MinPositiveValue,
              borrowingFromName = "borrowingFromName",
              connectedPartyStatus = Unconnected,
              reasonForBorrow = "reasonForBorrow"
            )
          )
        )
      )
    ),
    bonds = Some(
      EtmpBonds(
        recordVersion = Some("001"),
        bondsWereAdded = Yes,
        bondsWereDisposed = Yes,
        noOfTransactions = Some(1),
        bondTransactions = Some(
          Seq(
            EtmpBondTransactions(
              nameOfBonds = "nameOfBonds",
              methodOfHolding = "03",
              dateOfAcqOrContrib = Some(today),
              costOfBonds = Double.MaxValue,
              connectedPartyStatus = Some(Unconnected),
              bondsUnregulated = Yes,
              totalIncomeOrReceipts = Double.MaxValue,
              bondsDisposed = Some(
                Seq(
                  EtmpBondsDisposed(
                    methodOfDisposal = "01",
                    otherMethod = None,
                    dateSold = Some(today),
                    amountReceived = Some(Double.MaxValue),
                    bondsPurchaserName = Some("BondsPurchaserName"),
                    connectedPartyStatus = Some(Unconnected),
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
        recordVersion = Some("001"),
        otherAssetsWereHeld = Yes,
        otherAssetsWereDisposed = No,
        noOfTransactions = Some(1),
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
              connectedStatus = Some(Connected),
              supportedByIndepValuation = Some(No),
              movableSchedule29A = Yes,
              totalIncomeOrReceipts = Double.MaxValue,
              assetsDisposed = None
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
              totalIncomeOrReceipts = Double.MaxValue,
              assetsDisposed = Some(
                Seq(
                  EtmpAssetsDisposed(
                    methodOfDisposal = "01",
                    otherMethod = None,
                    dateSold = Some(today),
                    purchaserName = Some("PurchaserName"),
                    purchaserType = Some(
                      EtmpIdentityType(
                        indivOrOrgType = "03",
                        idNumber = None,
                        reasonNoIdNumber = Some("NoUtrReason"),
                        otherDescription = None
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
              assetDescription = "assetDescription",
              methodOfHolding = "03",
              dateOfAcqOrContrib = None,
              costOfAsset = Double.MaxValue,
              acquiredFromName = None,
              acquiredFromType = None,
              connectedStatus = None,
              supportedByIndepValuation = None,
              movableSchedule29A = No,
              totalIncomeOrReceipts = Double.MaxValue,
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
        )
      )
    )
  )
  private def expected(landOrPropertyInUK: Boolean, address: Address) = Assets(
    optLandOrProperty = Some(
      LandOrProperty(
        recordVersion = Some("001"),
        optLandOrPropertyHeld = Some(true),
        optDisposeAnyLandOrProperty = Some(true),
        landOrPropertyTransactions = List(
          LandOrPropertyTransactions(
            propertyDetails = PropertyDetails(
              landOrPropertyInUK = landOrPropertyInUK,
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
                      IdentityType.UKCompany,
                      None,
                      Some("NoCrnReason"),
                      None
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
            schemeAssetsValue = Double.MaxValue,
            amountBorrowed = Double.MaxValue,
            interestRate = Double.MinPositiveValue,
            borrowingFromName = "borrowingFromName",
            connectedPartyStatus = false,
            reasonForBorrow = "reasonForBorrow"
          )
        )
      )
    ),
    optBonds = Some(
      Bonds(
        recordVersion = Some("001"),
        bondsWereAdded = true,
        bondsWereDisposed = true,
        bondTransactions = Seq(
          BondTransactions(
            nameOfBonds = "nameOfBonds",
            methodOfHolding = Transfer,
            optDateOfAcqOrContrib = Some(today),
            costOfBonds = Double.MaxValue,
            optConnectedPartyStatus = Some(false),
            bondsUnregulated = true,
            totalIncomeOrReceipts = Double.MaxValue,
            optBondsDisposed = Some(
              Seq(
                BondDisposed(
                  methodOfDisposal = Sold,
                  optOtherMethod = None,
                  optDateSold = Some(today),
                  optAmountReceived = Some(Double.MaxValue),
                  optBondsPurchaserName = Some("BondsPurchaserName"),
                  optConnectedPartyStatus = Some(false),
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
        recordVersion = Some("001"),
        otherAssetsWereHeld = true,
        otherAssetsWereDisposed = false,
        otherAssetTransactions = Seq(
          OtherAssetTransaction(
            assetDescription = "assetDescription",
            methodOfHolding = Acquisition,
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
            optConnectedStatus = Some(true),
            optIndepValuationSupport = Some(false),
            movableSchedule29A = true,
            totalIncomeOrReceipts = Double.MaxValue,
            optOtherAssetDisposed = None
          ),
          OtherAssetTransaction(
            assetDescription = "assetDescription",
            methodOfHolding = Contribution,
            optDateOfAcqOrContrib = Some(today),
            costOfAsset = Double.MaxValue,
            optPropertyAcquiredFromName = None,
            optPropertyAcquiredFrom = None,
            optConnectedStatus = None,
            optIndepValuationSupport = Some(true),
            movableSchedule29A = false,
            totalIncomeOrReceipts = Double.MaxValue,
            optOtherAssetDisposed = Some(
              Seq(
                OtherAssetDisposed(
                  methodOfDisposal = Sold,
                  optOtherMethod = None,
                  optDateSold = Some(today),
                  optPurchaserName = Some("PurchaserName"),
                  optPropertyAcquiredFrom = Some(
                    PropertyAcquiredFrom(
                      IdentityType.UKPartnership,
                      None,
                      Some("NoUtrReason"),
                      None
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
            assetDescription = "assetDescription",
            methodOfHolding = SchemeHoldAsset.Transfer,
            optDateOfAcqOrContrib = None,
            costOfAsset = Double.MaxValue,
            optPropertyAcquiredFromName = None,
            optPropertyAcquiredFrom = None,
            optConnectedStatus = None,
            optIndepValuationSupport = None,
            movableSchedule29A = false,
            totalIncomeOrReceipts = Double.MaxValue,
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
      )
    )
  )

  "AssetsFromEtmp - PSR Assets should successfully transform from etmp format " should {

    "when optional fields are None" in {

      val assets = EtmpAssets(
        landOrProperty = None,
        borrowing = None,
        bonds = None,
        otherAssets = None
      )
      val expected = Assets(
        optLandOrProperty = None,
        optBorrowing = None,
        optBonds = None,
        optOtherAssets = None
      )
      transformation.transform(assets) shouldMatchTo expected
    }

    "when optional fields are non-None" in {

      transformation.transform(
        assets(
          landOrPropertyInUK = Yes,
          etmpAddress = EtmpAddress(
            addressLine1 = "testAddressLine1",
            addressLine2 = "testAddressLine2",
            addressLine3 = Some("testAddressLine3"),
            addressLine4 = Some("town"),
            addressLine5 = None,
            ukPostCode = Some("GB135HG"),
            countryCode = "GB"
          )
        )
      ) shouldMatchTo expected(
        landOrPropertyInUK = true,
        address = Address(
          "testAddressLine1",
          Some("testAddressLine2"),
          Some("testAddressLine3"),
          "town",
          Some("GB135HG"),
          "GB"
        )
      )
    }

    "when non UK address" in {

      transformation.transform(
        assets(
          landOrPropertyInUK = No,
          etmpAddress = EtmpAddress(
            addressLine1 = "testAddressLine1",
            addressLine2 = "testAddressLine2",
            addressLine3 = Some("testAddressLine3"),
            addressLine4 = Some("town"),
            addressLine5 = Some("nonUkPostcode"),
            ukPostCode = None,
            countryCode = "TR"
          )
        )
      ) shouldMatchTo expected(
        landOrPropertyInUK = false,
        address = Address(
          "testAddressLine1",
          Some("testAddressLine2"),
          Some("testAddressLine3"),
          "town",
          Some("nonUkPostcode"),
          "TR"
        )
      )
    }
  }
}
