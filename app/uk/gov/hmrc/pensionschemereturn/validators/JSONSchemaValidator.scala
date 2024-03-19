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

package uk.gov.hmrc.pensionschemereturn.validators

import com.networknt.schema.{JsonSchemaFactory, SpecVersion, ValidationMessage}
import com.google.inject.{Inject, Singleton}
import com.fasterxml.jackson.databind.ObjectMapper
import play.api.libs.json._

import scala.jdk.CollectionConverters.CollectionHasAsScala

case class SchemaValidationResult(errors: Set[ValidationMessage]) {
  override def toString: String =
    errors.map(validationMessage => validationMessage.toString + "\n").mkString + "\n"

  def hasErrors: Boolean = errors.nonEmpty
}

@Singleton()
class JSONSchemaValidator @Inject()() {

  def validatePayload(jsonSchemaPath: String, data: JsValue): SchemaValidationResult = {
    val schemaUrl = getClass.getResourceAsStream(jsonSchemaPath)
    val factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4)
    val schema = factory.getSchema(schemaUrl)
    val mapper = new ObjectMapper()
    val jsonNode = mapper.readTree(data.toString())

    val errors = schema.validate(jsonNode).asScala.toSet
    SchemaValidationResult(errors)
  }
}
