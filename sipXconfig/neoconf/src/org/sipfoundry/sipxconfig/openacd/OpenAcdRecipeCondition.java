/**
 *
 *
 * Copyright (c) 2010 / 2011 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.openacd;

import java.io.Serializable;

import com.mongodb.BasicDBObject;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class OpenAcdRecipeCondition implements Serializable {
    public static enum CONDITION {
        TICK_INTERVAL {
            public String toString() {
                return "ticks";
            }
        },
        AGENTS_AVAILABLE {
            public String toString() {
                return "available_agents";
            }
        },
        AGENTS_ELIGIBLE {
            public String toString() {
                return "eligible_agents";
            }
        },
        CALLS_IN_QUEUE {
            public String toString() {
                return "calls_queued";
            }
        },
        POSITION_IN_QUEUE {
            public String toString() {
                return "queue_position";
            }
        },
        CLIENT {
            public String toString() {
                return "client";
            }
        },
        HOUR {
            public String toString() {
                return "hour";
            }
        },
        DAY_OF_WEEK {
            public String toString() {
                return "weekday";
            }
        },
        MEDIA_TYPE {
            public String toString() {
                return "media_type";
            }
        },
        CLIENTS_QUEUED {
            public String toString() {
                return "client_calls_queued";
            }
        }
    }

    public static enum RELATION {
        IS {
            public String toString() {
                return "is";
            }
        },
        GREATER {
            public String toString() {
                return "greater";
            }
        },
        LESS {
            public String toString() {
                return "less";
            }
        },
        NOT {
            public String toString() {
                return "isNot";
            }
        }
    }

    private String m_condition = CONDITION.TICK_INTERVAL.toString();

    private String m_relation = RELATION.IS.toString();

    private String m_valueCondition = "5";

    public String getCondition() {
        return m_condition;
    }

    public void setCondition(String option) {
        m_condition = option;
    }

    public String getRelation() {
        return m_relation;
    }

    public void setRelation(String relation) {
        m_relation = relation;
    }

    public String getValueCondition() {
        return m_valueCondition;
    }

    public void setValueCondition(String valueCondition) {
        m_valueCondition = valueCondition;
    }

    public int hashCode() {
        return new HashCodeBuilder().append(m_condition).append(m_relation).append(m_valueCondition).toHashCode();
    }

    public boolean equals(Object other) {
        if (!(other instanceof OpenAcdRecipeCondition)) {
            return false;
        }
        if (this == other) {
            return true;
        }
        OpenAcdRecipeCondition bean = (OpenAcdRecipeCondition) other;
        return new EqualsBuilder().append(m_condition, bean.getCondition()).append(m_relation, bean.getRelation())
                .append(m_valueCondition, bean.getValueCondition()).isEquals();
    }

    public BasicDBObject getMongoObject() {
        BasicDBObject condition = new BasicDBObject();
        String conditionValue = m_condition;
        if (m_condition.equals(CONDITION.MEDIA_TYPE.toString())) {
            conditionValue = "type";
        }
        condition.put("condition", conditionValue);
        String relation = "=";
        if (m_relation.equals(RELATION.NOT.toString())) {
            relation = "!=";
        } else if (m_relation.equals(RELATION.GREATER.toString())) {
            relation = ">";
        } else if (m_relation.equals(RELATION.LESS.toString())) {
            relation = "<";
        }
        condition.put("relation", relation);
        condition.put("value", m_valueCondition);
        return condition;
    }
}
