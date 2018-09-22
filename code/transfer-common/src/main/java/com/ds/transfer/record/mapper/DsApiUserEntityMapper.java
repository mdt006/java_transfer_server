package com.ds.transfer.record.mapper;

import com.ds.transfer.record.entity.DsApiUserEntity;
import com.ds.transfer.record.entity.DsApiUserEntityExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DsApiUserEntityMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ds_api_user
     *
     * @mbggenerated Sat Oct 31 20:34:36 CST 2015
     */
    int countByExample(DsApiUserEntityExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ds_api_user
     *
     * @mbggenerated Sat Oct 31 20:34:36 CST 2015
     */
    int deleteByExample(DsApiUserEntityExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ds_api_user
     *
     * @mbggenerated Sat Oct 31 20:34:36 CST 2015
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ds_api_user
     *
     * @mbggenerated Sat Oct 31 20:34:36 CST 2015
     */
    int insert(DsApiUserEntity record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ds_api_user
     *
     * @mbggenerated Sat Oct 31 20:34:36 CST 2015
     */
    int insertSelective(DsApiUserEntity record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ds_api_user
     *
     * @mbggenerated Sat Oct 31 20:34:36 CST 2015
     */
    List<DsApiUserEntity> selectByExample(DsApiUserEntityExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ds_api_user
     *
     * @mbggenerated Sat Oct 31 20:34:36 CST 2015
     */
    DsApiUserEntity selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ds_api_user
     *
     * @mbggenerated Sat Oct 31 20:34:36 CST 2015
     */
    int updateByExampleSelective(@Param("record") DsApiUserEntity record, @Param("example") DsApiUserEntityExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ds_api_user
     *
     * @mbggenerated Sat Oct 31 20:34:36 CST 2015
     */
    int updateByExample(@Param("record") DsApiUserEntity record, @Param("example") DsApiUserEntityExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ds_api_user
     *
     * @mbggenerated Sat Oct 31 20:34:36 CST 2015
     */
    int updateByPrimaryKeySelective(DsApiUserEntity record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ds_api_user
     *
     * @mbggenerated Sat Oct 31 20:34:36 CST 2015
     */
    int updateByPrimaryKey(DsApiUserEntity record);
}