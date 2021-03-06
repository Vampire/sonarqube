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
package org.sonar.core.issue.db;

import org.junit.Test;
import org.sonar.api.issue.internal.DefaultIssueComment;
import org.sonar.api.issue.internal.FieldDiffs;
import org.sonar.api.utils.DateUtils;

import java.util.Date;

import static org.fest.assertions.Assertions.assertThat;

public class IssueChangeDtoTest {

  @Test
  public void create_from_comment() throws Exception {
    DefaultIssueComment comment = DefaultIssueComment.create("ABCDE", "emmerik", "the comment");

    IssueChangeDto dto = IssueChangeDto.of(comment);

    assertThat(dto.getChangeData()).isEqualTo("the comment");
    assertThat(dto.getChangeType()).isEqualTo("comment");
    assertThat(dto.getCreatedAt()).isNotNull();
    assertThat(dto.getUpdatedAt()).isNotNull();
    assertThat(dto.getIssueKey()).isEqualTo("ABCDE");
    assertThat(dto.getUserLogin()).isEqualTo("emmerik");
  }

  @Test
  public void create_from_diff() throws Exception {
    FieldDiffs diffs = new FieldDiffs();
    diffs.setDiff("severity", "INFO", "BLOCKER");
    diffs.setUserLogin("emmerik");
    diffs.setCreationDate(DateUtils.parseDate("2014-01-03"));

    IssueChangeDto dto = IssueChangeDto.of("ABCDE", diffs);

    assertThat(dto.getChangeData()).isEqualTo("severity=INFO|BLOCKER");
    assertThat(dto.getChangeType()).isEqualTo("diff");
    assertThat(dto.getCreatedAt()).isNotNull();
    assertThat(dto.getUpdatedAt()).isNotNull();
    assertThat(dto.getIssueChangeCreationDate()).isEqualTo(DateUtils.parseDate("2014-01-03"));
    assertThat(dto.getIssueKey()).isEqualTo("ABCDE");
    assertThat(dto.getUserLogin()).isEqualTo("emmerik");
  }

  @Test
  public void to_comment() throws Exception {
    IssueChangeDto changeDto = new IssueChangeDto()
      .setKey("EFGH")
      .setUserLogin("emmerik")
      .setChangeData("Some text")
      .setIssueKey("ABCDE")
      .setCreatedAt(new Date())
      .setUpdatedAt(new Date());

    DefaultIssueComment comment = changeDto.toComment();
    assertThat(comment.key()).isEqualTo("EFGH");
    assertThat(comment.markdownText()).isEqualTo("Some text");
    assertThat(comment.createdAt()).isNotNull();
    assertThat(comment.updatedAt()).isNotNull();
    assertThat(comment.userLogin()).isEqualTo("emmerik");
    assertThat(comment.issueKey()).isEqualTo("ABCDE");
  }

  @Test
  public void to_field_diffs_with_issue_creation_date() throws Exception {
    IssueChangeDto changeDto = new IssueChangeDto()
      .setKey("EFGH")
      .setUserLogin("emmerik")
      .setChangeData("Some text")
      .setIssueKey("ABCDE")
      .setIssueChangeCreationDate(new Date());

    FieldDiffs diffs = changeDto.toFieldDiffs();
    assertThat(diffs.userLogin()).isEqualTo("emmerik");
    assertThat(diffs.issueKey()).isEqualTo("ABCDE");
    assertThat(diffs.creationDate()).isNotNull();
  }

  @Test
  public void to_field_diffs_with_create_at() throws Exception {
    IssueChangeDto changeDto = new IssueChangeDto()
      .setKey("EFGH")
      .setUserLogin("emmerik")
      .setChangeData("Some text")
      .setIssueKey("ABCDE")
      .setCreatedAt(new Date());

    FieldDiffs diffs = changeDto.toFieldDiffs();
    assertThat(diffs.userLogin()).isEqualTo("emmerik");
    assertThat(diffs.issueKey()).isEqualTo("ABCDE");
    assertThat(diffs.creationDate()).isNotNull();
  }

  @Test
  public void to_string() throws Exception {
    DefaultIssueComment comment = DefaultIssueComment.create("ABCDE", "emmerik", "the comment");
    IssueChangeDto dto = IssueChangeDto.of(comment);
    assertThat(dto.toString()).contains("ABCDE");
  }
}
