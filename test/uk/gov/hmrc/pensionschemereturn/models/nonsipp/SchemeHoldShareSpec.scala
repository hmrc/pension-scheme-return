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

import uk.gov.hmrc.pensionschemereturn.models.nonsipp.shares.SchemeHoldShare._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class SchemeHoldShareSpec extends AnyWordSpec with Matchers {

  "SchemeHoldShare" should {

    "successfully convert from SchemeHoldShare to String" in {
      stringToSchemeHoldShare("01") shouldEqual Acquisition
      stringToSchemeHoldShare("02") shouldEqual Contribution
      stringToSchemeHoldShare("03") shouldEqual Transfer
    }
  }
}
