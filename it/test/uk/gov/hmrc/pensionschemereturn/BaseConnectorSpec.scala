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

package uk.gov.hmrc.pensionschemereturn

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import com.softwaremill.diffx.scalatest.DiffShouldMatcher
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.{Millis, Span}
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.test.{HttpClientV2Support, WireMockSupport}

import scala.jdk.CollectionConverters.*
import scala.reflect.ClassTag

abstract class BaseConnectorSpec
    extends AnyWordSpec
    with WireMockSupport
    with HttpClientV2Support
    with DiffShouldMatcher
    with ScalaFutures
    with Matchers {

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(500, Millis)), interval = scaled(Span(50, Millis)))

  protected def applicationBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "auditing.enabled" -> false,
        "metric.enabled" -> false
      )

  given queryParamsToJava: Conversion[Map[String, String], java.util.Map[String, StringValuePattern]] = _.map {
    case (k, v) =>
      k -> equalTo(v)
  }.asJava

  protected def injected[A: ClassTag](implicit app: Application): A = app.injector.instanceOf[A]

  def stubGet(url: String, response: ResponseDefinitionBuilder): StubMapping =
    wireMockServer.stubFor(
      get(urlEqualTo(url))
        .willReturn(response)
    )

  def stubGet(url: String, queryParams: Map[String, String], response: ResponseDefinitionBuilder): StubMapping =
    wireMockServer.stubFor(
      get(urlPathTemplate(url))
        .withQueryParams(queryParams)
        .willReturn(response)
    )

  def stubPost(url: String, requestBody: String, response: ResponseDefinitionBuilder): StubMapping =
    wireMockServer.stubFor(
      post(urlEqualTo(url))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(equalTo(requestBody))
        .willReturn(response)
    )

  def stubDelete(url: String, response: ResponseDefinitionBuilder): StubMapping =
    wireMockServer.stubFor(
      delete(urlEqualTo(url))
        .willReturn(response)
    )
}
