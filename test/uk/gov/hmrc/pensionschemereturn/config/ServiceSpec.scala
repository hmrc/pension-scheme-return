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

package uk.gov.hmrc.pensionschemereturn.config

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.ConfigLoader
import utils.BaseSpec

class ServiceSpec extends BaseSpec with ScalaCheckPropertyChecks {

  def get(config: Config, path: String)(implicit configLoader: ConfigLoader[Service]): Service =
    configLoader.load(config, path)

  "service" should {

    "populate a service from a config" in {
      val config = ConfigFactory.parseString("""
          |service {
          | protocol = http
          | host = localhost
          | port = 8000
          |}""".stripMargin)

      get(config, "service") mustBe Service("http", "localhost", 8000)
    }

    "use https when no protocol is provided" in {
      val config = ConfigFactory.parseString("""
          |service {
          | host = localhost
          | port = 8000
          |}""".stripMargin)

      get(config, "service") mustBe Service("https", "localhost", 8000)
    }
  }

}
