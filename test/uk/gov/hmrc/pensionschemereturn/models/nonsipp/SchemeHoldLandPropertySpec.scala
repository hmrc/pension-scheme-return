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

package uk.gov.hmrc.pensionschemereturn.models.nonsipp

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.SchemeHoldLandProperty.{
  schemeHoldLandPropertyToString,
  stringToSchemeHoldLandProperty,
  Acquisition,
  Contribution,
  Transfer
}

class SchemeHoldLandPropertySpec extends AnyWordSpec with Matchers {

  "SchemeHoldLandProperty" should {

    "successfully convert from string to SchemeHoldLandProperty" in {
      schemeHoldLandPropertyToString(Acquisition) shouldEqual "01"
      schemeHoldLandPropertyToString(Contribution) shouldEqual "02"
      schemeHoldLandPropertyToString(Transfer) shouldEqual "03"
    }

    "successfully convert from SchemeHoldLandProperty to String" in {
      stringToSchemeHoldLandProperty("01") shouldEqual Acquisition
      stringToSchemeHoldLandProperty("02") shouldEqual Contribution
      stringToSchemeHoldLandProperty("03") shouldEqual Transfer
    }
  }
}
