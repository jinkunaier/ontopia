/*
 * #!
 * Ontopia DB2TM
 * #-
 * Copyright (C) 2001 - 2013 The Ontopia Project
 * #-
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * !#
 */

package net.ontopia.topicmaps.db2tm;

/**
 * INTERNAL: Represents an &lt;expression-column> element in the
 * mapping file. Effectively a virtual column which uses a SQL
 * expression to produce a value inside the SQL query.
 */
public class ExpressionVirtualColumn {
  private String colname;
  private String sqlexpression;

  public ExpressionVirtualColumn(String colname) {
    this.colname = colname;
  }

  public String getColumnName() {
    return colname;
  }

  public String getSQLExpression() {
    return sqlexpression;
  }

  public void setSQLExpression(String sqlexpression) {
    this.sqlexpression = sqlexpression;
  }
}