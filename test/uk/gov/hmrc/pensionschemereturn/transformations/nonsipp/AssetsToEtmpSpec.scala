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

import org.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.nonsipp._
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer

import java.time.LocalDate

class AssetsToEtmpSpec extends PlaySpec with MockitoSugar with Transformer {

  private val transformation: AssetsToEtmp = new AssetsToEtmp()
  val today: LocalDate = LocalDate.now

  "AssetsToEtmp - PSR Assets should successfully transform to etmp format " should {
    "as an Individual for LandOrProperty" in {
      val assets = Assets(
        landOrProperty = LandOrProperty(
          landOrPropertyHeld = true,
          landOrPropertyTransactions = List(
            LandOrPropertyTransactions(
              propertyDetails = PropertyDetails(
                landOrPropertyInUK = true,
                addressDetails = Address(
                  "testAddressLine1",
                  "testAddressLine2",
                  Some("testAddressLine3"),
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
              )
            )
          )
        )
      )

      val expected = EtmpAssets(
        landOrProperty = EtmpLandOrProperty(
          recordVersion = None,
          heldAnyLandOrProperty = Yes,
          disposeAnyLandOrProperty = No,
          noOfTransactions = 1,
          landOrPropertyTransactions = Seq(
            EtmpLandOrPropertyTransactions(
              propertyDetails = EtmpPropertyDetails(
                landOrPropertyInUK = Yes,
                addressDetails = EtmpAddress(
                  addressLine1 = "testAddressLine1",
                  addressLine2 = "testAddressLine2",
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
              )
            )
          )
        ),
        borrowing = EtmpBorrowing(moneyWasBorrowed = No),
        bonds = EtmpBonds(bondsWereAdded = No, bondsWereDisposed = No),
        otherAssets = EtmpOtherAssets(otherAssetsWereHeld = No, otherAssetsWereDisposed = No)
      )

      transformation.transform(assets) mustEqual expected
    }
  }
}
