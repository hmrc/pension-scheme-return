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

import com.softwaremill.diffx.generic.AutoDerivation
import com.softwaremill.diffx.scalatest.DiffShouldMatcher
import org.mockito.MockitoSugar
import org.scalatest.{AppendedClues, BeforeAndAfterEach}
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer
import uk.gov.hmrc.pensionschemereturn.transformations.nonsipp._
import utils.TestValues

trait EtmpTransformerSpec
    extends PlaySpec
    with MockitoSugar
    with Transformer
    with BeforeAndAfterEach
    with TestValues
    with DiffShouldMatcher
    with AutoDerivation
    with AppendedClues {

  val employerContributionsTransformer = new EmployerContributionsTransformer()
  val memberPersonalDetailsTransformer = new MemberPersonalDetailsTransformer()
  val transferInTransformer = new TransferInTransformer()
  val transferOutTransformer = new TransferOutTransformer()

  val memberPaymentsTransformer = new MemberPaymentsTransformer(
    employerContributionsTransformer,
    memberPersonalDetailsTransformer,
    transferInTransformer,
    transferOutTransformer
  )
}
