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

package uk.gov.hmrc.pensionschemereturn.config

import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import play.api.Configuration

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject() (config: Configuration, servicesConfig: ServicesConfig, runModeConfiguration: Configuration) {

  lazy val appName: String = config.get[String](path = "appName")
  private val ifURL: String = servicesConfig.baseUrl(serviceName = "if-hod")
  private val pensionsSchemeURL: String = servicesConfig.baseUrl(serviceName = "pensionsScheme")

  lazy val integrationFrameworkEnvironment: String =
    runModeConfiguration.getOptional[String](path = "microservice.services.if-hod.env").getOrElse("local")
  lazy val integrationFrameworkAuthorization: String = "Bearer " + runModeConfiguration
    .getOptional[String](path = "microservice.services.if-hod.authorizationToken")
    .getOrElse("local")

  val getOverviewUrl: String = s"$ifURL${config.get[String](path = "serviceUrls.get-overview")}"
  val getVersionsUrl: String = s"$ifURL${config.get[String](path = "serviceUrls.get-versions")}"
  val submitStandardPsrUrl: String = s"$ifURL${config.get[String](path = "serviceUrls.submit-standard-psr")}"
  val getStandardPsrUrl: String = s"$ifURL${config.get[String](path = "serviceUrls.get-standard-psr")}"
  val isPsaAssociatedUrl: String = s"$pensionsSchemeURL${config.get[String](path = "serviceUrls.is-psa-associated")}"
  val earliestPsrPeriodStartDate: String = config.get[String]("earliestPsrPeriodStartDate")
}
