package com.ds.transfer.record.entity;

import java.util.ArrayList;
import java.util.List;

public class TransferOtherRuleExample {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table transfer_other_rule
     *
     * @mbggenerated Thu May 10 16:02:09 CST 2018
     */
    protected String orderByClause;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table transfer_other_rule
     *
     * @mbggenerated Thu May 10 16:02:09 CST 2018
     */
    protected boolean distinct;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table transfer_other_rule
     *
     * @mbggenerated Thu May 10 16:02:09 CST 2018
     */
    protected List<Criteria> oredCriteria;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table transfer_other_rule
     *
     * @mbggenerated Thu May 10 16:02:09 CST 2018
     */
    public TransferOtherRuleExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table transfer_other_rule
     *
     * @mbggenerated Thu May 10 16:02:09 CST 2018
     */
    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table transfer_other_rule
     *
     * @mbggenerated Thu May 10 16:02:09 CST 2018
     */
    public String getOrderByClause() {
        return orderByClause;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table transfer_other_rule
     *
     * @mbggenerated Thu May 10 16:02:09 CST 2018
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table transfer_other_rule
     *
     * @mbggenerated Thu May 10 16:02:09 CST 2018
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table transfer_other_rule
     *
     * @mbggenerated Thu May 10 16:02:09 CST 2018
     */
    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table transfer_other_rule
     *
     * @mbggenerated Thu May 10 16:02:09 CST 2018
     */
    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table transfer_other_rule
     *
     * @mbggenerated Thu May 10 16:02:09 CST 2018
     */
    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table transfer_other_rule
     *
     * @mbggenerated Thu May 10 16:02:09 CST 2018
     */
    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table transfer_other_rule
     *
     * @mbggenerated Thu May 10 16:02:09 CST 2018
     */
    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table transfer_other_rule
     *
     * @mbggenerated Thu May 10 16:02:09 CST 2018
     */
    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table transfer_other_rule
     *
     * @mbggenerated Thu May 10 16:02:09 CST 2018
     */
    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Integer value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Integer value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Integer value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Integer value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Integer value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Integer> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Integer> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Integer value1, Integer value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Integer value1, Integer value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andSiteIdIsNull() {
            addCriterion("site_id is null");
            return (Criteria) this;
        }

        public Criteria andSiteIdIsNotNull() {
            addCriterion("site_id is not null");
            return (Criteria) this;
        }

        public Criteria andSiteIdEqualTo(Integer value) {
            addCriterion("site_id =", value, "siteId");
            return (Criteria) this;
        }

        public Criteria andSiteIdNotEqualTo(Integer value) {
            addCriterion("site_id <>", value, "siteId");
            return (Criteria) this;
        }

        public Criteria andSiteIdGreaterThan(Integer value) {
            addCriterion("site_id >", value, "siteId");
            return (Criteria) this;
        }

        public Criteria andSiteIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("site_id >=", value, "siteId");
            return (Criteria) this;
        }

        public Criteria andSiteIdLessThan(Integer value) {
            addCriterion("site_id <", value, "siteId");
            return (Criteria) this;
        }

        public Criteria andSiteIdLessThanOrEqualTo(Integer value) {
            addCriterion("site_id <=", value, "siteId");
            return (Criteria) this;
        }

        public Criteria andSiteIdIn(List<Integer> values) {
            addCriterion("site_id in", values, "siteId");
            return (Criteria) this;
        }

        public Criteria andSiteIdNotIn(List<Integer> values) {
            addCriterion("site_id not in", values, "siteId");
            return (Criteria) this;
        }

        public Criteria andSiteIdBetween(Integer value1, Integer value2) {
            addCriterion("site_id between", value1, value2, "siteId");
            return (Criteria) this;
        }

        public Criteria andSiteIdNotBetween(Integer value1, Integer value2) {
            addCriterion("site_id not between", value1, value2, "siteId");
            return (Criteria) this;
        }

        public Criteria andUsernameIsNull() {
            addCriterion("username is null");
            return (Criteria) this;
        }

        public Criteria andUsernameIsNotNull() {
            addCriterion("username is not null");
            return (Criteria) this;
        }

        public Criteria andUsernameEqualTo(String value) {
            addCriterion("username =", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameNotEqualTo(String value) {
            addCriterion("username <>", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameGreaterThan(String value) {
            addCriterion("username >", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameGreaterThanOrEqualTo(String value) {
            addCriterion("username >=", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameLessThan(String value) {
            addCriterion("username <", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameLessThanOrEqualTo(String value) {
            addCriterion("username <=", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameLike(String value) {
            addCriterion("username like", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameNotLike(String value) {
            addCriterion("username not like", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameIn(List<String> values) {
            addCriterion("username in", values, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameNotIn(List<String> values) {
            addCriterion("username not in", values, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameBetween(String value1, String value2) {
            addCriterion("username between", value1, value2, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameNotBetween(String value1, String value2) {
            addCriterion("username not between", value1, value2, "username");
            return (Criteria) this;
        }

        public Criteria andTransferMoneyIsNull() {
            addCriterion("transfer_money is null");
            return (Criteria) this;
        }

        public Criteria andTransferMoneyIsNotNull() {
            addCriterion("transfer_money is not null");
            return (Criteria) this;
        }

        public Criteria andTransferMoneyEqualTo(String value) {
            addCriterion("transfer_money =", value, "transferMoney");
            return (Criteria) this;
        }

        public Criteria andTransferMoneyNotEqualTo(String value) {
            addCriterion("transfer_money <>", value, "transferMoney");
            return (Criteria) this;
        }

        public Criteria andTransferMoneyGreaterThan(String value) {
            addCriterion("transfer_money >", value, "transferMoney");
            return (Criteria) this;
        }

        public Criteria andTransferMoneyGreaterThanOrEqualTo(String value) {
            addCriterion("transfer_money >=", value, "transferMoney");
            return (Criteria) this;
        }

        public Criteria andTransferMoneyLessThan(String value) {
            addCriterion("transfer_money <", value, "transferMoney");
            return (Criteria) this;
        }

        public Criteria andTransferMoneyLessThanOrEqualTo(String value) {
            addCriterion("transfer_money <=", value, "transferMoney");
            return (Criteria) this;
        }

        public Criteria andTransferMoneyLike(String value) {
            addCriterion("transfer_money like", value, "transferMoney");
            return (Criteria) this;
        }

        public Criteria andTransferMoneyNotLike(String value) {
            addCriterion("transfer_money not like", value, "transferMoney");
            return (Criteria) this;
        }

        public Criteria andTransferMoneyIn(List<String> values) {
            addCriterion("transfer_money in", values, "transferMoney");
            return (Criteria) this;
        }

        public Criteria andTransferMoneyNotIn(List<String> values) {
            addCriterion("transfer_money not in", values, "transferMoney");
            return (Criteria) this;
        }

        public Criteria andTransferMoneyBetween(String value1, String value2) {
            addCriterion("transfer_money between", value1, value2, "transferMoney");
            return (Criteria) this;
        }

        public Criteria andTransferMoneyNotBetween(String value1, String value2) {
            addCriterion("transfer_money not between", value1, value2, "transferMoney");
            return (Criteria) this;
        }

        public Criteria andTransCenterBillnoIsNull() {
            addCriterion("trans_center_billno is null");
            return (Criteria) this;
        }

        public Criteria andTransCenterBillnoIsNotNull() {
            addCriterion("trans_center_billno is not null");
            return (Criteria) this;
        }

        public Criteria andTransCenterBillnoEqualTo(String value) {
            addCriterion("trans_center_billno =", value, "transCenterBillno");
            return (Criteria) this;
        }

        public Criteria andTransCenterBillnoNotEqualTo(String value) {
            addCriterion("trans_center_billno <>", value, "transCenterBillno");
            return (Criteria) this;
        }

        public Criteria andTransCenterBillnoGreaterThan(String value) {
            addCriterion("trans_center_billno >", value, "transCenterBillno");
            return (Criteria) this;
        }

        public Criteria andTransCenterBillnoGreaterThanOrEqualTo(String value) {
            addCriterion("trans_center_billno >=", value, "transCenterBillno");
            return (Criteria) this;
        }

        public Criteria andTransCenterBillnoLessThan(String value) {
            addCriterion("trans_center_billno <", value, "transCenterBillno");
            return (Criteria) this;
        }

        public Criteria andTransCenterBillnoLessThanOrEqualTo(String value) {
            addCriterion("trans_center_billno <=", value, "transCenterBillno");
            return (Criteria) this;
        }

        public Criteria andTransCenterBillnoLike(String value) {
            addCriterion("trans_center_billno like", value, "transCenterBillno");
            return (Criteria) this;
        }

        public Criteria andTransCenterBillnoNotLike(String value) {
            addCriterion("trans_center_billno not like", value, "transCenterBillno");
            return (Criteria) this;
        }

        public Criteria andTransCenterBillnoIn(List<String> values) {
            addCriterion("trans_center_billno in", values, "transCenterBillno");
            return (Criteria) this;
        }

        public Criteria andTransCenterBillnoNotIn(List<String> values) {
            addCriterion("trans_center_billno not in", values, "transCenterBillno");
            return (Criteria) this;
        }

        public Criteria andTransCenterBillnoBetween(String value1, String value2) {
            addCriterion("trans_center_billno between", value1, value2, "transCenterBillno");
            return (Criteria) this;
        }

        public Criteria andTransCenterBillnoNotBetween(String value1, String value2) {
            addCriterion("trans_center_billno not between", value1, value2, "transCenterBillno");
            return (Criteria) this;
        }

        public Criteria andTransMappingBillnoIsNull() {
            addCriterion("trans_mapping_billno is null");
            return (Criteria) this;
        }

        public Criteria andTransMappingBillnoIsNotNull() {
            addCriterion("trans_mapping_billno is not null");
            return (Criteria) this;
        }

        public Criteria andTransMappingBillnoEqualTo(String value) {
            addCriterion("trans_mapping_billno =", value, "transMappingBillno");
            return (Criteria) this;
        }

        public Criteria andTransMappingBillnoNotEqualTo(String value) {
            addCriterion("trans_mapping_billno <>", value, "transMappingBillno");
            return (Criteria) this;
        }

        public Criteria andTransMappingBillnoGreaterThan(String value) {
            addCriterion("trans_mapping_billno >", value, "transMappingBillno");
            return (Criteria) this;
        }

        public Criteria andTransMappingBillnoGreaterThanOrEqualTo(String value) {
            addCriterion("trans_mapping_billno >=", value, "transMappingBillno");
            return (Criteria) this;
        }

        public Criteria andTransMappingBillnoLessThan(String value) {
            addCriterion("trans_mapping_billno <", value, "transMappingBillno");
            return (Criteria) this;
        }

        public Criteria andTransMappingBillnoLessThanOrEqualTo(String value) {
            addCriterion("trans_mapping_billno <=", value, "transMappingBillno");
            return (Criteria) this;
        }

        public Criteria andTransMappingBillnoLike(String value) {
            addCriterion("trans_mapping_billno like", value, "transMappingBillno");
            return (Criteria) this;
        }

        public Criteria andTransMappingBillnoNotLike(String value) {
            addCriterion("trans_mapping_billno not like", value, "transMappingBillno");
            return (Criteria) this;
        }

        public Criteria andTransMappingBillnoIn(List<String> values) {
            addCriterion("trans_mapping_billno in", values, "transMappingBillno");
            return (Criteria) this;
        }

        public Criteria andTransMappingBillnoNotIn(List<String> values) {
            addCriterion("trans_mapping_billno not in", values, "transMappingBillno");
            return (Criteria) this;
        }

        public Criteria andTransMappingBillnoBetween(String value1, String value2) {
            addCriterion("trans_mapping_billno between", value1, value2, "transMappingBillno");
            return (Criteria) this;
        }

        public Criteria andTransMappingBillnoNotBetween(String value1, String value2) {
            addCriterion("trans_mapping_billno not between", value1, value2, "transMappingBillno");
            return (Criteria) this;
        }

        public Criteria andTransTypeIsNull() {
            addCriterion("trans_type is null");
            return (Criteria) this;
        }

        public Criteria andTransTypeIsNotNull() {
            addCriterion("trans_type is not null");
            return (Criteria) this;
        }

        public Criteria andTransTypeEqualTo(String value) {
            addCriterion("trans_type =", value, "transType");
            return (Criteria) this;
        }

        public Criteria andTransTypeNotEqualTo(String value) {
            addCriterion("trans_type <>", value, "transType");
            return (Criteria) this;
        }

        public Criteria andTransTypeGreaterThan(String value) {
            addCriterion("trans_type >", value, "transType");
            return (Criteria) this;
        }

        public Criteria andTransTypeGreaterThanOrEqualTo(String value) {
            addCriterion("trans_type >=", value, "transType");
            return (Criteria) this;
        }

        public Criteria andTransTypeLessThan(String value) {
            addCriterion("trans_type <", value, "transType");
            return (Criteria) this;
        }

        public Criteria andTransTypeLessThanOrEqualTo(String value) {
            addCriterion("trans_type <=", value, "transType");
            return (Criteria) this;
        }

        public Criteria andTransTypeLike(String value) {
            addCriterion("trans_type like", value, "transType");
            return (Criteria) this;
        }

        public Criteria andTransTypeNotLike(String value) {
            addCriterion("trans_type not like", value, "transType");
            return (Criteria) this;
        }

        public Criteria andTransTypeIn(List<String> values) {
            addCriterion("trans_type in", values, "transType");
            return (Criteria) this;
        }

        public Criteria andTransTypeNotIn(List<String> values) {
            addCriterion("trans_type not in", values, "transType");
            return (Criteria) this;
        }

        public Criteria andTransTypeBetween(String value1, String value2) {
            addCriterion("trans_type between", value1, value2, "transType");
            return (Criteria) this;
        }

        public Criteria andTransTypeNotBetween(String value1, String value2) {
            addCriterion("trans_type not between", value1, value2, "transType");
            return (Criteria) this;
        }

        public Criteria andLiveIdIsNull() {
            addCriterion("live_id is null");
            return (Criteria) this;
        }

        public Criteria andLiveIdIsNotNull() {
            addCriterion("live_id is not null");
            return (Criteria) this;
        }

        public Criteria andLiveIdEqualTo(Integer value) {
            addCriterion("live_id =", value, "liveId");
            return (Criteria) this;
        }

        public Criteria andLiveIdNotEqualTo(Integer value) {
            addCriterion("live_id <>", value, "liveId");
            return (Criteria) this;
        }

        public Criteria andLiveIdGreaterThan(Integer value) {
            addCriterion("live_id >", value, "liveId");
            return (Criteria) this;
        }

        public Criteria andLiveIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("live_id >=", value, "liveId");
            return (Criteria) this;
        }

        public Criteria andLiveIdLessThan(Integer value) {
            addCriterion("live_id <", value, "liveId");
            return (Criteria) this;
        }

        public Criteria andLiveIdLessThanOrEqualTo(Integer value) {
            addCriterion("live_id <=", value, "liveId");
            return (Criteria) this;
        }

        public Criteria andLiveIdIn(List<Integer> values) {
            addCriterion("live_id in", values, "liveId");
            return (Criteria) this;
        }

        public Criteria andLiveIdNotIn(List<Integer> values) {
            addCriterion("live_id not in", values, "liveId");
            return (Criteria) this;
        }

        public Criteria andLiveIdBetween(Integer value1, Integer value2) {
            addCriterion("live_id between", value1, value2, "liveId");
            return (Criteria) this;
        }

        public Criteria andLiveIdNotBetween(Integer value1, Integer value2) {
            addCriterion("live_id not between", value1, value2, "liveId");
            return (Criteria) this;
        }

        public Criteria andLiveTypeIsNull() {
            addCriterion("live_type is null");
            return (Criteria) this;
        }

        public Criteria andLiveTypeIsNotNull() {
            addCriterion("live_type is not null");
            return (Criteria) this;
        }

        public Criteria andLiveTypeEqualTo(String value) {
            addCriterion("live_type =", value, "liveType");
            return (Criteria) this;
        }

        public Criteria andLiveTypeNotEqualTo(String value) {
            addCriterion("live_type <>", value, "liveType");
            return (Criteria) this;
        }

        public Criteria andLiveTypeGreaterThan(String value) {
            addCriterion("live_type >", value, "liveType");
            return (Criteria) this;
        }

        public Criteria andLiveTypeGreaterThanOrEqualTo(String value) {
            addCriterion("live_type >=", value, "liveType");
            return (Criteria) this;
        }

        public Criteria andLiveTypeLessThan(String value) {
            addCriterion("live_type <", value, "liveType");
            return (Criteria) this;
        }

        public Criteria andLiveTypeLessThanOrEqualTo(String value) {
            addCriterion("live_type <=", value, "liveType");
            return (Criteria) this;
        }

        public Criteria andLiveTypeLike(String value) {
            addCriterion("live_type like", value, "liveType");
            return (Criteria) this;
        }

        public Criteria andLiveTypeNotLike(String value) {
            addCriterion("live_type not like", value, "liveType");
            return (Criteria) this;
        }

        public Criteria andLiveTypeIn(List<String> values) {
            addCriterion("live_type in", values, "liveType");
            return (Criteria) this;
        }

        public Criteria andLiveTypeNotIn(List<String> values) {
            addCriterion("live_type not in", values, "liveType");
            return (Criteria) this;
        }

        public Criteria andLiveTypeBetween(String value1, String value2) {
            addCriterion("live_type between", value1, value2, "liveType");
            return (Criteria) this;
        }

        public Criteria andLiveTypeNotBetween(String value1, String value2) {
            addCriterion("live_type not between", value1, value2, "liveType");
            return (Criteria) this;
        }

        public Criteria andTransRecordIdIsNull() {
            addCriterion("trans_record_id is null");
            return (Criteria) this;
        }

        public Criteria andTransRecordIdIsNotNull() {
            addCriterion("trans_record_id is not null");
            return (Criteria) this;
        }

        public Criteria andTransRecordIdEqualTo(Long value) {
            addCriterion("trans_record_id =", value, "transRecordId");
            return (Criteria) this;
        }

        public Criteria andTransRecordIdNotEqualTo(Long value) {
            addCriterion("trans_record_id <>", value, "transRecordId");
            return (Criteria) this;
        }

        public Criteria andTransRecordIdGreaterThan(Long value) {
            addCriterion("trans_record_id >", value, "transRecordId");
            return (Criteria) this;
        }

        public Criteria andTransRecordIdGreaterThanOrEqualTo(Long value) {
            addCriterion("trans_record_id >=", value, "transRecordId");
            return (Criteria) this;
        }

        public Criteria andTransRecordIdLessThan(Long value) {
            addCriterion("trans_record_id <", value, "transRecordId");
            return (Criteria) this;
        }

        public Criteria andTransRecordIdLessThanOrEqualTo(Long value) {
            addCriterion("trans_record_id <=", value, "transRecordId");
            return (Criteria) this;
        }

        public Criteria andTransRecordIdIn(List<Long> values) {
            addCriterion("trans_record_id in", values, "transRecordId");
            return (Criteria) this;
        }

        public Criteria andTransRecordIdNotIn(List<Long> values) {
            addCriterion("trans_record_id not in", values, "transRecordId");
            return (Criteria) this;
        }

        public Criteria andTransRecordIdBetween(Long value1, Long value2) {
            addCriterion("trans_record_id between", value1, value2, "transRecordId");
            return (Criteria) this;
        }

        public Criteria andTransRecordIdNotBetween(Long value1, Long value2) {
            addCriterion("trans_record_id not between", value1, value2, "transRecordId");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(String value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(String value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(String value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(String value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(String value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(String value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLike(String value) {
            addCriterion("create_time like", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotLike(String value) {
            addCriterion("create_time not like", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<String> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<String> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(String value1, String value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(String value1, String value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNull() {
            addCriterion("update_time is null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNotNull() {
            addCriterion("update_time is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeEqualTo(String value) {
            addCriterion("update_time =", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotEqualTo(String value) {
            addCriterion("update_time <>", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThan(String value) {
            addCriterion("update_time >", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThanOrEqualTo(String value) {
            addCriterion("update_time >=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThan(String value) {
            addCriterion("update_time <", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThanOrEqualTo(String value) {
            addCriterion("update_time <=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLike(String value) {
            addCriterion("update_time like", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotLike(String value) {
            addCriterion("update_time not like", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIn(List<String> values) {
            addCriterion("update_time in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotIn(List<String> values) {
            addCriterion("update_time not in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeBetween(String value1, String value2) {
            addCriterion("update_time between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotBetween(String value1, String value2) {
            addCriterion("update_time not between", value1, value2, "updateTime");
            return (Criteria) this;
        }
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table transfer_other_rule
     *
     * @mbggenerated do_not_delete_during_merge Thu May 10 16:02:09 CST 2018
     */
    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table transfer_other_rule
     *
     * @mbggenerated Thu May 10 16:02:09 CST 2018
     */
    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}