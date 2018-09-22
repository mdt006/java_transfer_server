package com.ds.transfer.record.mapper;

import com.ds.transfer.record.entity.KkwApiUserEntity;
import com.ds.transfer.record.entity.KkwApiUserEntityExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface KkwApiUserEntityMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table kkw_api_user
     *
     * @mbggenerated Wed Jul 11 15:12:02 CST 2018
     */
    int countByExample(KkwApiUserEntityExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table kkw_api_user
     *
     * @mbggenerated Wed Jul 11 15:12:02 CST 2018
     */
    int deleteByExample(KkwApiUserEntityExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table kkw_api_user
     *
     * @mbggenerated Wed Jul 11 15:12:02 CST 2018
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table kkw_api_user
     *
     * @mbggenerated Wed Jul 11 15:12:02 CST 2018
     */
    int insert(KkwApiUserEntity record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table kkw_api_user
     *
     * @mbggenerated Wed Jul 11 15:12:02 CST 2018
     */
    int insertSelective(KkwApiUserEntity record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table kkw_api_user
     *
     * @mbggenerated Wed Jul 11 15:12:02 CST 2018
     */
    List<KkwApiUserEntity> selectByExample(KkwApiUserEntityExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table kkw_api_user
     *
     * @mbggenerated Wed Jul 11 15:12:02 CST 2018
     */
    KkwApiUserEntity selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table kkw_api_user
     *
     * @mbggenerated Wed Jul 11 15:12:02 CST 2018
     */
    int updateByExampleSelective(@Param("record") KkwApiUserEntity record, @Param("example") KkwApiUserEntityExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table kkw_api_user
     *
     * @mbggenerated Wed Jul 11 15:12:02 CST 2018
     */
    int updateByExample(@Param("record") KkwApiUserEntity record, @Param("example") KkwApiUserEntityExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table kkw_api_user
     *
     * @mbggenerated Wed Jul 11 15:12:02 CST 2018
     */
    int updateByPrimaryKeySelective(KkwApiUserEntity record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table kkw_api_user
     *
     * @mbggenerated Wed Jul 11 15:12:02 CST 2018
     */
    int updateByPrimaryKey(KkwApiUserEntity record);
}