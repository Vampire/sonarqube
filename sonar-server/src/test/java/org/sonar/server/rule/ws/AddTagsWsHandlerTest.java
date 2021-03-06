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

package org.sonar.server.rule.ws;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.server.ws.WsTester;
import org.sonar.core.permission.GlobalPermissions;
import org.sonar.server.rule.Rule;
import org.sonar.server.rule.Rules;
import org.sonar.server.user.MockUserSession;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddTagsWsHandlerTest {

  @Mock
  Rules rules;

  WsTester tester;

  @Before
  public void setUp() throws Exception {
    tester = new WsTester(new RulesWs(mock(RuleSearchWsHandler.class), mock(RuleShowWsHandler.class), new AddTagsWsHandler(rules), mock(RemoveTagsWsHandler.class)));
  }

  @Test
  public void add_tags() throws Exception {
    String ruleKey = "squid:AvoidCycle";
    Rule rule = createStandardRule();
    when(rules.findByKey(RuleKey.of("squid", "AvoidCycle"))).thenReturn(rule);

    MockUserSession.set().setGlobalPermissions(GlobalPermissions.QUALITY_PROFILE_ADMIN);
    WsTester.TestRequest request = tester.newRequest("add_tags").setParam("key", ruleKey).setParam("tags", "tag1,tag2");
    request.execute().assertNoContent();

    ArgumentCaptor<Object> newTagsCaptor = ArgumentCaptor.forClass(Object.class);
    verify(rules).updateRuleTags(isA(Integer.class), newTagsCaptor.capture());
    Object newTags = newTagsCaptor.getValue();
    assertThat(newTags).isInstanceOf(List.class);
    assertThat((List<String>) newTags).hasSize(4).containsOnly("admin1", "admin2", "tag1", "tag2");
  }

  private Rule create(String repoKey, String key, String name, String description) {
    Rule mockRule = mock(Rule.class);
    when(mockRule.adminTags()).thenReturn(ImmutableList.of("admin1", "admin2"));
    return mockRule;
  }

  private Rule createStandardRule() {
    return create("xoo", "RuleWithTags", "Rule with tags", "This rule has tags set");
  }
}
