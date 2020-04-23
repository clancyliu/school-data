package com.example.schooldata.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author liugang
 * @date 2020/4/23 17:32
 */
@Data
public class School implements Serializable {
    private static final long serialVersionUID = -1030021454530372150L;

//    private Long id;

    private String name;
    /**
     * 省
     */
    private String province;
    private String provinceCode;
    /**
     * 市
     */
    private String city;
    private String cityCode;
    /**
     * 区县
     */
    private String area;
    private String areaCode;

    private SchoolTypeEnum schoolType;

    /**
     * 经纬度坐标
     */
    private String location;

    /**
     * 地址
     */
    private String address;

}
