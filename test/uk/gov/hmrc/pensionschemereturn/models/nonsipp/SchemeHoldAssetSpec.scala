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

package uk.gov.hmrc.pensionschemereturn.models.nonsipp

import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets.SchemeHoldAsset._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class SchemeHoldAssetSpec extends AnyWordSpec with Matchers {

  "SchemeHoldAsset" should {

    "successfully convert from string to SchemeHoldAsset" in {
      schemeHoldAssetToString(Acquisition) shouldEqual "01"
      schemeHoldAssetToString(Contribution) shouldEqual "02"
      schemeHoldAssetToString(Transfer) shouldEqual "03"
    }

    "successfully convert from SchemeHoldAsset to String" in {
      stringToSchemeHoldAsset("01") shouldEqual Acquisition
      stringToSchemeHoldAsset("02") shouldEqual Contribution
      stringToSchemeHoldAsset("03") shouldEqual Transfer
    }
  }
}
