/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.api.batch.rule.internal;

import org.junit.Test;
import org.sonar.api.batch.debt.DebtRemediationFunction;
import org.sonar.api.batch.rule.Rule;
import org.sonar.api.batch.rule.Rules;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rule.RuleStatus;
import org.sonar.api.rule.Severity;
import org.sonar.api.utils.Duration;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

public class RulesBuilderTest {
  @Test
  public void no_rules() throws Exception {
    RulesBuilder builder = new RulesBuilder();
    Rules rules = builder.build();
    assertThat(rules.findAll()).isEmpty();
  }

  @Test
  public void build_rules() throws Exception {
    RulesBuilder builder = new RulesBuilder();
    NewRule newSquid1 = builder.add(RuleKey.of("squid", "S0001"));
    newSquid1.setName("Detect bug");
    newSquid1.setDescription("Detect potential bug");
    newSquid1.setMetadata("foo=bar");
    newSquid1.setSeverity(Severity.CRITICAL);
    newSquid1.setStatus(RuleStatus.BETA);
    newSquid1.setDebtSubCharacteristic("COMPILER");
    newSquid1.setDebtRemediationFunction(DebtRemediationFunction.create(DebtRemediationFunction.Type.LINEAR_OFFSET, Duration.create(10), Duration.create(60)));
    newSquid1.addParam("min");
    newSquid1.addParam("max").setDescription("Maximum");
    // most simple rule
    builder.add(RuleKey.of("squid", "S0002"));
    builder.add(RuleKey.of("findbugs", "NPE"));

    Rules rules = builder.build();

    assertThat(rules.findAll()).hasSize(3);
    assertThat(rules.findByRepository("squid")).hasSize(2);
    assertThat(rules.findByRepository("findbugs")).hasSize(1);
    assertThat(rules.findByRepository("unknown")).isEmpty();

    Rule squid1 = rules.find(RuleKey.of("squid", "S0001"));
    assertThat(squid1.key().repository()).isEqualTo("squid");
    assertThat(squid1.key().rule()).isEqualTo("S0001");
    assertThat(squid1.name()).isEqualTo("Detect bug");
    assertThat(squid1.description()).isEqualTo("Detect potential bug");
    assertThat(squid1.metadata()).isEqualTo("foo=bar");
    assertThat(squid1.status()).isEqualTo(RuleStatus.BETA);
    assertThat(squid1.severity()).isEqualTo(Severity.CRITICAL);
    assertThat(squid1.debtSubCharacteristic()).isEqualTo("COMPILER");
    assertThat(squid1.debtRemediationFunction().type()).isEqualTo(DebtRemediationFunction.Type.LINEAR_OFFSET);
    assertThat(squid1.debtRemediationFunction().coefficient()).isEqualTo(Duration.create(10));
    assertThat(squid1.debtRemediationFunction().offset()).isEqualTo(Duration.create(60));
    assertThat(squid1.debtSubCharacteristic()).isEqualTo("COMPILER");
    assertThat(squid1.params()).hasSize(2);
    assertThat(squid1.param("min").key()).isEqualTo("min");
    assertThat(squid1.param("min").description()).isNull();
    assertThat(squid1.param("max").key()).isEqualTo("max");
    assertThat(squid1.param("max").description()).isEqualTo("Maximum");

    Rule squid2 = rules.find(RuleKey.of("squid", "S0002"));
    assertThat(squid2.key().repository()).isEqualTo("squid");
    assertThat(squid2.key().rule()).isEqualTo("S0002");
    assertThat(squid2.description()).isNull();
    assertThat(squid2.metadata()).isNull();
    assertThat(squid2.status()).isEqualTo(RuleStatus.defaultStatus());
    assertThat(squid2.severity()).isEqualTo(Severity.defaultSeverity());
    assertThat(squid2.debtSubCharacteristic()).isNull();
    assertThat(squid2.debtRemediationFunction()).isNull();
    assertThat(squid2.params()).isEmpty();
  }

  @Test
  public void fail_to_add_twice_the_same_rule() throws Exception {
    RulesBuilder builder = new RulesBuilder();
    builder.add(RuleKey.of("squid", "S0001"));
    try {
      builder.add(RuleKey.of("squid", "S0001"));
      fail();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessage("Rule 'squid:S0001' already exists");
    }
  }

  @Test
  public void fail_to_add_twice_the_same_param() throws Exception {
    RulesBuilder builder = new RulesBuilder();
    NewRule newRule = builder.add(RuleKey.of("squid", "S0001"));
    newRule.addParam("min");
    newRule.addParam("max");
    try {
      newRule.addParam("min");
      fail();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessage("Parameter 'min' already exists on rule 'squid:S0001'");
    }
  }
}
