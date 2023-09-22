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

package uk.gov.hmrc.pensionschemereturn.transformations

import play.api.libs.json._

trait Transformer {
  protected val doNothing: Reads[JsObject] = __.json.put(Json.obj())

  protected def fail[A]: Reads[A] = Reads.failed[A]("Unknown value")

  protected def fail[A](s: A): Reads[A] = Reads.failed[A](s"Unknown value: $s")

  protected def toYesNo(b: JsValue): JsString = if (b.as[JsBoolean].value) JsString("Yes") else JsString("No")

  protected val yes: JsString = JsString("Yes")

  def nodes(seqOfNodes: Seq[JsObject]): JsObject =
    seqOfNodes.foldLeft(Json.obj())((a, b) => a ++ b)

  def requiredNode(node: String)(nodeName: String = node): Reads[Option[JsObject]] =
    (__ \ node).read[String].map {
      case node: String =>
        Some(
          Json.obj(
            nodeName -> node
          )
        )
      case _ => None
    }

  def optionalNode(optNode: Option[String])(nodeName: String = optNode.getOrElse("")): Reads[Option[JsObject]] =
    (__ \ nodeName).readNullable[String].map {
      case Some(node) =>
        Some(
          Json.obj(
            nodeName -> node
          )
        )
      case _ => None
    }

}
