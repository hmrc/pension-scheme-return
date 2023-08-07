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

package uk.gov.hmrc.pensionschemereturn.base

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Environment
import play.api.inject.Injector
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.pensionschemereturn.config.AppConfig

import scala.io.Source

trait SpecBase extends PlaySpec with GuiceOneAppPerSuite {
  def injector: Injector = app.injector

  def environment: Environment = injector.instanceOf[Environment]

  def appConfig: AppConfig = injector.instanceOf[AppConfig]

  def readJsonFromFile(filePath: String): JsValue = {
    val path = Source.fromURL(getClass.getResource(filePath)).mkString
    Json.parse(path)
  }
}
