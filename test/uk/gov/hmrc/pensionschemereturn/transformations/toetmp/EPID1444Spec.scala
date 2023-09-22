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

package uk.gov.hmrc.pensionschemereturn.transformations.toetmp

import generators.GeneratorEPID1444
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json._

class EPID1444Spec extends AnyFreeSpec with Matchers with GeneratorEPID1444 with ScalaCheckPropertyChecks {

  private val transformer = new EPID1444

  "transformToETMPData" - {
    "must transform a randomly generated valid payload correctly for Standard PSR" in {
      forAll(generateUserAnswersAndPOSTBody) {
        case (userAnswers: JsValue, expectedResponse: JsValue) =>
          val result = userAnswers.validate(transformer.transformToETMPData)
          val expectedResult = JsSuccess(expectedResponse)
          result.asOpt mustBe expectedResult.asOpt
      }
    }
  }
}
