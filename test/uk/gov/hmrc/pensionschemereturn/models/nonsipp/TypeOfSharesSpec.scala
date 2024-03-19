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

import uk.gov.hmrc.pensionschemereturn.models.nonsipp.shares.TypeOfShares._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TypeOfSharesSpec extends AnyWordSpec with Matchers {

  "TypeOfShares" should {

    "successfully convert from TypeOfShares to String" in {
      stringToTypeOfShares("01") shouldEqual SponsoringEmployer
      stringToTypeOfShares("02") shouldEqual Unquoted
      stringToTypeOfShares("03") shouldEqual ConnectedParty
    }
  }
}
