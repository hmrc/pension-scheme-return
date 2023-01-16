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

package uk.gov.hmrc.pensionschemereturn.utils

import uk.gov.hmrc.pensionschemereturn.utils.FutureUtils._
import utils.BaseSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.{failed, successful}

class FutureUtilsSpec extends BaseSpec {

  case class Failure(msg: String) extends Exception {
    override def toString: String = msg
  }

  "tap" should {
    "not change the result on successful tap" in {
      successful("initial").tap(_ => successful("forget")).futureValue mustBe "initial"
    }

    "not change the result if tap fails" in {
      successful("initial").tap(_ => failed(Failure("failure"))).futureValue mustBe "initial"
    }
  }

  "tapError" should {
    "not change the result on successful tap" in {
      failed[String](Failure("initial")).tapError(_ => successful("forget")).failed.futureValue mustBe Failure("initial")
    }

    "not change the failure on failed tap" in {
      failed[String](Failure("initial")).tapError(_ => failed(Failure("failure"))).failed.futureValue mustBe Failure("initial")
    }
  }

  "as" should {

    "change the value and type" in {
      successful("initial").as(1).futureValue mustBe 1
    }

    "preserve the failure case" in {
      failed(Failure("fail")).as(1).failed.futureValue mustBe Failure("fail")
    }
  }
}